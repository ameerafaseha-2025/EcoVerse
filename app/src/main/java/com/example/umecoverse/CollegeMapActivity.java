
package com.example.umecoverse;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CollegeMapActivity extends AppCompatActivity {

    private MapView collegeMapView;
    private ExecutorService executor;
    private SharedPreferences locationCache;

    private final Set<String> displayedMarkers = new HashSet<>();

    // All 13 UM residential colleges
    // Format: short name, full name, OSM type, OSM ID
    private final String[][] colleges = {
            {"KK1", "Tunku Abdul Rahman", "way", "1007653223"},
            {"KK2", "Tuanku Bahiyah", "way", "1007727371"},
            {"KK3", "Tuanku Kurshiah", "way", "1021004515"},
            {"KK4", "Bestari", "relation", "18004465"},
            {"KK5", "Dayasari", "way", "996047856"},
            {"KK6", "Ibnu Sina (Avicenna)", "relation", "13372647"},
            {"KK7", "Za'ba", "way", "1162441098"},
            {"KK8", "Kinabalu", "relation", "16850845"},
            {"KK9", "Tun Syed Zahiruddin", "relation", "17087779"},
            {"KK10", "Tun Ahmad Zaidi", "way", "1031055118"},
            {"KK11", "Raja Ungku Aziz", "way", "996669810"},
            {"KK12", "Raja Dr. Nazrin Shah", "way", "992786250"},
            {"KK13", "Kolej Kediaman Ke-13", "relation", "14114109"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );

        Configuration.getInstance().setUserAgentValue(
                "UMEcoverse/1.0 (Android student project)"
        );

        setContentView(R.layout.activity_college_map);

        collegeMapView = findViewById(R.id.collegeMapView);

        locationCache = getSharedPreferences(
                "college_locations",
                MODE_PRIVATE
        );

        executor = Executors.newFixedThreadPool(4);

        setupMap();
        loadAllCollegeMarkers();
    }

    private void setupMap() {

        String apiKey = BuildConfig.MAPTILER_API_KEY;

        if (apiKey == null || apiKey.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "MapTiler API key is missing",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Existing MapTiler map
        OnlineTileSourceBase mapTilerSource =
                new OnlineTileSourceBase(
                        "MapTilerStreets",
                        0,
                        19,
                        256,
                        ".png",
                        new String[]{
                                "https://api.maptiler.com/maps/streets-v2/"
                        },
                        "© OpenStreetMap contributors © MapTiler",
                        new TileSourcePolicy(
                                2,
                                TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                        )
                ) {
                    @Override
                    public String getTileURLString(long tileIndex) {

                        int zoom = MapTileIndex.getZoom(tileIndex);
                        int x = MapTileIndex.getX(tileIndex);
                        int y = MapTileIndex.getY(tileIndex);

                        return getBaseUrl()
                                + zoom + "/" + x + "/" + y
                                + ".png?key=" + apiKey;
                    }
                };

        collegeMapView.setTileSource(mapTilerSource);
        collegeMapView.setMultiTouchControls(true);

        // University of Malaya
        GeoPoint umLocation = new GeoPoint(
                3.1209,
                101.6559
        );

        collegeMapView.getController().setZoom(15.5);
        collegeMapView.getController().setCenter(umLocation);
    }

    private void loadAllCollegeMarkers() {

        for (String[] college : colleges) {

            String shortName = college[0];
            String fullName = college[1];
            String type = college[2];
            String osmId = college[3];

            // Show previously saved location immediately
            String latKey = shortName + "_lat";
            String lonKey = shortName + "_lon";

            if (locationCache.contains(latKey)
                    && locationCache.contains(lonKey)) {

                double lat = Double.longBitsToDouble(
                        locationCache.getLong(latKey, 0)
                );

                double lon = Double.longBitsToDouble(
                        locationCache.getLong(lonKey, 0)
                );

                addCollegeMarker(
                        shortName,
                        fullName,
                        new GeoPoint(lat, lon)
                );

                continue;
            }

            // Download only locations not yet saved
            executor.execute(() -> {

                try {
                    GeoPoint location =
                            fetchOsmLocation(type, osmId);

                    if (location == null) {
                        throw new Exception("No OSM coordinates found");
                    }

                    locationCache.edit()
                            .putLong(
                                    latKey,
                                    Double.doubleToRawLongBits(
                                            location.getLatitude()
                                    )
                            )
                            .putLong(
                                    lonKey,
                                    Double.doubleToRawLongBits(
                                            location.getLongitude()
                                    )
                            )
                            .apply();

                    runOnUiThread(() -> {

                        if (!isFinishing() && !isDestroyed()) {
                            addCollegeMarker(
                                    shortName,
                                    fullName,
                                    location
                            );
                        }
                    });

                } catch (Exception e) {

                    Log.e(
                            "CollegeMap",
                            "Failed to load " + shortName,
                            e
                    );
                }
            });
        }
    }

    private GeoPoint fetchOsmLocation(
            String type,
            String osmId
    ) throws Exception {

        String endpoint =
                "https://api.openstreetmap.org/api/0.6/"
                        + type + "/" + osmId + "/full";

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(endpoint).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(12000);
            connection.setReadTimeout(20000);

            connection.setRequestProperty(
                    "User-Agent",
                    "UMEcoverse/1.0 (University student project)"
            );

            if (connection.getResponseCode() != 200) {
                throw new Exception(
                        "OSM HTTP " + connection.getResponseCode()
                );
            }

            XmlPullParserFactory factory =
                    XmlPullParserFactory.newInstance();

            XmlPullParser parser = factory.newPullParser();

            try (InputStream stream =
                         connection.getInputStream()) {

                parser.setInput(stream, "UTF-8");

                double minLat = Double.MAX_VALUE;
                double maxLat = -Double.MAX_VALUE;
                double minLon = Double.MAX_VALUE;
                double maxLon = -Double.MAX_VALUE;

                int nodeCount = 0;
                int event = parser.getEventType();

                while (event != XmlPullParser.END_DOCUMENT) {

                    if (event == XmlPullParser.START_TAG
                            && "node".equals(parser.getName())) {

                        String latValue =
                                parser.getAttributeValue(null, "lat");

                        String lonValue =
                                parser.getAttributeValue(null, "lon");

                        if (latValue != null && lonValue != null) {

                            double lat =
                                    Double.parseDouble(latValue);

                            double lon =
                                    Double.parseDouble(lonValue);

                            minLat = Math.min(minLat, lat);
                            maxLat = Math.max(maxLat, lat);

                            minLon = Math.min(minLon, lon);
                            maxLon = Math.max(maxLon, lon);

                            nodeCount++;
                        }
                    }

                    event = parser.next();
                }

                if (nodeCount == 0) {
                    return null;
                }

                // Approximate center of the OSM area
                return new GeoPoint(
                        (minLat + maxLat) / 2.0,
                        (minLon + maxLon) / 2.0
                );
            }

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void addCollegeMarker(
            String shortName,
            String fullName,
            GeoPoint location
    ) {

        if (displayedMarkers.contains(shortName)) {
            return;
        }

        displayedMarkers.add(shortName);

        Marker marker = new Marker(collegeMapView);

        marker.setPosition(location);

        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        marker.setTitle(
                shortName + " - " + fullName
        );

        marker.setSnippet(
                "Universiti Malaya Residential College"
        );

        marker.setOnMarkerClickListener(
                (clickedMarker, mapView) -> {

                    clickedMarker.showInfoWindow();

                    mapView.getController().animateTo(
                            clickedMarker.getPosition()
                    );

                    return true;
                }
        );

        collegeMapView.getOverlays().add(marker);
        collegeMapView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (collegeMapView != null) {
            collegeMapView.onResume();
        }
    }

    @Override
    protected void onPause() {

        if (collegeMapView != null) {
            collegeMapView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (executor != null) {
            executor.shutdownNow();
        }

        if (collegeMapView != null) {
            collegeMapView.onDetach();
        }

        super.onDestroy();
    }
}
