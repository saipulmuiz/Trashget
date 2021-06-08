package com.cektrend.trashget.customer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cektrend.trashget.adapter.TruckListAdapter;
import com.cektrend.trashget.Interface.NameValue;
import com.cektrend.trashget.R;
import com.cektrend.trashget.Track;
import com.cektrend.trashget.admin.AdminMapsActivity;
import com.cektrend.trashget.collector.TrackCollector;
import com.cektrend.trashget.collector.TrackTruck;
import com.cektrend.trashget.data.DataTrash;
import com.cektrend.trashget.data.TruckList;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_FINE_LOCATION;

public class GetRequest extends AppCompatActivity implements Track, NameValue, SwipeRefreshLayout.OnRefreshListener {
    List<TruckList> listCollector = new ArrayList<>();
    RecyclerView recyclerView;
    ProgressDialog pDialog;
    private RecyclerView.LayoutManager layoutManager;
    Button bt;
    List list1;
    public static Double latitude, longitude;
    TruckListAdapter adapter;
    ArrayList<String> arrayList = new ArrayList<String>();
    DatabaseReference dbTrash;
    SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_request);
        initComponents();
        getCollector();
    }

    private void initComponents() {
        dbTrash = FirebaseDatabase.getInstance().getReference();
        recyclerView = findViewById(R.id.rv_collector);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
    }

    private void getCollector() {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Memuat ...");
        showDialog();
        dbTrash.child("customers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listCollector.clear();
                hideDialog();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.e("TAG", "snapshot : " + snapshot);
                    TruckList truckList = snapshot.child("data").getValue(TruckList.class);
                    truckList.setId(snapshot.getKey());
                    listCollector.add(truckList);
                }
                adapter = new TruckListAdapter(listCollector, GetRequest.this);
                adapter.setTrack(GetRequest.this);
                adapter.setvalue(GetRequest.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Gagal memuat, cobalah periksa koneksi internet anda", Toast.LENGTH_SHORT).show();
                hideDialog();
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void track(final String idCustomer) {
        dbTrash.child("customers").child(idCustomer).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataTrash trash = dataSnapshot.child("data").getValue(DataTrash.class);
                if (trash != null) {
                    latitude = trash.getLatitude();
                    longitude = trash.getLongitude();
                    if (ActivityCompat.checkSelfPermission(GetRequest.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(GetRequest.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        askPermissionLocation();
                        return;
                    }
                    trackTrash();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE_PERMISSION_FINE_LOCATION) {// Note: If request is cancelled, the result arrays are empty.
            // Permissions granted (CALL_PHONE).
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(GetRequest.this, "Permission granted!", Toast.LENGTH_SHORT).show();
                trackTrash();
            } else {
                Toast.makeText(GetRequest.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void trackTrash() {
        Intent intent = new Intent(GetRequest.this, TrackTruck.class);
        intent.putExtra("lat", latitude);
        intent.putExtra("long", longitude);
        startActivity(intent);
    }

    @Override
    public void notifyMe(String idCustomer) {
        dbTrash.child("customers").child(idCustomer).child("data").child("status").setValue(1)
                .addOnSuccessListener(this, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(GetRequest.this, "Tps telah ditandai!", Toast.LENGTH_SHORT).show();
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
            }
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing()) pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing()) pDialog.dismiss();
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        getCollector();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}
