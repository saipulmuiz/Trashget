package com.cektrend.trashget.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cektrend.trashget.R;
import com.cektrend.trashget.admin.AdminActivity;
import com.cektrend.trashget.admin.DetailTrashActivity;
import com.cektrend.trashget.admin.ListTrashActivity;
import com.cektrend.trashget.data.DataTrash;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;

public class TrashListAdapter extends RecyclerView.Adapter<TrashListAdapter.ViewHolder> {
    private ArrayList<DataTrash> listTrash;
    private ItemClickListener mClickListener;
    private Context context;
    DatabaseReference dbTrash;

    public TrashListAdapter(ArrayList<DataTrash> listTrash, Context context) {
        this.listTrash = listTrash;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_trash, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView trashId, tvLocation, tvGas, tvOrganic, tvAnorganic;
        private ConstraintLayout listItem;
        private View deleteLayout;
        CircleProgress capacityOverall;

        ViewHolder(View itemView) {
            super(itemView);
            trashId = itemView.findViewById(R.id.trashid);
            tvLocation = itemView.findViewById(R.id.tv_location_value);
            tvGas = itemView.findViewById(R.id.tv_gas_value);
            tvOrganic = itemView.findViewById(R.id.tv_organic);
            tvAnorganic = itemView.findViewById(R.id.tv_anorganic);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            capacityOverall = itemView.findViewById(R.id.capacity);
            dbTrash = FirebaseDatabase.getInstance().getReference();
            listItem = itemView.findViewById(R.id.list_item);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.trashId.setText("Trash Id : " + listTrash.get(position).getId());
        holder.tvLocation.setText(listTrash.get(position).getLocation());
        holder.tvGas.setText(new StringBuilder(listTrash.get(position).getKadarGas() + " Mol"));
        holder.tvOrganic.setText(new StringBuilder(listTrash.get(position).getOrganicCapacity() + " %"));
        holder.tvAnorganic.setText(new StringBuilder(listTrash.get(position).getAnorganicCapacity() + " %"));
        holder.capacityOverall.setProgress((listTrash.get(position).getOrganicCapacity() + listTrash.get(position).getAnorganicCapacity()) / 2);

        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailTrashActivity.class);
                intent.putExtra(TRASH_ID, listTrash.get(position).getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTrash(position, String.valueOf(listTrash.get(position).getId()));
            }
        });
    }

    private void deleteTrash(final int position, String trashId) {
        if (dbTrash != null) {
            dbTrash.child("trashes")
                    .child(trashId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(context, "TPS Berhasil Dihapus", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public int getItemCount() {
        return listTrash.size();
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}