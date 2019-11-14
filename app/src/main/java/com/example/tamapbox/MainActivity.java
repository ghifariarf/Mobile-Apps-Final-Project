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

import static java.lang.String.format;


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
    private List<String> listJalur = new ArrayList<>();
    private List<Integer> bobots = new ArrayList<>();
    private List<Point> points = new ArrayList<>();
    private List<Object> datapoint = new ArrayList<>();
    private List<Jalur> jalurs = new ArrayList<>();
    private List<City> cityAwal = new ArrayList<>();
    private List<City> cityAkhir = new ArrayList<>();
    private ArrayAdapter<String> dataadapter;
    private FirebaseFirestore db;
    private RequestQueue mQueue;
    int indexTerpilih;

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
        mQueue = Volley.newRequestQueue(this);
        locSpinner.setEnabled(false); // buat tidak bisa diubah
//        tes
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
                    GeoPoint telkom = null;

                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        String namaJalur = document.getString("jalur");

                        if (!namaJalur.equals(lokasi.get(0))) {
                            String[] parts = namaJalur.split("-");
                            String namaLokasiAwal = parts[0]; // 004
                            String namaLokasiAkhir = parts[1]; // 034556

                            ArrayList kumpulanCoordinatVirtual = (ArrayList) document.get("coordinat"); // mengambil coordinat dari index yang terpilih sebelumnya

                            GeoPoint coordinatPertama = (GeoPoint) kumpulanCoordinatVirtual.get(0); // Ambil coordinat index 0, karna itu coordinat lokasi awal
                            Point coorAwalPoint = Point.fromLngLat(coordinatPertama.getLongitude(), coordinatPertama.getLatitude());
                            GeoPoint coordinatTerakhir = (GeoPoint) kumpulanCoordinatVirtual.get(kumpulanCoordinatVirtual.size() - 1); // Ambil coordinat index 0, karna itu coordinat lokasi awal
                            Point coorTerakhirPoint = Point.fromLngLat(coordinatTerakhir.getLongitude(), coordinatTerakhir.getLatitude());

                            City lokasiAwal = new City(coorAwalPoint, namaLokasiAwal); // Buat City pertama
                            City lokasiAkhir = new City(coorTerakhirPoint, namaLokasiAkhir); // Buat City pertama
                            cityAwal.add(lokasiAwal);
                            cityAkhir.add(lokasiAkhir);
                            listJalur.add(namaJalur);
                            datapoint.add(document.get("coordinat"));
                            bobots.add(document.getLong("jarak").intValue());


                        }else{
                            telkom = (GeoPoint) document.get("coordinat");
                        }
                    }

                    Log.d("JalurTes", "cityAwal : " + cityAwal.size());
                    Log.d("JalurTes", "cityAkhir : " + cityAkhir.size());
                    Log.d("JalurTes", "listJalur : " + listJalur.size());
                    Log.d("JalurTes", "datapoint : " + datapoint.size());
                    Log.d("JalurTes", "bobots : " + bobots.size());

                    Point telkomawal = Point.fromLngLat(telkom.getLongitude(), telkom.getLatitude());
                    points.add(telkomawal);
                    LatLng telkommarker = new LatLng(points.get(0).latitude(), points.get(0).longitude());
                    destinationMarker = map.addMarker(new MarkerOptions().position(telkommarker)); // Buat marker di telkom
                    locSpinner.setEnabled(true); // aktifkan kembali
                } else {
                    Log.e("CekFirebase", "Gagal");
                }
            }
        });

        dataadapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, lokasi);
        dataadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locSpinner.setAdapter(dataadapter);
        Log.d("CekFirebase", "Set Spinner");

        locSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (datapoint.size() > 0) {
                    Log.d("YangDipilihSpinner", String.valueOf(position));
                    int indexBobotTerkecil = -1;
                    String namaLokasiAwal = "";
                    for (int currentIndex = 0; currentIndex < listJalur.size(); currentIndex++) {
                        // Pastikan bahwa nama dari lokasi awal dan akhir
                        if (listJalur.get(currentIndex).contains(posisiawal) && listJalur.get(currentIndex).contains(lokasi.get(position))) {
                            namaLokasiAwal = listJalur.get(currentIndex).substring(0, posisiawal.length());
                            Log.d("Testt Sama", namaLokasiAwal);
                            if (posisiawal.equals(namaLokasiAwal)) {
                                if (indexBobotTerkecil == -1) {
                                    indexBobotTerkecil = currentIndex; // Ambil index pertama, karna belum cek jalur lain
                                } else if (bobots.get(currentIndex) < bobots.get(indexBobotTerkecil)) { // Jika index yang saat ini bobotnya lebih kecil, maka..
                                    indexBobotTerkecil = currentIndex; // Jadikan index saat ini menjadi index dengan bobot terkecil
                                }
                            }
                        }
                    }

                    ArrayList kumpulanCoordinatVirtual = (ArrayList) datapoint.get(indexBobotTerkecil); // mengambil coordinat dari index yang terpilih sebelumnya

                    indexTerpilih = indexBobotTerkecil; // hiraukan

//                    City lokasiAwal = new City(coorAwalPoint, namaLokasiAwal); // Buat City pertama
//                    City lokasiAkhir = new City(coorTerakhirPoint, lokasi.get(position)); // Buat City pertama
//
//                    Jalur jalurTerpilih = new Jalur(lokasiAwal, lokasiAkhir, bobots.get(indexTerpilih));
//                    jalurs.add(jalurTerpilih);
                    points.clear();// mengkosongkan titik jalur yang terakhir untuk dipakai jalur yang selanjutnya

                    for (int index = 0; index < kumpulanCoordinatVirtual.size(); index++) {
                        Log.d("CekFirebase", String.valueOf(kumpulanCoordinatVirtual.get(index)));

                        GeoPoint coordinateVirtual = (GeoPoint) kumpulanCoordinatVirtual.get(index); // Ambil geopoint pada suatu index
                        Point coorVirtualPoint = Point.fromLngLat(coordinateVirtual.getLongitude(), coordinateVirtual.getLatitude()); // Convert Geopoint ke Point
                        points.add(coorVirtualPoint); //coordinat yang telah di convert jadi point, dimasukkan ke dalam list point

                        // Jika coordinat saat ini adalah coordinat yang terakhir
                        if (index == (kumpulanCoordinatVirtual.size() - 1)) {
                            LatLng telkommarker = new LatLng(coorVirtualPoint.latitude(), coorVirtualPoint.longitude()); // Convert Point jadi LatLng
                            destinationMarker = map.addMarker(new MarkerOptions().position(telkommarker)); // Tampilkan marker dari titik terakhir
                        }
                    }

                    getRoute();

                    posisiawal = lokasi.get(position);
                    startButton.setEnabled(true);
                    startButton.setBackgroundResource(R.color.mapbox_blue);
                }
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
        tesHasil();
    }

    private void tesHasil() {
        City city = new City(60, 200);
        TourManager.addCity(city);
        City city2 = new City(180, 200);
        TourManager.addCity(city2);
        City city3 = new City(80, 180);
        TourManager.addCity(city3);
        City city4 = new City(140, 180);
        TourManager.addCity(city4);
        City city5 = new City(20, 160);
        TourManager.addCity(city5);
        City city6 = new City(100, 160);
        TourManager.addCity(city6);
        City city7 = new City(200, 160);
        TourManager.addCity(city7);
        City city8 = new City(140, 140);
        TourManager.addCity(city8);
        City city9 = new City(40, 120);
        TourManager.addCity(city9);
        City city10 = new City(100, 120);
        TourManager.addCity(city10);
        City city11 = new City(180, 100);
        TourManager.addCity(city11);
        City city12 = new City(60, 80);
        TourManager.addCity(city12);
        City city13 = new City(120, 80);
        TourManager.addCity(city13);
        City city14 = new City(180, 60);
        TourManager.addCity(city14);
        City city15 = new City(20, 40);
        TourManager.addCity(city15);
        City city16 = new City(100, 40);
        TourManager.addCity(city16);
        City city17 = new City(200, 40);
        TourManager.addCity(city17);
        City city18 = new City(20, 20);
        TourManager.addCity(city18);
        City city19 = new City(60, 20);
        TourManager.addCity(city19);
        City city20 = new City(160, 20);
        TourManager.addCity(city20);

        // Initialize population
        Population pop = new Population(50, true);
        System.out.println("Initial distance: " + pop.getFittest().getDistance());

        // Evolve population for 100 generations
        pop = GA.evolvePopulation(pop);
        for (int i = 0; i < 90; i++) {
            pop = GA.evolvePopulation(pop);
        }

        // Print final results
        System.out.println("Finished");
        System.out.println("Final distance: " + pop.getFittest().getDistance());
        System.out.println("Solution:");
        System.out.println(pop.getFittest());
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

    }

    List<DirectionsRoute> routes = new ArrayList<>();

    private void getRoute() {
//        int i = 0;
//        ArrayList<Point> copyPoints = new ArrayList() {};
//        ArrayList<Integer> removed = new ArrayList<>();
//        for (Point a : points) {
//            copyPoints.add(a);
//        }
//        for (Point a : copyPoints) {
//            Log.d("PrintPoint", a.latitude() + " " + a.longitude());
//            if (i != 0 && i != 5 && i != 8) {
//                Log.d("PrintPoint", i + " removed");
//                removed.add(i);
//            }
//            i++;
//        }
//        for (int index : removed){
//            points.remove(points.indexOf(copyPoints.get(index)));
//        }

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

