package com.cektrend.trashget.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cektrend.trashget.Interface.NameValue;
import com.cektrend.trashget.R;
import com.cektrend.trashget.Track;
import com.cektrend.trashget.data.TruckList;

import java.util.List;

public class TruckListAdapter extends RecyclerView.Adapter<com.cektrend.trashget.adapter.TruckListAdapter.ViewHolder> {
    Track track;
    NameValue nameValue;
    public List<TruckList> list;

    public TruckListAdapter(List<TruckList> list, Context context) {
        this.list = list;
        this.context = context;
    }

    private Context context;

    public void setTrack(Track t) {
        this.track = t;
    }

    public void setvalue(NameValue nameValue) {
        this.nameValue = nameValue;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_truck, viewGroup, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView head, body, collectorId;
        Button btnTrack, btnNotify;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            collectorId = itemView.findViewById(R.id.tv_collector_id);
            head = itemView.findViewById(R.id.tv_headname);
            head = itemView.findViewById(R.id.tv_headname);
            body = itemView.findViewById(R.id.tv_bodymobile);
            btnTrack = itemView.findViewById(R.id.btn_track);
            btnNotify = itemView.findViewById(R.id.btn_notify);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        TruckList truckList = list.get(i);
        viewHolder.collectorId.setText(new StringBuilder("Id Customer : " + truckList.getId()));
        viewHolder.head.setText(truckList.getName());
        viewHolder.body.setText(truckList.getPhone());
        viewHolder.btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (track != null) {
                    track.track(truckList.getId());
                }
            }
        });
        viewHolder.btnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameValue != null) {
                    nameValue.notifyMe(truckList.getId());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
