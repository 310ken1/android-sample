package com.example.sample.utility

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class GeoJSON(text: String) {
    val obj: GeoJSONObject

    companion object {
        fun parse(text: String): GeoJSONObject {
            return GeoJSON(text).obj
        }

        fun parse(file: File): GeoJSONObject {
            return GeoJSON(readString(file)).obj
        }

        private fun readString(file: File): String {
            val builder = StringBuilder()
            val reader = BufferedReader(FileReader(file.toString()))
            var string: String? = reader.readLine()
            while (string != null) {
                builder.append(string + System.getProperty("line.separator"))
                string = reader.readLine()
            }
            return builder.toString()
        }
    }

    init {
        val json = JSONObject(text)
        obj = when (json.optString("type")) {
            "FeatureCollection" -> getFeatureCollection(json)
            "Feature" -> getFeature(json)
            else -> getGeometry(json)
        }
    }

    constructor(file: File) : this(readString(file))

    class GeoJSONException(s: String) : Exception(s)

    open class GeoJSONObject(var bbox: Array<Double> = arrayOf<Double>())

    class FeatureCollectionObject(
        val features: Array<FeatureObject>, bbox: Array<Double>
    ) : GeoJSONObject(bbox)

    class FeatureObject(
        val geometry: GeometryObject, val properties: JSONObject?, bbox: Array<Double>
    ) : GeoJSONObject(bbox)

    open class GeometryObject(bbox: Array<Double>) : GeoJSONObject(bbox)

    class Point(val position: Position, bbox: Array<Double>) : GeometryObject(bbox)
    class MultiPoint(val positions: Array<Position>, bbox: Array<Double>) : GeometryObject(bbox)
    class LineString(val positions: Array<Position>, bbox: Array<Double>) : GeometryObject(bbox)
    class MultiLineString(val lines: Array<LineString>, bbox: Array<Double>) : GeometryObject(bbox)
    class Polygon(val lines: Array<LineString>, bbox: Array<Double>) : GeometryObject(bbox)
    class MultiPolygon(val polygons: Array<Polygon>, bbox: Array<Double>) : GeometryObject(bbox)
    class GeometryCollection(val geometries: Array<GeometryObject>, bbox: Array<Double>) :
        GeometryObject(bbox)

    data class Position(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double? = Double.NaN
    )

    private fun getFeatureCollection(obj: JSONObject): FeatureCollectionObject {
        return FeatureCollectionObject(
            getFeatures(obj.getJSONArray("features")),
            getBoundingBox(obj.optJSONArray("bbox"))
        )
    }

    private fun getFeatures(array: JSONArray): Array<FeatureObject> {
        var features = arrayOf<FeatureObject>()
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


    private fun getFeature(obj: JSONObject): FeatureObject {
        return FeatureObject(
            getGeometry(obj.getJSONObject("geometry")),
            obj.optJSONObject("properties"),
            getBoundingBox(obj.optJSONArray("bbox"))
        )
    }

    private fun getGeometry(obj: JSONObject): GeometryObject {
        val type = obj.optString("type").apply {
            if (isEmpty()) throw GeoJSONException("[Geometry] No type")
        }
        return when (type) {
            "Point" -> getPoint(obj)
            "MultiPoint" -> getMultiPoint(obj)
            "LineString" -> getLineString(obj)
            "MultiLineString" -> getMultiLineString(obj)
            "Polygon" -> getPolygon(obj)
            "MultiPolygon" -> getMultiPolygon(obj)
            "GeometryCollection" -> getGeometryCollection(obj)
            else -> throw GeoJSONException("[Geometry] type error")
        }
    }

    private fun getCoordinates(obj: JSONObject): JSONArray {
        return obj.optJSONArray("coordinates")
            ?: throw GeoJSONException("[Geometry] No coordinates")
    }

    private fun getGeometries(obj: JSONObject): JSONArray {
        return obj.optJSONArray("geometries")
            ?: throw GeoJSONException("[Geometries] No geometries")
    }

    private fun getPosition(array: JSONArray): Position {
        if (array.length() < 2) throw GeoJSONException("[Position] length error")
        return Position(
            array.getDouble(1),
            array.getDouble(0),
            array.optDouble(2)
        )
    }

    private fun getPoint(obj: JSONObject): GeometryObject {
        return Point(
            getPosition(getCoordinates(obj)),
            getBoundingBox(obj.optJSONArray("bbox"))
        )
    }

    private fun getMultiPoint(obj: JSONObject): GeometryObject {
        val array = getCoordinates(obj)
        return MultiPoint(
            Array(array.length()) { i -> getPosition(array.getJSONArray(i)) },
            getBoundingBox(obj.optJSONArray("bbox"))
        )
    }

    private fun getLineString(obj: JSONObject): GeometryObject {
        return getLineString(getCoordinates(obj)).apply {
            this.bbox = getBoundingBox(obj.optJSONArray("bbox"))
        }
    }

    private fun getLineString(array: JSONArray): GeometryObject {
        if (array.length() < 2) throw GeoJSONException("[LineString] length error")
        return LineString(
            Array(array.length()) { i -> getPosition(array.getJSONArray(i)) },
            arrayOf<Double>()
        )
    }

    private fun getMultiLineString(obj: JSONObject): GeometryObject {
        val array = getCoordinates(obj)
        return MultiLineString(
            Array(array.length()) { i -> getLineString(array.getJSONArray(i)) as LineString },
            getBoundingBox(obj.optJSONArray("bbox"))
        )
    }

    private fun getPolygon(obj: JSONObject): GeometryObject {
        return getPolygon(getCoordinates(obj)).apply {
            this.bbox = getBoundingBox(obj.optJSONArray("bbox"))
        }
    }

    private fun getPolygon(array: JSONArray): GeometryObject {
        return Polygon(
            Array(array.length()) { i ->
                val linestring = getLineString(array.getJSONArray(i)) as LineString
                if (linestring.positions.size < 4)
                    throw GeoJSONException("[Polygon] length error")
                if (linestring.positions.first() != linestring.positions.last())
                    throw GeoJSONException("[Polygon] first and last not match")
                linestring
            },
            arrayOf<Double>()
        )
    }

    private fun getMultiPolygon(obj: JSONObject): GeometryObject {
        val array = getCoordinates(obj)
        return MultiPolygon(
            Array(array.length()) { i ->
                getPolygon(array.getJSONArray(i)) as Polygon
            },
            getBoundingBox(obj.optJSONArray("bbox"))
        )
    }

    private fun getGeometryCollection(obj: JSONObject): GeometryObject {
        val array = getGeometries(obj)
        return GeometryCollection(
            Array(array.length()) { i ->
                getGeometry(array.getJSONObject(i))
            },
            getBoundingBox(obj.optJSONArray("bbox"))
        )
    }
}
