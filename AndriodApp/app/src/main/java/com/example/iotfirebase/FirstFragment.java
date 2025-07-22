package com.example.iotfirebase;

import static android.content.ContentValues.TAG;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.iotfirebase.databinding.FragmentFirstBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private Boolean hub_state, sys_state, network_state,
            sys_garage_door_status, sys_light_status, sys_tv_status, sys_microwave_status, sys_fire_status;
    private DocumentReference sys, garage_door, fire_alarm, light, tv;
    // Create the Handler object (on the main thread by default)
    private final Handler handler = new Handler();
    private Integer refresh_count = 0;

    // Define the code block to be executed
    private Runnable runnableCode;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);

        hub_state = sys_state = network_state = false;
        sys_garage_door_status = sys_light_status = sys_tv_status = sys_microwave_status = sys_fire_status = false;
        // System.out.println(hub_state.toString());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        sys = db.collection("HubSystem").document("System_Control");
        garage_door = db.collection("HubSystem").document("SmartGarageDoorSystem");
        fire_alarm = db.collection("HubSystem").document("SmartFireSystem");
        light = db.collection("HubSystem").document("SmartLightSystem");
        tv = db.collection("HubSystem").document("SmartTVSystem");

        // Start the initial runnable task by posting through the handler
//        handler.post(runnableCode);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button hub_trigger = view.findViewById(R.id.hub_trigger);
        Button sys_trigger = view.findViewById(R.id.sys_trigger);
        Button network_trigger = view.findViewById(R.id.network_trigger);

//        Button garage_door_button = view.findViewById(R.id.garage_door);
//        Button light_button = view.findViewById(R.id.light);
//        Button smart_tv_button = view.findViewById(R.id.smart_tv);
//        Button microwave_button = view.findViewById(R.id.microwave);

        Button fire_alarm_switch_button = view.findViewById(R.id.fire_alarm_switch);
        Button garage_door_switch_button = view.findViewById(R.id.garage_door_switch);
        Button light_switch_button = view.findViewById(R.id.light_switch);
//        Button smart_switch_tv_button = view.findViewById(R.id.smart_tv_switch);
//        Button microwave_switch_button = view.findViewById(R.id.microwave_switch);

        DocumentReference alerts = FirebaseFirestore.getInstance()
                .collection("HubSystem")
                .document("Alerts");

        alerts.addSnapshotListener((snap, e) -> {
            if (e != null) {
                Log.w(TAG, "listen failed", e);
                return;
            }
            if (snap == null || !snap.exists()) return;

            Map<String, Object> task = snap.getData();
            if (task == null) return;

            if (!Objects.equals(task.get("Alert1"), "")) {
                printAlert(task);
            }
            if (!Objects.equals(task.get("Alert2"), "")) {
                printAlert(task);
            }
        });

        runnableCode = () -> {
            Log.d("Handlers", String.valueOf(refresh_count+=1));

            sys.get().addOnCompleteListener((OnCompleteListener<DocumentSnapshot>) task -> {
                if (task.isSuccessful()) {
                    hub_state = (Boolean) task.getResult().get("status");
//                    Log.d("Handlers", String.valueOf(hub_state));
                    assert hub_state != null;
                    setButtonColor(hub_trigger, hub_state);
                    if (!hub_state){
                        sys_state = false;
                        updateSysStatus(sys, garage_door, false);
                    }

                    sys_fire_status = Boolean.TRUE.equals(task.getResult().get("sys_fire_status"));
                    setButtonColor(fire_alarm_switch_button, sys_fire_status);
                    if (sys_fire_status) {
                        fire_alarm_switch_button.setText(R.string.on);
                    } else {
                        fire_alarm_switch_button.setText(R.string.off);
                    }

                    sys_garage_door_status = Boolean.TRUE.equals(task.getResult().get("sys_garage_door_status"));
                    setButtonColor(garage_door_switch_button, sys_garage_door_status);
                    if (sys_garage_door_status) {
                        garage_door_switch_button.setText(R.string.on);
                    } else {
                        garage_door_switch_button.setText(R.string.off);
                    }

                    sys_light_status = Boolean.TRUE.equals(task.getResult().get("sys_light_status"));
                    setButtonColor(light_switch_button, sys_light_status);
                    if (sys_light_status) {
                        light_switch_button.setText(R.string.on);
                    } else {
                        light_switch_button.setText(R.string.off);
                    }

//                    sys_tv_status = Boolean.TRUE.equals(task.getResult().get("sys_tv_status"));
//                    setButtonColor(smart_switch_tv_button, sys_tv_status);
//                    if (sys_tv_status) {
//                        smart_switch_tv_button.setText(R.string.on);
//                    } else {
//                        smart_switch_tv_button.setText(R.string.off);
//                    }
//
//                    sys_microwave_status = Boolean.TRUE.equals(task.getResult().get("sys_microwave_status"));
//                    setButtonColor(microwave_switch_button, sys_microwave_status);
//                    if (sys_microwave_status) {
//                        microwave_switch_button.setText(R.string.on);
//                    } else {
//                        microwave_switch_button.setText(R.string.off);
//                    }

                    setButtonColor(sys_trigger, sys_garage_door_status&&sys_light_status&&sys_tv_status&&sys_microwave_status);
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            });

            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableCode, 2000);
        };
        handler.post(runnableCode);

        binding.hubTrigger.setOnClickListener(view1 -> {
            if (!hub_state) {
                hub_trigger.setText(R.string.hub_on);
                hub_state = true;
            }
            else {
                hub_trigger.setText(R.string.hub_off);
                hub_state = false;

                setButtonColor(sys_trigger, false);
                sys_trigger.setText(R.string.sys_off);
                updateSysStatus(sys, garage_door, false);
                sys_state = false;
            }
            setButtonColor(hub_trigger, hub_state);

            sys.update("status", hub_state);
        });

        binding.sysTrigger.setOnClickListener(view1 -> {
            if (hub_state) {
                if (!sys_state) {
                    sys_trigger.setText(R.string.sys_on);
                    sys_state = true;
                }
                else {
                    sys_trigger.setText(R.string.sys_off);
                    sys_state = false;
                }
                updateSysStatus(sys, garage_door, sys_state);
            }
        });

        binding.networkTrigger.setOnClickListener(view1 -> {
            if (!network_state) {
                network_trigger.setText(R.string.network_on);
                network_state = true;
            }
            else {
                network_trigger.setText(R.string.network_off);
                network_state = false;
            }
            setButtonColor(network_trigger, network_state);
        });

        binding.fireAlarm.setOnClickListener(view1 -> {
            if (sys_fire_status) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_FireAlarmFragment);
            }
        });

        binding.garageDoor.setOnClickListener(view1 -> {
            if (sys_garage_door_status) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.garageDoorSwitch.setOnClickListener(view1 -> {
            sys.update("sys_garage_door_status", !sys_garage_door_status);
            garage_door.update("status_system", !sys_garage_door_status);
        });

        binding.light.setOnClickListener(view1 -> {
            if (sys_light_status) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SmartLightFragment);
            }
        });

        binding.lightSwitch.setOnClickListener(view1 -> {
            sys.update("sys_light_status", !sys_light_status);
            light.update("status_system", !sys_light_status);
        });

//        binding.smartTv.setOnClickListener(view1 -> {
//            if (sys_tv_status) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SmartTVFragment);
//            }
//        });
//
//        binding.smartTvSwitch.setOnClickListener(view1 -> {
//            sys.update("sys_tv_status", !sys_tv_status);
//            tv.update("status_system", !sys_tv_status);
//        });
//
//        binding.microwave.setOnClickListener(view1 -> {
//            if (sys_microwave_status) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SmartMicrowaveFragment);
//            }
//        });
//
//        binding.microwaveSwitch.setOnClickListener(view1 -> {
//            sys.update("sys_microwave_status", !sys_microwave_status);
//        });
    }

    public void setButtonColor(Button button, Boolean state) {
        if (state){
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.on)));
        }
        else {
            button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.off)));
        }
    }

    public void setButtonNum(Button button, Number number) {
        button.setText(number.toString());
    }

    public void resetButton(Button button) {
        button.setText(R.string.zero);
    }

    public void updateSysStatus(DocumentReference sys, DocumentReference garage_door, Boolean sys_state) {
        sys.update("sys_fire_status", sys_state);
        sys.update("sys_garage_door_status", sys_state);
        sys.update("sys_light_status", sys_state);
        sys.update("sys_microwave_status", sys_state);
        sys.update("sys_tv_status", sys_state);

        garage_door.update("status_system", sys_state);
    }

    private void printAlert(Map<String, Object> task) {
        String text = Objects.requireNonNull(task.get("Alert1")).toString();
        String first;                                       // first word
        String rest;                                        // remainder

        int space = text.indexOf(';');                      // position of first space
        if (space == -1) {                                  // no space found
            first = text;
            rest  = "";                                     // nothing left
        } else {
            first = text.substring(0, space);
            rest  = text.substring(space + 1);              // skip the space itself
        }

        if(first.equals("High")) {
            space = rest.indexOf(';');                      // position of first space
            if (space == -1) {                                  // no space found
                first = rest;
                rest  = "";                                     // nothing left
            } else {
                first = rest.substring(0, space);
                rest  = rest.substring(space + 1);              // skip the space itself
            }
            showCenterAlert(first, rest);
        }
        else {
            Toast.makeText(requireContext(), rest, Toast.LENGTH_LONG).show();
        }
    }

    private void showCenterAlert(@NonNull String title, @NonNull String message) {
        new MaterialAlertDialogBuilder(requireContext())          // use getContext() in Activity
                .setTitle(title)                                   // optional
                .setMessage(message)
                .setCancelable(false)                                  // back-press won’t close it
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();                                  // *must* tap OK to close
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}


//
//        binding.Test2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Create a new user with a first and last name
//                Map<String, Object> user = new HashMap<>();
//                user.put("first", "Ada");
//                user.put("last", "Lovelace");
//                user.put("born", 1815);
//
//                // Add a new document with a generated ID
//                db.collection("HubSystem")
//                        .add(user)
//                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                            @Override
//                            public void onSuccess(DocumentReference documentReference) {
//                                Snackbar.make(view, "DocumentSnapshot added with ID: " + documentReference.getId(), Snackbar.LENGTH_LONG)
//                                        .setAnchorView(R.id.fab)
//                                        .setAction("Action", null).show();
//                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w(TAG, "Error adding document", e);
//                            }
//                        });
//            }
//        });
//
//        binding.Test3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                db.collection("HubSystem")
//                        .get()
//                        .addOnCompleteListener((OnCompleteListener<QuerySnapshot>) task -> {
//                            if (task.isSuccessful()) {
//                                StringBuilder log_message = new StringBuilder();
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                    Log.d(TAG, document.getId() + " => " + document.getData());
//                                    log_message.append(document.getId()).append(" => ").append(document.getData()).append('\n');
//                                }
//                                Snackbar.make(view, log_message, Snackbar.LENGTH_LONG)
//                                        .setAnchorView(R.id.fab)
//                                        .setAction("Action", null).show();
//                            } else {
//                                Log.w(TAG, "Error getting documents.", task.getException());
//                            }
//                        });
//            }
//        });
