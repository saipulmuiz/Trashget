package com.cektrend.trashget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cektrend.trashget.admin.AdminLogin;
import com.cektrend.trashget.admin.AdminMapsActivity;
import com.cektrend.trashget.admin.ListTrashActivity;
import com.cektrend.trashget.customer.GetRequest;
import com.cektrend.trashget.customer.NotificationService;
import com.cektrend.trashget.customer.PeopleActivity;

import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.MY_REQUEST_CODE_PERMISSION_FINE_LOCATION;
import static com.cektrend.trashget.utils.ConstantUtil.MY_SHARED_PREFERENCES;
import static com.cektrend.trashget.utils.ConstantUtil.SESSION_NAME;
import static com.cektrend.trashget.utils.ConstantUtil.SESSION_STATUS;
import static com.cektrend.trashget.utils.ConstantUtil.SESSION_USERNAME;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    CardView cvPemetaan, cvListTrash, cvCollecting, cvCustomer, cvLogout;
    TextView tvUser;
    String username, name;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
        initListener();
        sharedPreferences = getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        username = sharedPreferences.getString(SESSION_USERNAME, "Trashget");
        name = sharedPreferences.getString(SESSION_NAME, "Trashget");
        tvUser.setText(new StringBuilder("Welcome, " + name));
        Intent intent = new Intent(MainActivity.this, NotificationService.class);
        if (!isMyServiceRunning(NotificationService.class)) {
            startService(intent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    private void initComponents() {
        tvUser = findViewById(R.id.tv_user);
        cvPemetaan = findViewById(R.id.cv_pemetaan);
        cvListTrash = findViewById(R.id.cv_list_trash);
        // cvCollecting = findViewById(R.id.cv_collecting);
        // cvCustomer = findViewById(R.id.cv_customer);
        cvLogout = findViewById(R.id.cv_logout);
    }

    private void initListener() {
        cvPemetaan.setOnClickListener(this);
        cvListTrash.setOnClickListener(this);
        // cvCollecting.setOnClickListener(this);
        // cvCustomer.setOnClickListener(this);
        cvLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.cv_pemetaan) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askPermissionLocation();
                return;
            }
            Intent toPemetaan = new Intent(MainActivity.this, AdminMapsActivity.class);
            startActivity(toPemetaan);
        } else if (id == R.id.cv_list_trash) {
            Intent toLisTrash = new Intent(MainActivity.this, ListTrashActivity.class);
            startActivity(toLisTrash);
        // }
        // else if (id == R.id.cv_collecting) {
        //     Intent toCollecting = new Intent(MainActivity.this, GetRequest.class);
        //     startActivity(toCollecting);
        // } else if (id == R.id.cv_customer) {
        //     // Intent toPemetaan = new Intent(MainActivity.this, AdminMapsActivity.class);
        //     // startActivity(toPemetaan);
        //     Toast.makeText(this, "Customer di klik!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.cv_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false);
            builder.setMessage("Apakah kamu ingin logout ?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SESSION_STATUS, false);
                editor.commit();
                Intent intent = new Intent(MainActivity.this, AdminLogin.class);
                finish();
                startActivity(intent);
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE_PERMISSION_FINE_LOCATION) {// Note: If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission granted!", Toast.LENGTH_SHORT).show();
                Intent toPemetaan = new Intent(MainActivity.this, AdminMapsActivity.class);
                startActivity(toPemetaan);
            } else {
                Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
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
}