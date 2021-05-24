package com.cektrend.trashget.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.cektrend.trashget.collector.TrackTruck;
import com.cektrend.trashget.data.DataTrash;
import com.cektrend.trashget.data.TruckList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRequest extends AppCompatActivity implements Track, NameValue, SwipeRefreshLayout.OnRefreshListener {
    List<TruckList> listCollector = new ArrayList<>();
    RecyclerView recyclerView;
    ProgressDialog pDialog;
    private RecyclerView.LayoutManager layoutManager;
    Button bt;
    List list1;
    public static String latitude, longitude;
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
                    // String[] split = response.split(",");
                    String latitude = trash.getLatitude().toString();
                    String longitude = trash.getLongitude().toString();
                    Intent intent = new Intent(GetRequest.this, TrackTruck.class);
                    intent.putExtra("lat", latitude);
                    intent.putExtra("lang", longitude);
                    startActivity(intent);
                }
                // Log.e("TAG", "latitude : " + latitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
        //   Log.e("iiii",""+i);
        // RequestQueue requestQueue = Volley.newRequestQueue(this);
        // String url = "http://192.168.137.1/php/tracktruck.php";
        // StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
        //     @Override
        //     public void onResponse(String response) {
        //         String[] split = response.split(",");
        //         String latitude = split[0];
        //         String longitude = split[1];
        //         Intent intent = new Intent(GetRequest.this, TrackTruck.class);
        //         intent.putExtra("lat", latitude);
        //         intent.putExtra("lang", longitude);
        //         startActivity(intent);
        //     }
        // }, new Response.ErrorListener() {
        //     @Override
        //     public void onErrorResponse(VolleyError error) {
        //     }
        // }) {
        //     @Override
        //     protected Map<String, String> getParams() throws AuthFailureError {
        //         Map<String, String> params = new HashMap<String, String>();
        //         params.put("name", name);
        //         return params;
        //     }
        // };
        // requestQueue.add(stringRequest);
    }

    @Override
    public void notifyme(String username) {
        String[] split = username.split(":");
        String username1 = split[1];
        Log.e("userr", username1);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://fundevelopers.website/TomTom/statusupdate.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", username1);
                return params;
            }
        };
        requestQueue.add(stringRequest);
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
