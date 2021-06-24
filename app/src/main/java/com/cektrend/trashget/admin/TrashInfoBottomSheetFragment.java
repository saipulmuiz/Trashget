package com.cektrend.trashget.admin;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cektrend.trashget.R;
import com.cektrend.trashget.data.DataTrash;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.Executor;

import static com.cektrend.trashget.utils.ConstantUtil.TRASH_ID;

public class TrashInfoBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    TextView tvTrash, tvLocation, tvGasvalue, tvDeteksiApi;
    Button btnBottomSheet;
    CircleProgress cpOrganik, cpAnorganik;
    ImageView imgApi;
    DatabaseReference dbTrash;
    DataTrash dataTrash;
    String idTrash;
    boolean showFAB = true;
    FloatingActionButton fabDelete;
    Animation growAnimation, shrinkAnimation;
    BottomSheetBehavior behavior;

    public TrashInfoBottomSheetFragment() {
        // Required empty public constructor
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        idTrash = getArguments() != null ? getArguments().getString(TRASH_ID) : null;
        View root = inflater.inflate(R.layout.fragment_trash_info_bottom_sheet, container, false);
        initComponents(root);
        fabDelete.setOnClickListener(this);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getTrashInfo(idTrash);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initComponents(View view) {
        tvTrash = view.findViewById(R.id.tv_trash);
        tvLocation = view.findViewById(R.id.tv_location);
        tvGasvalue = view.findViewById(R.id.tv_gas_value);
        tvDeteksiApi = view.findViewById(R.id.tv_deteksi_api);
        cpOrganik = view.findViewById(R.id.capacity_organic);
        cpAnorganik = view.findViewById(R.id.capacity_anorganic);
        imgApi = view.findViewById(R.id.img_api);
        fabDelete = view.findViewById(R.id.delete_fab);
        btnBottomSheet = view.findViewById(R.id.btn_bottom_sheet);
        growAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_grow);
        shrinkAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.simple_shrink);
        fabDelete.setVisibility(View.VISIBLE);
        fabDelete.startAnimation(growAnimation);
        btnBottomSheet.setOnClickListener(this);
        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fabDelete.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        dbTrash = FirebaseDatabase.getInstance().getReference();
    }

    private void getTrashInfo(String idTrash) {
        dbTrash.child("trashes").child(idTrash).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataTrash trash = dataSnapshot.child("data").getValue(DataTrash.class);
                if (trash != null) {
                    tvTrash.setText(new StringBuilder("Bak Sampah : " + idTrash));
                    cpOrganik.setProgress(trash.getOrganicCapacity());
                    cpAnorganik.setProgress(trash.getAnorganicCapacity());
                    tvGasvalue.setText(String.valueOf(trash.getKadarGas()));
                    tvLocation.setText(new StringBuilder("Lokasi : " + trash.getLocation()));
                    if (trash.getFire()) {
                        tvDeteksiApi.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.red));
                        DrawableCompat.setTint(imgApi.getDrawable(), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.orage));
                        tvDeteksiApi.setText("Api Terdeteksi !!");
                    } else {
                        tvDeteksiApi.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.gray));
                        DrawableCompat.setTint(imgApi.getDrawable(), ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.gray));
                        tvDeteksiApi.setText("Api Tidak Terdeteksi");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MyListActivity", databaseError.getDetails() + " " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.delete_fab) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
            builder1.setMessage("Yakin mau menghapus TPS " + idTrash + " ?");
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            onDeleteTrash(idTrash);
                            dialog.cancel();
                        }
                    });

            builder1.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        } else if (id == R.id.btn_bottom_sheet) {
            if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    public void onDeleteTrash(String trashId) {
        if (dbTrash != null) {
            dbTrash.child("trashes")
                    .child(trashId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(getActivity(), "TPS Berhasil Dihapus", Toast.LENGTH_SHORT).show();
                            dismiss();
                        }
                    });
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        dialog.setContentView(R.layout.fragment_trash_info_bottom_sheet);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                CoordinatorLayout coordinatorLayout = d.findViewById(R.id.coordinator_sheet);
                View bottomSheet = coordinatorLayout.findViewById(R.id.bottomSheetContainer);
                behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(View bottomSheet, int newState) {
                        Log.e("TAG", "newstate : " + newState);
                        switch (newState) {
                            case BottomSheetBehavior.STATE_DRAGGING:
                                if (showFAB)
                                    fabDelete.startAnimation(shrinkAnimation);
                                break;
                            case BottomSheetBehavior.STATE_COLLAPSED:
                                showFAB = true;
                                fabDelete.setVisibility(View.VISIBLE);
                                fabDelete.startAnimation(growAnimation);
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED:
                                showFAB = false;
                                break;
                        }
                    }

                    @Override
                    public void onSlide(View bottomSheet, float slideOffset) {
                        coordinatorLayout.animate().y(slideOffset <= 0 ?
                                bottomSheet.getY() + behavior.getPeekHeight() - coordinatorLayout.getHeight() :
                                bottomSheet.getHeight() - coordinatorLayout.getHeight()).setDuration(0).start();
                    }
                });
            }
        });
        return dialog;
    }
}