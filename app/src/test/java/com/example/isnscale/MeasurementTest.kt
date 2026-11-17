package com.example.isnscale

import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementTest {

    @Test
    fun distance3d_measuresDiagonal() {
        assertEquals(5.0, Measurement.distance3d(0.0, 0.0, 0.0, 3.0, 4.0, 0.0), 1e-9)
    }

    @Test
    fun distance3d_usesDepthComponent() {
        // 1 m apart along Z only: an AR depth measurement across space.
        assertEquals(1.0, Measurement.distance3d(0.2, 0.5, 2.0, 0.2, 0.5, 3.0), 1e-9)
    }

    @Test
    fun mToCm_convertsMetres() {
        assertEquals(150.0, Measurement.mToCm(1.5), 1e-9)
    }

    @Test
    fun format_usesCentimetresBelowOneMetre() {
        assertEquals("12.3 cm", Measurement.format(12.34))
    }

    @Test
    fun format_usesMetresFromOneMetre() {
        assertEquals("1.50 m", Measurement.format(150.0))
    }

    @Test
    fun format_convertsToInches() {
        assertEquals("1.0 in", Measurement.format(2.54, imperial = true))
    }

    @Test
    fun format_rejectsInvalidValues() {
        assertEquals("--", Measurement.format(Double.NaN))
        assertEquals("--", Measurement.format(Double.POSITIVE_INFINITY))
    }
}
