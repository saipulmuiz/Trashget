package com.cektrend.trashget.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cektrend.trashget.Interface.NameValue;
import com.cektrend.trashget.R;
import com.cektrend.trashget.Track;
import com.cektrend.trashget.collector.TruckList;

import java.util.List;

public class TruckListAdapter extends RecyclerView.Adapter<com.cektrend.trashget.adapter.TruckListAdapter.ViewHolder> {
    Track track;
    NameValue nameValue;
    public TruckListAdapter(List<TruckList> list) {
        this.list = list;
    }

    public void setTrack(Track t) {
        this.track = t;
    }

    public void setvalue(NameValue nameValue) {
        this.nameValue = nameValue;
    }

    public List<TruckList> list;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_truck, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        TruckList truckList = list.get(i);
        viewHolder.head.setText(truckList.getHead());
        viewHolder.body.setText(truckList.getBody());
        viewHolder.bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (track != null) {
                    String name = viewHolder.head.getText().toString();
                    track.track(name);
                }
            }
        });
        viewHolder.bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = viewHolder.head.getText().toString();
                if (nameValue != null) {
                    nameValue.notifyme(username);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView head, body;
        Button bt, bt1;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.headname);
            body = itemView.findViewById(R.id.bodymobile);
            bt = itemView.findViewById(R.id.trackbt);
            bt1 = itemView.findViewById(R.id.notify);
        }
    }
}
