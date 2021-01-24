package com.example.sample;

public class CoordinateConverter {
    public static Double L = 85.05112878; // 最大緯度

    public static int pixelX(double longitude, Double zoom) {
        return (int) (Math.pow(2, zoom + 7) * (longitude / 180 + 1));
    }

    public static int pixelY(double latitude, Double zoom) {
        return (int) (
                (Math.pow(2, zoom + 7) / Math.PI) *
                        ((-1 * atanh(Math.sin((Math.PI / 180) * latitude))) +
                                atanh(Math.sin((Math.PI / 180) * L)))
        );
    }

    public static Double longitude(int x, Double zoom) {
        return 180 * (x / Math.pow(2, zoom + 7) - 1);

    }

    public static Double latitude(int y, Double zoom) {
        return (180 / Math.PI) *
                (Math.asin(Math.tanh((-1 * Math.PI / Math.pow(2, zoom + 7) * y) +
                        atanh(Math.sin(Math.PI / 180 * L)))));
    }

    public static int tileX(int x) {
        return x / 256;
    }

    public static int tileY(int y) {
        return y / 256;
    }

    public static double atanh(Double x) {
        return (Math.log((1 + x) / (1 - x))) / 2;
    }
}
