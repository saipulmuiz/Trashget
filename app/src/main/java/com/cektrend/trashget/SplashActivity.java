package com.cektrend.trashget;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cektrend.trashget.admin.AdminLogin;

public class SplashActivity extends AppCompatActivity {
    ImageView imgSplash;
    TextView tvNameDev, tvAppName;
    Animation fromBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgSplash = findViewById(R.id.splashImage);
        tvNameDev = findViewById(R.id.nameDev);
        tvAppName = findViewById(R.id.appName);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.frombottom);

        imgSplash.setAnimation(fromBottom);
        tvNameDev.setAnimation(fromBottom);
        tvAppName.setAnimation(fromBottom);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, AdminLogin.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
