package com.cektrend.trashget.utils;

import com.google.android.gms.maps.model.LatLng;

public class ConstantUtil {
    public static final String MY_SHARED_PREFERENCES = "my_shared_preferences";
    public static final String SESSION_STATUS = "session_status";
    public static final String SESSION_USERNAME = "session_username";
    public static final String SESSION_NAME = "session_name";
    public static final String TRASH_ID = "trashId";
    public static LatLng POINT_DEST = null;
    public static final int MY_REQUEST_CODE_PERMISSION_FINE_LOCATION = 1000;
    public static final int MY_REQUEST_CODE_PERMISSION_COARSE_LOCATION = 2000;
    public static final String CHANNEL_ID_TRASH_FULL = "1010";
    public static final String CHANNEL_NAME_TRASH_FULL = "Notif Tempat Sampah Penuh";
    public static final String CHANNEL_ID_IS_FIRE = "1011";
    public static final String CHANNEL_NAME_IS_FIRE = "Notif Api Terdeteksi";
    public static int NOTIFICATION_ID = 1;
}
