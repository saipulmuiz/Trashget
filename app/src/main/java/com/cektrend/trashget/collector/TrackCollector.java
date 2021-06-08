package com.cektrend.trashget.collector;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.location.Address;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_FINE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.POINT_DEST;
import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;

public class TrackCollector extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, RoutingListener, View.OnClickListener {
    private GoogleMap mMap = null;
    private LocationManager locationManager = null;
    private TextView tvDistance, tvTime, tvArrdep;
    private Button btnAlternateRoute;

    //Global UI Map markers
    private Marker basket = null;
    private Marker destMarker = null;
    private LatLng start = null, stop = null;
    private Polyline line = null;
    int count2 = 0;
    private LatLng departurePosition, destinationPosition;
    //     TextView tvArrdep, tvDistance, tvConsumption;
    Geocoder geocoder;
    static double latitude2, longitude2;
    Double latitude, longitude;
    List<Address> addresses;
    static String bask, user;
    private Bitmap departureIcon, destinationIcon;
    //     int count = 0;
    //     MarkerOptions basket;
    //     private RoutingApi routePlannerAPI;

    //Global flags
    private boolean firstRefresh = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_collector);
        geocoder = new Geocoder(this, Locale.getDefault());
        initComponents();
    }

    private void initComponents() {
        tvDistance = findViewById(R.id.tv_distance);
        tvArrdep = findViewById(R.id.tv_arrdep);
        tvTime = findViewById(R.id.tv_consumption);
        departureIcon = BitmapFactory.decodeResource(TrackCollector.this.getResources(), R.drawable.ic_map_route_departure);
        destinationIcon = BitmapFactory.decodeResource(TrackCollector.this.getResources(), R.drawable.ic_map_route_destination);
        btnAlternateRoute = findViewById(R.id.btn_alternate_route);
        POINT_DEST = new LatLng(-7.1252805, 108.219001);

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
        btnAlternateRoute.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        getBasket();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        Toast.makeText(this, "Please wait for routing", Toast.LENGTH_SHORT).show();
        MarkerOptions markerOptions1 = new MarkerOptions().title("Test");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askPermissionLocation();
            return;
        }
        mMap.setMyLocationEnabled(true);
        getLocation(mMap);
        mMap.setOnMarkerClickListener(this);
    }

    private void askPermissionLocation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23
            int permissonFineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int permissonCoarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissonFineLocation != PackageManager.PERMISSION_GRANTED && permissonCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_CODE_PERMISSION_FINE_LOCATION);
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE_PERMISSION_FINE_LOCATION) {// Note: If request is cancelled, the result arrays are empty.
            // Permissions granted (CALL_PHONE).
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(TrackCollector.this, "Permission granted!", Toast.LENGTH_SHORT).show();
                getLocation(mMap);
            } else {
                Toast.makeText(TrackCollector.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocation(GoogleMap googleMap) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                start = new LatLng(latitude, longitude);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 14));
                try {
                    if (addresses != null && addresses.size() > 0) {
                        addresses = geocoder.getFromLocation(start.latitude, start.longitude, 1);
                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getLocality();
                        String[] split = address.split(",");
                        user = split[1];
                        if (TrackCollector.user != null && TrackCollector.bask != null) {
                            tvArrdep.setText(new StringBuilder("" + TrackCollector.user + " to" + TrackCollector.bask));
                        }
                        Log.e("address", address);
                        if (firstRefresh) {
                            basket = mMap.addMarker(new MarkerOptions()
                                    .position(start)
                                    .title(address + "\n" + city));//.icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
                            // destMarker = mMap.addMarker(new MarkerOptions().position(POINT_DEST).title("Destination"));//.icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
                            // mMap.moveCamera(CameraUpdateFactory.newLatLng(POINT_DEST));
                            // mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            // distance(latitude, longitude, stop.latitude, stop.longitude);
                            Routing(start, stop);
                            // getRoutingPath();
                            firstRefresh = false;
                        } else {
                            basket.remove();
                            basket = mMap.addMarker(new MarkerOptions()
                                    .position(start)
                                    .title(address + "\n" + city));
                            distance(latitude, longitude, stop.latitude, stop.longitude);
                        }
                    }
                } catch (IOException e) {
                    handleApiError(e);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
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
            // Routing(start, stop);
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        handleApiError(e);
        clearMap();
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> list, int i) {
        try {
            // for (FullRoute fullRoute : routes) {
            //     route = tom.addRoute(new RouteBuilder(
            //             fullRoute.getCoordinates()).startIcon(departureIcon).endIcon(destinationIcon).isActive(true));
            // }
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
            tvDistance.setText("Distance: " + list.get(0).getDistanceText());
            tvTime.setText("Duration: " + list.get(0).getDurationText());

            //Focus on map bounds
            mMap.moveCamera(CameraUpdateFactory.newLatLng(list.get(0).getLatLgnBounds().getCenter()));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(start);
            // builder.include(POINT_DEST);
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

    void getBasket() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            double latitude1 = bundle.getDouble("lat");
            double longitude1 = bundle.getDouble("long");
            stop = new LatLng(latitude1, longitude1);
            try {
                addresses = geocoder.getFromLocation(latitude1, longitude1, 1);
                if (addresses != null && addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String[] split = address.split(",");
                    bask = split[1];
                    Log.e("address", address);
                    Log.e("TAG", "get Max Adress : " + addresses.get(0));
                    //basket.markerBalloon(new SimpleMarkerBalloon(address+"\n"+b
                    Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.smallkutty);
                    basket = mMap.addMarker(new MarkerOptions()
                            .position(stop)
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .title(address + "\n" + city));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Routing(LatLng start, LatLng stop) {
        try {
            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(start, stop)
                    .build();
            routing.execute();
        } catch (Exception e) {
            Toast.makeText(TrackCollector.this, "Unable to Route", Toast.LENGTH_SHORT).show();
        }

        // RouteQuery routeQuery = new RouteQueryBuilder(start, stop).withRouteType(RouteType.FASTEST).withConsiderTraffic(true).build();
        // routePlannerAPI.planRoute(routeQuery)
        //         .subscribeOn(Schedulers.io())
        //         .observeOn(AndroidSchedulers.mainThread())
        //         .subscribe(new DisposableSingleObserver<RouteResponse>() {
        //             @Override
        //             public void onSuccess(RouteResponse routeResponse) {
        //                 displayRoutes(routeResponse.getRoutes());
        //                 //displayInfoAboutRoute(routeResponse);
        //                 tom.displayRoutesOverview();
        //             }
        //
        //             @Override
        //             public void onError(Throwable e) {
        //                 handleApiError(e);
        //                 clearMap();
        //             }
        //         });
    }

    private void distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        Double d = new Double(dist);
        int k = d.intValue();
        tvDistance.setText("" + k + "km");
        Log.e("distance", "" +
                k + "km");
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void getRoutingPath() {
        try {
            //Do Routing
            Routing routing = new Routing.Builder()
                    .travelMode(Routing.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(start, stop)
                    .build();
            routing.execute();
        } catch (Exception e) {
            Toast.makeText(TrackCollector.this, "Unable to Route", Toast.LENGTH_SHORT).show();
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

    private void handleApiError(Throwable e) {
        Toast.makeText(TrackCollector.this, getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
    }

    private void clearMap() {
        mMap.clear();
        departurePosition = null;
        destinationPosition = null;
        // route = null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}