package com.cektrend.trashget.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.cektrend.trashget.R;
import com.cektrend.trashget.adapter.TrashListAdapter;
import com.cektrend.trashget.data.DataTrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ListTrashActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    TrashListAdapter adapter;
    ArrayList<DataTrash> dataTrash = new ArrayList<DataTrash>();
    DatabaseReference dbTrash;
    private Toolbar toolbar;
    ProgressDialog pDialog;
    SwipeRefreshLayout swipeRefresh;
    // private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_trash);
        initComponents();
        MyRecyclerView();
        showDataTrash();
        swipeRefresh.setOnRefreshListener(this);
    }

    private void initComponents() {
        recyclerView = findViewById(R.id.rv_trash);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        toolbar = findViewById(R.id.toolbar_trash_list);
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}