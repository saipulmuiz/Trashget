package com.cektrend.trashget.helpers;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;

import com.cektrend.trashget.R;
import com.cektrend.trashget.admin.DetailTrashActivity;
import com.cektrend.trashget.admin.ListTrashActivity;
import com.cektrend.trashget.collector.TrackTruck;

import static com.cektrend.trashget.utils.ConstantUtil.CHANNEL_ID_IS_FIRE;
import static com.cektrend.trashget.utils.ConstantUtil.CHANNEL_ID_TRASH_FULL;
import static com.cektrend.trashget.utils.ConstantUtil.CHANNEL_NAME_IS_FIRE;
import static com.cektrend.trashget.utils.ConstantUtil.CHANNEL_NAME_TRASH_FULL;
import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;

public class NotificationHelper extends ContextWrapper {
    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelTrashFull();
            createChannelIsFire();
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void createChannelTrashFull() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID_TRASH_FULL, CHANNEL_NAME_TRASH_FULL, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notifiksi TPS Penuh");
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(channel);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannelIsFire() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID_IS_FIRE, CHANNEL_NAME_IS_FIRE, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Notifiksi Api Terdeteksi");
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getNotificationTrashFull(String title, String body, String subtext, Double latitude, Double longitude, int notif_id) {
        // Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
        Intent listTrashIntent = new Intent(this, ListTrashActivity.class);
        Intent detailTrashIntent = new Intent(this, DetailTrashActivity.class);
        Intent trackingTrashIntent = new Intent(this, TrackTruck.class);
        listTrashIntent.putExtra("notif_id", notif_id);
        trackingTrashIntent.putExtra("lat", latitude);
        trackingTrashIntent.putExtra("long", longitude);
        trackingTrashIntent.putExtra("notif_id", notif_id);
        detailTrashIntent.putExtra(TRASH_ID, title);
        PendingIntent intentTrashList = PendingIntent.getActivity(this, 1, listTrashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent intentDetailTrash = PendingIntent.getActivity(this, 2, detailTrashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent intentTrackingTrash = PendingIntent.getActivity(this, 3, trackingTrashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID_TRASH_FULL)
                .setContentTitle("Bak Sampah " + title + " Sudah Penuh")
                .setSubText(subtext)
                .setColor(getResources().getColor(R.color.blue_500))
                .setContentText(body)
                .setSmallIcon(R.drawable.smallkutty)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.smallkutty))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setGroup("Trash Info")
                .setContentIntent(intentDetailTrash)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.drawable.dump_truck, "Tracking", intentTrackingTrash)
                .addAction(android.R.drawable.ic_menu_compass, "Show List Trash", intentTrashList);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getNotificationIsFire(String title, String body, String subtext, Double latitude, Double longitude, int notif_id) {
        Intent trackingTrashIntent = new Intent(this, TrackTruck.class);
        trackingTrashIntent.putExtra("lat", latitude);
        trackingTrashIntent.putExtra("long", longitude);
        trackingTrashIntent.putExtra("notif_id", notif_id);
        PendingIntent intentTrackingTrash = PendingIntent.getActivity(this, 4, trackingTrashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID_TRASH_FULL)
                .setContentTitle("TPS " + title + " Terdeteksi Api!")
                .setSubText(subtext)
                .setColor(getResources().getColor(R.color.red))
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_baseline_local_fire_department_24)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.trash_fire_80))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setGroup("Trash Info")
                .setContentIntent(intentTrackingTrash)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(Notification.PRIORITY_HIGH);
    }
}
