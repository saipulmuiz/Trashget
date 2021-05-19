package com.cektrend.trashget.people;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.cektrend.trashget.Interface.MultiMarker;
import com.cektrend.trashget.MainActivity;
import com.cektrend.trashget.R;
import com.google.gson.JsonObject;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapConstants;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQuery;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder;
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.tomtom.online.sdk.map.MapConstants.DEFAULT_ZOOM_LEVEL;

public class GetLocation extends AppCompatActivity implements OnMapReadyCallback,TomtomMapCallback.OnMarkerClickListener {
    TomtomMap tom;
    private SearchApi searchApi;
    //  Button bt, bt1, bt2;
    MultiMarker multiMarker;
    double latitude, longitude;
    static double setlat = 0, setlang = 0;
    int count = 0, count1 = 0;
    String marklatitude;
    String marklongitude;
    private LatLng departurePosition;
    private LatLng destinationPosition;
    private Icon departureIcon;
    private Icon destinationIcon;
    Marker marker;
    Vibrator vibrator;
    static MarkerBuilder markerBuilder1;
    int count2=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //    bt2=findViewById(R.id.recenter);
//bt1=findViewById(R.id.setmarker);
        searchApi= OnlineSearchApi.create(this);
        departureIcon = Icon.Factory.fromResources(GetLocation.this, R.drawable.ic_map_route_departure);
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
    }

    private void getlocation(TomtomMap tomtomMap) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Double doubl=new Double(latitude);
                String latitude1=doubl.toString();
                LatLng latLng=new LatLng(latitude,longitude) ;
                if(count2==0) {
                    if(latitude1!=null) {
                        tomtomMap.centerOn(latLng.getLatitude(), latLng.getLongitude(), DEFAULT_ZOOM_LEVEL, MapConstants.ORIENTATION_NORTH);
                        count2++;
                    }
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

//    @Override
//    public void onBackPressed() {
//        Intent i = new Intent(this, People.class);
//// set the new task and clear flags
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(i);
//    }

    @Override
    public void onMapReady(@NonNull final TomtomMap tomtomMap) {
        tom = tomtomMap;
        tom.clear();

        getlocation(tomtomMap);
        Toast toast=  Toast.makeText(getApplicationContext(),"Click the Nearby Garbage to get the location",Toast.LENGTH_LONG);
        toast.show();
        tom.addOnMarkerClickListener(this);
        this.tom.setMyLocationEnabled(true);
        addgarbage(tomtomMap);
        //this.tom.addOnMapLongClickListener(this);

//garbage();


//        bt2.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//   tomtomMap.clear();
//        Toast.makeText(GetLocation.this,"Again long click to add marker",Toast.LENGTH_LONG).show();
//    count1=0;
//    }
//});
//
//   bt1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(GetLocation.setlat!=0 && GetLocation.setlang !=0)
//                {
//
//                    Toast.makeText(GetLocation.this,""+GetLocation.setlat,Toast.LENGTH_LONG).show();
//                    Intent intent=new Intent(GetLocation.this,People.class);
//                    startActivity(intent);
//                }
//                else
//                {
//                    Toast.makeText(GetLocation.this,"called",Toast.LENGTH_LONG).show();
//                }
//            }
//        });

    }

    private void addgarbage(TomtomMap tomtomMap) {
        String url="http://fundevelopers.website/TomTom/garbageget.php";
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int count=0;
                while(count<response.length())
                {
                    try {
                        JSONObject jsonObject=response.getJSONObject(count);
                        marklatitude= jsonObject.getString("latitude");
                        marklongitude= jsonObject.getString("longitude");
                        //String string=new String();
                        GetLocation.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                double lat=Double.parseDouble(marklatitude);
                                double lang=Double.parseDouble(marklongitude);
                                LatLng latLng1=new LatLng(lat,lang);
                                MarkerBuilder markerBuilder=new MarkerBuilder(latLng1).markerBalloon(new SimpleMarkerBalloon("garbage"))
                                        .icon(Icon.Factory.fromResources(getApplicationContext(),R.drawable.smallkutty));
                                tomtomMap.addMarker(markerBuilder);
                                Log.e("latt",""+lat);
                            }
                        });


                        count++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonArrayRequest);
    }


    //    @Override
//    public void onMapLongClick(@NonNull LatLng latLng) {
//        if (isDeparturePositionSet() && isDestinationPositionSet()) {
////            clearMap();
//        } else {
//            handleLongClick(latLng);
//        }
//        TomtomMapCallback.OnMarkerDragListener onMarkerDragListener = new TomtomMapCallback.OnMarkerDragListener() {
//            @Override
//            public void onStartDragging(@NonNull Marker marker) {
//                vibrator.vibrate(500);
//                Log.e("StartDragging",marker.getPosition().getLatitude()+""+marker.getPosition().getLongitude());
//            }
//
//            @Override
//            public void onStopDragging(@NonNull Marker marker) {
//                Log.e("StopDragging",marker.getPosition().getLatitude()+""+marker.getPosition().getLongitude());
//           setlat=marker.getPosition().getLatitude();
//           setlang=marker.getPosition().getLongitude();
//            }
//
//            @Override
//            public void onDragging(@NonNull Marker marker) {
//
//            }
//        };
//        tom.getMarkerSettings().addOnMarkerDragListener(onMarkerDragListener);
//
//    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.tom.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private boolean isDestinationPositionSet() {
        return destinationPosition != null;
    }

    private boolean isDeparturePositionSet() {
        return departurePosition != null;
    }
    private void handleLongClick(@NonNull LatLng latLng) {
        Log.e("clicked", "" + latLng.getLatitude());

        MarkerBuilder markerBuilder=new MarkerBuilder(latLng)
                .markerBalloon(new SimpleMarkerBalloon("hello"))
                .draggable(true)
                .decal(true);
        if(count1==0)
        {
            tom.addMarker(markerBuilder);
            count1++;
        }
        setlat=latLng.getLatitude();
        setlang=latLng.getLongitude();

    }

    @Override
    public void onMarkerClick(@NonNull Marker marker) {
        Log.e("marrk",""+marker.getPosition().getLatitude());
        setlat=marker.getPosition().getLatitude();
        setlang=marker.getPosition().getLongitude();
//        Intent intent=new Intent(GetLocation.this,People.class);
//        startActivity(intent);
//        finish();
    }

//        searchApi.reverseGeocoding(new ReverseGeocoderSearchQuery(latLng.getLatitude(),latLng.getLongitude(),))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new DisposableSingleObserver<ReverseGeocoderSearchResponse>() {
//                    @Override
//                    public void onSuccess(ReverseGeocoderSearchResponse response) {
//                        processResponse(response);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        handleApiError(e);
//                    }
//
//                    private void processResponse(ReverseGeocoderSearchResponse response) {
//                        if (response.hasResults()) {
//                            processFirstResult(response.getAddresses().get(0).getPosition());
//                        }
//                        else {
//                            Toast.makeText(GetLocation.this, getString(R.string.geocode_no_results), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                    private void handleApiError(Throwable e) {
//                        Toast.makeText(GetLocation.this, getString(R.string.api_response_error, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
//                    }
//                    private void processFirstResult(LatLng geocodedPosition) {
//                        if (!isDeparturePositionSet()) {
//                            setAndDisplayDeparturePosition(geocodedPosition);
//                        } else {
//                            destinationPosition = geocodedPosition;
//                            tom.removeMarkers();
//                            //drawRoute(departurePosition, destinationPosition);
//                        }
//                    }
//
//                    private void setAndDisplayDeparturePosition(LatLng geocodedPosition) {
//                        departurePosition = geocodedPosition;
//                        //createMarkerIfNotPresent(departurePosition, departureIcon);
//                    }
    //           });
}













//        bt1=findViewById(R.id.setmarker);
//bt2=findViewById(R.id.recenter);
//        vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        LocationListener locationListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                latitude = location.getLatitude();
//                longitude = location.getLongitude();
//                Log.e("latt", String.valueOf(latitude));
//                Log.e("long", String.valueOf(longitude));
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//
//            }
//        };
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        //LatLng latLng=new LatLng(13.0827,80.2707);]
//
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
//                2000,
//                10, locationListener);

//   bt = findViewById(R.id.getmarker);





//  Location location=tomtomMap.getUserLocation();
//   bt1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(GetLocation.setlat!=0 && GetLocation.setlang !=0)
//                {
//
//                    Toast.makeText(GetLocation.this,""+GetLocation.setlat,Toast.LENGTH_LONG).show();
//                    Intent intent=new Intent(GetLocation.this,People.class);
//
//                    startActivity(intent);
//                }
//                else
//                {
//                    Toast.makeText(GetLocation.this,"called",Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//bt2.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View v) {
//   tomtomMap.clear();
//        final LatLng latLng=new LatLng(latitude,longitude);
//        MarkerBuilder markerBuilder=new MarkerBuilder(latLng)
//                .markerBalloon(new SimpleMarkerBalloon("hello"))
//                .draggable(true)
//                .decal(true);
//        if(count1==0)
//        {
//            tom.addMarker(markerBuilder);
//            count1++;
//        }
//
//    }
//});
//        bt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                final LatLng latLng=new LatLng(latitude,longitude);
//                MarkerBuilder markerBuilder=new MarkerBuilder(latLng)
//                        .markerBalloon(new SimpleMarkerBalloon("hello"))
//                        .draggable(true)
//                        .decal(true);
//       if(count==0)
//       {
//           tom.addMarker(markerBuilder);
//       count++;
//       }
//
//            }
//        });
//        TomtomMapCallback.OnMarkerDragListener onMarkerDragListener = new TomtomMapCallback.OnMarkerDragListener() {
//            @Override
//            public void onStartDragging(@NonNull Marker marker) {
//                vibrator.vibrate(500);
//                Log.e("StartDragging",marker.getPosition().getLatitude()+""+marker.getPosition().getLongitude());
//            }
//
//            @Override
//            public void onStopDragging(@NonNull Marker marker) {
//                Log.e("StopDragging",marker.getPosition().getLatitude()+""+marker.getPosition().getLongitude());
//           setlat=marker.getPosition().getLatitude();
//           setlang=marker.getPosition().getLongitude();
//            }
//
//            @Override
//            public void onDragging(@NonNull Marker marker) {
//
//            }
//        };
//        tomtomMap.getMarkerSettings().addOnMarkerDragListener(onMarkerDragListener);
//
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        this.tom.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    @Override
//    public void onMarkerClick(@NonNull Marker marker) {
//
//
//    }
//
