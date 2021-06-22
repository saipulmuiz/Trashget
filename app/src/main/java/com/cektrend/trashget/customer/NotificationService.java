package com.cektrend.trashget.customer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.cektrend.trashget.data.DataTrash;
import com.cektrend.trashget.helpers.NotificationHelper;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationService extends Service {
    Notification.Builder builder;
    NotificationHelper helper;
    Context context = this;
    int count = 0, count1 = 0;
    Boolean status = true, status2 = true;
    ArrayList<DataTrash> trashArrayList = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    DatabaseReference dbTrash;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbTrash = FirebaseDatabase.getInstance().getReference();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            helper = new NotificationHelper(context);
        }
        checkStatus();

        // Handler handler = new Handler();
        // handler.postDelayed(new Runnable() {
        //     @Override
        //     public void run() {
        //         handler.postDelayed(this, 5000);
        //     }
        // }, 5000);
        return START_STICKY;
    }

    private void checkStatus() {
        dbTrash.child("trashes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                StatusBarNotification[] notifications = new StatusBarNotification[0];
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    i++;
                    // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    //     notifications = mNotificationManager.getActiveNotifications();
                    // }
                    DataTrash trash = snapshot.child("data").getValue(DataTrash.class);
                    trash.setId(snapshot.getKey());
                    getNotification(trash, i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    private void getNotification(DataTrash trash, int i) {
        int overallCapacityTrash = (trash.getOrganicCapacity() + trash.getAnorganicCapacity()) / 2;
        if (trash.getFire()) {
            builder = helper.getNotificationIsFire(trash.getId(), " Segera padamkan!", trash.getLocation(), trash.getLatitude(), trash.getLongitude(), i);
            if (trash.getFireNotif()) {
                helper.getManager().notify(trash.getId(), i, builder.build());
            }
            dbTrash.child("trashes").child(trash.getId()).child("data").child("fireNotif").setValue(false);
        } else if (overallCapacityTrash > 80) {
            builder = helper.getNotificationTrashFull(trash.getId(), " Ayo bersihkan!", trash.getLocation(), trash.getLatitude(), trash.getLongitude(), i);
            if (trash.getNotif()) {
                helper.getManager().notify(trash.getId(), i, builder.build());
            }
            dbTrash.child("trashes").child(trash.getId()).child("data").child("notif").setValue(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}