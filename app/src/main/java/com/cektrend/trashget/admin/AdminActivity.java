package com.cektrend.trashget.admin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cektrend.trashget.MainActivity;
import com.cektrend.trashget.R;
import com.github.clans.fab.FloatingActionButton;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapConstants;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.Marker;
import com.tomtom.online.sdk.map.MarkerAnchor;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.map.TomtomMapCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.cektrend.trashget.collector.GetRequest.latitude;
import static com.tomtom.online.sdk.map.MapConstants.DEFAULT_ZOOM_LEVEL;

public class AdminActivity extends AppCompatActivity implements OnMapReadyCallback, TomtomMapCallback.OnMarkerClickListener {
    TomtomMap tom;
    int count = 0;
    int count2 = 0;
    int temp = 0;
    static double latitude = 0, longitude = 0;
    //Button bt,bt1;
    int count1 = 0;
    //int count2=0;
    MarkerBuilder markerBuilder;
    Vibrator vibrator;
    CheckBox checkBox;
    FloatingActionButton addgarbage, setgarbage, adddriver1;

    @Override
    public void onBackPressed() {

        LatLng latLng = new LatLng(20.5937, 78.9629);
        if (count1 == 0) {
            tom.centerOn(
                    latLng.getLatitude(), latLng.getLongitude(),
                    3.0,
                    MapConstants.ORIENTATION_NORTH);
            count1++;
        } else {
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        //      bt=findViewById(R.id.garbage);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        addgarbage = findViewById(R.id.addgarbage);
        setgarbage = findViewById(R.id.setgarbage1);
        adddriver1 = findViewById(R.id.adddriver1);
//bt1=findViewById(R.id.setgarbage);
        checkBox = findViewById(R.id.checkremove);
        //adddriver=findViewById(R.id.adddriver);
        adddriver1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, AddDriver.class);
                startActivity(intent);
            }
        });

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
        addgarbage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp = 1;
                Toast.makeText(getApplicationContext(), "Drag the marker and click set garbage", Toast.LENGTH_LONG).show();
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        if (count == 0) {
//                         int height = 100;
//                         int width = 100;
//                         BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.gar);
//                         Bitmap b=bitmapdraw.getBitmap();
//                         Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            MarkerBuilder markerBuilder = new MarkerBuilder(latLng).decal(true).draggable(true)
                                    .markerBalloon(new SimpleMarkerBalloon("garbage"))
                                    .iconAnchor(MarkerAnchor.Bottom);

                            tom.addMarker(markerBuilder);
                            count++;
                        }
                        Log.e("latt", String.valueOf(latitude));
                        Log.e("long", String.valueOf(longitude));
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

                if (ActivityCompat.checkSelfPermission(AdminActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AdminActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                //LatLng latLng=new LatLng(13.0827,80.2707);]
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        2000,
                        10, locationListener);
            }
        });
        setgarbage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AdminActivity.latitude != 0 && AdminActivity.longitude != 0) {
                    if (temp == 1) {
                        insertvalues(AdminActivity.latitude, AdminActivity.longitude);
                    } else {
                        Toast.makeText(getApplicationContext(), "Click the ADD GARBAGE first", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void addgarbage(TomtomMap tomtomMap) {
        String url = "http://fundevelopers.website/TomTom/garbageget.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int count = 0;
                while (count < response.length()) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(count);
                        String marklatitude = jsonObject.getString("latitude");
                        String marklongitude = jsonObject.getString("longitude");
                        //String string=new String();
                        AdminActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                double lat = Double.parseDouble(marklatitude);
                                double lang = Double.parseDouble(marklongitude);
                                LatLng latLng1 = new LatLng(lat, lang);
                                markerBuilder = new MarkerBuilder(latLng1).markerBalloon(new SimpleMarkerBalloon("garbage"))
                                        .icon(Icon.Factory.fromResources(getApplicationContext(), R.drawable.smallkutty));
                                tomtomMap.addMarker(markerBuilder);
                                Log.e("latt", "" + lat);
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


    private void insertvalues(double latitude, double longitude) {
        String url = "http://fundevelopers.website/TomTom/garbage.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", String.valueOf(latitude));
                params.put("long", String.valueOf(longitude));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        this.tom = tomtomMap;
        tom.clear();
        tom.setMyLocationEnabled(true);
        addgarbage(tomtomMap);
        getlocation(tomtomMap);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "Click the garbage to delete", Toast.LENGTH_SHORT).show();
                    tom.addOnMarkerClickListener(AdminActivity.this);
                } else {
                    tom.removeOnMarkerClickListener(AdminActivity.this);
                }
            }
        });
        TomtomMapCallback.OnMarkerDragListener onMarkerDragListener = new TomtomMapCallback.OnMarkerDragListener() {
            @Override
            public void onStartDragging(@NonNull Marker marker) {
                vibrator.vibrate(100);
            }

            @Override
            public void onStopDragging(@NonNull Marker marker) {
                latitude = marker.getPosition().getLatitude();
                longitude = marker.getPosition().getLongitude();
            }

            @Override
            public void onDragging(@NonNull Marker marker) {

            }
        };
        tom.getMarkerSettings().addOnMarkerDragListener(onMarkerDragListener);
    }

    @Override
    public void onMarkerClick(@NonNull Marker marker) {
//        Toast.makeText(getApplicationContext(),"clicked",Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(AdminActivity.this);
        builder1.setMessage("Are you sure you want to delete the garbage ?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // String username1= PeopleLogin.username1;
                        String latitude = String.valueOf(marker.getPosition().getLatitude());
                        String longitude = String.valueOf(marker.getPosition().getLongitude());
                        RequestQueue requestQueue = Volley.newRequestQueue(AdminActivity.this);
                        String url = "http://fundevelopers.website/TomTom/Removedriver.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                recreate();
                                checkBox.setChecked(false);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("latitude", latitude);
                                params.put("longitude", longitude);
                                return params;
                            }
                        };
                        requestQueue.add(stringRequest);
                        dialog.cancel();

                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

    private void getlocation(TomtomMap tomtomMap) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Double doubl = new Double(latitude);
                String latitude1 = doubl.toString();
                LatLng latLng = new LatLng(latitude, longitude);
                if (count2 == 0) {
                    if (latitude1 != null) {
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

}
