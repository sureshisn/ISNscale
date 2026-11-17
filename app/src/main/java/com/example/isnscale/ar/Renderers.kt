package com.example.isnscale.ar

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Frame
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/** Small helpers for compiling and linking the two shader programs we use. */
internal object GlUtil {

    private const val TAG = "ISNscaleGl"

    fun loadShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) {
            Log.e(TAG, "Failed to create shader")
            return 0
        }

        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            Log.e(TAG, "Shader compile failed: $log")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun createProgram(vertexShader: Int, fragmentShader: Int): Int {
        if (vertexShader == 0 || fragmentShader == 0) {
            Log.e(TAG, "One or both shaders are 0")
            return 0
        }

        val program = GLES20.glCreateProgram()
        if (program == 0) {
            Log.e(TAG, "Failed to create program")
            return 0
        }

        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val status = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(program)
            Log.e(TAG, "Program link failed: $log")
            GLES20.glDeleteProgram(program)
            return 0
        }
        return program
    }

    fun createFloatBuffer(values: FloatArray): FloatBuffer {
        val buffer = ByteBuffer
            .allocateDirect(values.size * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(values)
        buffer.position(0)
        return buffer
    }
}

/**
 * Draws the camera image as a full-screen textured quad.
 *
 * This is the "AR background": the live camera feed that the 3D ruler is drawn on top of.
 */
internal class CameraBackgroundRenderer {

    private companion object {
        private const val TAG = "ISNscaleBg"

        const val VERTEX_SHADER = """
            attribute vec4 a_Position;
            attribute vec2 a_TexCoord;
            varying vec2 v_TexCoord;
            void main() {
                gl_Position = a_Position;
                v_TexCoord = a_TexCoord;
            }
        """

        const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 v_TexCoord;
            uniform samplerExternalOES u_Texture;
            void main() {
                gl_FragColor = texture2D(u_Texture, v_TexCoord);
            }
        """

        /** Corner positions in normalised device coordinates (two triangles as a strip). */
        val QUAD_COORDS = floatArrayOf(
            -1.0f, -1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
        )
    }

    /** OpenGL texture that ARCore writes the camera image into. */
    var textureId: Int = -1
        private set

    private var program = 0
    private var positionAttrib = 0
    private var texCoordAttrib = -1

    private var quadCoords: FloatBuffer? = null
    private var quadTexCoords: FloatBuffer? = null
    private val texCoords = FloatArray(8)

    fun createOnGlThread() {
        // Clear the color buffer to black initially
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]

        if (textureId == 0) {
            Log.e(TAG, "Failed to generate texture ID")
            return
        }

        // Create an external OES texture for camera frames
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        val vertexShader = GlUtil.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = GlUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        if (vertexShader == 0 || fragmentShader == 0) {
            Log.e(TAG, "Failed to create shaders")
            return
        }

        program = GlUtil.createProgram(vertexShader, fragmentShader)

        Log.d(TAG, "Shader program created: $program")

        // The shaders are owned by the program once linked.
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)

        if (program == 0) {
            Log.e(TAG, "Failed to create program")
            return
        }

        positionAttrib = GLES20.glGetAttribLocation(program, "a_Position")
        texCoordAttrib = GLES20.glGetAttribLocation(program, "a_TexCoord")
        
        Log.d(TAG, "Attrib locations - position: $positionAttrib, texCoord: $texCoordAttrib")

        quadCoords = GlUtil.createFloatBuffer(QUAD_COORDS)
        quadTexCoords = GlUtil.createFloatBuffer(FloatArray(8))
    }

    fun draw(frame: Frame) {
        // Skip if not initialized (called before onSurfaceCreated completed)
        if (program == 0 || quadCoords == null || quadTexCoords == null) {
            return
        }

        // Frames with a 0 timestamp have no camera image attached yet (they are emitted
        // while the camera is still starting). transformCoordinates2d would throw on
        // them, and an exception escaping onDrawFrame crashes the whole process.
        if (frame.timestamp == 0L) {
            return
        }

        val coords = quadCoords!!
        val texBuffer = quadTexCoords!!

        // ARCore tells us how the camera image maps onto the quad. Recomputed every frame so
        // the preview is always correctly aligned, even across rotations and resizes.
        runCatching {
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                QUAD_COORDS.copyOf(),
                Coordinates2d.TEXTURE_NORMALIZED,
                texCoords
            )
        }.onFailure { e ->
            // A bad frame must never take down the GL thread (and with it the app).
            Log.e(TAG, "transformCoordinates2d failed", e)
            return
        }
        texBuffer.clear()
        texBuffer.put(texCoords)
        texBuffer.position(0)

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUseProgram(program)

        GLES20.glEnableVertexAttribArray(positionAttrib)
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, coords)

        GLES20.glEnableVertexAttribArray(texCoordAttrib)
        GLES20.glVertexAttribPointer(texCoordAttrib, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionAttrib)
        GLES20.glDisableVertexAttribArray(texCoordAttrib)

        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }
}

/**
 * Draws the virtual ruler: a line between the two tapped points plus a marker at each end.
 *
 * Because the endpoints are world-space anchors from ARCore, the line stays glued to the
 * real object as the phone moves - the "virtual reality" feel of commercial AR measure apps.
 */
internal class RulerRenderer {

    private companion object {
        const val VERTEX_SHADER = """
            uniform mat4 u_Mvp;
            attribute vec4 a_Position;
            void main() {
                gl_Position = u_Mvp * a_Position;
                gl_PointSize = 16.0;
            }
        """

        const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """
    }

    private var program = 0
    private var positionAttrib = 0
    private var colorUniform = 0
    private var mvpUniform = 0

    private var segment: FloatBuffer? = null
    private var marker: FloatBuffer? = null

    fun createOnGlThread() {
        val vertexShader = GlUtil.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = GlUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        program = GlUtil.createProgram(vertexShader, fragmentShader)

        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)

        if (program == 0) return

        positionAttrib = GLES20.glGetAttribLocation(program, "a_Position")
        mvpUniform = GLES20.glGetUniformLocation(program, "u_Mvp")
        colorUniform = GLES20.glGetUniformLocation(program, "u_Color")

        segment = GlUtil.createFloatBuffer(FloatArray(6))
        marker = GlUtil.createFloatBuffer(FloatArray(3))
    }

    fun draw(mvp: FloatArray, start: FloatArray?, end: FloatArray?) {
        if (program == 0) return
        if (start == null) return

        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(mvpUniform, 1, false, mvp, 0)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // Keep the ruler visible even where it sits inside another surface.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        GLES20.glEnableVertexAttribArray(positionAttrib)

        if (end != null) {
            segment?.apply {
                clear()
                put(start)
                put(end)
                position(0)
                GLES20.glUniform4f(colorUniform, 0.13f, 0.87f, 0.98f, 1.0f)
                GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, this)
                GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
            }
        }

        GLES20.glUniform4f(colorUniform, 1.0f, 1.0f, 1.0f, 1.0f)
        for (point in listOfNotNull(start, end)) {
            marker?.apply {
                clear()
                put(point)
                position(0)
                GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, this)
                GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
            }
        }

        GLES20.glDisableVertexAttribArray(positionAttrib)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }
}
