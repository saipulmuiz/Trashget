package com.cektrend.trashget.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cektrend.trashget.MainActivity;
import com.cektrend.trashget.R;
import com.cektrend.trashget.data.DataAdmin;
import com.cektrend.trashget.data.DataTrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.cektrend.trashget.utils.ConstantUtil.MY_SHARED_PREFERENCES;
import static com.cektrend.trashget.utils.ConstantUtil.SESSION_NAME;
import static com.cektrend.trashget.utils.ConstantUtil.SESSION_STATUS;
import static com.cektrend.trashget.utils.ConstantUtil.SESSION_USERNAME;

public class AdminLogin extends AppCompatActivity implements View.OnClickListener {
    EditText edtUsername, edtPassword;
    Button btnLogin;
    ProgressDialog pDialog;
    SharedPreferences sharedPreferences;
    Boolean session = false;
    DatabaseReference dbTrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        initCompoents();
        btnLogin.setOnClickListener(this);
    }

    private void initCompoents() {
        edtUsername = findViewById(R.id.edt_username);
        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        dbTrash = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        session = sharedPreferences.getBoolean(SESSION_STATUS, false);
        if (session) {
            startActivity(new Intent(AdminLogin.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_login) {
            String user = edtUsername.getText().toString().trim();
            String passwd = edtPassword.getText().toString().trim();
            if (!user.isEmpty() || !passwd.isEmpty()) {
                login(user, passwd);
            } else {
                edtUsername.setError("Please insert your username");
                edtPassword.setError("Please insert your password");
            }
        }
    }

    private void login(String username, String password) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Verifikasi...");
        showDialog();
        dbTrash.child("admin").child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hideDialog();
                DataAdmin admin = dataSnapshot.getValue(DataAdmin.class);
                if (admin != null) {
                    if (admin.getPassword().equals(password)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(SESSION_STATUS, true);
                        editor.putString(SESSION_NAME, admin.getName());
                        editor.putString(SESSION_USERNAME, admin.getUsername());
                        editor.apply();
                        Intent login = new Intent(AdminLogin.this, MainActivity.class);
                        startActivity(login);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Gagal Login", Toast.LENGTH_SHORT).show();
                        edtPassword.setText("");
                        edtUsername.setText("");
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

    private void showDialog() {
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
    }

    private void hideDialog() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }
}