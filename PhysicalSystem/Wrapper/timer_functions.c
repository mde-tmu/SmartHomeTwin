#include "Arduino.h"
#include "STL4IoT-V3/iot-sc-template-library/src/sc_types.h"
#include "timer_functions.h"

// Timer structure to track timer information
typedef struct {
    void* handle;              // Handle to the state machine
    sc_eventid evid;           // Event ID to raise when timer expires
    sc_integer time_ms;        // Timer duration in milliseconds
    sc_boolean periodic;       // Whether the timer is periodic
    unsigned long start_time;  // When the timer was started
    sc_boolean active;         // Whether the timer is active
} Timer;

// Maximum number of timers that can be active at once
#define MAX_TIMERS 20

// Array to store active timers
static Timer timers[MAX_TIMERS];

// Initialize timers
void init_timers() {
    for (int i = 0; i < MAX_TIMERS; i++) {
        timers[i].active = false;
    }
}


// Find an available timer slot
static int find_available_timer() {
    for (int i = 0; i < MAX_TIMERS; i++) {
        if (!timers[i].active) {
            return i;
        }
    }
    return -1; // No available slots
}

// Find a timer by handle and event ID
static int find_timer(void* handle, const sc_eventid evid) {
    for (int i = 0; i < MAX_TIMERS; i++) {
        if (timers[i].active && timers[i].handle == handle && timers[i].evid == evid) {
            return i;
        }
    }
    return -1; // Timer not found
}

// Process all active timers - call this in the main loop
void process_timers() {
    unsigned long current_time = millis();

    for (int i = 0; i < MAX_TIMERS; i++) {
        if (timers[i].active) {
            // Check if timer has expired
            if (current_time - timers[i].start_time >= timers[i].time_ms) {
                // Raise the event
                void (*raise_event)(void*, const sc_eventid) = (void (*)(void*, const sc_eventid))timers[i].handle;
                raise_event(timers[i].handle, timers[i].evid);

                if (timers[i].periodic) {
                    // Reset the start time for periodic timers
                    timers[i].start_time = current_time;
                } else {
                    // Deactivate non-periodic timers
                    timers[i].active = false;
                }
            }
        }
    }

    // Yield to avoid watchdog timeout
    yield();
}



void setTimer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    int timer_index = find_timer(handle, evid);

    // If timer doesn't exist, find an available slot
    if (timer_index == -1) {
        timer_index = find_available_timer();
        if (timer_index == -1) {
            // No available timer slots
//        	Serial.println("Error: No available timer slots");
            return;
        }
    }

    // Set up the timer
    timers[timer_index].handle = handle;
    timers[timer_index].evid = evid;
    timers[timer_index].time_ms = time_ms;
    timers[timer_index].periodic = periodic;
    timers[timer_index].start_time = millis();
    timers[timer_index].active = true;
}

void unsetTimer (void* handle, const sc_eventid evid) {
    int timer_index = find_timer(handle, evid);

    // If timer exists, deactivate it
    if (timer_index != -1) {
        timers[timer_index].active = false;
    }
}

// Timer functions for SmartLightSystem
void smartLightSystem_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void smartLightSystem_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Actuator_Component
void actuator_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void actuator_Component_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for LEDLight_Unit
void lEDLight_Unit_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // For Arduino, we can use millis() to track time
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void lEDLight_Unit_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Controller_Component
void controller_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void controller_Component_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Network_Component
void network_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void network_Component_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Power_Component
void power_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void power_Component_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Ultrasonic_MotionDetector
void ultrasonic_MotionDetector_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void ultrasonic_MotionDetector_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Ultrasonic_Sensor
void ultrasonic_Sensor_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void ultrasonic_Sensor_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for SmartGarageDoorSystem
void smartGarageDoorSystem_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void smartGarageDoorSystem_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for GarageDoor_Unit
void garageDoor_Unit_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );

}

void garageDoor_Unit_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for SmartFireSystem
void smartFireSystem_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void smartFireSystem_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for FireAlarm_Unit
void fireAlarm_Unit_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void fireAlarm_Unit_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Sensor_Component_Fire
void sensor_Component_Fire_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void sensor_Component_Fire_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Actuator_Component_2
void actuator_Component_2_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );

}

void actuator_Component_2_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Controller_Component_2
void controller_Component_2_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void controller_Component_2_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for DeviceTemp_Component
void deviceTemp_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void deviceTemp_Component_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Sensor_Component
void sensor_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void sensor_Component_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}

// Timer functions for Sensor_Component
void smartLightHUB_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic) {
    // Implementation for setting a timer
    // This is a stub implementation
	setTimer(handle, evid, time_ms, periodic );
}

void smartLightHUB_unset_timer(void* handle, const sc_eventid evid) {
    // Implementation for unsetting a timer
    // This is a stub implementation
	unsetTimer(handle,evid);
}
