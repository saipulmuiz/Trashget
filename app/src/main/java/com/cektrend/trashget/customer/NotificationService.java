package com.cektrend.trashget.customer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cektrend.trashget.data.DataTrash;
import com.cektrend.trashget.helpers.NotificationHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
                    getNotif(trash, i);
                    // if (notifications.length == 0) {
                    //     getNotif(trash, i);
                    // } else {
                    //     for (StatusBarNotification notification : notifications) {
                    //         Log.e("TAG", notification.getTag() + " = " + trash.getId());
                    //         if (!notification.getTag().equals(trash.getId())) {
                    //             getNotif(trash, i);
                    //         }
                    //     }
                    // }
                    // if (notifications.length == 0) {
                    //     getNotif(trash, i);
                    // } else {
                    //
                    // }
                    // else {
                    //     if (status2) {
                    //         builder = helper.getNotification("Dust bin", "is cleaned");
                    //         helper.getManager().notify(1, builder.build());
                    //         startForeground(1, builder.build());
                    //         status2 = false;
                    //     }
                    // }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    private void getNotif(DataTrash trash, int i) {
        int overallCapacityTrash = (trash.getOrganicCapacity() + trash.getAnorganicCapacity()) / 2;
        if (trash.getFire()) {
            builder = helper.getNotificationIsFire(trash.getId(), " Segera padamkan!", trash.getLocation(), trash.getLatitude(), trash.getLongitude(), i);
            helper.getManager().notify(trash.getId(), i, builder.build());
        } else if (overallCapacityTrash > 85) {
            builder = helper.getNotificationTrashFull(trash.getId(), " Ayo bersihkan!", trash.getLocation(), trash.getLatitude(), trash.getLongitude(), i);
            helper.getManager().notify(trash.getId(), i, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}