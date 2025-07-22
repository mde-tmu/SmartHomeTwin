package com.example.iotfirebase;

import static android.content.ContentValues.TAG;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.iotfirebase.databinding.FragmentSmartTVBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;
import java.util.Objects;

public class SmartTVFragment extends Fragment {

    private FragmentSmartTVBinding binding;
    private DocumentReference tv;
    private Boolean tv_usage_status;
    private String tv_cable;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState
    ) {
        binding = FragmentSmartTVBinding.inflate(inflater, container, false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        tv = db.collection("HubSystem").document("SmartTVSystem");
        try {
            listenerRegistration = tv.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                assert value != null;
                Map<String, Object> task = value.getData();
                assert task != null;
                tv_usage_status = (Boolean) Objects.requireNonNull(task.get("usage_tv"));
                tv_cable = (String) Objects.requireNonNull(task.get("tv_cable"));

                Button tv_usage_button = container.findViewById(R.id.tv_usage);
                Button tv_cable_button = container.findViewById(R.id.tv_cable_tv);

                if (tv_usage_status) {
                    tv_usage_button.setText(R.string.on);
                }
                else {
                    tv_usage_button.setText(R.string.off);
                }
                setButtonColor(tv_usage_button, tv_usage_status);
                if (tv_cable.equals(String.valueOf(R.string.tv_hdmi))) {
                    tv_cable_button.setText(R.string.tv_hdmi);
                } else if (tv_cable.equals(String.valueOf(R.string.tv_cable))) {
                    tv_cable_button.setText(R.string.tv_cable);
                } else if (tv_cable.equals(String.valueOf(R.string.tv_satellite))) {
                    tv_cable_button.setText(R.string.tv_satellite);
                }
            });
        }
        catch (NullPointerException ignored) {}

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button tv_usage_button = view.findViewById(R.id.tv_usage);
        Button tv_cable_button = view.findViewById(R.id.tv_cable_tv);

        binding.tvUsage.setOnClickListener(view1 -> {
            if (tv_usage_status) {
                tv_usage_button.setText(R.string.off);
            }
            else {
                tv_usage_button.setText(R.string.on);
            }
            tv_usage_status = !tv_usage_status;
            setButtonColor(tv_usage_button, tv_usage_status);
            tv.update("usage_tv", tv_usage_status);
        });

        binding.tvCableTv.setOnClickListener(view1 -> {
            if (tv_usage_status) {
                if (tv_cable.equals(String.valueOf(R.string.tv_hdmi))) {
                    tv_cable_button.setText(R.string.tv_cable);
                    tv_cable = String.valueOf(R.string.tv_cable);
                } else if (tv_cable.equals(String.valueOf(R.string.tv_cable))) {
                    tv_cable_button.setText(R.string.tv_satellite);
                    tv_cable = String.valueOf(R.string.tv_satellite);
                } else if (tv_cable.equals(String.valueOf(R.string.tv_satellite))) {
                    tv_cable_button.setText(R.string.tv_hdmi);
                    tv_cable = String.valueOf(R.string.tv_hdmi);
                }
                tv.update("tv_cable", tv_cable);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    public void setButtonColor(Button button, Boolean state) {
        if (!state){
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.off)));
        }
        else {
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.on)));
        }
    }
}