package com.example.gbird;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Url;

public class MapHotspots extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private LatLng lastKnownLocationLatLng;
    private GoogleMap myMap;
    private Polyline routePolyline;
    private Button Nearhotspot, AllHotspot;

    private SwitchMaterial metricSwitch;
    private TextView metricTV;
    FloatingActionButton goHome;
    private MyApi api;
    private Circle circle;
    private double latitude;
    private double longitude;
    BitmapDescriptor customMarker, myOwnMarker, seenBird, seeneye, seenCuteBird;
    private final static int REQUEST_CODE = 100;
    private boolean useMetricUnits = true;

    private List<Marker> markers = new ArrayList<>();
    HashMap<Marker, Boolean> markersUpdated = new HashMap<>();
    private DatabaseReference birdSightingsRef;
    private HashMap<String, Marker> birdMarkers;
    private DatabaseReference userSettingsRef;
    private UserSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_hotspots);
        metricSwitch = findViewById(R.id.metricSwitch); // Initialize metricSwitch
        metricTV = findViewById(R.id.metricTV); // Initialize metricTV
        goHome = findViewById(R.id.more);


        // Initialize Firebase components
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userSettingsRef = FirebaseDatabase.getInstance().getReference("user_settings").child(userId);
        }

        // Initialize UserSettings
        settings = (UserSettings) getApplication();

       // loadFirebaseThemeSetting();
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentt = new Intent(MapHotspots.this, home.class);
                startActivity(intentt);
                finish();
            }
        });

        birdSightingsRef = FirebaseDatabase.getInstance().getReference("BirdSightings");
        birdMarkers = new HashMap<>();

        metricSwitch.setChecked(true);

        metricSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    // Kilometer is selected
                    metricTV.setText("Kilometer");
                    useMetricUnits = true;

                    // Check if any markers have been clicked before updating titles
                    if (markersUpdated != null && !markersUpdated.isEmpty()) {
                        for (Marker marker : markersUpdated.keySet()) {
                            double distance = calculateDistance(lastKnownLocationLatLng, marker.getPosition());
                            double displayedDistance = useMetricUnits ? distance : distance * 0.621371192;

                            String unitText = useMetricUnits ? "Kilometer" : "Miles";
                            marker.setTitle(marker.getTitle() + " (" + String.format("%.2f", displayedDistance) + " " + unitText + ")");
                            marker.showInfoWindow();
                        }
                    }
                } else {
                    // Miles is selected
                    metricTV.setText("Miles");
                    useMetricUnits = false;

                    // Check if any markers have been clicked before updating titles
                    if (markersUpdated != null && !markersUpdated.isEmpty()) {
                        for (Marker marker : markersUpdated.keySet()) {
                            double distance = calculateDistance(lastKnownLocationLatLng, marker.getPosition());
                            double displayedDistance = useMetricUnits ? distance : distance * 0.621371192;

                            String unitText = useMetricUnits ? "Kilometer" : "Miles";
                            marker.setTitle(marker.getTitle() + " (" + String.format("%.2f", displayedDistance) + " " + unitText + ")");
                            marker.showInfoWindow();
                        }
                    }
                }
                // Handle any unit-specific functionality or UI updates here
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        customMarker = BitmapDescriptorFactory.fromResource(R.drawable.birdfour);
        myOwnMarker = BitmapDescriptorFactory.fromResource(R.drawable.locationicon);
        seenBird = BitmapDescriptorFactory.fromResource(R.drawable.xheart);
        seeneye = BitmapDescriptorFactory.fromResource(R.drawable.xeye);
        seenCuteBird = BitmapDescriptorFactory.fromResource(R.drawable.xcutebird);
        Nearhotspot = findViewById(R.id.nearhotspot);
        AllHotspot = findViewById(R.id.everyhotspot);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            askPermission();
        }

        Nearhotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // method to show only hotspots 10kilometers from the current location
                showCircle();
                filterAndDisplayNearbyMarkers(10.0);
                Toast.makeText(MapHotspots.this, "Displaying Hotspots 10 KM near you", Toast.LENGTH_LONG).show();
            }
        });

        AllHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clear all markers on the map
                LatLng userLatLng = lastKnownLocationLatLng;
                myMap.clear();

                // Re-add all the hotspots to the map
                for (Marker marker : markers) {
                    myMap.addMarker(new MarkerOptions()
                            .position(marker.getPosition())
                            .title(marker.getTitle())
                            .icon(customMarker));
                }

                // Re-add all bird markers to the map
                for (Marker birdMarker : birdMarkers.values()) {
                    myMap.addMarker(new MarkerOptions()
                            .position(birdMarker.getPosition())
                            .title(birdMarker.getTitle())
                            .icon(seenCuteBird));
                }

                // Add the user's location marker
                myMap.addMarker(new MarkerOptions()
                        .position(userLatLng)
                        .title("Current Location")
                        .icon(myOwnMarker));
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create Retrofit instance
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        clientBuilder.addInterceptor(new RetryInterceptor(3)); // Retry up to 3 times

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.ebird.org/v2/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(clientBuilder.build())
                .build();

        api = retrofit.create(MyApi.class);

        getLastLocation();
        fetchBirdSightings();
        setupFirebase();
    }

    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userSettingsRef = FirebaseDatabase.getInstance().getReference("user_settings").child(userId);
        }

        loadFirebaseThemeSetting();
    }

    private void loadFirebaseThemeSetting() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userSettingsRef = FirebaseDatabase.getInstance().getReference("user_settings").child(userId);
            userSettingsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            String userSettings = dataSnapshot.child("unitsPreference").getValue(String.class);
                            if (userSettings != null && userSettings.equals(UserSettings.KILOMETERS)) {
                                metricSwitch.setChecked(true);
                            }
                            else{
                                metricSwitch.setChecked(false);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle the exception, e.g., log it or show a toast
                        Toast.makeText(MapHotspots.this, "Error loading settings", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                    Toast.makeText(MapHotspots.this, "Database error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

        private void fetchBirdSightings() {
        birdSightingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear existing markers
                clearBirdMarkers();

                for (DataSnapshot sightingSnapshot : dataSnapshot.getChildren()) {
                    birdModel birdSighting = sightingSnapshot.getValue(birdModel.class);
                    if (birdSighting != null) {
                        // Create a marker for each bird sighting
                        createBirdMarker(birdSighting);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MapHotspots", "Error fetching bird sightings: " + databaseError.getMessage());
                Toast.makeText(MapHotspots.this, "Error fetching bird sightings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createBirdMarker(birdModel birdSighting) {
        String birdName = birdSighting.getBirdnames();
        double latitude = birdSighting.getBirdLatitudes();
        double longitude = birdSighting.getBirdLongitudes();
        String countz = birdSighting.getBirdCounts();

        // Check if the bird marker already exists
        if (!birdMarkers.containsKey(birdName)) {
            // Create a new marker
            Marker marker = myMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(countz + " "+ birdName)
                    .icon(seenCuteBird));

            // Add the marker to the map and the HashMap
            birdMarkers.put(birdName, marker);
        }
    }

    private void clearBirdMarkers() {
        // Clear existing bird markers on the map
        for (Marker marker : birdMarkers.values()) {
            marker.remove();
        }

        // Clear the HashMap
        birdMarkers.clear();
    }




                private void updateMarkerTitles() {
        for (Marker marker : markers) {
            if (!marker.getTitle().equals("Current Location")) {
                LatLng destination = marker.getPosition();
                double distance = calculateDistance(lastKnownLocationLatLng, destination);
                double displayDistance;
                String unit;

                if (useMetricUnits) {
                    displayDistance = distance; // Kilometers
                    unit = "km";
                } else {
                    displayDistance = distance * 0.621371192; // Miles
                    unit = "Miles";
                }

                marker.setTitle(marker.getTitle() + " " + String.format("%.2f", displayDistance) + " " + unit);
                marker.showInfoWindow();

            }
        }
    }


    private void showCircle() {
        // Show the circle overlay when the "Nearhotspot" button is clicked
        if (circle != null) {
            circle.setVisible(true);
        }
    }


    public void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();

                                // Make the API request here with the updated URL
                                fetchDataFromApi();
                            }
                        }
                    });
        } else {
            askPermission();
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(MapHotspots.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private void fetchDataFromApi() {
        // Replace the curly braces in the API URL with the actual latitude and longitude values
        String apiUrl = "ref/hotspot/geo?lat=" + latitude + "&lng=" + longitude;
        Call<ResponseBody> call = api.getData(apiUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseText = response.body().string();

                        // Split the response text into lines
                        String[] lines = responseText.split("\n");

                        // Create a JsonArray to store the data
                        JsonArray jsonArray = new JsonArray();

                        for (String line : lines) {
                            // Split each line into individual components using commas
                            String[] components = line.split(",");

                            // Check if there are enough components
                            if (components.length >= 9) {
                                // Handle empty values for latitude and longitude
                                try {
                                    double latitude = Double.parseDouble(components[4]);
                                    double longitude = Double.parseDouble(components[5]);
                                    // Add markers for each latitude and longitude
                                    addMarker(latitude, longitude, components[6]); // Use place as marker title
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    Log.e("MapHotspots", "Error parsing latitude or longitude as a double.");
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("MapHotspots", "Error reading response body.");
                        Toast.makeText(MapHotspots.this, "Error reading body", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the error response
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("MapHotspots", "Error Response: " + errorBody);
                        // Handle the error message in the errorBody
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("MapHotspots", "Error reading error response.");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Handle the network error

                // Log a message for the network error
                Log.e("MapHotspots", "Network request failed: " + t.getMessage());
            }
        });
    }

    // Function to add a marker with a given latitude, longitude, and title

    private void addMarker(double latitude, double longitude, String title) {
        LatLng location = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(title)
                .icon(customMarker); // Set the custom marker icon

        Marker marker = myMap.addMarker(markerOptions);
        markers.add(marker);
    }


    private void filterAndDisplayNearbyMarkers(double maxDistanceInKm) {
        if (lastKnownLocationLatLng == null) {
            Log.e("Location", "User's location is not available.");
            return;
        }

        LatLng userLatLng = lastKnownLocationLatLng;

        // Clear all markers on the map
        myMap.clear();

        // Add the user's location marker
        myMap.addMarker(new MarkerOptions()
                .position(userLatLng)
                .title("Current Location")
                .icon(myOwnMarker));

        // Add the general markers within the specified distance
        for (Marker marker : markers) {
            LatLng hotspotLatLng = marker.getPosition();
            double distance = calculateDistance(userLatLng, hotspotLatLng);
            if (distance <= maxDistanceInKm) {
                myMap.addMarker(new MarkerOptions()
                        .position(hotspotLatLng)
                        .title(marker.getTitle())
                        .icon(customMarker));
            }
        }

        // Add the bird markers within the specified distance
        for (Marker birdMarker : birdMarkers.values()) {
            LatLng birdLatLng = birdMarker.getPosition();
            double distance = calculateDistance(userLatLng, birdLatLng);
            if (distance <= maxDistanceInKm) {
                myMap.addMarker(new MarkerOptions()
                        .position(birdLatLng)
                        .title(birdMarker.getTitle())
                        .icon(seenCuteBird));
            }
        }
    }





    // Retrofit API Interface
    public interface MyApi {
        @GET
        Call<ResponseBody> getData(@Url String apiUrl);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        // Add a marker at the user's location
        addMarkerAtCurrentUserLocation();

        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(0, 0)) // Center coordinates (initially at the equator)
                .radius(1000) // Default radius (1 kilometer)
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(70, 0, 0, 255));

        // Add a marker for the default circle center (0, 0)
        myMap.addMarker(new MarkerOptions().position(circleOptions.getCenter()).title("Circle Center"));

        // Create the circle based on the circle options
        circle = myMap.addCircle(circleOptions);

        // Initially hide the circle
        circle.setVisible(false);// Set the fill color with transparency

        Circle circle = myMap.addCircle(circleOptions);

        // Set up marker click listener
        myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Check if the clicked marker is not the user's location marker
                if (!marker.getTitle().equals("Current Location")) {
                    LatLng destination = marker.getPosition();
                    // Calculate the distance
                    double distance = calculateDistance(lastKnownLocationLatLng, destination);

                    // Clear previous polyline
                    if (routePolyline != null) {
                        routePolyline.remove();
                    }

                    double displayedDistance = useMetricUnits ? distance : distance * 0.621371192;

                    // Update the title based on the selected unit
                    String unitText = useMetricUnits ? "Kilometer" : "Miles";
                    marker.setTitle(marker.getTitle() + " (" + String.format("%.2f", displayedDistance) + " " + unitText + ")");
                    marker.showInfoWindow();

                    // Calculate and draw the route from user's location to the clicked marker
                    calculateRouteAndDrawPolyline(destination);
                }
                return false;
            }
        });


        // Filter and display nearby markers within the circle
        for (Marker marker : markers) {
            LatLng markerPosition = marker.getPosition();
            double distance = calculateDistance(lastKnownLocationLatLng, markerPosition);

            if (distance <= 10) { // Adjust the distance as needed (in kilometers)
                marker.setVisible(true);
            } else {
                marker.setVisible(false);
            }
        }
    }

    private void addMarkerAtCurrentUserLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lastKnownLocation = location;
                                lastKnownLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                myMap.addMarker(new MarkerOptions().position(userLocation).title("Current Location").icon(myOwnMarker));
                                // Zoom to the user's location

                                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 11)); // Adjust the zoom level as needed
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Location", "Failed to get user's location: " + e.getMessage());
                        }
                    });
        } else {
            askPermission();
        }
    }


    private void calculateRouteAndDrawPolyline(LatLng destination) {
        if (lastKnownLocationLatLng != null) {
            new CalculateRouteTask().execute(lastKnownLocationLatLng, destination);
        } else {
            Log.e("Location", "User's location is not available.");
        }
    }



    private class CalculateRouteTask extends AsyncTask<LatLng, Void, DirectionsResult> {

        @Override
        protected DirectionsResult doInBackground(LatLng... params) {
            LatLng origin = params[0];
            LatLng destination = params[1];

            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyCH9rymQPCk6z5EFQc9ICHrbgdRmghovOc") // Replace with your Google Maps API key
                    .build();

            DirectionsApiRequest request = DirectionsApi.newRequest(context)
                    .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .mode(TravelMode.DRIVING);

            try {
                return request.await();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(DirectionsResult result) {
            if (result != null) {
                DirectionsRoute[] routes = result.routes;
                if (routes != null && routes.length > 0) {
                    String encodedPolyline = routes[0].overviewPolyline.getEncodedPath();
                    List<LatLng> points = PolyUtil.decode(encodedPolyline);

                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(points)
                            .color(getResources().getColor(R.color.polylineColor))
                            .width(10);

                    routePolyline = myMap.addPolyline(polylineOptions);
                }
            }
        }
    }

    private double calculateDistance(LatLng start, LatLng end) {
        Location startPoint = new Location("startPoint");
        startPoint.setLatitude(start.latitude);
        startPoint.setLongitude(start.longitude);

        Location endPoint = new Location("endPoint");
        endPoint.setLatitude(end.latitude);
        endPoint.setLongitude(end.longitude);

        return startPoint.distanceTo(endPoint) / 1000; // Distance in kilometers
    }
}