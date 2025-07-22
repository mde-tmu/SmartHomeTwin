package com.example.iotfirebase;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.iotfirebase.databinding.FragmentFireAlarmBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;
import java.util.Objects;

public class FireAlarmFragment extends Fragment {
    private FragmentFireAlarmBinding binding;
    private DocumentReference fireAlarmDoc;
    private ListenerRegistration reg;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentFireAlarmBinding.inflate(inflater, container, false);
        fireAlarmDoc = FirebaseFirestore.getInstance()
                .collection("HubSystem")
                .document("SmartFireSystem");

        reg = fireAlarmDoc.addSnapshotListener((snap, e) -> {
            if (e != null) { Log.w(TAG, "listen failed", e); return; }
            if (snap == null || !snap.exists()) return;

            Map<String, Object> data = snap.getData();
            if (data == null) return;

            boolean systemOn = Boolean.TRUE.equals(data.get("status_system"));
            updateSystemStatus(systemOn);

            updateSensorButton(binding.flameSensorValue, (Number) data.get("Flame"), systemOn);
            updateSensorButton(binding.smokeSensorValue, (Number) data.get("Smoke"), systemOn);
            updateSensorButton(binding.heatSensorValue,  (Number) data.get("Heat"),  systemOn);
        });

        return binding.getRoot();
    }

    private void updateSystemStatus(boolean on) {
        TextView status = binding.fireAlarmSystemStatus;
        status.setText(on ? R.string.on : R.string.off);
        status.setTextColor(ContextCompat.getColor(requireContext(),
                on ? R.color.on : R.color.off));
    }

    private void updateSensorButton(Button btn, Number reading, boolean systemOn) {
        if (!systemOn || reading == null) {
            btn.setText(R.string.zero);
        } else {
            btn.setText(String.valueOf(reading));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.pause.setOnClickListener(v -> {
            updateSystemStatus(false);
            fireAlarmDoc.update("pause", true, "status_system", false);

            mainHandler.postDelayed(() -> {
                updateSystemStatus(true);
                fireAlarmDoc.update("pause", false, "status_system", true);
            }, 5000);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reg != null) reg.remove();
        binding = null;
    }
}