package com.example.sample.utility

import kotlin.math.*

object CoordinateConverter {
    var L = 85.05112878 // 最大緯度

    @JvmStatic
    fun pixelX(longitude: Double, zoom: Double): Int {
        return (2.0.pow(zoom + 7) * (longitude / 180 + 1)).toInt()
    }

    @JvmStatic
    fun pixelY(latitude: Double, zoom: Double): Int {
        return (2.0.pow(zoom + 7) / Math.PI *
                (-1 * atanh(sin(Math.PI / 180 * latitude)) +
                        atanh(sin(Math.PI / 180 * L)))).toInt()
    }

    @JvmStatic
    fun longitude(x: Int, zoom: Double): Double {
        return 180 * (x / 2.0.pow(zoom + 7) - 1)
    }

    @JvmStatic
    fun latitude(y: Int, zoom: Double): Double {
        return 180 / Math.PI * asin(
            tanh(
                -1 * Math.PI / 2.0.pow(zoom + 7) * y +
                        atanh(sin(Math.PI / 180 * L))
            )
        )
    }

    @JvmStatic
    fun tileX(x: Int): Int {
        return x / 256
    }

    @JvmStatic
    fun tileY(y: Int): Int {
        return y / 256
    }

    @JvmStatic
    fun atanh(x: Double): Double {
        return ln((1 + x) / (1 - x)) / 2
    }
}