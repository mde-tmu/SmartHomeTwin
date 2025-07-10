#ifndef TIMER_FUNCTIONS_H
#define TIMER_FUNCTIONS_H


#ifdef __cplusplus
extern "C" {
#endif

#include "STL4IoT-V3/iot-sc-template-library/src/sc_types.h"

void init_timers();
void process_timers();
//void print_timer_status();

// Timer functions for SmartLightSystem
void smartLightSystem_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void smartLightSystem_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Actuator_Component
void actuator_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void actuator_Component_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for LEDLight_Unit
void lEDLight_Unit_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void lEDLight_Unit_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Controller_Component
void controller_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void controller_Component_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Network_Component
void network_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void network_Component_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Power_Component
void power_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void power_Component_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Ultrasonic_MotionDetector
void ultrasonic_MotionDetector_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void ultrasonic_MotionDetector_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Ultrasonic_Sensor
void ultrasonic_Sensor_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void ultrasonic_Sensor_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for SmartGarageDoorSystem
void smartGarageDoorSystem_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void smartGarageDoorSystem_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for GarageDoor_Unit
void garageDoor_Unit_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void garageDoor_Unit_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for SmartFireSystem
void smartFireSystem_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void smartFireSystem_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for FireAlarm_Unit
void fireAlarm_Unit_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void fireAlarm_Unit_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Sensor_Component_Fire
void sensor_Component_Fire_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void sensor_Component_Fire_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Actuator_Component_2
void actuator_Component_2_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void actuator_Component_2_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Controller_Component_2
void controller_Component_2_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void controller_Component_2_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for DeviceTemp_Component
void deviceTemp_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void deviceTemp_Component_unset_timer(void* handle, const sc_eventid evid);

// Timer functions for Sensor_Component
void sensor_Component_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void sensor_Component_unset_timer(void* handle, const sc_eventid evid);

void smartLightHUB_set_timer(void* handle, const sc_eventid evid, const sc_integer time_ms, const sc_boolean periodic);
void smartLightHUB_unset_timer(void* handle, const sc_eventid evid);

#ifdef __cplusplus
}
#endif


#endif /* TIMER_FUNCTIONS_H */
