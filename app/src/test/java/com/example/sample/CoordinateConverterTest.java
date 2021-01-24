package com.example.sample;

import org.junit.Test;

import static org.junit.Assert.*;

public class CoordinateConverterTest {

    @Test
    public void pixelX() {
        assertEquals(29941927,
                CoordinateConverter.pixelX(141.242035,17.0));
    }

    @Test
    public void pixelY() {
        assertEquals(12046802,
                CoordinateConverter.pixelY(45.178506,17.0));
    }

    @Test
    public void longitude() {
        assertEquals(141.242035,
                CoordinateConverter.longitude(29941927,17.0), 0.0001);

        assertEquals(141.242035,
                CoordinateConverter.longitude(
                        CoordinateConverter.pixelX(141.242035, 17.0)
                        ,17.0), 0.0001);
    }

    @Test
    public void latitude() {
        assertEquals(45.178506,
                CoordinateConverter.latitude(12046802,17.0), 0.0001);

        assertEquals(45.178506,
                CoordinateConverter.latitude(
                        CoordinateConverter.pixelY(45.178506, 17.0)
                        ,17.0), 0.0001);
    }

    @Test
    public void tileX() {
        assertEquals(58097,
                CoordinateConverter.tileX(14873077));
    }

    @Test
    public void tileY() {
        assertEquals(25859,
                CoordinateConverter.tileY(6620093 ));
    }
}