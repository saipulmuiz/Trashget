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
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cektrend.trashget.R;
import com.cektrend.trashget.data.DataTrash;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.tomtom.online.sdk.map.MapConstants.DEFAULT_ZOOM_LEVEL;

public class AdminActivity extends AppCompatActivity implements OnMapReadyCallback, TomtomMapCallback.OnMarkerClickListener, View.OnClickListener {
    private static final int MY_REQUEST_CODE_PERMISSION_FINE_LOCATION = 1000;
    private static final int MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION = 2000;
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
    FloatingActionButton addGarbage, setGarbage, addDriver, showData;
    DatabaseReference dbTrash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        dbTrash = FirebaseDatabase.getInstance().getReference();
        initComponents();
        initiClickListener();
    }

    private void initComponents() {
        // bt=findViewById(R.id.garbage);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        addGarbage = findViewById(R.id.addgarbage);
        setGarbage = findViewById(R.id.setgarbage);
        addDriver = findViewById(R.id.adddriver);
        showData = findViewById(R.id.showdata);
        // bt1=findViewById(R.id.setgarbage);
        checkBox = findViewById(R.id.checkremove);
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getAsyncMap(this);
    }

    private void initiClickListener() {
        addGarbage.setOnClickListener(this);
        setGarbage.setOnClickListener(this);
        addDriver.setOnClickListener(this);
        showData.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.addgarbage) {
            askPermissionLocation();
            temp = 1;
            Toast.makeText(getApplicationContext(), "Silahkan pindahkan marker ke lokasi TPS!", Toast.LENGTH_LONG).show();
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
                                .markerBalloon(new SimpleMarkerBalloon("trash"))
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

            //LatLng latLng=new LatLng(13.0827,80.2707);]
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    2000,
                    10, locationListener);
        } else if (id == R.id.setgarbage) {
            if (AdminActivity.latitude != 0 && AdminActivity.longitude != 0) {
                if (temp == 1) {
                    insertvalues(AdminActivity.latitude, AdminActivity.longitude);
                } else {
                    Toast.makeText(getApplicationContext(), "Click the ADD GARBAGE first", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (id == R.id.adddriver) {
            Intent intent = new Intent(AdminActivity.this, AddDriver.class);
            startActivity(intent);
        } else if (id == R.id.showdata) {
            Intent intent = new Intent(AdminActivity.this, ListTrashActivity.class);
            startActivity(intent);
        }
    }

    private void askPermissionLocation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23
            int permissonFineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int permissonCoarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissonFineLocation != PackageManager.PERMISSION_GRANTED && permissonCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_CODE_PERMISSION_FINE_LOCATION);
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION);
                return;
            }
        }
    }

    @Override
    public void onMapReady(@NonNull TomtomMap tomtomMap) {
        this.tom = tomtomMap;
        tom.clear();
        tom.setMyLocationEnabled(true);
        getTrash(tomtomMap);
        getlocation(tomtomMap);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getApplicationContext(), "Pilih TPS yang akan dihapus!", Toast.LENGTH_SHORT).show();
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
        builder1.setMessage("Yakin mau menghapus TPS " + marker.getTag() + " ?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onDeleteTrash(marker.getTag().toString());
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

    public void onDeleteTrash(String trashId) {
        if (dbTrash != null) {
            dbTrash.child("trashes")
                    .child(trashId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(AdminActivity.this, "TPS Berhasil Dihapus", Toast.LENGTH_SHORT).show();
                            recreate();
                            checkBox.setChecked(false);
                        }
                    })
                    .addOnFailureListener(this, error -> Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show());
            ;
        }
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

        askPermissionLocation();

        //LatLng latLng=new LatLng(13.0827,80.2707);]

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                2000,
                10, locationListener);

    }

    private void getTrash(TomtomMap tomtomMap) {
        dbTrash.child("trashes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataTrash trash = snapshot.child("data").getValue(DataTrash.class);
                    trash.setId(snapshot.getKey());
                    Double latitude = trash.getLatitude();
                    Double longitude = trash.getLongitude();
                    //String string=new String();
                    AdminActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LatLng latLng = new LatLng(latitude, longitude);
                            // markerBuilder = new MarkerBuilder(latLng).markerBalloon(new SimpleMarkerBalloon("Trash : " + trash.getId()))
                            markerBuilder = new MarkerBuilder(latLng)
                                    .icon(Icon.Factory.fromResources(getApplicationContext(), R.drawable.smallkutty))
                                    .markerBalloon(new SimpleMarkerBalloon("trash : " + trash.getId())).tag(trash.getId());
                            tomtomMap.addMarker(markerBuilder);
                            Log.e("latt", "" + latitude);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Toast.makeText(getApplicationContext(), "Gagal memuat, cobalah periksa koneksi internet anda", Toast.LENGTH_SHORT).show();
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    private void insertvalues(double latitude, double longitude) {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyHHSS", Locale.getDefault());
        String trashId = df.format(c);
        DataTrash dataTrash = new DataTrash(latitude, longitude, 0, 0, 0, false, "Undefined");
        dbTrash.child("trashes").child("TR-" + trashId).child("data").setValue(dataTrash)
                .addOnSuccessListener(this, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(AdminActivity.this, "TPS telah ditambahkan!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, error -> Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        LatLng latLng = new LatLng(-7.1352807, 108.2417008);
        if (count1 == 0) {
            tom.centerOn(
                    latLng.getLatitude(), latLng.getLongitude(),
                    3.0,
                    MapConstants.ORIENTATION_NORTH);
            count1++;
        } else {
            // Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // startActivity(intent);
            finish();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}
