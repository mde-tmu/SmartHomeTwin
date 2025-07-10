#ifdef __IN_ECLIPSE__
//This is a automatic generated file
//Please do not modify this file
//If you touch this file your change will be overwritten during the next build
//This file has been generated on 2025-07-04 20:12:21

#include "Arduino.h"
#include "Arduino.h"
#include "Wifi.h"
#include <esp_now.h>
#include <esp_wifi.h>
#include <dhtESP32-rmt.h>
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Sensor_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Actuator_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Controller_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Actuator_Component_2.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Controller_Component_2.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/DeviceTemp_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Network_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Power_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/SmartFireSystem.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Sensor_Component_Fire.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/FireAlarm_Unit.h"
#include "timer_functions.h"
#include "configuration.h"
#include <Wire.h>
#include <INA226_WE.h>

int extractInt(const char* str) ;
bool extractBool(const char* str) ;
float extractFloat(const char* str) ;
void onDataSent(const uint8_t *mac_addr, esp_now_send_status_t status) ;
void onDataRecv(const esp_now_recv_info_t *info, const uint8_t *data, int len) ;
void sendDataToHub(const String& path, const String& value, const String& type) ;
void handleAlarmOverrideButton();
void checkIsTriggered() ;
void checkSensorAndProcess(String sensor);
void fireAlarm() ;
void systemPinsInit() ;
void generalStateChartInit() ;
void fireAlarmSystemInit() ;
void startAllSystems() ;
void setupESPNow() ;
void checkCurrent() ;
void setup() ;
void loop() ;

#include "Arduino_Setup.ino"


#endif
