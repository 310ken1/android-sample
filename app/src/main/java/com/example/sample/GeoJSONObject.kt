package com.example.sample

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

class GeoJSONObject() {

    companion object {
        class GeoJSONException(s: String) : Exception(s) {}
        open class GeoJSON() {
            var bbox: Array<Double>? = null
        }

        class FeatureCollection(val features: Array<Feature>) : GeoJSON()
        class Feature(val geometry: Geometry, val properties: JSONObject?) : GeoJSON()
        open class Geometry
        class Point(val position: Position) : Geometry() {}
        class MultiPoint(val positions: Array<Position>) : Geometry() {}
        class LineString(val positions: Array<Position>) : Geometry() {}
        class MultiLineString(val lines: Array<LineString>) : Geometry() {}
        class Polygon(val lines: Array<LineString>) : Geometry() {}
        class MultiPolygon(val polygons: Array<Polygon>) : Geometry() {}
        class GeometryCollection(val geometries: Array<Geometry>) : Geometry() {}
        data class Position(
            val latitude: Double,
            val longitude: Double,
            val altitude: Double? = Double.NaN
        ) {}

        @Throws(JSONException::class, GeoJSONException::class)
        fun parse(geojson: String): FeatureCollection {
            val obj = JSONObject(geojson)
            val type = obj.optString("type")
            if (type != "FeatureCollection")
                throw GeoJSONException("[FeatureCollection] No FeatureCollection")
            val collection = FeatureCollection(getFeatures(obj.getJSONArray("features")))
            val bbox = obj.optJSONArray("bbox")
            if (null != bbox) {
                collection.bbox = getBoundingBox(bbox)
            }
            return collection
        }

        @Throws(IOException::class, JSONException::class, GeoJSONException::class)
        fun parse(file: File): FeatureCollection {
            return parse(readString(file))
        }

        private fun getFeatures(array: JSONArray): Array<Feature> {
            var features = arrayOf<Feature>()
            for (i in 0 until array.length()) {
                features += getFeature(array.getJSONObject(i))
            }
            return features
        }

        private fun getBoundingBox(array: JSONArray): Array<Double> {
            if (0 != (array.length() % 2)) throw GeoJSONException("[bbox] length error")
            var bbox = arrayOf<Double>()
            for (i in 0 until array.length()) {
                val d = array.optDouble(i)
                if (d.isNaN()) throw GeoJSONException("[bbox] value is not Double")
                bbox += d
            }
            return bbox
        }

        private fun getFeature(obj: JSONObject): Feature {
            return Feature(
                getGeometry(obj.getJSONObject("geometry")),
                obj.optJSONObject("properties")
            )
        }

        private fun getGeometry(obj: JSONObject): Geometry {
            val type = obj.optString("type")
                ?: throw GeoJSONException("[Geometry] No type")
            val coordinates = obj.optJSONArray("coordinates")
                ?: throw GeoJSONException("[Geometry] No coordinates")
            when (type) {
                "Point" -> return getPoint(coordinates)
                "MultiPoint" -> return getMultiPoint(coordinates)
                "LineString" -> return getLineString(coordinates)
                "MultiLineString" -> return getMultiLineString(coordinates)
                "Polygon" -> return getPolygon(coordinates)
                "MultiPolygon" -> return getMultiPolygon(coordinates)
                "GeometryCollection" -> return getGeometryCollection(coordinates)
                else -> throw GeoJSONException("[Geometry] type error")
            }
        }

        private fun getPosition(array: JSONArray): Position {
            if (array.length() < 2) throw GeoJSONException("[Position] length error")
            return Position(
                array.getDouble(1),
                array.getDouble(0),
                array.optDouble(2)
            )
        }

        private fun getPoint(array: JSONArray): Geometry {
            return Point(getPosition(array))
        }

        private fun getMultiPoint(array: JSONArray): Geometry {
            return MultiPoint(Array(array.length()) { i -> getPosition(array.getJSONArray(i)) })
        }

        private fun getLineString(array: JSONArray): Geometry {
            if (array.length() < 2) throw GeoJSONException("[LineString] length error")
            return LineString(Array(array.length()) { i -> getPosition(array.getJSONArray(i)) })
        }

        private fun getMultiLineString(array: JSONArray): Geometry {
            return MultiLineString(Array(array.length()) { i ->
                getLineString(array.getJSONArray(i)) as LineString
            })
        }

        private fun getPolygon(array: JSONArray): Geometry {
            return Polygon(Array(array.length()) { i ->
                val linestring = getLineString(array.getJSONArray(i)) as LineString
                if (linestring.positions.size < 4)
                    throw GeoJSONException("[Polygon] lenght error")
                if (linestring.positions.first() != linestring.positions.last())
                    throw GeoJSONException("[Polygon] first and last not match")
                linestring
            })
        }

        private fun getMultiPolygon(array: JSONArray): Geometry {
            return MultiPolygon(Array(array.length()) { i ->
                getPolygon(array.getJSONArray(i)) as Polygon
            })
        }

        private fun getGeometryCollection(array: JSONArray): Geometry {
            return GeometryCollection(Array(array.length()) { i ->
                getGeometry(array.getJSONObject(i))
            })
        }

        @Throws(IOException::class)
        private fun readString(file: File): String {
            val builder = StringBuilder()
            val reader = BufferedReader(FileReader(file.toString()))
            var string = reader.readLine()
            while (string != null) {
                builder.append(string + System.getProperty("line.separator"))
                string = reader.readLine()
            }
            return builder.toString()
        }
    }
}