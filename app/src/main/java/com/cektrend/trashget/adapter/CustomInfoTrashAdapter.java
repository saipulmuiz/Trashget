package com.cektrend.trashget.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cektrend.trashget.R;
import com.cektrend.trashget.data.DataTrash;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class CustomInfoTrashAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private Context mContext;
    private TextView trashId, tvTrashId, tvLocation, tvGas, tvOrganic, tvAnorganic;
    CircleProgress capacityOverall;
    DatabaseReference dbTrash;
    DataTrash mDataTrash;

    public CustomInfoTrashAdapter(Context context, DataTrash dataTrash) {
        mContext = context;
        mDataTrash = dataTrash;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_trash, null);
    }

    private void rendowWindowText(Marker marker, View view) {
        String title = marker.getTitle();
        initComponents(view);

        if (!title.equals("")) {
            trashId.setText(title);
            tvTrashId.setText(title);
            // tvOrganic.setText(new StringBuilder(mDataTrash.getOrganicCapacity() + " %"));
            // tvAnorganic.setText(new StringBuilder(mDataTrash.getAnorganicCapacity() + " %"));
            // tvGas.setText(new StringBuilder(mDataTrash.getKadarGas() + " Mol"));
            // tvLocation.setText(new StringBuilder("Lokasi : " + mDataTrash.getLocation()));
            // capacityOverall.setProgress((mDataTrash.getOrganicCapacity() + mDataTrash.getAnorganicCapacity()) / 2);
        }
    }

    private void initComponents(View view) {
        trashId = view.findViewById(R.id.tv_title_trash);
        tvTrashId = view.findViewById(R.id.trashid);
        tvLocation = view.findViewById(R.id.tv_location_value);
        tvGas = view.findViewById(R.id.tv_gas_value);
        tvOrganic = view.findViewById(R.id.tv_organic);
        tvAnorganic = view.findViewById(R.id.tv_anorganic);
        capacityOverall = view.findViewById(R.id.capacity);
        dbTrash = FirebaseDatabase.getInstance().getReference();
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        rendowWindowText(marker, mWindow);
        return mWindow;
    }
}
