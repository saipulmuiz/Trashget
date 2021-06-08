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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.cektrend.trashget.R;
import com.cektrend.trashget.data.DataTrackingTrash;
import com.cektrend.trashget.utils.RouteUtils;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.util.DateFormatter;
import com.tomtom.online.sdk.data.SpeedToConsumptionMap;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.RouteSettings;
import com.tomtom.online.sdk.map.RouteStyle;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.RoutingException;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResponse;
import com.tomtom.online.sdk.routing.data.Summary;
import com.tomtom.online.sdk.routing.data.VehicleEngineType;
import com.tomtom.online.sdk.routing.route.CombustionVehicleDescriptor;
import com.tomtom.online.sdk.routing.route.RouteCalculationDescriptor;
import com.tomtom.online.sdk.routing.route.RouteCallback;
import com.tomtom.online.sdk.routing.route.RouteDescriptor;
import com.tomtom.online.sdk.routing.route.RoutePlan;
import com.tomtom.online.sdk.routing.route.RouteSpecification;
import com.tomtom.online.sdk.routing.route.calculation.InstructionsType;
import com.tomtom.online.sdk.routing.route.diagnostic.ReportType;
import com.tomtom.online.sdk.routing.route.information.FullRoute;
import com.tomtom.online.sdk.routing.route.vehicle.CombustionVehicleConsumption;
import com.tomtom.online.sdk.routing.route.vehicle.VehicleDimensions;
import com.tomtom.online.sdk.routing.route.vehicle.VehicleEfficiency;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class TrackTrash extends FragmentActivity implements OnMapReadyCallback, TomtomMapCallback.OnMarkerClickListener, View.OnClickListener {
    TomtomMap tom;
    int count2 = 0;
    private TextView tvDistance, tvTime, tvArrdep, tvConsumption;
    private Button btnAlternateRoute;
    static LatLng start, stop;
    double latitude, longitude;
    protected Map<Long, com.tomtom.online.sdk.routing.route.information.FullRoute> routesMap = new LinkedHashMap<>();
    //    Button bt2;
    MarkerBuilder basket;
    private SearchApi searchApi;
    private RoutingApi routePlannerAPI;
    //    Button starttrack;
    int count = 0;
    static double latitude2, longitude2;
    SimpleMarkerBalloon balloon;
    private Route route;
    List<LatLng> list;
    Marker marker;
    Geocoder geocoder;
    List<Address> addresses;
    private LatLng departurePosition, destinationPosition;
    private Icon departureIcon;
    static String bask, user;
    private Icon destinationIcon;
    ArrayList<DataTrackingTrash> listTrackingTrash = new ArrayList<>();
    DatabaseReference dbTrash;
    private Boolean isClear = false;
    private final List<LatLng> wayPoints = new ArrayList<>();
    private static final double CURRENT_FUEL_IN_LITERS = 50.0;
    private static final double AUXILIARY_POWER_IN_LITERS_PER_HOUR = 0.2;
    private static final double FUEL_ENERGY_DENSITY_IN_MJOULES_PER_LITER = 34.2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_truck);
        geocoder = new Geocoder(this, Locale.getDefault());
        tvDistance = findViewById(R.id.tv_distance);
        tvArrdep = findViewById(R.id.tv_arrdep);
        tvConsumption = findViewById(R.id.tv_consumption);
        btnAlternateRoute = findViewById(R.id.btn_alternate_route);
        dbTrash = FirebaseDatabase.getInstance().getReference();
        initTomTomServices();
        initUIViews();
        setupUIViewListeners();
        getTrackingTrash();
    }

    private void initTomTomServices() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.track_truck_map);
        if (mapFragment != null) {
            mapFragment.getAsyncMap(this);
        }
        searchApi = OnlineSearchApi.create(this);
        routePlannerAPI = OnlineRoutingApi.create(this);
    }

    private void initUIViews() {
        departureIcon = Icon.Factory.fromResources(TrackTrash.this, R.drawable.ic_map_route_departure);
        destinationIcon = Icon.Factory.fromResources(TrackTrash.this, R.drawable.ic_map_route_destination);
    }

    private void clearMap() {
        tom.clear();
        tom.removeOnRouteClickListener(onRouteClickListener);
        departurePosition = null;
        destinationPosition = null;
        route = null;
    }

    private void setupUIViewListeners() {
    }

    private void handleApiError(Throwable e) {
        Toast.makeText(TrackTrash.this, getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(@NonNull final TomtomMap tomtomMap) {
        tom = tomtomMap;
        tom.clear();
        getBasket();
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
        Toast.makeText(this, "Please wait for routing", Toast.LENGTH_LONG).show();
        balloon = new SimpleMarkerBalloon("Welcome to TomTom");
        btnAlternateRoute.setOnClickListener(this);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                start = new LatLng(latitude, longitude);
                try {
                    addresses = geocoder.getFromLocation(start.getLatitude(), start.getLongitude(), 1);
                    String address = addresses.get(0).getAddressLine(0);
                    // String city = addresses.get(0).getLocality();
                    String[] split = address.split(",");
                    user = split[1];
                    //basket.markerBalloon(new SimpleMarkerBalloon(address+"\n"+city));
                    if (TrackTrash.user != null && TrackTrash.bask != null) {
                        tvArrdep.setText(new StringBuilder("" + TrackTrash.user + " to " + TrackTrash.bask));
                    }
                    // Log.e("address", address);
                    // if (count == 0) {
                    // basket = new MarkerBuilder(start)
                    //         .markerBalloon(new SimpleMarkerBalloon(address + "\n" + city))
                    //         .tag("basket")
                    //         .decal(true);
                    // tom.addMarker(basket);
                    // tvDistance.setText(new StringBuilder("" + distance(latitude, longitude, stop.getLatitude(), stop.getLongitude()) + " km"));
                    // displayFuel(route);
                    // Routing(start, stop);
                    displayRoute(getRouteSpecification());
                    // count++;
                    // }
                    // else {
                    //     tom.removeMarkerByTag("basket");
                    //     basket = new MarkerBuilder(start)
                    //             .markerBalloon(new SimpleMarkerBalloon(address + "\n" + city))
                    //             .tag("basket")
                    //             .decal(true);
                    //     tom.addMarker(basket);
                    //     //   Routing(start,stop);
                    //     tvDistance.setText("" + distance(latitude, longitude, stop.getLatitude(), stop.getLongitude()) + " km");
                    // }
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                2000,
                10, locationListener);
    }

    void getBasket() {
        if (listTrackingTrash != null) {
            for (int i = 0; i < listTrackingTrash.size(); i++) {
                double latitude1 = listTrackingTrash.get(i).getLatitude();
                double longitude1 = listTrackingTrash.get(i).getLongitude();
                addWaypoint(new LatLng(latitude1, longitude1));
                //tomtomMap.clear();
                stop = new LatLng(latitude1, longitude1);
                try {
                    addresses = geocoder.getFromLocation(latitude1, longitude1, 1);
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String[] split = address.split(",");
                    bask = split[1];
                    // Timber.e(address);
                    basket = new MarkerBuilder(stop)
                            .markerBalloon(new SimpleMarkerBalloon(address + "\n" + city))
                            .icon(Icon.Factory.fromResources(getApplicationContext(), R.drawable.smallkutty))
                            .tag("basket")
                            .decal(true);
                    tom.addMarker(basket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayRoute(RouteSpecification routeSpecification) {
        tom.clearRoute();
        showRoute(routeSpecification);
    }


    // protected void displayRoutes(RoutePlan routePlan) {
    //     displayRoutes(routePlan, RouteStyle.DEFAULT_ROUTE_STYLE, departureIcon, destinationIcon);
    // }

    protected void displayRoutes(RoutePlan routePlan) {
        for (LatLng waypoint : wayPoints) {
            try {
                addresses = geocoder.getFromLocation(waypoint.getLatitude(), waypoint.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String[] split = address.split(",");
            bask = split[1];
            basket = new MarkerBuilder(stop)
                    .markerBalloon(new SimpleMarkerBalloon(address + "\n" + city))
                    .icon(Icon.Factory.fromResources(getApplicationContext(), R.drawable.smallkutty))
                    .tag("basket")
                    .decal(true);
            tom.addMarker(basket);
        }
        displayRoutex(routePlan);
    }

    private void displayRoutex(RoutePlan routePlan) {
        displayRoutez(routePlan, departureIcon, destinationIcon);
    }

    protected void displayRoutez(RoutePlan routePlan, Icon startIcon, Icon endIcon) {
        routesMap.clear();
        displayFullRoutes(routePlan, startIcon, endIcon);
        tom.displayRoutesOverview();
    }

    // protected void displayFullRoutes(RoutePlan routePlan) {
    //     displayFullRoutes(routePlan, RouteStyle.DEFAULT_ROUTE_STYLE, departureIcon, destinationIcon);
    // }

    protected void displayFullRoutes(RoutePlan routePlan, Icon startIcon, Icon endIcon) {
        List<com.tomtom.online.sdk.routing.route.information.FullRoute> routes = routePlan.getRoutes();
        for (com.tomtom.online.sdk.routing.route.information.FullRoute route : routes) {
            addMapRoute(startIcon, endIcon, route);
        }
        processAddedRoutes(routes);
    }

    protected void addMapRoute(Icon startIcon, Icon endIcon, com.tomtom.online.sdk.routing.route.information.FullRoute route) {
        //tag::doc_display_route[]
        RouteBuilder routeBuilder = new RouteBuilder(route.getCoordinates())
                .endIcon(endIcon)
                .startIcon(startIcon)
                .style(RouteStyle.DEFAULT_ROUTE_STYLE);
        final Route mapRoute = tom.addRoute(routeBuilder);
        //end::doc_display_route[]

        routesMap.put(mapRoute.getId(), route);
    }

    protected void processAddedRoutes(List<com.tomtom.online.sdk.routing.route.information.FullRoute> routes) {
        selectFirstRouteAsActive();
        if (!routes.isEmpty()) {
            displayInfoAboutRoute(routes.get(0));
        }
    }

    protected void selectFirstRouteAsActive() {
        if (!tom.getRouteSettings().getRoutes().isEmpty()) {
            RouteSettings routeSettings = tom.getRouteSettings();
            long routeId = tom.getRouteSettings().getRoutes().get(0).getId();

            RouteUtils.setRoutesInactive(routeSettings);
            RouteUtils.setRouteActive(routeId, routeSettings);
        }
    }

    protected void displayInfoAboutRoute(com.tomtom.online.sdk.routing.route.information.FullRoute routeResult) {
        Toast.makeText(this, "Jalur selesai dibuat!", Toast.LENGTH_SHORT).show();
        // viewModel.hideRoutingInProgressDialog();
        // displayFuel(routeResult);
        tvConsumption.setText(new StringBuilder("+/- Konsumsi : " + routeResult.getSummary().getFuelConsumptionInLiters() + " Liter"));
        tvDistance.setText(new StringBuilder("" + routeResult.getSummary().getLengthInMeters() / 1000 + " km"));
        tom.addOnRouteClickListener(onRouteClickListener);
    }

    private TomtomMapCallback.OnRouteClickListener onRouteClickListener = route -> {
        long routeId = route.getId();
        RouteSettings routeSettings = tom.getRouteSettings();

        RouteUtils.setRoutesInactive(routeSettings);
        RouteUtils.setRouteActive(routeId, routeSettings);

        FullRoute fullRoute = routesMap.get(routeId);
        displayInfoAboutRoute(fullRoute);
    };

    // private void displayRoutes(RouteSpecification routeSpecification) {
    //     for (FullRoute fullRoute : routes) {
    //         route = tom.addRoute(new RouteBuilder(
    //                 fullRoute.getCoordinates()).startIcon(departureIcon).endIcon(destinationIcon).isActive(true));
    //     }
    // }

    @Override
    public void onMarkerClick(@NonNull Marker marker) {
        // Log.e("get", marker.getTag().toString());
        // if (marker.getTag().toString().equals("basket")) {
        //
        // }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_alternate_route) {
            displayRoute(getRouteSpecification());
            // RouteQuery routeQuery = new RouteQueryBuilder(start, stop).withMaxAlternatives(1)
            //         .withReport(Report.EFFECTIVE_SETTINGS)
            //         .withInstructionsType(InstructionsType.TEXT)
            //         .withConsiderTraffic(false).build();
            // routePlannerAPI.planRoute(routeQuery)
            //         .subscribeOn(Schedulers.io())
            //         .observeOn(AndroidSchedulers.mainThread())
            //         .subscribe(new DisposableSingleObserver<RouteResponse>() {
            //             @Override
            //             public void onSuccess(RouteResponse routeResponse) {
            //                 displayRoutes(routeResponse.getRoutes());
            //                 tom.displayRoutesOverview();
            //             }
            //
            //             @Override
            //             public void onError(Throwable e) {
            //                 handleApiError(e);
            //
            //             }
            //         });

        }
    }

    private void getTrackingTrash() {
        dbTrash.child("trackings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                if (isClear) {
                    listTrackingTrash.clear();
                }
                isClear = true;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataTrackingTrash trash = snapshot.getValue(DataTrackingTrash.class);
                    if (trash != null) {
                        trash.setId(snapshot.getKey());
                    }
                    listTrackingTrash.add(trash);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Timber.e(databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    @VisibleForTesting
    protected RouteSpecification getRouteSpecification() {
        //tag::doc_route_waypoints[]
        RouteDescriptor routeDescriptor = new RouteDescriptor.Builder()
                .considerTraffic(false)
                .build();

        RouteCalculationDescriptor routeCalculationDescriptor = new RouteCalculationDescriptor.Builder()
                .routeDescription(routeDescriptor)
                .maxAlternatives(2)
                .waypoints(wayPoints)
                .reportType(ReportType.EFFECTIVE_SETTINGS)
                .instructionType(InstructionsType.TEXT)
                .build();

        Map<Double, Double> speedToConsumptionMap = new HashMap<>();
        speedToConsumptionMap.put(10.0, 6.5);
        speedToConsumptionMap.put(30.0, 7.0);
        speedToConsumptionMap.put(50.0, 8.0);
        speedToConsumptionMap.put(70.0, 8.4);
        speedToConsumptionMap.put(90.0, 7.7);
        speedToConsumptionMap.put(120.0, 7.5);
        speedToConsumptionMap.put(150.0, 9.0);

        CombustionVehicleConsumption combustionVehicleConsumption = new CombustionVehicleConsumption(
                CURRENT_FUEL_IN_LITERS,
                AUXILIARY_POWER_IN_LITERS_PER_HOUR,
                FUEL_ENERGY_DENSITY_IN_MJOULES_PER_LITER,
                speedToConsumptionMap
        );

        CombustionVehicleDescriptor combustionVehicleDescriptor = new CombustionVehicleDescriptor.Builder()
                .vehicleConsumption(combustionVehicleConsumption)
                .vehicleDimensions(new VehicleDimensions.Builder()
                        .vehicleWeightInKg(1600)
                        .build()
                )
                .vehicleEfficiency(new VehicleEfficiency.Builder()
                        .accelerationEfficiency(0.33)
                        .decelerationEfficiency(0.33)
                        .downhillEfficiency(0.33)
                        .uphillEfficiency(0.33)
                        .build()
                )
                .build();

        //end::doc_route_waypoints[]
        return new RouteSpecification.Builder(start, stop)
                .routeCalculationDescriptor(routeCalculationDescriptor)
                .combustionVehicleDescriptor(combustionVehicleDescriptor)
                .build();
    }

    private void addWaypoint(LatLng latLng) {
        wayPoints.add(latLng);
    }

    public void showRoute(RouteSpecification routeSpecification) {
        routePlannerAPI.planRoute(routeSpecification, routeCallback);
    }

    private final RouteCallback routeCallback = new RouteCallback() {
        @Override
        public void onSuccess(@NonNull RoutePlan routePlan) {
            displayRoutes(routePlan);
            // displayFuel(routePlan.getRoutes());
        }

        @Override
        public void onError(@NonNull RoutingException error) {
            handleApiError(error);
            clearMap();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        clearMap();
    }

    // protected void displayInfoAboutRoute(FullRoute routeResult) {
    //
    //     List<Instruction> instructions = Arrays.asList(routeResult.getGuidance().getInstructions());
    //     Log.e("man", instructions.toString());
    // }
}