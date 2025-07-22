package com.example.iotfirebase;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.iotfirebase.databinding.FragmentSmartMicrowaveBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;
import java.util.Objects;

public class SmartMicrowaveFragment extends Fragment {

    private FragmentSmartMicrowaveBinding binding;
    private DocumentReference mw;
    private Boolean mw_usage_status;
    private Long mw_remain_time;
    private Boolean mw_door_status;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState
    ) {
        binding = FragmentSmartMicrowaveBinding.inflate(inflater, container, false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mw = db.collection("HubSystem").document("SmartMicrowaveSystem");
        try {
            listenerRegistration = mw.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                assert value != null;
                Map<String, Object> task = value.getData();
                assert task != null;
                mw_usage_status = (Boolean) Objects.requireNonNull(task.get("usage_mw"));
                mw_door_status = (Boolean) Objects.requireNonNull(task.get("usage_mw_door")); // True -> Door Open
                try {
                    mw_remain_time = (Long) Objects.requireNonNull(task.get("count_down_num"));
                }
                catch (ClassCastException ignored) {
                    Double pad = (Double) Objects.requireNonNull(task.get("count_down_num"));
                    mw_remain_time = pad.longValue();
                }

                Button microwave_status_standby = container.findViewById(R.id.microwave_status_standby);
                Button microwave_door_open = container.findViewById(R.id.microwave_door_open);
                Button microwave_timer = container.findViewById(R.id.microwave_timer);

                if (mw_usage_status) {
                    microwave_status_standby.setText(R.string.microwave_status_standby);
                } else {
                    microwave_status_standby.setText(R.string.microwave_status_in_use);
                }
                setButtonColor(microwave_status_standby, mw_usage_status);

                if (mw_door_status) {
                    microwave_door_open.setText(R.string.microwave_door_open);
                }
                else {
                    microwave_door_open.setText(R.string.microwave_door_close);
                }
                setButtonColor(microwave_door_open, mw_door_status);

                microwave_timer.setText("+" + mw_remain_time + " seconds");
            });
        }
        catch (NullPointerException ignored) {}

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button microwave_status_standby = view.findViewById(R.id.microwave_status_standby);
        Button microwave_door_open = view.findViewById(R.id.microwave_door_open);
        Button microwave_timer = view.findViewById(R.id.microwave_timer);

        binding.microwaveStatusStandby.setOnClickListener(view1 -> {
            if (!mw_door_status) {
                if (mw_usage_status) {
                    microwave_status_standby.setText(R.string.microwave_status_in_use);
                }
                else {
                    microwave_status_standby.setText(R.string.microwave_status_standby);
                }
                mw_usage_status = !mw_usage_status;
                setButtonColor(microwave_status_standby, mw_usage_status);
                mw.update("usage_mw", mw_usage_status);
            }
        });

        binding.microwaveDoorOpen.setOnClickListener(view1 -> {
            if (mw_usage_status) {
                if (mw_door_status) {
                    microwave_door_open.setText(R.string.microwave_door_close);
                }
                else {
                    microwave_door_open.setText(R.string.microwave_door_open);
                }
                mw_door_status = !mw_door_status;
                setButtonColor(microwave_door_open, mw_door_status);
                mw.update("usage_mw_door", mw_door_status);
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
            button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.off));
        }
        else {
            button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.on));
        }
    }
}

//    private DocumentReference tv;
//    private Boolean tv_usage_status;
//    private String tv_cable;
//
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            ViewGroup container, Bundle savedInstanceState
//    ) {
//        binding = FragmentSmartTVBinding.inflate(inflater, container, false);
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        tv = db.collection("HubSystem").document("SmartTVSystem");
//        try {
//            tv.addSnapshotListener((value, error) -> {
//                if (error != null) {
//                    Log.w(TAG, "Listen failed.", error);
//                    return;
//                }
//
//                assert value != null;
//                Map<String, Object> task = value.getData();
//                assert task != null;
//                tv_usage_status = (Boolean) Objects.requireNonNull(task.get("usage_tv"));
//                tv_cable = (String) Objects.requireNonNull(task.get("tv_cable"));
//
//                Button tv_usage_button = container.findViewById(R.id.tv_usage);
//                Button tv_cable_button = container.findViewById(R.id.tv_cable_tv);
//
//                if (tv_usage_status) {
//                    tv_usage_button.setText(R.string.on);
//                }
//                else {
//                    tv_usage_button.setText(R.string.off);
//                }
//                setButtonColor(tv_usage_button, tv_usage_status);
//                if (tv_cable.equals(String.valueOf(R.string.tv_hdmi))) {
//                    tv_cable_button.setText(R.string.tv_hdmi);
//                } else if (tv_cable.equals(String.valueOf(R.string.tv_cable))) {
//                    tv_cable_button.setText(R.string.tv_cable);
//                } else if (tv_cable.equals(String.valueOf(R.string.tv_satellite))) {
//                    tv_cable_button.setText(R.string.tv_satellite);
//                }
//            });
//        }
//        catch (NullPointerException ignored) {}
//
//        return binding.getRoot();
//    }
//
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        Button tv_usage_button = view.findViewById(R.id.tv_usage);
//        Button tv_cable_button = view.findViewById(R.id.tv_cable_tv);
//
//        binding.tvUsage.setOnClickListener(view1 -> {
//            if (tv_usage_status) {
//                tv_usage_button.setText(R.string.off);
//            }
//            else {
//                tv_usage_button.setText(R.string.on);
//            }
//            tv_usage_status = !tv_usage_status;
//            setButtonColor(tv_usage_button, tv_usage_status);
//            tv.update("usage_tv", tv_usage_status);
//        });
//
//        binding.tvCableTv.setOnClickListener(view1 -> {
//            if (tv_usage_status) {
//                if (tv_cable.equals(String.valueOf(R.string.tv_hdmi))) {
//                    tv_cable_button.setText(R.string.tv_cable);
//                    tv_cable = String.valueOf(R.string.tv_cable);
//                } else if (tv_cable.equals(String.valueOf(R.string.tv_cable))) {
//                    tv_cable_button.setText(R.string.tv_satellite);
//                    tv_cable = String.valueOf(R.string.tv_satellite);
//                } else if (tv_cable.equals(String.valueOf(R.string.tv_satellite))) {
//                    tv_cable_button.setText(R.string.tv_hdmi);
//                    tv_cable = String.valueOf(R.string.tv_hdmi);
//                }
//                tv.update("tv_cable", tv_cable);
//            }
//        });
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//
//    public void setButtonColor(Button button, Boolean state) {
//        if (!state){
//            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.off)));
//        }
//        else {
//            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.on)));
//        }
//    }
