package com.example.iotfirebase;

import static android.content.ContentValues.TAG;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.iotfirebase.databinding.FragmentSecondBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Map;

public class SecondFragment extends Fragment {
    private FragmentSecondBinding binding;
    private DocumentReference garage_door;
    private Boolean door_status, blocked;
    private ListenerRegistration reg;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        sys = db.collection("HubSystem").document("System_Control");
        garage_door = db.collection("HubSystem").document("SmartGarageDoorSystem");

        door_status = false;
        blocked = false;

        reg = garage_door.addSnapshotListener((snap, e) -> {
            if (e != null) { Log.w(TAG, "listen failed", e); return; }
            if (snap == null || !snap.exists()) return;

            Map<String, Object> data = snap.getData();
            if (data == null) return;

            door_status = Boolean.TRUE.equals(data.get("status_door"));
            blocked = Boolean.TRUE.equals(data.get("door_blocked"));
            if (door_status) {
                if (blocked) {
                    binding.textviewSecond.setText(R.string.garage_door_warning_text);
                }
                else {
                    binding.textviewSecond.setText(R.string.garage_door_open_text);
                }
            }
            else {
                binding.textviewSecond.setText(R.string.garage_door_close_text);
            }
            int door_status_text = door_status ? R.string.garage_door_open : R.string.garage_door_close;
            binding.buttonSecond.setText(door_status_text);
            setButtonColor(binding.buttonSecond, door_status, blocked);
            int door_block_text = blocked ? R.string.garage_door_block : R.string.garage_door_unblock;
//            binding.blockDoor.setText(door_block_text);
//            setButtonColor(binding.blockDoor, true, blocked);
        });

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button door_status_button = view.findViewById(R.id.button_second);
//        Button block_door = view.findViewById(R.id.block_door);
//        TextView door_message = view.findViewById(R.id.textview_second);

        binding.buttonSecond.setOnClickListener(view1 -> {
            if (door_status) {
                if (blocked) {
//                    door_message.setText(R.string.garage_door_warning_text);
                }
                else {
//                    door_message.setText(R.string.garage_door_open_text);
                    door_status = false;
                }
            }
            else {
//                door_message.setText(R.string.garage_door_close_text);
                door_status = true;
            }
            setButtonColor(door_status_button, door_status, blocked);
            garage_door.update("status_door", door_status);
            garage_door.update("sync", true);
        });

//        binding.blockDoor.setOnClickListener(view1 -> {
//            blocked = !blocked;
//            setButtonColor(block_door, true, blocked);
//            garage_door.update("door_blocked", blocked);
//            garage_door.update("sync", true);
//        });
    }

    public void setButtonColor(Button button, Boolean door_state, Boolean blocked) {
        int color;
        if (!door_state){
            color = ContextCompat.getColor(requireContext(), R.color.off);
        }
        else if (blocked){
            color = ContextCompat.getColor(requireContext(), R.color.warning);
        }
        else {
            color = ContextCompat.getColor(requireContext(), R.color.on);
        }
        button.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (reg != null) reg.remove();
        binding = null;
    }
}