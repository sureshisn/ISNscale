package com.example.isnscale

import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementTest {

    @Test
    fun distancePx_computesEuclideanDistance() {
        assertEquals(5.0f, Measurement.distancePx(0f, 0f, 3f, 4f), 0.0001f)
    }

    @Test
    fun cmPerPixel_dividesByReferenceLength() {
        assertEquals(0.1, Measurement.cmPerPixel(10.0, 100.0), 0.0001)
    }

    @Test
    fun cmPerPixel_returnsZeroForZeroReference() {
        assertEquals(0.0, Measurement.cmPerPixel(10.0, 0.0), 0.0001)
    }

    @Test
    fun pxToCm_scalesByRatio() {
        assertEquals(25.0, Measurement.pxToCm(250.0, 0.1), 0.0001)
    }
}
