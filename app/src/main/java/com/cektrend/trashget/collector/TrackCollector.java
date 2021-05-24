package com.cektrend.trashget.collector;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cektrend.trashget.R;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.cektrend.trashget.utils.ConstantUtil.POINT_DEST;
import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;

public class TrackCollector extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, RoutingListener, View.OnClickListener {
    private GoogleMap mMap = null;
    private LocationManager locationManager = null;
    private TextView txtDistance, txtTime;

    //Global UI Map markers
    private Marker currentMarker = null;
    private Marker destMarker = null;
    private LatLng currentLatLng = null;
    private Polyline line = null;
    private Button btnAlternateRoute;
    //     TextView tvArrdep, tvDistance, tvConsumption;
    //     Geocoder geocoder;
    //     static LatLng start, stop;
    //     Double latitude, longitude;
    //     List<Address> addresses;
    //     static String bask, user;
    //     int count = 0;
    //     MarkerOptions basket;
    //     private RoutingApi routePlannerAPI;

    //Global flags
    private boolean firstRefresh = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_collector);
        initComponents();
    }

    private void initComponents() {
        txtDistance = findViewById(R.id.distance);
        // tvArrdep = findViewById(R.id.arrdep);
        txtTime = findViewById(R.id.consumption);
        POINT_DEST = new LatLng(-7.1252805, 108.219001);

        btnAlternateRoute = findViewById(R.id.btn_alternate_route);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_collector_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).
                    getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(180, 500, 0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firstRefresh = true;
        //Ensure the GPS is ON and location permission enabled for the application.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // if (!PermissionCheck.getInstance().checkGPSPermission(this, locationManager)) {
        //     //GPS not enabled for the application.
        // } else if (!PermissionCheck.getInstance().checkLocationPermission(this)) {
        //     //Location permission not given.
        // } else {
        //     Toast.makeText(TrackCollector.this, "Fetching Location", Toast.LENGTH_SHORT).show();
        //     try {
        //         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
        //     } catch(Exception e)
        //     {
        //         Toast.makeText(TrackCollector.this, "ERROR: Cannot start location listener", Toast.LENGTH_SHORT).show();
        //     }
    }

    @Override
    protected void onPause() {
        if (locationManager != null) {
            //Check needed in case of  API level 23.

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            try {
                locationManager.removeUpdates(this);
            } catch (Exception e) {
            }
        }
        locationManager = null;
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setOnMarkerClickListener(this);

        // getbasket();
        Toast.makeText(this, "Please wait for routing", Toast.LENGTH_SHORT).show();
        MarkerOptions markerOptions1 = new MarkerOptions().title("Test");
        btnAlternateRoute.setOnClickListener(this);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                currentLatLng = new LatLng(lat, lng);
                if (firstRefresh) {
                    //Add Start Marker.
                    currentMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Position"));//.icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
                    firstRefresh = false;
                    destMarker = mMap.addMarker(new MarkerOptions().position(POINT_DEST).title("Destination"));//.icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(POINT_DEST));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    getRoutingPath();
                } else {
                    currentMarker.setPosition(currentLatLng);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //LatLng latLng=new LatLng(13.0827,80.2707);]

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                2000,
                10, locationListener);

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.e("get", marker.getTag().toString());
        if (marker.getTitle().contains("Destination")) {
            //Do some task on dest pin click
        } else if (marker.getTitle().contains("Current")) {
            //Do some task on current pin click
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_alternate_route) {
            getRoutingPath();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(TrackCollector.this, "Routing Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> list, int i) {
        try {
            //Get all points and plot the polyLine route.
            List<LatLng> listPoints = list.get(0).getPoints();
            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            Iterator<LatLng> iterator = listPoints.iterator();
            while (iterator.hasNext()) {
                LatLng data = iterator.next();
                options.add(data);
            }

            //If line not null then remove old polyline routing.
            if (line != null) {
                line.remove();
            }
            line = mMap.addPolyline(options);

            //Show distance and duration.
            txtDistance.setText("Distance: " + list.get(0).getDistanceText());
            txtTime.setText("Duration: " + list.get(0).getDurationText());

            //Focus on map bounds
            mMap.moveCamera(CameraUpdateFactory.newLatLng(list.get(0).getLatLgnBounds().getCenter()));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(currentLatLng);
            builder.include(POINT_DEST);
            LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        } catch (Exception e) {
            Toast.makeText(TrackCollector.this, "EXCEPTION: Cannot parse routing response", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {
        Toast.makeText(TrackCollector.this, "Routing Cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void getRoutingPath() {
        try {
            //Do Routing
            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(currentLatLng, POINT_DEST)
                    .build();
            routing.execute();
        } catch (Exception e) {
            Toast.makeText(TrackCollector.this, "Unable to Route", Toast.LENGTH_SHORT).show();
        }
    }
}