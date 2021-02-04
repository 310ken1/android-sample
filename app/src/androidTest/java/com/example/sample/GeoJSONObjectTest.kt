package com.example.sample

import org.junit.Assert
import org.junit.Test

class GeoJSONObjectTest {

    @Test
    fun point() {
        val geojson = GeoJSONObject.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [100.0, 0.0]
                        },
                        "properties": {}
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(geojson.features[0].geometry is GeoJSONObject.Companion.Point)
        val geometry = geojson.features[0].geometry as GeoJSONObject.Companion.Point
        Assert.assertEquals(
            GeoJSONObject.Companion.Position(0.0, 100.0),
            geometry.position
        )
    }

    @Test
    fun lineStrings() {
        val geojson = GeoJSONObject.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "LineString",
                            "coordinates": [
                                [100.0, 0.0],
                                [101.0, 1.0]
                            ]
                        },
                        "properties": {}
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(geojson.features[0].geometry is GeoJSONObject.Companion.LineString)
        val geometry = geojson.features[0].geometry as GeoJSONObject.Companion.LineString
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(0.0, 100.0),
                GeoJSONObject.Companion.Position(1.0, 101.0)
            ), geometry.positions
        )
    }

    @Test
    fun polygons_no_holes() {
        val geojson = GeoJSONObject.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Polygon",
                            "coordinates": [
                                [
                                    [100.0, 0.0],
                                    [101.0, 0.0],
                                    [101.0, 1.0],
                                    [100.0, 1.0],
                                    [100.0, 0.0]
                                ]
                            ]
                        },
                        "properties": {}
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(geojson.features[0].geometry is GeoJSONObject.Companion.Polygon)
        val geometry = geojson.features[0].geometry as GeoJSONObject.Companion.Polygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(0.0, 100.0),
                GeoJSONObject.Companion.Position(0.0, 101.0),
                GeoJSONObject.Companion.Position(1.0, 101.0),
                GeoJSONObject.Companion.Position(1.0, 100.0),
                GeoJSONObject.Companion.Position(0.0, 100.0)
            ), geometry.lines[0].positions
        )
    }

    @Test
    fun polygons_with_holes() {
        val geojson = GeoJSONObject.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Polygon",
                            "coordinates": [
                                [
                                    [100.0, 0.0],
                                    [101.0, 0.0],
                                    [101.0, 1.0],
                                    [100.0, 1.0],
                                    [100.0, 0.0]
                                ],
                                [
                                    [100.8, 0.8],
                                    [100.8, 0.2],
                                    [100.2, 0.2],
                                    [100.2, 0.8],
                                    [100.8, 0.8]
                                ]
                            ]
                        },
                        "properties": {}
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(geojson.features[0].geometry is GeoJSONObject.Companion.Polygon)
        val geometry = geojson.features[0].geometry as GeoJSONObject.Companion.Polygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(0.0, 100.0),
                GeoJSONObject.Companion.Position(0.0, 101.0),
                GeoJSONObject.Companion.Position(1.0, 101.0),
                GeoJSONObject.Companion.Position(1.0, 100.0),
                GeoJSONObject.Companion.Position(0.0, 100.0)
            ), geometry.lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(0.8, 100.8),
                GeoJSONObject.Companion.Position(0.2, 100.8),
                GeoJSONObject.Companion.Position(0.2, 100.2),
                GeoJSONObject.Companion.Position(0.8, 100.2),
                GeoJSONObject.Companion.Position(0.8, 100.8)
            ), geometry.lines[1].positions
        )
    }

    @Test
    fun multiPoints() {
        val geojson = GeoJSONObject.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "MultiPoint",
                            "coordinates": [
                                [100.0, 0.0],
                                [101.0, 1.0]
                            ]
                        },
                        "properties": {}
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(geojson.features[0].geometry is GeoJSONObject.Companion.MultiPoint)
        val geometry = geojson.features[0].geometry as GeoJSONObject.Companion.MultiPoint
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(0.0, 100.0),
                GeoJSONObject.Companion.Position(1.0, 101.0)
            ), geometry.positions
        )
    }

    @Test
    fun multiLineStrings() {
        val geojson = GeoJSONObject.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "MultiLineString",
                            "coordinates": [
                                [
                                    [100.0, 0.0],
                                    [101.0, 1.0]
                                ],
                                [
                                    [102.0, 2.0],
                                    [103.0, 3.0]
                                ]
                            ]
                        },
                        "properties": {}
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(geojson.features[0].geometry is GeoJSONObject.Companion.MultiLineString)
        val geometry = geojson.features[0].geometry as GeoJSONObject.Companion.MultiLineString
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(0.0, 100.0),
                GeoJSONObject.Companion.Position(1.0, 101.0)
            ), geometry.lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(2.0, 102.0),
                GeoJSONObject.Companion.Position(3.0, 103.0)
            ), geometry.lines[1].positions
        )
    }

    @Test
    fun multiPolygons() {
        val geojson = GeoJSONObject.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "MultiPolygon",
                            "coordinates": [
                                [
                                    [
                                        [102.0, 2.0],
                                        [103.0, 2.0],
                                        [103.0, 3.0],
                                        [102.0, 3.0],
                                        [102.0, 2.0]
                                    ]
                                ],
                                [
                                    [
                                        [100.0, 0.0],
                                        [101.0, 0.0],
                                        [101.0, 1.0],
                                        [100.0, 1.0],
                                        [100.0, 0.0]
                                    ],
                                    [
                                        [100.2, 0.2],
                                        [100.2, 0.8],
                                        [100.8, 0.8],
                                        [100.8, 0.2],
                                        [100.2, 0.2]
                                    ]
                                ]
                            ]
                        },
                        "properties": {}
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(geojson.features[0].geometry is GeoJSONObject.Companion.MultiPolygon)
        val geometry = geojson.features[0].geometry as GeoJSONObject.Companion.MultiPolygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(2.0, 102.0),
                GeoJSONObject.Companion.Position(2.0, 103.0),
                GeoJSONObject.Companion.Position(3.0, 103.0),
                GeoJSONObject.Companion.Position(3.0, 102.0),
                GeoJSONObject.Companion.Position(2.0, 102.0)
            ), geometry.polygons[0].lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(0.0, 100.0),
                GeoJSONObject.Companion.Position(0.0, 101.0),
                GeoJSONObject.Companion.Position(1.0, 101.0),
                GeoJSONObject.Companion.Position(1.0, 100.0),
                GeoJSONObject.Companion.Position(0.0, 100.0)
            ), geometry.polygons[1].lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSONObject.Companion.Position(0.2, 100.2),
                GeoJSONObject.Companion.Position(0.8, 100.2),
                GeoJSONObject.Companion.Position(0.8, 100.8),
                GeoJSONObject.Companion.Position(0.2, 100.8),
                GeoJSONObject.Companion.Position(0.2, 100.2)
            ), geometry.polygons[1].lines[1].positions
        )
    }
}
