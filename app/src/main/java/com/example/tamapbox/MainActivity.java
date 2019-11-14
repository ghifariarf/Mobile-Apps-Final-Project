package com.example.tamapbox;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.matching.v5.MapboxMapMatching;
import com.mapbox.api.matching.v5.models.MapMatchingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener,
        PermissionsListener, MapboxMap.OnMapClickListener {
    private MapView mapView;
    private MapboxMap map;
    private static final String SOURCE_ID = "SOURCE_ID";
    private Button startButton;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private Point telkomPosition;
    private Point originPosition;
    private Point currentPosition;
    private Point destinationPosition;
    private Marker destinationMarker;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private Spinner locSpinner;
    private List<String> lokasi = new ArrayList<>();
    private List<String> lokasifb = new ArrayList<>();
    private List<Point> points = new ArrayList<>();
    private List<Object> datapoint = new ArrayList<>();
    private ArrayAdapter<String> dataadapter;
    private FirebaseFirestore db;
    private RequestQueue mQueue;

    public static String posisiawal = "Telkom University";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.acces_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        startButton = findViewById(R.id.startButton);
        locSpinner = findViewById(R.id.Spinner);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locSpinner = (Spinner) findViewById(R.id.Spinner);
        mQueue= Volley.newRequestQueue(this);

        lokasi.add("Telkom University");
        lokasi.add("Museum Asia Afrika");
        lokasi.add("Museum Geologi");
        lokasi.add("Museum Siliwangi");
        lokasi.add("Museum Sri Baduga");
        lokasi.add("Monumen Perjuangan Rakyat Jawa Barat");



        db = FirebaseFirestore.getInstance();
        // Get document dari Collection Tempat
        db.collection("tempat").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        String nama = document.getString("jalur");
                        lokasifb.add(nama);
                        datapoint.add(document.get("coordinat"));

                    }

//                    ArrayList test = (ArrayList) datapoint.get(1);
//                    for(int i=0; i<test.size(); i++){
//                        Log.d("CekFirebase", String.valueOf(test.get(i)));
//                    }


                    GeoPoint telkom = (GeoPoint) datapoint.get(0);
                    Point telkomawal = Point.fromLngLat(telkom.getLongitude(),telkom.getLatitude());
                    points.add(telkomawal);
                    LatLng telkommarker = new LatLng(points.get(0).latitude(), points.get(0).longitude());
                    destinationMarker = map.addMarker(new MarkerOptions().position(telkommarker));

                    dataadapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, lokasi);
                    dataadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    locSpinner.setAdapter(dataadapter);
                    Log.d("CekFirebase", "Set Spinner");

                } else {
                    Log.e("CekFirebase", "Gagal");
                }
            }

        });

        final int[] nomor = new int[1];

        locSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                for(int i=0;i<lokasifb.size(); i++){
                    if(lokasifb.get(i).contains(posisiawal) && lokasifb.get(i).contains(lokasi.get(position))){
                        String potongan = lokasifb.get(i).substring(0,posisiawal.length());
                        Log.d("Testt Sama",potongan);
                        if(posisiawal.equals(potongan)){
                            nomor[0] = i;
                        }
                    }
                }

                points.clear();

                ArrayList test = (ArrayList) datapoint.get(nomor[0]);
                for(int i=0; i<test.size(); i++){
                    Log.d("CekFirebase", String.valueOf(test.get(i)));
                    GeoPoint telkom = (GeoPoint) test.get(i);
                    Point telkomawal = Point.fromLngLat(telkom.getLongitude(),telkom.getLatitude());
                    points.add(telkomawal);
                    if(i==(test.size()-1)){
                        LatLng telkommarker = new LatLng(telkomawal.latitude(), telkomawal.longitude());
                        destinationMarker = map.addMarker(new MarkerOptions().position(telkommarker));
                    }
                }

                getRoute();

                posisiawal = lokasi.get(position);
                startButton.setEnabled(true);
                startButton.setBackgroundResource(R.color.mapbox_blue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                for(DirectionsRoute currentRoute : routes){
                for (int index = 0; index < routes.size(); index++) {
                    NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                            .directionsRoute(routes.get(index))
//                          .origin(originPosition)
//                          .destination(destinationPosition)
                            .shouldSimulateRoute(true)
                            .build();
                    NavigationLauncher.startNavigation(MainActivity.this, options);
                }
            }
        });


		/* Let us create the following weighted graph
		10
		(0)------->(3)
		|		 /|\
		5 |		 |
		|		 | 1
		\|/		 |
		(1)------->(2)
		3		 */



    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.addOnMapClickListener(this);
        EnableLocation();


    }


    private void EnableLocation() {
        if (permissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.GPS);
    }

    private void setCameraPosition(Location location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), 130.0));
    }


    //coba coba

    @Override
    public void onMapClick(@NonNull LatLng point) {
//        if (destinationMarker != null){
//            map.removeMarker(destinationMarker);
//        }
//        destinationMarker = lokasi.add(new MarkerOptions().position(new LatLng(poin;
//        destinationMarker = map.addMarker(new MarkerOptions().position(point));
//        destinationPosition = Point.fromLngLat(point.getLongitude(), point.getLatitude());
//        originPosition = Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
//        if (currentPosition != null)
//        getRoute(currentPosition, destinationPosition);
//        else
//        getRoute(originPosition, destinationPosition);
//        currentPosition = destinationPosition;
//        startButton.setEnabled(true);
//        startButton.setBackgroundResource(R.color.mapboxBlue);
    }

    List<DirectionsRoute> routes = new ArrayList<>();

    private void getRoute() {


//        Point point1 = Point.fromLngLat(107.609273,-6.921405 );
//        Point point2 = Point.fromLngLat(107.604115, -6.920795 );
//        Point point3 = Point.fromLngLat(107.603081,-6.937908 );
//        List<Point> testpoint = new ArrayList<>();
//        testpoint.add(point1);
//        testpoint.add(point2);
//        testpoint.add(point3);
//
//        Log.d("Point apa", points.toString());
        MapboxMapMatching.builder()
                .accessToken(Mapbox.getAccessToken())
                .coordinates(points)
                .steps(true)
                .voiceInstructions(true)
                .bannerInstructions(true)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .build()
                .enqueueCall(new Callback<MapMatchingResponse>() {

                    @Override
                    public void onResponse(Call<MapMatchingResponse> call, Response<MapMatchingResponse> response) {
                        if (response.isSuccessful()) {
                            if (navigationMapRoute == null) {
                                navigationMapRoute = new NavigationMapRoute(null, mapView, map);
                            }

                            DirectionsRoute route = response.body().matchings().get(0).toDirectionRoute();
                            routes.add(route);
                            navigationMapRoute.addRoutes(routes);
                        }
                    }

                    @Override
                    public void onFailure(Call<MapMatchingResponse> call, Throwable throwable) {

                    }
                });
//
//        NavigationRoute.Builder builder = NavigationRoute.builder();
//
//        builder.accessToken(Mapbox.getAccessToken())
//                .origin(origin)
//                .destination(destination)
//                .build()
//                .getRoute(new Callback<DirectionsResponse>() {
//                    @Override
//                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                        if (response.body() == null) {
//                            Log.e(TAG, "No routes found, check right user and acces token");
//                            return;
//                        } else if (response.body().routes().size() == 0) {
//                            Log.e(TAG, "No routes found");
//                            return;
//                        }
//
////                        DirectionsRoute currentRoute = response.body().routes().get(0);
//
//                        if (navigationMapRoute == null) {
//                            navigationMapRoute = new NavigationMapRoute(null, mapView, map);
//                        }
//                        routes.add(response.body().routes().get(0));
//                        navigationMapRoute.addRoutes(routes);
//                    }
//
//                    @Override
//                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
//                        Log.e(TAG, "Error:" + t.getMessage());
//                    }
//                });
    }


    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            setCameraPosition(location);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            EnableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();

        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }

}
