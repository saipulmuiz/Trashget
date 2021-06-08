package com.cektrend.trashget.customer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
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
import com.cektrend.trashget.R;
import com.cektrend.trashget.admin.AdminMapsActivity;
import com.cektrend.trashget.data.DataTrash;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tomtom.online.sdk.map.MapConstants.DEFAULT_ZOOM_LEVEL;

public class GetLocation extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    // private SearchApi searchApi;
    //  Button bt, bt1, bt2;
    MultiMarker multiMarker;
    double latitude, longitude;
    static double setlat = 0, setlang = 0;
    int count = 0, count1 = 0;
    String marklatitude;
    String marklongitude;
    private LatLng departurePosition;
    private LatLng destinationPosition;
    // private Icon departureIcon, destinationIcon;
    Marker marker;
    Vibrator vibrator;
    // static MarkerBuilder markerBuilder1;
    int count2 = 0;
    MarkerOptions markerOptions;
    DatabaseReference dbTrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        dbTrash = FirebaseDatabase.getInstance().getReference();
        //    bt2=findViewById(R.id.recenter);
        //bt1=findViewById(R.id.setmarker);
        // searchApi = OnlineSearchApi.create(this);
        // departureIcon = Icon.Factory.fromResources(GetLocation.this, R.drawable.ic_map_route_departure);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.get_location_map);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        getLocation(googleMap);
        getTrash(googleMap);
        Toast.makeText(this, "Klik TPS terdekat untuk mendapatkan lokasi!", Toast.LENGTH_LONG).show();
        mMap.setOnMarkerClickListener(GetLocation.this);
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

    private void getLocation(GoogleMap googleMap) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                if (count2 == 0) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                    count2++;
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

    private void getTrash(GoogleMap googleMap) {
        dbTrash.child("trashes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                googleMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataTrash trash = snapshot.child("data").getValue(DataTrash.class);
                    trash.setId(snapshot.getKey());
                    Double latitude = trash.getLatitude();
                    Double longitude = trash.getLongitude();
                    GetLocation.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LatLng latLng = new LatLng(latitude, longitude);
                            Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.smallkutty);
                            markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(icon))
                                    .snippet(trash.getId())
                                    .title("trash : " + trash.getId());
                            googleMap.addMarker(markerOptions);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
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

    private boolean isDestinationPositionSet() {
        return destinationPosition != null;
    }

    private boolean isDeparturePositionSet() {
        return departurePosition != null;
    }

    // private void handleLongClick(@NonNull LatLng latLng) {
    //     Log.e("clicked", "" + latLng.getLatitude());
    //
    //     MarkerBuilder markerBuilder = new MarkerBuilder(latLng)
    //             .markerBalloon(new SimpleMarkerBalloon("hello"))
    //             .draggable(true)
    //             .decal(true);
    //     if (count1 == 0) {
    //         tom.addMarker(markerBuilder);
    //         count1++;
    //     }
    //     setlat = latLng.getLatitude();
    //     setlang = latLng.getLongitude();
    //
    // }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.e("marrk", "" + marker.getPosition().latitude);
        setlat = marker.getPosition().latitude;
        setlang = marker.getPosition().longitude;
        Intent intent = new Intent(GetLocation.this, PeopleActivity.class);
        startActivity(intent);
        finish();
        return false;
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

    @Override
    public void onBackPressed() {
        LatLng latLng = new LatLng(-7.1252805, 108.219001);
        if (count1 == 0) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6));
            count1++;
        } else {
            Intent i = new Intent(this, PeopleActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }
}