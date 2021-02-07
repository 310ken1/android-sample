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
        open class GeoJSON(val bbox: Array<Double>)
        class FeatureCollection(val features: Array<Feature>, bbox: Array<Double>) : GeoJSON(bbox)
        class Feature(val geometry: Geometry, val properties: JSONObject?, bbox: Array<Double>) :
            GeoJSON(bbox)

        open class Geometry
        class Point(val position: Position) : Geometry()
        class MultiPoint(val positions: Array<Position>) : Geometry()
        class LineString(val positions: Array<Position>) : Geometry()
        class MultiLineString(val lines: Array<LineString>) : Geometry()
        class Polygon(val lines: Array<LineString>) : Geometry()
        class MultiPolygon(val polygons: Array<Polygon>) : Geometry()
        class GeometryCollection(val geometries: Array<Geometry>) : Geometry()
        data class Position(
            val latitude: Double,
            val longitude: Double,
            val altitude: Double? = Double.NaN
        )

        @Throws(JSONException::class, GeoJSONException::class)
        fun parse(geojson: String): FeatureCollection {
            val obj = JSONObject(geojson)
            val type = obj.optString("type")
            if (type != "FeatureCollection")
                throw GeoJSONException("[FeatureCollection] No FeatureCollection")
            return FeatureCollection(
                getFeatures(obj.getJSONArray("features")),
                getBoundingBox(obj.optJSONArray("bbox"))
            )
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

        private fun getBoundingBox(array: JSONArray?): Array<Double> {
            var bbox = arrayOf<Double>()
            if (null != array) {
                if (0 != (array.length() % 2)) throw GeoJSONException("[bbox] length error")
                for (i in 0 until array.length()) {
                    val d = array.optDouble(i)
                    if (d.isNaN()) throw GeoJSONException("[bbox] value is not Double")
                    bbox += d
                }
            }
            return bbox
        }

        private fun getFeature(obj: JSONObject): Feature {
            return Feature(
                getGeometry(obj.getJSONObject("geometry")),
                obj.optJSONObject("properties"),
                getBoundingBox(obj.optJSONArray("bbox"))
            )
        }

        private fun getGeometry(obj: JSONObject): Geometry {
            val type = obj.optString("type")
                ?: throw GeoJSONException("[Geometry] No type")
            when (type) {
                "Point" -> return getPoint(getCoordinates(obj))
                "MultiPoint" -> return getMultiPoint(getCoordinates(obj))
                "LineString" -> return getLineString(getCoordinates(obj))
                "MultiLineString" -> return getMultiLineString(getCoordinates(obj))
                "Polygon" -> return getPolygon(getCoordinates(obj))
                "MultiPolygon" -> return getMultiPolygon(getCoordinates(obj))
                "GeometryCollection" -> return getGeometryCollection(getGeometries(obj))
                else -> throw GeoJSONException("[Geometry] type error")
            }
        }

        private fun getCoordinates(obj: JSONObject): JSONArray {
            return obj.optJSONArray("coordinates")
                ?: throw GeoJSONException("[Geometry] No coordinates")
        }

        private fun getGeometries(obj: JSONObject): JSONArray {
            return obj.optJSONArray("geometries")
                ?: throw GeoJSONException("[Geometry] No geometries")
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