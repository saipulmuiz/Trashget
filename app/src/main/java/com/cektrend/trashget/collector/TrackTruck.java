package com.cektrend.trashget.collector;


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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.cektrend.trashget.R;
import com.google.common.collect.ImmutableMap;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.data.SpeedToConsumptionMap;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.InstructionsType;
import com.tomtom.online.sdk.routing.data.Report;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.routing.data.VehicleEngineType;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class TrackTruck extends FragmentActivity implements OnMapReadyCallback,TomtomMapCallback.OnMarkerClickListener {
    TomtomMap tom;
    int count2=0;
    Button bt, bt1;
    static LatLng start, stop;
    double latitude, longitude;
    //    Button bt2;
    Geocoder geocoder;
    MarkerBuilder basket;
    private SearchApi searchApi;
    private RoutingApi routePlannerAPI;
    //    Button starttrack;
    int count=0;
    static double latitude2,longitude2;
    SimpleMarkerBalloon balloon;
    private Route route;
    List<LatLng> list;
    Marker marker;
    List<Address> addresses;
    private LatLng departurePosition;
    private LatLng destinationPosition;
    private LatLng wayPointPosition;
    private Icon departureIcon;
    static String bask,user;
    private Icon destinationIcon;
    TextView textView,textView1,textView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_truck);
//bt2=findViewById(R.id.com);
        textView=findViewById(R.id.distance);
        textView1=findViewById(R.id.arrdep);
        textView2=findViewById(R.id.consumption);
//starttrack=findViewById(R.id.starttracking);

        geocoder = new Geocoder(this, Locale.getDefault());
        bt1 = findViewById(R.id.bt1);
        //bt = findViewById(R.id.bt);

        initTomTomServices();
        initUIViews();
        setupUIViewListeners();


    }

    private void displayfuelvalue() {
        RouteQuery queryBuilder = RouteQueryBuilder.create(start, stop)
                .withConsiderTraffic(false)
                .withMaxAlternatives(2)
                .withVehicleWeightInKg(1600) //vehicle weight in kilograms
                .withCurrentFuelInLiters(50.0)
                .withAuxiliaryPowerInLitersPerHour(0.2)
                .withFuelEnergyDensityInMJoulesPerLiter(34.2)
                .withAccelerationEfficiency(0.33) //e.g. KineticEnergyGained/ChemicalEnergyConsumed
                .withDecelerationEfficiency(0.33) //e.g. ChemicalEnergySaved/KineticEnergyLost
                .withUphillEfficiency(0.33) //e.g. PotentialEnergyGained/ChemicalEnergyConsumed
                .withDownhillEfficiency(0.33) //e.g. ChemicalEnergySaved/PotentialEnergyLost
                .withVehicleEngineType(VehicleEngineType.COMBUSTION)
                .withConstantSpeedConsumptionInLitersPerHundredKm(SpeedToConsumptionMap.create(ImmutableMap.<Integer, Double>builder()
                        //vehicle specific consumption model <speed, consumption in liters>
                        .put(10, 6.5)
                        .put(30, 7.0)
                        .put(50, 8.0)
                        .put(70, 8.4)
                        .put(90, 7.7)
                        .put(120, 7.5)
                        .put(150, 9.0)
                        .build())
                ).build();
        routePlannerAPI.planRoute(queryBuilder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<RouteResponse>() {
                    @Override
                    public void onSuccess(RouteResponse routeResponse) {
                        Log.e("res",routeResponse.toString());
                        displayfuel(routeResponse.getRoutes());
////displayInfoAboutRoute(routeResponse);
                        //                    tom.displayRoutesOverview();
                    }

                    @Override
                    public void onError(Throwable e) {
                        handleApiError(e);
                        clearMap();
                    }
                });

    }

    private void displayfuel(List<FullRoute> routes) {
        for(FullRoute fullRoute:routes)
        {
            if(count2==0) {
                String s = String.valueOf(fullRoute.getSummary().getFuelConsumptionInLiters());
                textView2.setText("Consumption:  "+s+" Litres");
                count2++;

                Log.e("ss", s);
            }
        }
    }

    private void initTomTomServices() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
        searchApi = OnlineSearchApi.create(this);
        routePlannerAPI = OnlineRoutingApi.create(this);
    }

    private void initUIViews() {
        departureIcon = Icon.Factory.fromResources(TrackTruck.this, R.drawable.ic_map_route_departure);
        destinationIcon = Icon.Factory.fromResources(TrackTruck.this, R.drawable.ic_map_route_destination);
    }

    private void clearMap() {
        tom.clear();
        departurePosition = null;
        destinationPosition = null;
        route = null;
    }

    private void setupUIViewListeners() {
    }

    private void handleApiError(Throwable e) {
        Toast.makeText(TrackTruck.this, getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(@NonNull final TomtomMap tomtomMap) {
        tom = tomtomMap;
        tom.clear();
        getbasket();
        tom.getUiSettings().getZoomingControlsView().show();
        //addgarbage(tomtomMap);
        tom.addOnMarkerClickListener(this);
//starttrack.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//        ChevronBuilder chevronBuilder = ChevronBuilder.create(departureIcon, destinationIcon);
//        Chevron chevron =  tomtomMap.getDrivingSettings().addChevron(chevronBuilder);
//        tomtomMap.getDrivingSettings().startTracking(chevron);
//    }
//});
        Toast.makeText(this,"Please wait for routing",Toast.LENGTH_SHORT).show();
        balloon = new SimpleMarkerBalloon("Welcome to TomTom");
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RouteQuery routeQuery = new RouteQueryBuilder(start, stop).withMaxAlternatives(1)
                        .withReport(Report.EFFECTIVE_SETTINGS)
                        .withInstructionsType(InstructionsType.TEXT)
                        .withConsiderTraffic(false).build();
                routePlannerAPI.planRoute(routeQuery)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<RouteResponse>() {
                            @Override
                            public void onSuccess(RouteResponse routeResponse) {
                                displayRoutes(routeResponse.getRoutes());
                                tomtomMap.displayRoutesOverview();
                            }

                            @Override
                            public void onError(Throwable e) {
                                handleApiError(e);

                            }
                        });

            }
        });
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {


                latitude = location.getLatitude();
                longitude = location.getLongitude();
                start = new LatLng(latitude, longitude);

                try {
                    addresses = geocoder.getFromLocation(start.getLatitude(), start.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String[] split=address.split(",");
                    user =split[1];
//basket.markerBalloon(new SimpleMarkerBalloon(address+"\n"+city));
                    if(TrackTruck.user !=null && TrackTruck.bask !=null)
                    {
                        textView1.setText(""+TrackTruck.user+" to"+TrackTruck.bask);

                    }
                    Log.e("address",address);
                    if(count==0) {
                        basket = new MarkerBuilder(start)
                                .markerBalloon(new SimpleMarkerBalloon(address + "\n" + city))
                                .tag("basket")

                                .decal(true);
                        tom.addMarker(basket);
                        distance(latitude, longitude, stop.getLatitude(), stop.getLongitude());
                        displayfuelvalue();
                        Routing(start,stop);
                        count++;
                    }

                    else
                    {
                        tom.removeMarkerByTag("basket");
                        basket = new MarkerBuilder(start)
                                .markerBalloon(new SimpleMarkerBalloon(address + "\n" + city))
                                .tag("basket")
                                .decal(true);
                        tom.addMarker(basket);
                        //   Routing(start,stop);
                        distance(latitude, longitude, stop.getLatitude(), stop.getLongitude());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
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

    void getbasket()
    {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String latitude1 = bundle.getString("lat");
            String longitude1 = bundle.getString("lang");
            //tomtomMap.clear();
            latitude2 = Double.parseDouble(latitude1);
            longitude2 = Double.parseDouble(longitude1);
            stop = new LatLng(this.latitude2, this.longitude2);
            LatLng latLng = new LatLng(stop.getLatitude(), stop.getLongitude());
            try {
                addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
//basket.markerBalloon(new SimpleMarkerBalloon(address+"\n"+b
                String[] split=address.split(",");
                bask=split[1];

                Log.e("address",address);
                basket = new MarkerBuilder(stop)
                        .markerBalloon(new SimpleMarkerBalloon(address+"\n"+city))
                        .icon(Icon.Factory.fromResources(getApplicationContext(),R.drawable.smallkutty))
                        .tag("basket")
                        .decal(true);
                tom.addMarker(basket);



            } catch (IOException e) {
                e.printStackTrace();
            }




        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.tom.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void displayRoutes(List<FullRoute> routes) {
        for (FullRoute fullRoute : routes) {
            route = tom.addRoute(new RouteBuilder(
                    fullRoute.getCoordinates()).startIcon(departureIcon).endIcon(destinationIcon).isActive(true));
        }
    }

    @Override
    public void onMarkerClick(@NonNull Marker marker) {
        Log.e("get",marker.getTag().toString());
        if (marker.getTag().toString() == "basket") {

        }

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
        Double d=new Double(dist);
        int k=d.intValue();
        textView.setText(""+k+"km");
        Log.e("distance",""+
                k+"km");
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    public void Routing(LatLng start, LatLng stop)
    {
        RouteQuery routeQuery = new RouteQueryBuilder(start, stop).withRouteType(RouteType.FASTEST).withConsiderTraffic(true).build();
        routePlannerAPI.planRoute(routeQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<RouteResponse>() {
                    @Override
                    public void onSuccess(RouteResponse routeResponse) {
                        displayRoutes(routeResponse.getRoutes());
//displayInfoAboutRoute(routeResponse);
                        tom.displayRoutesOverview();
                    }

                    @Override
                    public void onError(Throwable e) {
                        handleApiError(e);
                        clearMap();
                    }
                });
    }
//    protected void displayInfoAboutRoute(FullRoute routeResult) {
//
//        List<Instruction> instructions = Arrays.asList(routeResult.getGuidance().getInstructions());
//        Log.e("man",instructions.toString());
//    }
}