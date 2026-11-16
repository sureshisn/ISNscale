package com.example.isnscale.ar

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import com.google.ar.core.ArCoreApk
import kotlinx.coroutines.delay

/** Whether this device can run the AR measurement engine. */
enum class ArStatus {
    /** Still asking ARCore. */
    CHECKING,

    /** ARCore is installed and the device is supported. */
    READY,

    /** The device supports AR, but Google Play Services for AR must be installed. */
    NEEDS_INSTALL,

    /** The device cannot run AR at all. */
    UNSUPPORTED,
}

/** Finds the Activity behind a (possibly wrapped) Compose/View context. */
internal fun Context.findActivity(): Activity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Thin wrapper around [ArCoreApk] so the UI only has to deal with four states.
 *
 * ARCore is what makes reference-free measurement possible: it tracks the device in
 * 3D space and reports the real-world depth of tapped points, so the app does not need
 * a calibration card or any other object of known size.
 */
object ArSupport {

    private fun isEmulator(): Boolean =
        Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.lowercase().contains("vbox") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for") ||
            Build.PRODUCT.contains("sdk_gphone")

    /**
     * Resolves the current support state.
     *
     * [ArCoreApk.checkAvailability] answers asynchronously at first
     * ([ArCoreApk.Availability.isTransient]), so this polls a few times before giving up.
     */
    suspend fun resolve(context: Context): ArStatus {
        repeat(25) {
            val availability = try {
                ArCoreApk.getInstance().checkAvailability(context)
            } catch (_: Exception) {
                return ArStatus.UNSUPPORTED
            }

            when (availability) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED -> return ArStatus.READY
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED,
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> return ArStatus.NEEDS_INSTALL

                ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> return ArStatus.UNSUPPORTED

                else -> {
                    // UNKNOWN_CHECKING / UNKNOWN_TIMED_OUT / UNKNOWN_ERROR
                    if (availability.isTransient) {
                        delay(200)
                    } else {
                        return ArStatus.UNSUPPORTED
                    }
                }
            }
        }

        // Timed out. Emulators without Play Services for AR land here, and are not usable
        // for AR measurement anyway.
        return if (isEmulator()) ArStatus.UNSUPPORTED else ArStatus.NEEDS_INSTALL
    }

    /** Asks the Play Store to install Google Play Services for AR. */
    fun requestInstall(context: Context): Boolean {
        val activity = context.findActivity() ?: return false
        return try {
            ArCoreApk.getInstance().requestInstall(activity, true) ==
                ArCoreApk.InstallStatus.INSTALL_REQUESTED
        } catch (_: Exception) {
            false
        }
    }

    fun hasCameraPermission(context: Context): Boolean =
        context.checkSelfPermission(android.Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
}
