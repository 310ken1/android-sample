package com.example.sample

import com.example.sample.CoordinateConverter.latitude
import com.example.sample.CoordinateConverter.longitude
import com.example.sample.CoordinateConverter.pixelX
import com.example.sample.CoordinateConverter.pixelY
import com.example.sample.CoordinateConverter.tileX
import com.example.sample.CoordinateConverter.tileY
import org.junit.Assert
import org.junit.Test

class CoordinateConverterTest {
    @Test
    fun pixelX() {
        Assert.assertEquals(
            29941927,
            pixelX(141.242035, 17.0).toLong()
        )
    }

    @Test
    fun pixelY() {
        Assert.assertEquals(
            12046802,
            pixelY(45.178506, 17.0).toLong()
        )
    }

    @Test
    fun longitude() {
        Assert.assertEquals(
            141.242035,
            longitude(29941927, 17.0), 0.0001
        )
        Assert.assertEquals(
            141.242035,
            longitude(
                pixelX(141.242035, 17.0), 17.0
            ), 0.0001
        )
    }

    @Test
    fun latitude() {
        Assert.assertEquals(
            45.178506,
            latitude(12046802, 17.0), 0.0001
        )
        Assert.assertEquals(
            45.178506,
            latitude(
                pixelY(45.178506, 17.0), 17.0
            ), 0.0001
        )
    }

    @Test
    fun tileX() {
        Assert.assertEquals(
            58097,
            tileX(14873077).toLong()
        )
    }

    @Test
    fun tileY() {
        Assert.assertEquals(
            25859,
            tileY(6620093).toLong()
        )
    }
}