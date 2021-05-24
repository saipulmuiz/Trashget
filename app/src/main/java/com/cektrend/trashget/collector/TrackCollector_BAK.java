package com.cektrend.trashget.collector;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import com.cektrend.trashget.admin.TrashInfoBottomSheetFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.ImmutableMap;
import com.tomtom.online.sdk.data.SpeedToConsumptionMap;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.VehicleEngineType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;

// public class TrackCollector extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener {
//     private GoogleMap mMap;
//     Button btnAlternateRoute;
//     TextView tvArrdep, tvDistance, tvConsumption;
//     Geocoder geocoder;
//     static LatLng start, stop;
//     Double latitude, longitude;
//     List<Address> addresses;
//     static String bask, user;
//     int count = 0;
//     MarkerOptions basket;
//     private RoutingApi routePlannerAPI;
//
//     @Override
//     protected void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         setContentView(R.layout.activity_track_collector);
//         initComponents();
//     }
//
//     private void initComponents() {
//         tvDistance = findViewById(R.id.distance);
//         tvArrdep = findViewById(R.id.arrdep);
//         tvConsumption = findViewById(R.id.consumption);
//         //starttrack=findViewById(R.id.starttracking);
//
//         geocoder = new Geocoder(this, Locale.getDefault());
//         btnAlternateRoute = findViewById(R.id.bt1);
//         SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                 .findFragmentById(R.id.track_collector_map);
//         if (mapFragment != null) {
//             mapFragment.getMapAsync(this);
//             View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).
//                     getParent()).findViewById(Integer.parseInt("2"));
//             RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
//             rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
//             rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//             rlp.setMargins(180, 500, 0, 0);
//         }
//     }
//
//     @Override
//     public void onMapReady(GoogleMap googleMap) {
//         mMap = googleMap;
//
//         googleMap.clear();
//         // getbasket();
//         // googleMap.getUiSettings().getZoomingControlsView().show();
//         //addgarbage(tomtomMap);
//         googleMap.setOnMarkerClickListener(TrackCollector.this);
//         Toast.makeText(this, "Please wait for routing", Toast.LENGTH_SHORT).show();
//         MarkerOptions markerOptions1 = new MarkerOptions().title("Test");
//         btnAlternateRoute.setOnClickListener(this);
//         LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//         LocationListener locationListener = new LocationListener() {
//             @Override
//             public void onLocationChanged(Location location) {
//
//
//                 latitude = location.getLatitude();
//                 longitude = location.getLongitude();
//                 start = new LatLng(latitude, longitude);
//
//                 try {
//                     addresses = geocoder.getFromLocation(start.latitude, start.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                     String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                     String city = addresses.get(0).getLocality();
//                     String[] split = address.split(",");
//                     user = split[1];
//                     //basket.markerBalloon(new SimpleMarkerBalloon(address+"\n"+city));
//                     if (TrackTruck.user != null && TrackTruck.bask != null) {
//                         tvArrdep.setText("" + TrackTruck.user + " to" + TrackTruck.bask);
//                     }
//                     Log.e("address", address);
//                     if (count == 0) {
//                         basket = new MarkerOptions()
//                                 .position(start)
//                                 .title("basket");
//                         googleMap.addMarker(basket);
//                         // basket = new MarkerBuilder(start)
//                         //         .markerBalloon(new SimpleMarkerBalloon(address + "\n" + city))
//                         //         .tag("basket")
//                         //
//                         //         .decal(true);
//                         // tom.addMarker(basket);
//                         distance(latitude, longitude, stop.latitude, stop.longitude);
//                         displayfuelvalue();
//                         Routing(start, stop);
//                         count++;
//                     } else {
//                         mMap.removeMarkerByTag("basket");
//                         basket = new MarkerBuilder(start)
//                                 .markerBalloon(new SimpleMarkerBalloon(address + "\n" + city))
//                                 .tag("basket")
//                                 .decal(true);
//                         tom.addMarker(basket);
//                         //   Routing(start,stop);
//                         distance(latitude, longitude, stop.getLatitude(), stop.getLongitude());
//                     }
//
//                 } catch (IOException e) {
//                     e.printStackTrace();
//                 }
//             }
//
//             @Override
//             public void onStatusChanged(String provider, int status, Bundle extras) {
//
//             }
//
//             @Override
//             public void onProviderEnabled(String provider) {
//
//             }
//
//             @Override
//             public void onProviderDisabled(String provider) {
//
//             }
//         };
//
//         if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//             return;
//         }
//
//         //LatLng latLng=new LatLng(13.0827,80.2707);]
//
//         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                 2000,
//                 10, locationListener);
//
//     }
//
//     @Override
//     public boolean onMarkerClick(@NonNull Marker marker) {
//         Log.e("get", marker.getTag().toString());
//         if (marker.getTag().toString() == "basket") {
//
//         }
//         return false;
//     }
//
//     private void distance(double lat1, double lon1, double lat2, double lon2) {
//         double theta = lon1 - lon2;
//         double dist = Math.sin(deg2rad(lat1))
//                 * Math.sin(deg2rad(lat2))
//                 + Math.cos(deg2rad(lat1))
//                 * Math.cos(deg2rad(lat2))
//                 * Math.cos(deg2rad(theta));
//         dist = Math.acos(dist);
//         dist = rad2deg(dist);
//         dist = dist * 60 * 1.1515;
//         Double d = new Double(dist);
//         int k = d.intValue();
//         tvDistance.setText("" + k + "km");
//         Log.e("distance", "" +
//                 k + "km");
//     }
//
//     private double deg2rad(double deg) {
//         return (deg * Math.PI / 180.0);
//     }
//
//     private double rad2deg(double rad) {
//         return (rad * 180.0 / Math.PI);
//     }
//
//     private void displayfuelvalue() {
//         RouteQuery queryBuilder = RouteQueryBuilder.create(start, stop)
//                 .withConsiderTraffic(false)
//                 .withMaxAlternatives(2)
//                 .withVehicleWeightInKg(1600) //vehicle weight in kilograms
//                 .withCurrentFuelInLiters(50.0)
//                 .withAuxiliaryPowerInLitersPerHour(0.2)
//                 .withFuelEnergyDensityInMJoulesPerLiter(34.2)
//                 .withAccelerationEfficiency(0.33) //e.g. KineticEnergyGained/ChemicalEnergyConsumed
//                 .withDecelerationEfficiency(0.33) //e.g. ChemicalEnergySaved/KineticEnergyLost
//                 .withUphillEfficiency(0.33) //e.g. PotentialEnergyGained/ChemicalEnergyConsumed
//                 .withDownhillEfficiency(0.33) //e.g. ChemicalEnergySaved/PotentialEnergyLost
//                 .withVehicleEngineType(VehicleEngineType.COMBUSTION)
//                 .withConstantSpeedConsumptionInLitersPerHundredKm(SpeedToConsumptionMap.create(ImmutableMap.<Integer, Double>builder()
//                         //vehicle specific consumption model <speed, consumption in liters>
//                         .put(10, 6.5)
//                         .put(30, 7.0)
//                         .put(50, 8.0)
//                         .put(70, 8.4)
//                         .put(90, 7.7)
//                         .put(120, 7.5)
//                         .put(150, 9.0)
//                         .build())
//                 ).build();
//         routePlannerAPI.planRoute(queryBuilder)
//                 .subscribeOn(Schedulers.io())
//                 .observeOn(AndroidSchedulers.mainThread())
//                 .subscribe(new DisposableSingleObserver<RouteResponse>() {
//                     @Override
//                     public void onSuccess(RouteResponse routeResponse) {
//                         Log.e("res", routeResponse.toString());
//                         displayfuel(routeResponse.getRoutes());
//                         ////displayInfoAboutRoute(routeResponse);
//                         //                    tom.displayRoutesOverview();
//                     }
//
//                     @Override
//                     public void onError(Throwable e) {
//                         handleApiError(e);
//                         clearMap();
//                     }
//                 });
//
//     }
//
//     @Override
//     public void onClick(View v) {
//
//         RouteQuery routeQuery = new RouteQueryBuilder(start, stop).withMaxAlternatives(1)
//                 .withReport(Report.EFFECTIVE_SETTINGS)
//                 .withInstructionsType(InstructionsType.TEXT)
//                 .withConsiderTraffic(false).build();
//         routePlannerAPI.planRoute(routeQuery)
//                 .subscribeOn(Schedulers.io())
//                 .observeOn(AndroidSchedulers.mainThread())
//                 .subscribe(new DisposableSingleObserver<RouteResponse>() {
//                     @Override
//                     public void onSuccess(RouteResponse routeResponse) {
//                         displayRoutes(routeResponse.getRoutes());
//                         tomtomMap.displayRoutesOverview();
//                     }
//
//                     @Override
//                     public void onError(Throwable e) {
//                         handleApiError(e);
//
//                     }
//                 });
//
//     }
// }