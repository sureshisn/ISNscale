package com.example.isnscale.ar

import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.WindowManager
import com.example.isnscale.Measurement
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.DepthPoint
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.hypot
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Everything the Compose UI needs to know about the live AR session. */
data class ArUiState(
    val tracking: Boolean = false,
    val points: Int = 0,
    val distanceCm: Double? = null,
    val hint: String = "Move the phone slowly, then tap two points on the object",
    val error: String? = null,
)

/**
 * Camera + ARCore view that measures the real-world distance between two tapped points.
 *
 * No reference object and no calibration are required: ARCore tracks the phone in 3D and
 * reports the world-space position of whatever the user taps, so the distance is computed
 * from actual depth rather than guessed from pixels.
 */
class ArMeasureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs), GLSurfaceView.Renderer {

    private val _state = MutableStateFlow(ArUiState())
    val state: StateFlow<ArUiState> = _state

    private val background = CameraBackgroundRenderer()
    private val ruler = RulerRenderer()

    private var session: Session? = null
    private var installRequested = false
    private var resumed = false

    private var surfaceWidth = 0
    private var surfaceHeight = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    /** World-space endpoints, in metres, as reported by ARCore. */
    @Volatile
    private var startPoint: FloatArray? = null

    @Volatile
    private var endPoint: FloatArray? = null

    private val tapLock = Any()
    private var pendingTapX = 0f
    private var pendingTapY = 0f
    private var hasPendingTap = false

    private var downX = 0f
    private var downY = 0f

    /** Set true after session is created (typically in onSurfaceCreated for GL thread safety). */
    private var sessionInitialized = false

    /** The session needs to be created and configured. Done on GL thread. */
    private var pendingSessionCreation = false

    init {
        setEGLContextClientVersion(2)
        setPreserveEGLContextOnPause(true)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
        setOnTouchListener { _, event -> handleTouch(event) }
    }

    // ---------------------------------------------------------------- lifecycle

    override fun onResume() {
        super.onResume()
        resumed = true
    }

    override fun onPause() {
        super.onPause()
        resumed = false
        runCatching { session?.pause() }
    }

    /**
     * Marks that a session creation request has been made. Use this to trigger
     * session creation on the GL thread.
     */
    fun requestSessionCreation() {
        pendingSessionCreation = true
    }

    // ------------------------------------------------------------------ input

    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }

            MotionEvent.ACTION_UP -> {
                val moved = hypot((event.x - downX).toDouble(), (event.y - downY).toDouble())
                if (moved < 30.0) {
                    synchronized(tapLock) {
                        pendingTapX = event.x
                        pendingTapY = event.y
                        hasPendingTap = true
                    }
                    performClick()
                }
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /** Forgets the current measurement so the user can start a new one. */
    fun clear() {
        startPoint = null
        endPoint = null
        _state.update {
            it.copy(
                points = 0,
                distanceCm = null,
                hint = "Move the phone slowly, then tap two points on the object",
            )
        }
    }

    /**
     * Checks if camera permission has been granted.
     */
    private fun hasCameraPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Creates the ARCore session - must be called ON THE GL THREAD.
     *
     * @param hasPermission Whether we have camera permission
     * @param requestInstall Whether to request install if needed
     */
    internal fun createSessionInternal(hasPermission: Boolean, requestInstall: Boolean) {
        if (sessionInitialized) return

        if (!hasPermission) {
            _state.update { it.copy(error = "Camera permission is required") }
            return
        }

        try {
            val activity = context.findActivity() ?: run {
                _state.update { it.copy(error = "Cannot find activity") }
                return
            }

            val installStatus = ArCoreApk.getInstance().requestInstall(activity, requestInstall)
            if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                installRequested = true
                _state.update { it.copy(error = "Installing ARCore...") }
                return
            }

            val newSession = Session(context)
            val config = Config(newSession)
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL)
            config.setFocusMode(Config.FocusMode.AUTO)
            config.setLightEstimationMode(Config.LightEstimationMode.AMBIENT_INTENSITY)
            if (newSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                config.setDepthMode(Config.DepthMode.AUTOMATIC)
            }
            newSession.configure(config)

            session = newSession

            if (surfaceWidth > 0 && surfaceHeight > 0) {
                runCatching {
                    newSession.setDisplayGeometry(displayRotation(), surfaceWidth, surfaceHeight)
                }
            }

            // Register the camera texture with ARCore - this tells ARCore
            // to write camera frames to the OpenGL texture for rendering.
            // IMPORTANT: textureId must be valid (created on GL thread) before this call.
            runCatching { newSession.setCameraTextureName(background.textureId) }

            runCatching { newSession.resume() }

            sessionInitialized = true
            _state.update { it.copy(error = null) }
        } catch (e: UnavailableException) {
            _state.update { it.copy(error = "AR is not available on this device: ${e.message}") }
        } catch (e: CameraNotAvailableException) {
            _state.update { it.copy(error = "The camera is being used by another app") }
        } catch (e: Exception) {
            Log.e("ISNscaleAr", "Failed to create AR session", e)
            _state.update { it.copy(error = "Could not start AR: ${e.message}") }
        }
    }

    @Suppress("DEPRECATION")
    private fun displayRotation(): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        return windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
    }

    // ------------------------------------------------------------ GL rendering

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("ISNscaleAr", "onSurfaceCreated - setting up GL")
        background.createOnGlThread()
        ruler.createOnGlThread()
        
        // If session creation was already requested, now is the time to do it
        if (pendingSessionCreation) {
            val wasPending = pendingSessionCreation
            pendingSessionCreation = false
            createSessionInternal(hasCameraPermission(context), requestInstall = !installRequested)
            Log.d("ISNscaleAr", "Session creation triggered from onSurfaceCreated: $wasPending")
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d("ISNscaleAr", "onSurfaceChanged($width, $height)")
        GLES20.glViewport(0, 0, width, height)
        surfaceWidth = width
        surfaceHeight = height
        session?.let { 
            runCatching { it.setDisplayGeometry(displayRotation(), width, height) }
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Create session on GL thread if requested
        if (pendingSessionCreation) {
            pendingSessionCreation = false
            createSessionInternal(hasCameraPermission(context), requestInstall = !installRequested)
        }

        val activeSession = session ?: return
        if (!resumed) return

        val frame: Frame? = try {
            activeSession.update()
        } catch (e: Exception) {
            Log.e("ISNscaleAr", "Frame update failed", e)
            null
        }

        // Process all valid frames, including the first one (timestamp may be 0L initially)
        if (frame == null) return

        // Draw the live camera background
        background.draw(frame)

        val camera = frame.camera
        if (camera.trackingState != TrackingState.TRACKING) {
            _state.update {
                it.copy(
                    tracking = false,
                    hint = "Move the phone slowly so AR can find surfaces",
                )
            }
            return
        }

        _state.update { it.copy(tracking = true) }

        processTap(frame)

        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)
        camera.getViewMatrix(viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Draw the virtual ruler on top of the camera
        ruler.draw(mvpMatrix, startPoint, endPoint)
    }

    /** Converts the queued screen tap into a world-space point and updates the measurement. */
    private fun processTap(frame: Frame) {
        val tap = synchronized(tapLock) {
            if (!hasPendingTap) null else {
                hasPendingTap = false
                pendingTapX to pendingTapY
            }
        } ?: return

        val point = hitTest(frame, tap.first, tap.second)
        if (point == null) {
            _state.update {
                it.copy(
                    hint = "No surface there - point at the object and tap again",
                )
            }
            return
        }

        val first = startPoint
        if (first == null || endPoint != null) {
            // Start a new measurement.
            startPoint = point
            endPoint = null
            _state.update {
                it.copy(points = 1, distanceCm = null, hint = "Tap the second point")
            }
            return
        }

        endPoint = point
        val distanceCm = Measurement.mToCm(
            Measurement.distance3d(
                first[0].toDouble(),
                first[1].toDouble(),
                first[2].toDouble(),
                point[0].toDouble(),
                point[1].toDouble(),
                point[2].toDouble(),
            )
        )

        _state.update {
            it.copy(
                points = 2,
                distanceCm = distanceCm,
                hint = "Tap again to start a new measurement",
            )
        }
    }

    /**
     * Finds the real-world point under a screen tap.
     *
     * Depth hits are preferred because they land on arbitrary objects. Plane and feature
     * point hits are the fallback on devices without depth support.
     */
    private fun hitTest(frame: Frame, x: Float, y: Float): FloatArray? {
        val hits = try {
            frame.hitTest(x, y)
        } catch (_: Exception) {
            emptyList()
        }

        val hit = hits.firstOrNull { it.trackable is DepthPoint }
            ?: hits.firstOrNull { hit ->
                val trackable = hit.trackable
                (trackable is Plane || trackable is Point) &&
                    trackable.trackingState == TrackingState.TRACKING
            }
            ?: return null

        val pose = hit.hitPose ?: return null
        return floatArrayOf(pose.tx(), pose.ty(), pose.tz())
    }
}