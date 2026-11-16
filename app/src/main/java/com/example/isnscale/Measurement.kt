package com.example.isnscale

import kotlin.math.hypot

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
     */
    fun cmPerPixel(referenceLengthCm: Double, referenceLengthPx: Double): Double =
        if (referenceLengthPx <= 0.0) 0.0 else referenceLengthCm / referenceLengthPx

    /** Converts a pixel distance to centimetres using a previously calibrated ratio. */
    fun pxToCm(lengthPx: Double, cmPerPixel: Double): Double = lengthPx * cmPerPixel
}
