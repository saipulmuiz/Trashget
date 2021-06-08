package com.cektrend.trashget.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cektrend.trashget.MultiTrack;
import com.cektrend.trashget.R;
import com.cektrend.trashget.collector.TrackTrash;
import com.cektrend.trashget.collector.TrackTruck;
import com.cektrend.trashget.customer.GetRequest;
import com.cektrend.trashget.data.DataTrackingTrash;
import com.cektrend.trashget.utils.SortByOverallCapacity;
import com.cektrend.trashget.adapter.TrashListAdapter;
import com.cektrend.trashget.data.DataTrash;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_FINE_LOCATION;

public class ListTrashActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    TrashListAdapter adapter;
    ArrayList<DataTrash> dataTrash = new ArrayList<>();
    ArrayList<DataTrackingTrash> listTrackingTrash = new ArrayList<>();
    DatabaseReference dbTrash;
    private Toolbar toolbar;
    ProgressDialog pDialog;
    SwipeRefreshLayout swipeRefresh;
    private Button btnTracking;
    private TextView tvCountTrash;
    private Boolean isClear = false;
    // private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_trash);
        initComponents();
        MyRecyclerView();
        showDataTrash();
        getTrackingTrash();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(getIntent().getIntExtra("notif_id", 0));
        swipeRefresh.setOnRefreshListener(this);
        btnTracking.setOnClickListener(view -> {
            // Toast.makeText(this, String.valueOf(listTrackingTrash.size()), Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(ListTrashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ListTrashActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askPermissionLocation();
                return;
            }
            if (listTrackingTrash.size() != 0) {
                trackTrash();
            } else {
                Toast.makeText(this, "Silahkan ceklist dulu untuk tracking!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTrackingTrash() {
        dbTrash.child("trackings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isClear) {
                    listTrackingTrash.clear();
                }
                isClear = true;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataTrackingTrash trash = snapshot.getValue(DataTrackingTrash.class);
                    trash.setId(snapshot.getKey());
                    listTrackingTrash.add(trash);
                    tvCountTrash.setText(new StringBuilder("Tracking " + listTrackingTrash.size() + " Bak"));
                    btnTracking.setText(new StringBuilder("Tracking (" + listTrackingTrash.size() + ")"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    private void initComponents() {
        recyclerView = findViewById(R.id.rv_trash);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        toolbar = findViewById(R.id.toolbar_trash_list);
        tvCountTrash = findViewById(R.id.tv_count_trash);
        btnTracking = findViewById(R.id.btn_tracking);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Data Bak Sampah");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // auth = FirebaseAuth.getInstance();
        dbTrash = FirebaseDatabase.getInstance().getReference();
    }

    public void showDataTrash() {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Memuat ...");
        showDialog();
        dbTrash.child("trashes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataTrash.clear();
                hideDialog();
                //Inisialisasi ArrayList
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.e("TAG", "snapshot : " + snapshot);
                    DataTrash trash = snapshot.child("data").getValue(DataTrash.class);
                    trash.setId(snapshot.getKey());
                    dataTrash.add(trash);
                }
                Collections.sort(dataTrash, new SortByOverallCapacity());
                adapter = new TrashListAdapter(dataTrash, ListTrashActivity.this);
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

    private void MyRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        // Membuat Underline pada Setiap Item Didalam List
        // DividerItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        // itemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.line));
        // recyclerView.addItemDecoration(itemDecoration);
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
        showDataTrash();
        dbTrash.child("trackings").removeValue();
        tvCountTrash.setText(new StringBuilder("Tracking 0 Bak"));
        btnTracking.setText(new StringBuilder("Tracking (0)"));
    }

    private void trackTrash() {
        Intent intent = new Intent(ListTrashActivity.this, TrackTrash.class);
        startActivity(intent);
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
                Toast.makeText(ListTrashActivity.this, "Permission granted!", Toast.LENGTH_SHORT).show();
                if (listTrackingTrash.size() != 0) {
                    trackTrash();
                } else {
                    Toast.makeText(this, "Silahkan ceklist dulu untuk tracking!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ListTrashActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        dbTrash.child("trackings").removeValue();
        return true;
    }

    @Override
    public void onBackPressed() {
        dbTrash.child("trackings").removeValue();
        super.onBackPressed();
    }
}