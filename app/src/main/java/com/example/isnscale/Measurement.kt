package com.example.isnscale

import java.util.Locale
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Pure measurement math, kept free of Android APIs so it can be unit tested.
 */
object Measurement {

    /** Straight-line distance between two points, in pixels. */
    fun distancePx(x1: Float, y1: Float, x2: Float, y2: Float): Float =
        hypot(x2 - x1, y2 - y1)

    /**
     * Centimetres per pixel, derived from a reference object of known length.
     * Returns 0.0 when the reference has no measurable size.
     *
     * AR measurements do not need this, but it is kept for reference-object mode
     * and for the unit tests.
     */
    fun cmPerPixel(referenceLengthCm: Double, referenceLengthPx: Double): Double =
        if (referenceLengthPx <= 0.0) 0.0 else referenceLengthCm / referenceLengthPx

    /** Converts a pixel distance to centimetres using a previously calibrated ratio. */
    fun pxToCm(lengthPx: Double, cmPerPixel: Double): Double = lengthPx * cmPerPixel

    /**
     * Straight-line distance between two points in 3D space, in the same unit as the input.
     *
     * ARCore reports world positions in metres, so passing those positions in gives
     * metres back. This is the core of a reference-free measurement: no known object
     * is needed, because the depth of each tapped point is known.
     */
    fun distance3d(
        ax: Double,
        ay: Double,
        az: Double,
        bx: Double,
        by: Double,
        bz: Double,
    ): Double {
        val dx = bx - ax
        val dy = by - ay
        val dz = bz - az
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /** Metres to centimetres. */
    fun mToCm(metres: Double): Double = metres * 100.0

    /** Centimetres to metres. */
    fun cmToM(cm: Double): Double = cm / 100.0

    /**
     * Formats a centimetre value for display.
     *
     * - metric: `12.3 cm`, or `1.50 m` once the value reaches a metre
     * - imperial: `4.8 in`
     *
     * Returns `--` for values that cannot be represented (NaN / infinity).
     */
    fun format(cm: Double, imperial: Boolean = false): String {
        if (cm.isNaN() || cm.isInfinite()) return "--"
        return if (imperial) {
            String.format(Locale.US, "%.1f in", cm / 2.54)
        } else if (cm >= 100.0) {
            String.format(Locale.US, "%.2f m", cm / 100.0)
        } else {
            String.format(Locale.US, "%.1f cm", cm)
        }
    }
}
