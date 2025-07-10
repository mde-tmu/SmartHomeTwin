package com.digitaltwin.bridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarthub.java.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statechart")
@CrossOrigin(origins = "*")
public class StatechartController {
    
    @Autowired
    private StatechartService statechartService;

    @Autowired
    private WebSocketService webSocketService;
    
    @PostMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/error")
    public ResponseEntity<String> error() {
        throw new RuntimeException("Test error");
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeAllSystems() {
        try {
            // Define all available system types
            String[] systemTypes = {
                "SmartGarageDoorSystem",
                "SmartLightSystem",
                "SmartMicrowaveSystem",
                "SmartFireSystem",
                "SmartTrafficLightSystem"
            };

            Map<String, Object> results = new HashMap<>();
            Map<String, Object> createdSystems = new HashMap<>();

            // Create and start each system type
            for (String systemType : systemTypes) {
                try {
                    String systemId = systemType + "_1"; // Use a default ID pattern

                    // Create the system
                    statechartService.createSystem(systemId, systemType);

                    // Start the system (calls enter() and raiseOn())
                    statechartService.startSystem(systemId);

                    // Get the system state
                    Map<String, Object> systemState = getSystemStateInternal(systemId);
                    createdSystems.put(systemId, systemState);

                } catch (Exception e) {
                    // Log the error but continue with other systems
                    createdSystems.put(systemType, Map.of("error", e.getMessage()));
                }
            }

            results.put("success", true);
            results.put("message", "Systems initialization completed");
            results.put("systems", createdSystems);
            results.put("totalSystems", createdSystems.size());

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/systems/{systemId}")
    public ResponseEntity<Map<String, Object>> createSystem(
            @PathVariable String systemId,
            @RequestParam String systemType) {
        try {
            statechartService.createSystem(systemId, systemType);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "System created successfully");
            response.put("systemId", systemId);
            response.put("systemType", systemType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/systems/{systemId}/start")
    public ResponseEntity<Map<String, Object>> startSystem(@PathVariable String systemId) {
        try {
            statechartService.startSystem(systemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "System started successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/systems/{systemId}/stop")
    public ResponseEntity<Map<String, Object>> stopSystem(@PathVariable String systemId) {
        try {
            statechartService.stopSystem(systemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "System stopped successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/systems/{systemId}")
    public ResponseEntity<Map<String, Object>> removeSystem(@PathVariable String systemId) {
        try {
            statechartService.removeSystem(systemId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "System removed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/systems/{systemId}/runCycle")
    public ResponseEntity<Map<String, Object>> runCycle(@PathVariable String systemId) {
        try {
            Object system = statechartService.getSystem(systemId);
            if (system == null) {
                throw new IllegalArgumentException("System not found: " + systemId);
            }
            
            // Run cycle based on system type
            if (system instanceof SmartGarageDoorSystem) {
                ((SmartGarageDoorSystem) system).triggerWithoutEvent();
            } else if (system instanceof SmartLightSystem) {
                ((SmartLightSystem) system).triggerWithoutEvent();
            } else if (system instanceof SmartMicrowaveSystem) {
                ((SmartMicrowaveSystem) system).triggerWithoutEvent();
            } else if (system instanceof SmartFireSystem) {
                ((SmartFireSystem) system).triggerWithoutEvent();
            } else if (system instanceof SmartTrafficLightSystem) {
                ((SmartTrafficLightSystem) system).triggerWithoutEvent();
            }
            
            // Get updated system state and broadcast it
            Map<String, Object> systemState = getSystemStateInternal(systemId);
            webSocketService.broadcastSystemStateChange(systemId, systemState);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cycle executed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/systems/{systemId}/setEnvironmentData")
    public ResponseEntity<Map<String, Object>> setEnvironmentData(
            @PathVariable String systemId,
            @RequestParam String sensorType,
            @RequestParam long value) {
        try {
            Object system = statechartService.getSystem(systemId);
            if (system == null) {
                throw new IllegalArgumentException("System not found: " + systemId);
            }
            
            // Set environment data based on system and sensor type
            if (system instanceof SmartGarageDoorSystem) {
                SmartGarageDoorSystem garageSystem = (SmartGarageDoorSystem) system;
                if (("UltraSonic_Sensor".equals(sensorType) || "ultrasonic".equals(sensorType)) && garageSystem.getSensor() != null) {
                    garageSystem.getSensor().setEnvironmentData(value);
                    garageSystem.getSensor().setSensorData(value);
                }
            } else if (system instanceof SmartLightSystem) {
                SmartLightSystem lightSystem = (SmartLightSystem) system;
                if ("motion".equals(sensorType) && lightSystem.getMotionDetector() != null) {
                    lightSystem.getMotionDetector().setEnvironmentData(value);
                }
            } else if (system instanceof SmartMicrowaveSystem) {
                SmartMicrowaveSystem microwaveSystem = (SmartMicrowaveSystem) system;
                if ("door".equals(sensorType) && microwaveSystem.getSensor() != null) {
                    microwaveSystem.getSensor().setEnvironmentData(value);
                }
            } else if (system instanceof SmartFireSystem) {
                SmartFireSystem fireSystem = (SmartFireSystem) system;
                if ("smoke".equals(sensorType) && fireSystem.getSmokeSensor() != null) {
                    fireSystem.getSmokeSensor().setEnvironmentData(value);
                } else if ("temperature".equals(sensorType) && fireSystem.getHeatSensor() != null) {
                    fireSystem.getHeatSensor().setEnvironmentData(value);
                } else if ("flame".equals(sensorType) && fireSystem.getFlameSensor() != null) {
                    fireSystem.getFlameSensor().setEnvironmentData(value);
                }
            } else if (system instanceof SmartTrafficLightSystem) {
                // Traffic light system sensor access is protected, skip for now
                System.out.println("Traffic light sensor update not implemented due to protected access");
            }

            // Run a cycle to process the sensor data
            runCycle(systemId);

            // Get updated system state and broadcast it
            Map<String, Object> systemState = getSystemStateInternal(systemId);
            webSocketService.broadcastSystemStateChange(systemId, systemState);
            System.out.println("State: " + systemState);

            
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Environment data set successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/systems/{systemId}/raiseEvent")
    public ResponseEntity<Map<String, Object>> raiseEvent(
            @PathVariable String systemId,
            @RequestParam String componentName,
            @RequestParam String eventName) {
        try {
            Object system = statechartService.getSystem(systemId);
            if (system == null) {
                throw new IllegalArgumentException("System not found: " + systemId);
            }
            
            // Raise events based on system type and component
            if (system instanceof SmartGarageDoorSystem) {
                SmartGarageDoorSystem garageSystem = (SmartGarageDoorSystem) system;
                if ("GarageDoor_Unit".equals(componentName) && garageSystem.getGarageDoor() != null) {
                    if ("raiseOpen_door".equals(eventName)) {
                        garageSystem.getGarageDoor().door().raiseOpen_door();
                    } else if ("raiseClose_door".equals(eventName)) {
                        garageSystem.getGarageDoor().door().raiseClose_door();
                    } else if ("raiseOn".equals(eventName)) {
                        garageSystem.getGarageDoor().system().raiseOn();
                    } else if ("raiseOff".equals(eventName)) {
                        garageSystem.getGarageDoor().system().raiseOff();
                    }
                }
            } else if (system instanceof SmartLightSystem) {
                SmartLightSystem lightSystem = (SmartLightSystem) system;
                if ("LEDLight_Unit".equals(componentName) && lightSystem.getLights() != null) {
                    if ("raiseOn".equals(eventName)) {
                        lightSystem.getLights().device().raiseOn();
                    } else if ("raiseOff".equals(eventName)) {
                        lightSystem.getLights().device().raiseOff();
                    }
                }
            } else if (system instanceof SmartMicrowaveSystem) {
                SmartMicrowaveSystem microwaveSystem = (SmartMicrowaveSystem) system;
                if ("Microwave_Unit".equals(componentName) && microwaveSystem.getMW() != null) {
                    if ("raiseStart".equals(eventName)) {
                        microwaveSystem.getMW().device().raiseStart();
                    } else if ("raisePause".equals(eventName)) {
                        microwaveSystem.getMW().device().raisePause();
                    }
                }
            } else if (system instanceof SmartFireSystem) {
                SmartFireSystem fireSystem = (SmartFireSystem) system;
                if ("FireAlarm_Unit".equals(componentName) && fireSystem.getFireAlarm() != null) {
                    if ("raiseEnable".equals(eventName)) {
                        fireSystem.getFireAlarm().timer().raiseEnable();
                    } else if ("raiseDisable".equals(eventName)) {
                        fireSystem.getFireAlarm().timer().raiseDisable();
                    }
                }
            } else if (system instanceof SmartTrafficLightSystem) {
                // Traffic light system access is protected, skip for now
                System.out.println("Traffic light event raising not implemented due to protected access");
            }
            
            // Get updated system state and broadcast it
            Map<String, Object> systemState = getSystemStateInternal(systemId);
            webSocketService.broadcastSystemStateChange(systemId, systemState);

            // Also broadcast the specific event
            Map<String, Object> eventData = Map.of(
                "componentName", componentName,
                "eventName", eventName
            );
            webSocketService.broadcastSystemEvent(systemId, "EVENT_RAISED", eventData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Event raised successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    //get all systems
    @GetMapping("/systems")
    public ResponseEntity<Map<String, Object>> getAllSystems() {
        try {
            Map<String, Object> systems = new HashMap<>();
            for (String systemId : statechartService.getSystemIds()) {
                systems.put(systemId, getSystemStateInternal(systemId));
            }
            return ResponseEntity.ok(systems);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/systems/{systemId}/state")
    public ResponseEntity<Map<String, Object>> getSystemState(@PathVariable String systemId) {
        try {
            Map<String, Object> state = getSystemStateInternal(systemId);
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private Map<String, Object> getSystemStateInternal(String systemId) {
        Object system = statechartService.getSystem(systemId);
        if (system == null) {
            throw new IllegalArgumentException("System not found: " + systemId);
        }

        Map<String, Object> state = new HashMap<>();
        state.put("systemId", systemId);
        state.put("isActive", true);

            // Extract state based on system type
            if (system instanceof SmartGarageDoorSystem) {
                SmartGarageDoorSystem garageSystem = (SmartGarageDoorSystem) system;
                Map<String, Object> components = new HashMap<>();

                if (garageSystem.getGarageDoor() != null) {
                    Map<String, Object> garageDoor = new HashMap<>();
                    garageDoor.put("isOn", garageSystem.getGarageDoor().system().getIsOn());
                    garageDoor.put("door_status", garageSystem.getGarageDoor().system().getDoor_status());
                    garageDoor.put("door_closed", garageSystem.getGarageDoor().door().getDoor_closed());
                    garageDoor.put("block", garageSystem.getGarageDoor().door().getBlock());
                    garageDoor.put("current_open_time", garageSystem.getGarageDoor().door().getCurrent_open_time());
                    components.put("GarageDoor_Unit", garageDoor);
                }

                if (garageSystem.getSensor() != null) {
                    Map<String, Object> sensor = new HashMap<>();
                    sensor.put("activity", garageSystem.getSensor().status().getActivity());
                    sensor.put("environmentData", garageSystem.getSensor().getEnvironmentData());
                    sensor.put("sensorData", garageSystem.getSensor().getSensorData());
                    components.put("UltraSonic_Sensor", sensor);
                }

                if (garageSystem.getPower() != null) {
                    Map<String, Object> power = new HashMap<>();
                    power.put("isOn", garageSystem.getPower().device().getIsOn());
                    power.put("kWh", garageSystem.getPower().getKWh());
                    components.put("Power_Component", power);
                }

                state.put("components", components);
                state.put("message", garageSystem.getMessage());
                state.put("wiFi_connection", garageSystem.getWiFi_connection());
                state.put("temp_value", garageSystem.getTemp_value());
                state.put("power_total", garageSystem.getPower_total());

            } else if (system instanceof SmartLightSystem) {
                SmartLightSystem lightSystem = (SmartLightSystem) system;
                Map<String, Object> components = new HashMap<>();

                if (lightSystem.getLights() != null) {
                    Map<String, Object> lights = new HashMap<>();
                    lights.put("isOn", lightSystem.getLights().device().getIsOn());
                    lights.put("brightness", lightSystem.getLights().brightness().getLevel());
                    components.put("LEDLight_Unit", lights);
                }

                if (lightSystem.getMotionDetector() != null) {
                    Map<String, Object> motion = new HashMap<>();
                    motion.put("activity", lightSystem.getMotionDetector().status().getActivity());
                    motion.put("environmentData", lightSystem.getMotionDetector().getEnvironmentData());
                    motion.put("sensorData", lightSystem.getMotionDetector().getSensorData());
                    components.put("Motion_Sensor", motion);
                }

                state.put("components", components);
                state.put("message", lightSystem.getMessage());

            } else if (system instanceof SmartMicrowaveSystem) {
                SmartMicrowaveSystem microwaveSystem = (SmartMicrowaveSystem) system;
                Map<String, Object> components = new HashMap<>();

                if (microwaveSystem.getMW() != null) {
                    Map<String, Object> microwave = new HashMap<>();
                    microwave.put("isOn", microwaveSystem.getMW().device().getIsOn());
                    microwave.put("timer", microwaveSystem.getMW().device().getTimer());
                    microwave.put("in_use", microwaveSystem.getMW().getIn_use());
                    components.put("Microwave_Unit", microwave);
                }

                state.put("components", components);
                state.put("message", microwaveSystem.getMessage());

            } else if (system instanceof SmartFireSystem) {
                SmartFireSystem fireSystem = (SmartFireSystem) system;
                Map<String, Object> components = new HashMap<>();

                if (fireSystem.getFireAlarm() != null) {
                    Map<String, Object> alarm = new HashMap<>();
                    alarm.put("activated", fireSystem.getFireAlarm().alarm().getActivated());
                    alarm.put("threshold_reached", fireSystem.getFireAlarm().alarm().getThreshold_reached());
                    alarm.put("triggerSignal_received", fireSystem.getFireAlarm().sensors().getTriggerSignal_received());
                    components.put("FireAlarm_Unit", alarm);
                }

                if (fireSystem.getSmokeSensor() != null) {
                    Map<String, Object> smoke = new HashMap<>();
                    smoke.put("environmentData", fireSystem.getSmokeSensor().getEnvironmentData());
                    smoke.put("sensorData", fireSystem.getSmokeSensor().getSensorData());
                    smoke.put("threshold", fireSystem.getSmokeSensor().getThreshold());
                    smoke.put("triggered", fireSystem.getSmokeSensor().getSensorData() > fireSystem.getSmokeSensor().getThreshold());
                    components.put("Smoke_Sensor", smoke);
                }

                if (fireSystem.getHeatSensor() != null) {
                    Map<String, Object> heat = new HashMap<>();
                    heat.put("environmentData", fireSystem.getHeatSensor().getEnvironmentData());
                    heat.put("sensorData", fireSystem.getHeatSensor().getSensorData());
                    heat.put("threshold", fireSystem.getHeatSensor().getThreshold());
                    heat.put("triggered", fireSystem.getHeatSensor().getSensorData() > fireSystem.getHeatSensor().getThreshold());
                    components.put("Heat_Sensor", heat);
                }

                if (fireSystem.getFlameSensor() != null) {
                    Map<String, Object> flame = new HashMap<>();
                    flame.put("environmentData", fireSystem.getFlameSensor().getEnvironmentData());
                    flame.put("sensorData", fireSystem.getFlameSensor().getSensorData());
                    flame.put("threshold", fireSystem.getFlameSensor().getThreshold());
                    flame.put("triggered", fireSystem.getFlameSensor().getSensorData() > fireSystem.getFlameSensor().getThreshold());
                    components.put("Flame_Sensor", flame);
                }

                state.put("components", components);
                state.put("message", fireSystem.getMessage());
                state.put("alarm_status", fireSystem.system().getAlarm_status());

            } else if (system instanceof SmartTrafficLightSystem) {
                SmartTrafficLightSystem trafficSystem = (SmartTrafficLightSystem) system;
                Map<String, Object> components = new HashMap<>();

                // Traffic light system components are protected, so we can only access system-level data
                state.put("components", components);
                state.put("message", trafficSystem.getMessage());
                state.put("light_status", trafficSystem.system().getLight_status());
                state.put("crossing_status", trafficSystem.system().getCrossing_status());
                state.put("timer", trafficSystem.system().getTimer());
            }

            return state;
    }
}
