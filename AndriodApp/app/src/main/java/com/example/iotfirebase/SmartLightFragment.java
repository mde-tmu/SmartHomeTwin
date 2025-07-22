package com.example.iotfirebase;

import static android.content.ContentValues.TAG;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.iotfirebase.databinding.FragmentSmartLightBinding;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;
import java.util.Objects;

public class SmartLightFragment extends Fragment {
    private FragmentSmartLightBinding binding;
    private DocumentReference lightDoc;
    private boolean light1_switch, light2_switch, light3_switch;
    private float  light1Bright, light2Bright, light3Bright;
    private ListenerRegistration reg;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentSmartLightBinding.inflate(inflater, container, false);

        lightDoc = FirebaseFirestore.getInstance()
                .collection("HubSystem")
                .document("SmartLightSystem");

        reg = lightDoc.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Log.w(TAG, "listen failed", e);
                return;
            }
            if (snap == null || !snap.exists()) return;

            Map<String, Object> task = snap.getData();
            if (task == null) return;

            light1_switch = (Boolean) Objects.requireNonNull(task.get("Light1_status"));
            light2_switch = (Boolean) Objects.requireNonNull(task.get("Light2_status"));
            light3_switch = (Boolean) Objects.requireNonNull(task.get("Light3_status"));
            light1Bright = ((Number) Objects.requireNonNull(task.get("Light1_brightness"))).floatValue();
            light2Bright = ((Number) Objects.requireNonNull(task.get("Light2_brightness"))).floatValue();
            light3Bright = ((Number) Objects.requireNonNull(task.get("Light3_brightness"))).floatValue();

            updateUI();
        });

        return binding.getRoot();
    }

    private void updateUI() {
        setSwitch(binding.light1SwitchStatus, light1_switch);
        setSwitch(binding.light2SwitchStatus, light2_switch);
        setSwitch(binding.light3SwitchStatus, light3_switch);

        binding.light1Slider.setValue(light1Bright);
        binding.light2Slider.setValue(light2Bright);
        binding.light3Slider.setValue(light3Bright);
    }

    private void setSwitch(Button btn, boolean switch_on) {
        btn.setText(switch_on ? R.string.on : R.string.off);
        int color = ContextCompat.getColor(requireContext(),
                switch_on ? R.color.on : R.color.off);
        btn.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        binding.masterSwitchStatus.setOnClickListener(v -> {
            setAllFalse();
        });

        binding.light1SwitchStatus.setOnClickListener(v -> toggleLight(1));
        binding.light2SwitchStatus.setOnClickListener(v -> toggleLight(2));
        binding.light3SwitchStatus.setOnClickListener(v -> toggleLight(3));

        binding.light1Slider.addOnSliderTouchListener(new SliderTouch(1));
        binding.light2Slider.addOnSliderTouchListener(new SliderTouch(2));
        binding.light3Slider.addOnSliderTouchListener(new SliderTouch(3));
    }

    private void toggleLight(int idx) {
        boolean newState;
        switch (idx) {
            case 1: newState = !light1_switch; lightDoc.update("Light1_status", newState); break;
            case 2: newState = !light2_switch; lightDoc.update("Light2_status", newState); break;
            case 3: newState = !light3_switch; lightDoc.update("Light3_status", newState); break;
            default: break;
        }
    }

    private void setAllFalse() {
        lightDoc.update(
                "Light1_status", false,
                "Light2_status", false,
                "Light3_status", false);
    }

    private class SliderTouch implements Slider.OnSliderTouchListener {
        private final int idx;
        SliderTouch(int i) { idx = i; }
        @Override public void onStartTrackingTouch(@NonNull Slider s) {}
        @Override public void onStopTrackingTouch(@NonNull Slider s) {
            float v = s.getValue();
            switch (idx) {
                case 1: lightDoc.update("Light1_brightness", v); break;
                case 2: lightDoc.update("Light2_brightness", v); break;
                case 3: lightDoc.update("Light3_brightness", v); break;
                default: break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (reg != null) reg.remove();
    }
}
