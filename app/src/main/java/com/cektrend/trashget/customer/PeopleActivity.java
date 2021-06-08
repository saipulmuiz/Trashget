package com.cektrend.trashget.customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.cektrend.trashget.MainActivity;
import com.cektrend.trashget.R;
import com.cektrend.trashget.data.DataCustomer;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_FINE_LOCATION;

public class PeopleActivity extends AppCompatActivity implements View.OnClickListener {
    EditText name, mobileno;
    Button getlocation, submit;
    String user = null;
    TextView textView;
    Geocoder geocoder;
    List<Address> addresses;
    DatabaseReference dbTrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        initComponents();
        initClickListener();
        if (GetLocation.setlat != 0 && GetLocation.setlang != 0) {
            // location.setText(GetLocation.setlat+","+GetLocation.setlang);
            try {
                addresses = geocoder.getFromLocation(GetLocation.setlat, GetLocation.setlang, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                textView.setText(address + "\n" + city);
                //basket.markerBalloon(new SimpleMarkerBalloon(address+"\n"+city));
                Log.e("address", address);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initComponents() {
        dbTrash = FirebaseDatabase.getInstance().getReference();
        geocoder = new Geocoder(this, Locale.getDefault());
        textView = findViewById(R.id.textv);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        name = findViewById(R.id.name);
        //location=findViewById(R.id.garloc);
        getlocation = findViewById(R.id.getlocbt);
        submit = findViewById(R.id.submitbt);
        name.setText("975497");
    }

    private void initClickListener() {
        getlocation.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        //finishAffinity();
        GetLocation.setlang = 0;
        GetLocation.setlat = 0;
        Intent intent = new Intent(PeopleActivity.this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.getlocbt) {
            if (ActivityCompat.checkSelfPermission(PeopleActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PeopleActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askPermissionLocation();
                return;
            }
            doGetLocation();
        } else if (id == R.id.submitbt) {
            final String idCustomer, mobile, location1;
            final Double latitude = GetLocation.setlat;
            final Double longitude = GetLocation.setlang;
            idCustomer = name.getText().toString();
            HashMap<String, Object> values = new HashMap<>();
            values.put("latitude", latitude);
            values.put("longitude", longitude);
            values.put("status", 0);
            dbTrash.child("customers").child(idCustomer).child("data")
                    .updateChildren(values)
                    .addOnSuccessListener(PeopleActivity.this, new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(PeopleActivity.this, "Your request success!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PeopleActivity.this, NotificationService.class);
                            startService(intent);
                        }
                    })
                    .addOnFailureListener(PeopleActivity.this, error -> Toast.makeText(PeopleActivity.this, error.toString(), Toast.LENGTH_SHORT).show());
        }
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
                Toast.makeText(PeopleActivity.this, "Permission granted!", Toast.LENGTH_SHORT).show();
                doGetLocation();
            } else {
                Toast.makeText(PeopleActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doGetLocation() {
        Intent intent = new Intent(PeopleActivity.this, GetLocation.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
