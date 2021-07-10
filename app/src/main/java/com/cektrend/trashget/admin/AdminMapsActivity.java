package com.cektrend.trashget.admin;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cektrend.trashget.R;
import com.cektrend.trashget.adapter.CustomInfoTrashAdapter;
import com.cektrend.trashget.adapter.TrashListAdapter;
import com.cektrend.trashget.data.DataTrash;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_FINE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;

public class AdminMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, View.OnClickListener {
    private GoogleMap mMap;
    int count = 0;
    int count2 = 0;
    int temp = 0;
    static double latitude = 0, longitude = 0;
    int count1 = 0;
    Vibrator vibrator;
    FloatingActionButton addGarbage, setGarbage;
    DatabaseReference dbTrash;
    MarkerOptions markerOptions;
    DataTrash dataTrash;
    BottomSheetDialog bottomSheetDialog;
    Bitmap icon;
    List<Address> addresses;
    Geocoder geocoder;
    String bask, city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_maps);
        dbTrash = FirebaseDatabase.getInstance().getReference();
        geocoder = new Geocoder(this, Locale.getDefault());
        initComponents();
        initiClickListener();
    }

    private void initComponents() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        addGarbage = findViewById(R.id.addgarbage);
        setGarbage = findViewById(R.id.setgarbage);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.admin_map);
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

    private void initiClickListener() {
        addGarbage.setOnClickListener(this);
        setGarbage.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        getTrash(googleMap);
        getLocation(googleMap);
        mMap.setOnMarkerClickListener(AdminMapsActivity.this);
        mMap.setOnMarkerDragListener(this);
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
                    int overallCapacityTrash = (trash.getOrganicCapacity() + trash.getAnorganicCapacity()) / 2;
                    AdminMapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LatLng latLng = new LatLng(latitude, longitude);
                            if (overallCapacityTrash >= 0 && overallCapacityTrash <= 50) {
                                icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.trash_empty_30);
                            } else if (overallCapacityTrash > 50 && overallCapacityTrash <= 80) {
                                icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.trash_warn_30);
                            } else if (overallCapacityTrash > 80) {
                                icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.trash_full_30);
                            }
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

        askPermissionLocation();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                2000,
                10, locationListener);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.addgarbage) {
            askPermissionLocation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
            temp = 1;
            count1 = 0;
            Toast.makeText(getApplicationContext(), "Silahkan pindahkan marker ke lokasi TPS! dan pilih Set Gargabage!", Toast.LENGTH_LONG).show();
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (count == 0) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        MarkerOptions markerOptions1 = new MarkerOptions()
                                .position(latLng)
                                .title("Trash")
                                .draggable(true)
                                .anchor(0.0F, -1.0F);
                        mMap.addMarker(markerOptions1);
                        count++;
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    }
                }
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    2000,
                    10, locationListener);
        } else if (id == R.id.setgarbage) {
            if (AdminMapsActivity.latitude != 0 && AdminMapsActivity.longitude != 0) {
                if (temp == 1) {
                    insertValues(AdminMapsActivity.latitude, AdminMapsActivity.longitude);
                } else {
                    Toast.makeText(getApplicationContext(), "Click the ADD GARBAGE first", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        if (marker.getSnippet() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(TRASH_ID, marker.getSnippet());
            TrashInfoBottomSheetFragment trashInfoBottomSheetFragment = new TrashInfoBottomSheetFragment();
            trashInfoBottomSheetFragment.setArguments(bundle);
            trashInfoBottomSheetFragment.show(getSupportFragmentManager(), trashInfoBottomSheetFragment.getTag());
        }
        return true;
    }

    private void insertValues(double latitude, double longitude) {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyHHSS", Locale.getDefault());
        String trashId = df.format(c);
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
                String[] split = address.split(",");
                bask = split[1];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataTrash dataTrash = new DataTrash(latitude, longitude, 0, 0, (long) 0, false, bask, true, true, false, 0, 0);
        dbTrash.child("trashes").child("TR-" + trashId).child("data").setValue(dataTrash)
                .addOnSuccessListener(this, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(AdminMapsActivity.this, "TPS telah ditambahkan!", Toast.LENGTH_SHORT).show();
                        count = 0;
                    }
                })
                .addOnFailureListener(this, error -> Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show());
    }

    private void askPermissionLocation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23
            int permissonFineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int permissonCoarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissonFineLocation != PackageManager.PERMISSION_GRANTED && permissonCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST_CODE_PERMISSION_FINE_LOCATION);
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION);
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        LatLng latLng = new LatLng(-7.1252805, 108.219001);
        if (count1 == 0) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6));
            count1++;
        } else {
            // Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // startActivity(intent);
            finish();
        }
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        vibrator.vibrate(100);
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
    }
}