package com.example.sample.utility

import org.json.JSONException
import org.junit.Assert
import org.junit.Test

class GeoJSONTest {

    @Test
    fun featureCollection_point() {
        val obj = GeoJSON.parse(
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
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertTrue(features[0].geometry is GeoJSON.Point)
        val geometry = features[0].geometry as GeoJSON.Point
        Assert.assertEquals(
            GeoJSON.Position(0.0, 100.0),
            geometry.position
        )
    }

    @Test
    fun feature_point() {
        val obj = GeoJSON.parse(
            """
            {
               "type": "Feature",
               "geometry": {
                   "type": "Point",
                   "coordinates": [102.0, 0.5]
               },
               "properties": {
                   "prop0": "value0"
               }
            }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureObject)
        val feature = obj as GeoJSON.FeatureObject
        Assert.assertEquals(
            GeoJSON.Position(0.5, 102.0),
            (feature.geometry as GeoJSON.Point).position
        )
        Assert.assertEquals(
            "value0",
            feature.properties?.getString("prop0")
        )
    }

    @Test
    fun point() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "Point",
                "coordinates": [100.0, 0.0]
            }        
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.Point)
        Assert.assertEquals(
            GeoJSON.Position(0.0, 100.0),
            (obj as GeoJSON.Point).position
        )
    }

    @Test
    fun featureCollection_lineStrings() {
        val obj = GeoJSON.parse(
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
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertTrue(features[0].geometry is GeoJSON.LineString)
        val geometry = features[0].geometry as GeoJSON.LineString
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(1.0, 101.0)
            ), geometry.positions
        )
    }

    @Test
    fun feature_lineStrings() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [
                            [102.0, 0.0],
                            [103.0, 1.0],
                            [104.0, 0.0],
                            [105.0, 1.0]
                        ]
                    },
                    "properties": {
                        "prop0": "value0",
                        "prop1": 0.0
                    }
                }
            }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureObject)
        val feature = obj as GeoJSON.FeatureObject
        Assert.assertTrue(feature.geometry is GeoJSON.LineString)
        val geometry = feature.geometry as GeoJSON.LineString
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 102.0),
                GeoJSON.Position(1.0, 103.0),
                GeoJSON.Position(0.0, 104.0),
                GeoJSON.Position(1.0, 105.0),
            ), geometry.positions
        )
        Assert.assertEquals(
            "value0",
            feature.properties?.getString("prop0")
        )
        Assert.assertEquals(
            0.0,
            feature.properties?.getDouble("prop1")
        )
    }

    @Test
    fun lineStrings() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "LineString",
                "coordinates": [
                    [100.0, 0.0],
                    [101.0, 1.0]
                ]
            }   
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.LineString)
        val geometry = obj as GeoJSON.LineString
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(1.0, 101.0),
            ), geometry.positions
        )
    }

    @Test
    fun featureCollection_polygons_no_holes() {
        val obj = GeoJSON.parse(
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
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertTrue(features[0].geometry is GeoJSON.Polygon)
        val geometry = features[0].geometry as GeoJSON.Polygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 101.0),
                GeoJSON.Position(1.0, 100.0),
                GeoJSON.Position(0.0, 100.0)
            ), geometry.lines[0].positions
        )
    }

    @Test
    fun featureCollection_polygons_with_holes() {
        val obj = GeoJSON.parse(
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
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertTrue(features[0].geometry is GeoJSON.Polygon)
        val geometry = features[0].geometry as GeoJSON.Polygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 101.0),
                GeoJSON.Position(1.0, 100.0),
                GeoJSON.Position(0.0, 100.0)
            ), geometry.lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.8, 100.8),
                GeoJSON.Position(0.2, 100.8),
                GeoJSON.Position(0.2, 100.2),
                GeoJSON.Position(0.8, 100.2),
                GeoJSON.Position(0.8, 100.8)
            ), geometry.lines[1].positions
        )
    }

    @Test
    fun feature_polygons() {
        val obj = GeoJSON.parse(
            """
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
               "properties": {
                   "prop0": "value0",
                   "prop1": {
                       "this": "that"
                   }
               }
            }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureObject)
        val feature = obj as GeoJSON.FeatureObject
        Assert.assertTrue(feature.geometry is GeoJSON.Polygon)
        val geometry = feature.geometry as GeoJSON.Polygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 101.0),
                GeoJSON.Position(1.0, 100.0),
                GeoJSON.Position(0.0, 100.0),
            ), geometry.lines[0].positions
        )
        Assert.assertEquals(
            "value0",
            feature.properties?.getString("prop0")
        )
        Assert.assertEquals(
            "that",
            feature.properties?.getJSONObject("prop1")?.getString("this")
        )
    }

    @Test
    fun polygons() {
        val obj = GeoJSON.parse(
            """
            {
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
            }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.Polygon)
        val geometry = obj as GeoJSON.Polygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 101.0),
                GeoJSON.Position(1.0, 100.0),
                GeoJSON.Position(0.0, 100.0),
            ), geometry.lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.8, 100.8),
                GeoJSON.Position(0.2, 100.8),
                GeoJSON.Position(0.2, 100.2),
                GeoJSON.Position(0.8, 100.2),
                GeoJSON.Position(0.8, 100.8),
            ), geometry.lines[1].positions
        )
    }

    @Test
    fun featureCollection_multiPoints() {
        val obj = GeoJSON.parse(
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
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertTrue(features[0].geometry is GeoJSON.MultiPoint)
        val geometry = features[0].geometry as GeoJSON.MultiPoint
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(1.0, 101.0)
            ), geometry.positions
        )
    }

    @Test
    fun feature_multiPoints() {
        val obj = GeoJSON.parse(
            """
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
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureObject)
        val feature = obj as GeoJSON.FeatureObject
        Assert.assertTrue(feature.geometry is GeoJSON.MultiPoint)
        val geometry = feature.geometry as GeoJSON.MultiPoint
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(1.0, 101.0),
            ), geometry.positions
        )
    }

    @Test
    fun multiPoints() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "MultiPoint",
                "coordinates": [
                    [100.0, 0.0],
                    [101.0, 1.0]
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.MultiPoint)
        val geometry = obj as GeoJSON.MultiPoint
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(1.0, 101.0),
            ), geometry.positions
        )
    }

    @Test
    fun featureCollection_multiLineStrings() {
        val obj = GeoJSON.parse(
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
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertTrue(features[0].geometry is GeoJSON.MultiLineString)
        val geometry = features[0].geometry as GeoJSON.MultiLineString
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(1.0, 101.0)
            ), geometry.lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(2.0, 102.0),
                GeoJSON.Position(3.0, 103.0)
            ), geometry.lines[1].positions
        )
    }

    @Test
    fun feature_multiLineStrings() {
        val obj = GeoJSON.parse(
            """
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
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureObject)
        val feature = obj as GeoJSON.FeatureObject
        Assert.assertTrue(feature.geometry is GeoJSON.MultiLineString)
        val geometry = feature.geometry as GeoJSON.MultiLineString
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(1.0, 101.0),
            ), geometry.lines[0].positions
        )
    }

    @Test
    fun multiLineStrings() {
        val obj = GeoJSON.parse(
            """
           {
               "type": "MultiLineString",
               "coordinates": [
                   [
                       [170.0, 45.0], [180.0, 45.0]
                   ], [
                       [-180.0, 45.0], [-170.0, 45.0]
                   ]
               ]
           }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.MultiLineString)
        val geometry = obj as GeoJSON.MultiLineString
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(45.0, 170.0),
                GeoJSON.Position(45.0, 180.0),
            ), geometry.lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(45.0, -180.0),
                GeoJSON.Position(45.0, -170.0),
            ), geometry.lines[1].positions
        )
    }

    @Test
    fun featureCollection_multiPolygons() {
        val obj = GeoJSON.parse(
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
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertTrue(features[0].geometry is GeoJSON.MultiPolygon)
        val geometry = features[0].geometry as GeoJSON.MultiPolygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(2.0, 102.0),
                GeoJSON.Position(2.0, 103.0),
                GeoJSON.Position(3.0, 103.0),
                GeoJSON.Position(3.0, 102.0),
                GeoJSON.Position(2.0, 102.0)
            ), geometry.polygons[0].lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 101.0),
                GeoJSON.Position(1.0, 100.0),
                GeoJSON.Position(0.0, 100.0)
            ), geometry.polygons[1].lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.2, 100.2),
                GeoJSON.Position(0.8, 100.2),
                GeoJSON.Position(0.8, 100.8),
                GeoJSON.Position(0.2, 100.8),
                GeoJSON.Position(0.2, 100.2)
            ), geometry.polygons[1].lines[1].positions
        )
    }

    @Test
    fun feature_multiPolygons() {
        val obj = GeoJSON.parse(
            """
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
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureObject)
        val feature = obj as GeoJSON.FeatureObject
        Assert.assertTrue(feature.geometry is GeoJSON.MultiPolygon)
        val geometry = feature.geometry as GeoJSON.MultiPolygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(2.0, 102.0),
                GeoJSON.Position(2.0, 103.0),
                GeoJSON.Position(3.0, 103.0),
                GeoJSON.Position(3.0, 102.0),
                GeoJSON.Position(2.0, 102.0)
            ), geometry.polygons[0].lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 101.0),
                GeoJSON.Position(1.0, 100.0),
                GeoJSON.Position(0.0, 100.0)
            ), geometry.polygons[1].lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.2, 100.2),
                GeoJSON.Position(0.8, 100.2),
                GeoJSON.Position(0.8, 100.8),
                GeoJSON.Position(0.2, 100.8),
                GeoJSON.Position(0.2, 100.2)
            ), geometry.polygons[1].lines[1].positions
        )
    }

    @Test
    fun multiPolygons() {
        val obj = GeoJSON.parse(
            """
           {
               "type": "MultiPolygon",
               "coordinates": [
                   [
                       [
                           [180.0, 40.0], [180.0, 50.0], [170.0, 50.0],
                           [170.0, 40.0], [180.0, 40.0]
                       ]
                   ],
                   [
                       [
                           [-170.0, 40.0], [-170.0, 50.0], [-180.0, 50.0],
                           [-180.0, 40.0], [-170.0, 40.0]
                       ]
                   ]
               ]
           }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.MultiPolygon)
        val geometry = obj as GeoJSON.MultiPolygon
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(40.0, 180.0),
                GeoJSON.Position(50.0, 180.0),
                GeoJSON.Position(50.0, 170.0),
                GeoJSON.Position(40.0, 170.0),
                GeoJSON.Position(40.0, 180.0)
            ), geometry.polygons[0].lines[0].positions
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(40.0, -170.0),
                GeoJSON.Position(50.0, -170.0),
                GeoJSON.Position(50.0, -180.0),
                GeoJSON.Position(40.0, -180.0),
                GeoJSON.Position(40.0, -170.0)
            ), geometry.polygons[1].lines[0].positions
        )
    }

    @Test
    fun featureCollection_geometryCollections() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                             "type": "GeometryCollection",
                             "geometries": [{
                                 "type": "Point",
                                 "coordinates": [100.0, 0.0]
                             }, {
                                 "type": "LineString",
                                 "coordinates": [
                                     [101.0, 0.0],
                                     [102.0, 1.0]
                                 ]
                             }]
                        },
                        "properties": {}
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertTrue(features[0].geometry is GeoJSON.GeometryCollection)
        val collection = features[0].geometry as GeoJSON.GeometryCollection
        Assert.assertEquals(
            GeoJSON.Position(0.0, 100.0),
            (collection.geometries[0] as GeoJSON.Point).position
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 102.0)
            ), (collection.geometries[1] as GeoJSON.LineString).positions
        )
    }

    @Test
    fun feature_geometryCollections() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "Feature",
                "geometry": {
                     "type": "GeometryCollection",
                     "geometries": [{
                         "type": "Point",
                         "coordinates": [100.0, 0.0]
                     }, {
                         "type": "LineString",
                         "coordinates": [
                             [101.0, 0.0],
                             [102.0, 1.0]
                         ]
                     }]
                },
                "properties": {}
            }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureObject)
        val feature = obj as GeoJSON.FeatureObject
        Assert.assertTrue(feature.geometry is GeoJSON.GeometryCollection)
        val collection = feature.geometry as GeoJSON.GeometryCollection
        Assert.assertEquals(
            GeoJSON.Position(0.0, 100.0),
            (collection.geometries[0] as GeoJSON.Point).position
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 102.0)
            ), (collection.geometries[1] as GeoJSON.LineString).positions
        )
    }

    @Test
    fun geometryCollections() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "GeometryCollection",
                "geometries": [{
                    "type": "Point",
                    "coordinates": [100.0, 0.0]
                }, {
                    "type": "LineString",
                    "coordinates": [
                        [101.0, 0.0],
                        [102.0, 1.0]
                    ]
                }]
            }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.GeometryCollection)
        val collection = obj as GeoJSON.GeometryCollection
        Assert.assertEquals(
            GeoJSON.Position(0.0, 100.0),
            (collection.geometries[0] as GeoJSON.Point).position
        )
        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 102.0)
            ), (collection.geometries[1] as GeoJSON.LineString).positions
        )
    }

    @Test
    fun bbox2DFeature() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                       "type": "Feature",
                       "bbox": [-10.0, -10.0, 10.0, 10.0],
                       "geometry": {
                           "type": "Polygon",
                           "coordinates": [
                               [
                                   [-10.0, -10.0],
                                   [10.0, -10.0],
                                   [10.0, 10.0],
                                   [-10.0, -10.0]
                               ]
                           ]
                       }
                    }
                ]
            }
            """.trimIndent()
        )
        Assert.assertArrayEquals(
            arrayOf(-10.0, -10.0, 10.0, 10.0),
            (obj as GeoJSON.FeatureCollectionObject).features[0].bbox
        )
    }

    @Test
    fun bbox2DFeatureCollection() {
        val obj = GeoJSON.parse(
            """
            {
               "type": "FeatureCollection",
               "bbox": [100.0, 0.0, 105.0, 1.0],
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
        Assert.assertArrayEquals(
            arrayOf(100.0, 0.0, 105.0, 1.0),
            (obj as GeoJSON.FeatureCollectionObject).bbox
        )
    }

    @Test
    fun example() {
        val obj = GeoJSON.parse(
            """
           {
               "type": "FeatureCollection",
               "features": [{
                   "type": "Feature",
                   "geometry": {
                       "type": "Point",
                       "coordinates": [102.0, 0.5]
                   },
                   "properties": {
                       "prop0": "value0"
                   }
               }, {
                   "type": "Feature",
                   "geometry": {
                       "type": "LineString",
                       "coordinates": [
                           [102.0, 0.0],
                           [103.0, 1.0],
                           [104.0, 0.0],
                           [105.0, 1.0]
                       ]
                   },
                   "properties": {
                       "prop0": "value0",
                       "prop1": 0.0
                   }
               }, {
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
                   "properties": {
                       "prop0": "value0",
                       "prop1": {
                           "this": "that"
                       }
                   }
               }]
           }
            """.trimIndent()
        )
        Assert.assertTrue(obj is GeoJSON.FeatureCollectionObject)
        val features = (obj as GeoJSON.FeatureCollectionObject).features
        Assert.assertEquals(
            GeoJSON.Position(0.5, 102.0),
            (features[0].geometry as GeoJSON.Point).position
        )
        Assert.assertEquals(
            "value0",
            features[0].properties?.getString("prop0")
        )

        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 102.0),
                GeoJSON.Position(1.0, 103.0),
                GeoJSON.Position(0.0, 104.0),
                GeoJSON.Position(1.0, 105.0)
            ), (features[1].geometry as GeoJSON.LineString).positions
        )
        Assert.assertEquals(
            "value0",
            features[1].properties?.getString("prop0")
        )
        Assert.assertEquals(
            0.0,
            features[1].properties?.getDouble("prop1")
        )

        Assert.assertArrayEquals(
            arrayOf(
                GeoJSON.Position(0.0, 100.0),
                GeoJSON.Position(0.0, 101.0),
                GeoJSON.Position(1.0, 101.0),
                GeoJSON.Position(1.0, 100.0),
                GeoJSON.Position(0.0, 100.0),
            ), (features[2].geometry as GeoJSON.Polygon).lines[0].positions
        )
        Assert.assertEquals(
            "value0",
            features[2].properties?.getString("prop0")
        )
        Assert.assertEquals(
            "that",
            features[2].properties?.getJSONObject("prop1")?.getString("this")
        )
    }

    @Test(expected = JSONException::class)
    fun jsonException() {
        val obj = GeoJSON.parse(
            """
            {
                "type": "Point"
                "coordinates": [100.0, 0.0]
            }        
            """.trimIndent()
        )
    }

    @Test
    fun geojsonException_bbox_length_error() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "Feature",
                    "bbox": [-10.0, -10.0, 10.0],
                    "geometry": {
                        "type": "Polygon",
                        "coordinates": [
                            [
                                [-10.0, -10.0],
                                [10.0, -10.0],
                                [10.0, 10.0],
                                [-10.0, -10.0]
                            ]
                        ]
                    }
                }        
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[bbox] length error", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_bbox_type_error() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "Feature",
                    "bbox": [-10.0, -10.0, 10.0, "Error"],
                    "geometry": {
                        "type": "Polygon",
                        "coordinates": [
                            [
                                [-10.0, -10.0],
                                [10.0, -10.0],
                                [10.0, 10.0],
                                [-10.0, -10.0]
                            ]
                        ]
                    }
                }        
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[bbox] value is not Double", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_geometry_no_type() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                   "type": "Feature",
                   "geometry": {
                       "coordinates": [102.0, 0.5]
                   },
                   "properties": {
                       "prop0": "value0"
                   }
                }        
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[Geometry] No type", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_geometry_type_error() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "Points",
                    "coordinates": [100.0, 0.0]
                }       
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[Geometry] type error", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_geometry_no_coordinates() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "Point"
                }       
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[Geometry] No coordinates", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_geometries_no_geometries() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "GeometryCollection"
                }   
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[Geometries] No geometries", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_position_length_error() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "Point",
                    "coordinates": [100.0]
                }    
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[Position] length error", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_linestring_length_error() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "LineString",
                    "coordinates": [
                        [100.0, 0.0]
                    ]
                }    
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[LineString] length error", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_polygon_length_error() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "Polygon",
                    "coordinates": [
                        [
                            [100.0, 0.0],
                            [101.0, 0.0],
                            [100.0, 0.0]
                        ]
                    ]
                }   
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[Polygon] length error", result.exceptionOrNull()?.message
        )
    }

    @Test
    fun geojsonException_polygon_first_and_last_not_match() {
        val result = runCatching {
            GeoJSON.parse(
                """
                {
                    "type": "Polygon",
                    "coordinates": [
                        [
                            [100.0, 0.0],
                            [101.0, 0.0],
                            [101.0, 1.0],
                            [100.0, 1.0]
                        ]
                    ]
                }   
                """.trimIndent()
            )
        }
        Assert.assertEquals(
            "[Polygon] first and last not match", result.exceptionOrNull()?.message
        )
    }
}
