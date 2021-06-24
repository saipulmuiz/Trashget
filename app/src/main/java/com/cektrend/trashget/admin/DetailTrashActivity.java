package com.cektrend.trashget.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.cektrend.trashget.R;
import com.cektrend.trashget.data.DataTrash;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;

public class DetailTrashActivity extends AppCompatActivity {
    TextView tvTrash, tvLocation, tvGasvalue, tvDeteksiApi;
    CircleProgress cpOrganik, cpAnorganik;
    ImageView imgApi;
    String trashId;
    private Toolbar toolbar;
    DatabaseReference dbTrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_trash);
        dbTrash = FirebaseDatabase.getInstance().getReference();
        initComponents();
        trashId = getIntent().getStringExtra(TRASH_ID);
        initValue();
        getDetailTrash();
    }

    private void initComponents() {
        tvTrash = findViewById(R.id.tv_trash);
        tvLocation = findViewById(R.id.tv_location);
        tvGasvalue = findViewById(R.id.tv_gas_value);
        tvDeteksiApi = findViewById(R.id.tv_deteksi_api);
        cpOrganik = findViewById(R.id.capacity_organic);
        cpAnorganik = findViewById(R.id.capacity_anorganic);
        imgApi = findViewById(R.id.img_api);
        toolbar = findViewById(R.id.toolbar_detail_trash);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Informasi TPS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initValue() {
        tvTrash.setText(new StringBuilder("Bak Sampah : " + trashId));
    }

    private void getDetailTrash() {
        dbTrash.child("trashes").child(trashId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataTrash trash = dataSnapshot.child("data").getValue(DataTrash.class);
                if (trash != null) {
                    cpOrganik.setProgress(trash.getOrganicCapacity());
                    cpAnorganik.setProgress(trash.getAnorganicCapacity());
                    tvGasvalue.setText(String.valueOf(trash.getKadarGas()));
                    tvLocation.setText(new StringBuilder("Lokasi : " + trash.getLocation()));
                    if (trash.getFire()) {
                        tvDeteksiApi.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                        DrawableCompat.setTint(imgApi.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.orage));
                        tvDeteksiApi.setText("Api Terdeteksi !!");
                    } else {
                        tvDeteksiApi.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
                        DrawableCompat.setTint(imgApi.getDrawable(), ContextCompat.getColor(getApplicationContext(), R.color.gray));
                        tvDeteksiApi.setText("Api Tidak Terdeteksi");
                    }
                }
                // Log.e("TAG", "latitude : " + latitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}