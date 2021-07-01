package com.cektrend.trashget.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cektrend.trashget.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static com.cektrend.trashget.utils.ConstantUtil.TRASH_HEIGHT;
import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;
import static com.cektrend.trashget.utils.ConstantUtil.TRASH_LOCATION;

public class EditTrashActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvIdTrash;
    private EditText edtTinggi, edtLocation;
    private Button btnSave;
    DatabaseReference dbTrash;
    private String idTrash, locationTrash;
    private Integer heightTrash;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trash);
        dbTrash = FirebaseDatabase.getInstance().getReference();
        initComponents();
        initValue();
        btnSave.setOnClickListener(this);
    }

    private void initComponents() {
        tvIdTrash = findViewById(R.id.tv_trash);
        edtTinggi = findViewById(R.id.edt_tinggi);
        edtLocation = findViewById(R.id.edt_location);
        btnSave = findViewById(R.id.btn_save);
        toolbar = findViewById(R.id.toolbar_edit_trash);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Data TPS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initValue() {
        idTrash = getIntent().getStringExtra(TRASH_ID);
        locationTrash = getIntent().getStringExtra(TRASH_LOCATION);
        heightTrash = getIntent().getIntExtra(TRASH_HEIGHT, 0);
        tvIdTrash.setText(new StringBuilder("Bak Sampah : " + idTrash));
        edtLocation.setText(locationTrash);
        edtTinggi.setText(String.valueOf(heightTrash));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_save) {
            String location = edtLocation.getText().toString();
            Integer tinggi = Integer.valueOf(edtTinggi.getText().toString());
            Map<String, Object> updateTrash = new HashMap<>();
            updateTrash.put("location", location);
            updateTrash.put("tinggiBak", tinggi);
            dbTrash.child("trashes").child(idTrash).child("data").updateChildren(updateTrash)
                    .addOnSuccessListener(this, new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(EditTrashActivity.this, "Data berhasil diperbarui..", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(this, error -> Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show());
            ;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}