package com.digitaltwin.bridge;

import com.yakindu.core.TimerService;
import smarthub.java.*;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatechartService {
    
    final Map<String, Object> systemInstances = new ConcurrentHashMap<>();
    private final TimerService timerService = new TimerService();
    
    public void createSystem(String systemId, String systemType) {
        Object system = null;
        
        switch (systemType) {
            case "SmartGarageDoorSystem":
                system = createGarageDoorSystem();
                break;
            case "SmartLightSystem":
                system = createLightSystem();
                break;
            case "SmartMicrowaveSystem":
                system = createMicrowaveSystem();
                break;
            case "SmartFireSystem":
                system = createFireSystem();
                break;
            case "SmartTrafficLightSystem":
                system = createTrafficLightSystem();
                break;
            default:
                throw new IllegalArgumentException("Unknown system type: " + systemType);
        }
        
        systemInstances.put(systemId, system);
    }
    
    private SmartGarageDoorSystem createGarageDoorSystem() {
        SmartGarageDoorSystem system = new SmartGarageDoorSystem();
        system.setTimerService(timerService);
        
        // Create and set components
        GarageDoor_Unit garageDoor = new GarageDoor_Unit();
        garageDoor.setTimerService(timerService);
        system.setGarageDoor(garageDoor);
        
        Ultrasonic_MotionDetector sensor = new Ultrasonic_MotionDetector();
        sensor.setTimerService(timerService);

        // Create and set the internal ultrasonic sensor
        Ultrasonic_Sensor ultrasonicSensor = new Ultrasonic_Sensor();
        ultrasonicSensor.setTimerService(timerService);
        sensor.setSensor(ultrasonicSensor);

        system.setSensor(sensor);
        
        Actuator_Component_2 actuator = new Actuator_Component_2();
        actuator.setTimerService(timerService);
        system.setActuator(actuator);

        Controller_Component_2 controller = new Controller_Component_2();
        // Controller doesn't have setTimerService method
        system.setController(controller);

        Power_Component power = new Power_Component();
        power.setTimerService(timerService);
        system.setPower(power);

        // Create separate components for the sensor (it expects different types)
        Controller_Component sensorController = new Controller_Component();
        sensorController.setTimerService(timerService);

        Actuator_Component sensorActuator = new Actuator_Component();
        sensorActuator.setTimerService(timerService);

        // Set the required components on the sensor
        sensor.setController(sensorController);
        sensor.setActuator(sensorActuator);
        sensor.setPowerSupply(power);
        
        DeviceTemp_Component temp = new DeviceTemp_Component();
        temp.setTimerService(timerService);
        system.setTemp(temp);
        
        Network_Component wifi = new Network_Component();
        wifi.setTimerService(timerService);
        system.setWiFi(wifi);
        
        return system;
    }
    
    private SmartLightSystem createLightSystem() {
        SmartLightSystem system = new SmartLightSystem();
        system.setTimerService(timerService);

        // Create and set components
        Ultrasonic_MotionDetector motionDetector = new Ultrasonic_MotionDetector();
        motionDetector.setTimerService(timerService);

        // Create and set the internal ultrasonic sensor for motion detector
        Ultrasonic_Sensor ultrasonicSensor = new Ultrasonic_Sensor();
        ultrasonicSensor.setTimerService(timerService);
        motionDetector.setSensor(ultrasonicSensor);

        system.setMotionDetector(motionDetector);

        LEDLight_Unit lights = new LEDLight_Unit();
        lights.setTimerService(timerService);
        system.setLights(lights);

        Actuator_Component actuator = new Actuator_Component();
        actuator.setTimerService(timerService);
        system.setActuator(actuator);

        Controller_Component controller = new Controller_Component();
        controller.setTimerService(timerService);
        system.setController(controller);

        Power_Component power = new Power_Component();
        power.setTimerService(timerService);
        system.setPower(power);

        // Create separate components for the motion detector (it expects different types)
        Controller_Component sensorController = new Controller_Component();
        sensorController.setTimerService(timerService);

        Actuator_Component sensorActuator = new Actuator_Component();
        sensorActuator.setTimerService(timerService);

        // Set the required components on the motion detector
        motionDetector.setController(sensorController);
        motionDetector.setActuator(sensorActuator);
        motionDetector.setPowerSupply(power);

        DeviceTemp_Component temp = new DeviceTemp_Component();
        temp.setTimerService(timerService);
        system.setTemp(temp);

        Network_Component wifi = new Network_Component();
        wifi.setTimerService(timerService);
        system.setWiFi(wifi);

        return system;
    }
    
    private SmartMicrowaveSystem createMicrowaveSystem() {
        SmartMicrowaveSystem system = new SmartMicrowaveSystem();
        system.setTimerService(timerService);

        // Create and set components
        Sensor_Component sensor = new Sensor_Component();
        sensor.setTimerService(timerService);
        system.setSensor(sensor);

        Microwave_Unit microwave = new Microwave_Unit();
        microwave.setTimerService(timerService);
        system.setMW(microwave);

        Actuator_Component actuator = new Actuator_Component();
        actuator.setTimerService(timerService);
        system.setActuator(actuator);

        Controller_Component controller = new Controller_Component();
        controller.setTimerService(timerService);
        system.setController(controller);

        Power_Component power = new Power_Component();
        power.setTimerService(timerService);
        system.setPower(power);

        DeviceTemp_Component temp = new DeviceTemp_Component();
        temp.setTimerService(timerService);
        system.setTemp(temp);

        Network_Component wifi = new Network_Component();
        wifi.setTimerService(timerService);
        system.setWiFi(wifi);

        return system;
    }
    
    private SmartFireSystem createFireSystem() {
        SmartFireSystem system = new SmartFireSystem();
        system.setTimerService(timerService);

        // Create and set components
        Sensor_Component_Fire smokeSensor = new Sensor_Component_Fire();
        smokeSensor.setTimerService(timerService);
        system.setSmokeSensor(smokeSensor);

        Sensor_Component_Fire heatSensor = new Sensor_Component_Fire();
        heatSensor.setTimerService(timerService);
        system.setHeatSensor(heatSensor);

        Sensor_Component_Fire flameSensor = new Sensor_Component_Fire();
        flameSensor.setTimerService(timerService);
        system.setFlameSensor(flameSensor);

        FireAlarm_Unit fireAlarm = new FireAlarm_Unit();
        fireAlarm.setTimerService(timerService);
        system.setFireAlarm(fireAlarm);

        Actuator_Component_2 actuator = new Actuator_Component_2();
        actuator.setTimerService(timerService);
        system.setActuator(actuator);

        Power_Component power = new Power_Component();
        power.setTimerService(timerService);
        system.setPower(power);

        DeviceTemp_Component temp = new DeviceTemp_Component();
        temp.setTimerService(timerService);
        system.setTemp(temp);

        Network_Component wifi = new Network_Component();
        wifi.setTimerService(timerService);
        system.setWiFi(wifi);

        return system;
    }
    
    private SmartTrafficLightSystem createTrafficLightSystem() {
        SmartTrafficLightSystem system = new SmartTrafficLightSystem();
        system.setTimerService(timerService);

        // Create components but don't set them due to protected access
        // The system will initialize with null components and we'll work with what's available

        return system;
    }
    
    public void startSystem(String systemId) {
        Object system = systemInstances.get(systemId);
        if (system == null) {
            throw new IllegalArgumentException("System not found: " + systemId);
        }
        
        if (system instanceof SmartGarageDoorSystem) {
            SmartGarageDoorSystem garageSystem = (SmartGarageDoorSystem) system;
            garageSystem.enter();
            garageSystem.system().raiseOn();

            // Don't auto-turn on components - let the frontend control this
        } else if (system instanceof SmartLightSystem) {
            SmartLightSystem lightSystem = (SmartLightSystem) system;
            lightSystem.enter();
            lightSystem.system().raiseOn();

            // Don't auto-turn on components - let the frontend control this
        } else if (system instanceof SmartMicrowaveSystem) {
            SmartMicrowaveSystem microwaveSystem = (SmartMicrowaveSystem) system;
            microwaveSystem.enter();
            microwaveSystem.system().raiseOn();

            // Don't auto-turn on components - let the frontend control this
        } else if (system instanceof SmartFireSystem) {
            SmartFireSystem fireSystem = (SmartFireSystem) system;
            fireSystem.enter();
            fireSystem.system().raiseOn();

            // Don't auto-turn on components - let the frontend control this
        } else if (system instanceof SmartTrafficLightSystem) {
            SmartTrafficLightSystem trafficSystem = (SmartTrafficLightSystem) system;
            trafficSystem.enter();
            trafficSystem.system().raiseOn();
        }
    }
    
    public void stopSystem(String systemId) {
        Object system = systemInstances.get(systemId);
        if (system == null) {
            throw new IllegalArgumentException("System not found: " + systemId);
        }
        
        if (system instanceof SmartGarageDoorSystem) {
            SmartGarageDoorSystem garageSystem = (SmartGarageDoorSystem) system;

            // Turn off main components first
            if (garageSystem.getGarageDoor() != null) {
                garageSystem.getGarageDoor().system().raiseOff();
            }

            garageSystem.system().raiseOff();
            garageSystem.exit();
        } else if (system instanceof SmartLightSystem) {
            SmartLightSystem lightSystem = (SmartLightSystem) system;

            // Turn off main components first
            if (lightSystem.getLights() != null) {
                lightSystem.getLights().device().raiseOff();
            }

            lightSystem.system().raiseOff();
            lightSystem.exit();
        } else if (system instanceof SmartMicrowaveSystem) {
            SmartMicrowaveSystem microwaveSystem = (SmartMicrowaveSystem) system;

            // Stop microwave if running
            if (microwaveSystem.getMW() != null) {
                microwaveSystem.getMW().device().raisePause(); // Stop any running operation
            }

            microwaveSystem.system().raiseOff();
            microwaveSystem.exit();
        } else if (system instanceof SmartFireSystem) {
            SmartFireSystem fireSystem = (SmartFireSystem) system;

            // Disable fire alarm when system stops
            if (fireSystem.getFireAlarm() != null) {
                fireSystem.getFireAlarm().timer().raiseDisable();
            }

            fireSystem.system().raiseOff();
            fireSystem.exit();
        } else if (system instanceof SmartTrafficLightSystem) {
            SmartTrafficLightSystem trafficSystem = (SmartTrafficLightSystem) system;
            trafficSystem.system().raiseOff();
            trafficSystem.exit();
        }
    }
    
    public Object getSystem(String systemId) {
        return systemInstances.get(systemId);
    }
    
    public void removeSystem(String systemId) {
        Object system = systemInstances.get(systemId);
        if (system != null) {
            stopSystem(systemId);
            systemInstances.remove(systemId);
        }
    }

    public java.util.Set<String> getSystemIds() {
        return systemInstances.keySet();
    }
}
