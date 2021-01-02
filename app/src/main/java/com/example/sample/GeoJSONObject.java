package com.example.sample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeoJSONObject {
    public ArrayList<Feature> features;

    public enum Type {
        None,
        FeatureCollection,
        Feature,
        Point,
        Polygon,
        MultiPolygon,
    }

    public class Coordinate {
    }

    public class Point extends Coordinate {
        Double latitude;
        Double longitude;

        Point(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public class Polygon extends Coordinate {
        ArrayList<Point> outside = new ArrayList<>();
        ArrayList<Point> inside = new ArrayList<>();

    }

    public class MultiPolygon extends Coordinate {
        ArrayList<Polygon> polygons = new ArrayList<>();
    }

    public class Geometry {
        Type type = Type.None;
        Coordinate coordinate = null;
    }

    public class Feature {
        Geometry geometry;
        Map<String, String> properties;

        Feature(Geometry geometry, Map<String, String> properties) {
            this.geometry = geometry;
            this.properties = properties;
        }
    }

    public GeoJSONObject(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        features = getFeatures(object.optJSONArray("features"));
    }

    public GeoJSONObject(File file) throws IOException, JSONException {
        this(readString(file));
    }

    private static String readString(File file) throws IOException {
        StringBuilder builder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new FileReader(file.toString()));
        String string = reader.readLine();
        while (string != null) {
            builder.append(string + System.getProperty("line.separator"));
            string = reader.readLine();
        }

        return builder.toString();
    }

    private ArrayList<Feature> getFeatures(JSONArray array) {
        ArrayList<Feature> features = new ArrayList<>();
        if (null != array) {
            for (int i = 0; i < array.length(); i++) {
                Feature feature = getFeature(array.optJSONObject(i));
                if (null != feature) {
                    features.add(feature);
                }
            }
        }
        return features;
    }

    private Feature getFeature(JSONObject object) {
        if (null == object) return null;
        Feature feature = new Feature(
                getGeometry(object.optJSONObject("geometry")),
                getProperties(object.optJSONObject("properties"))
        );
        return feature;
    }

    private Geometry getGeometry(JSONObject object) {
        if (null == object) return null;
        Geometry geometry = new Geometry();
        String typename = object.optString("type");
        JSONArray array = object.optJSONArray("coordinates");
        switch (typename) {
            case "Point":
                geometry.type = Type.Point;
                geometry.coordinate = getPoint(array);
                break;
            case "Polygon":
                geometry.type = Type.Polygon;
                geometry.coordinate = getPolygon(array);
                break;
            case "MultiPolygon":
                geometry.type = Type.MultiPolygon;
                geometry.coordinate = getMultiPolygon(array);
                break;
            default:
                break;
        }
        return geometry;
    }

    private Map<String, String> getProperties(JSONObject object) {
        Map<String, String> properties = new HashMap<>();
        JSONArray names = object.names();
        for (int i = 0; i < names.length(); i++) {
            String name = names.optString(i);
            properties.put(name, object.optString(name));
        }
        return properties;
    }

    private Point getPoint(JSONArray array) {
        if (null == array || 2 != array.length()) return null;
        return new Point(array.optDouble(0), array.optDouble(1));
    }

    private Polygon getPolygon(JSONArray array) {
        if (null == array || array.length() < 1 || 2 < array.length()) return null;
        Polygon polygon = new Polygon();
        polygon.outside = getPoints(array.optJSONArray(0));
        polygon.inside = getPoints(array.optJSONArray(1));
        return polygon;
    }

    private ArrayList<Point> getPoints(JSONArray array) {
        if (null == array || array.length() < 0) return null;
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Point point = getPoint(array.optJSONArray(i));
            if (null != point) {
                points.add(point);
            }
        }
        return points;
    }

    private MultiPolygon getMultiPolygon(JSONArray array) {
        if (null == array) return null;
        MultiPolygon multi = new MultiPolygon();
        for (int i = 0; i < array.length(); i++) {
            Polygon polygon = getPolygon(array.optJSONArray(i));
            if (null != polygon) {
                multi.polygons.add(polygon);
            }
        }
        return multi;
    }
}
