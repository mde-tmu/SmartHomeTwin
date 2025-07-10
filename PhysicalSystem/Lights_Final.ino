#include "Arduino.h"
#include "Wifi.h"
#include <esp_now.h>
#include <esp_wifi.h>
#include <Wire.h>
#include <INA226_WE.h>
#include "timer_functions.h"


#include "STL4IoT-V3/iot-sc-template-library/src-gen/SmartLightSystem.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/LEDLight_Unit.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/SmartLightHub.h"

#include "STL4IoT-V3/iot-sc-template-library/src-gen/Actuator_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Controller_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/DeviceTemp_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Network_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Power_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Ultrasonic_Sensor.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Ultrasonic_MotionDetector.h"



#define LDR_THRESHOLD 1800
#define CURRENT_SDA 18
#define CURRENT_SCL 19
static SmartLightHUB LightsHub;

//Lights 1

static SmartLightSystem lights1_System;

static Ultrasonic_Sensor lights1_MotionSensor;
static Ultrasonic_MotionDetector lights1_MotionDetector;
static Actuator_Component lights1_Actuator_Motion;
static Controller_Component lights1_Controller_Motion;

static Actuator_Component lights1_Actuator;
static Controller_Component lights1_Controller;

static DeviceTemp_Component lights1_deviceTemp;
static Network_Component lights1_network;
static Power_Component lights1_power;

static LEDLight_Unit lights1_led;

//Lights 2

static SmartLightSystem lights2_System;

static Ultrasonic_Sensor lights2_MotionSensor;
static Ultrasonic_MotionDetector lights2_MotionDetector;
static Actuator_Component lights2_Actuator_Motion;
static Controller_Component lights2_Controller_Motion;

static Actuator_Component lights2_Actuator;
static Controller_Component lights2_Controller;

static DeviceTemp_Component lights2_deviceTemp;
static Network_Component lights2_network;
static Power_Component lights2_power;

static LEDLight_Unit lights2_led;

//Lights 3

static SmartLightSystem lights3_System;

static Ultrasonic_Sensor lights3_MotionSensor;
static Ultrasonic_MotionDetector lights3_MotionDetector;
static Actuator_Component lights3_Actuator_Motion;
static Controller_Component lights3_Controller_Motion;

static Actuator_Component lights3_Actuator;
static Controller_Component lights3_Controller;

static DeviceTemp_Component lights3_deviceTemp;
static Network_Component lights3_network;
static Power_Component lights3_power;

static LEDLight_Unit lights3_led;

bool motionOn[3] = {false, false, false};
unsigned long lastMotionOn[3] = {0,0,0};

INA226_WE ina226(0x40);

const int pirPins[3] = {14, 27, 33};
const int ldrPins[3] = {34, 35, 32};
const int lightPins[3] = {25, 26, 22};

//uint8_t peerMac[] = {0x08, 0xA6, 0xF7, 0xA8, 0x67, 0x00};
uint8_t peerMac[] = {0x10, 0x52, 0x1C, 0x67, 0xB5, 0x9C};

//String inputBuffer;
//
//bool buzzerActive = false, isDoorOpen = false, isDoorMoving = false;
//bool prevDoorBtn = HIGH;
//unsigned long garageDoorOpenTime = 0;
static unsigned long lastPoll = 0;
//int currentButtonState;
//unsigned long lastDebounceTime = 0;
//unsigned long debounceDelay = 50;
bool checkedSensor = true;

// Function to extract an integer value from the string
int extractInt(const char* str) {
  return atoi(str);
}

// Function to extract a boolean value from the string
bool extractBool(const char* str) {

  Serial.print("TEST DEVICE CHANG STRING: ");
  Serial.print(str);
  return (strcmp(str, "true") == 0);
}

// Function to extract a float value from the string
float extractFloat(const char* str) {
  return atof(str);
}

void sendDataToHub(const String& path, const String& value, const String& type);
void onDataSent(const uint8_t *mac_addr, esp_now_send_status_t status) {
  Serial.print("Send Status: ");
  Serial.println(status == ESP_NOW_SEND_SUCCESS ? "Success" : "Fail");
}

void onDataRecv(const esp_now_recv_info_t *info, const uint8_t *data, int len) {
  char macStr[18];
  snprintf(macStr, sizeof(macStr), "%02X:%02X:%02X:%02X:%02X:%02X",
           info->src_addr[0], info->src_addr[1], info->src_addr[2],
           info->src_addr[3], info->src_addr[4], info->src_addr[5]);

  String val = "";
  for (int i = 0; i < len; i++) {
    val += (char)data[i];
  }
  Serial.print("Received: ");
  Serial.println(val);

  String key1 = "Light1_status";
  String key2 = "Light2_status";
  String key3 = "Light3_status";
  if (val.indexOf(key1) != -1) {
      bool light1Status = extractBool(val.substring(val.indexOf("status_door") + key1.length()+1, val.indexOf(",", val.indexOf(key1))).c_str());
      LEDLight_Unit* light1 = smartLightSystem_get_lights(&lights1_System);
      Actuator_Component* actuator1 =  smartLightSystem_get_actuator(&lights1_System);
      if (light1Status){
    	  Serial.print("TEST DEVICE CHANG: ");
    	  Serial.print(light1Status);
    	  lEDLight_Unit_device_raise_on(light1);
    	  actuator_Component_set_isTriggered(actuator1, true);
      } else {
    	  lEDLight_Unit_device_raise_off(light1);
    	  actuator_Component_set_isTriggered(actuator1, false);
      }
   } else if (val.indexOf(key2) != -1) {
	  bool light2Status = extractBool(val.substring(val.indexOf("status_door") + key2.length()+1, val.indexOf(",", val.indexOf(key2))).c_str());
	  LEDLight_Unit* light2 = smartLightSystem_get_lights(&lights2_System);
	  Actuator_Component* actuator2 =  smartLightSystem_get_actuator(&lights2_System);
	  if (light2Status){
		  lEDLight_Unit_device_raise_on(light2);
		  actuator_Component_set_isTriggered(actuator2, true);
	  } else {
		  lEDLight_Unit_device_raise_off(light2);
		  actuator_Component_set_isTriggered(actuator2, false);
	  }
   } else if (val.indexOf(key3) != -1) {
		  bool light3Status = extractBool(val.substring(val.indexOf("status_door") + key3.length()+1, val.indexOf(",", val.indexOf(key3))).c_str());
		  LEDLight_Unit* light3 = smartLightSystem_get_lights(&lights2_System);
		  Actuator_Component* actuator3 =  smartLightSystem_get_actuator(&lights2_System);
		  if (light3Status){
			  lEDLight_Unit_device_raise_on(light3);
			  actuator_Component_set_isTriggered(actuator3, true);
		  } else {
			  lEDLight_Unit_device_raise_off(light3);
			  actuator_Component_set_isTriggered(actuator3, false);
		  }
	   }

  Serial.write(data, len);
  Serial.println();
}

void checkMotionAndTrigger(int LightId){
	SmartLightSystem selectedSystem;

	if (LightId == 1){
		selectedSystem = lights1_System;
	} else if (LightId == 2){
		selectedSystem = lights2_System;
	} else if (LightId == 3){
		selectedSystem = lights3_System;
	}

	LEDLight_Unit* light = smartLightSystem_get_lights(&selectedSystem);
	Actuator_Component* actuator =  smartLightSystem_get_actuator(&selectedSystem);

//	Ultrasonic_Sensor ultraSonic = ultrasonic_MotionDetector_get_sensor(smartLightSystem_get_motionDetector(&selectedSystem));

    int motion = digitalRead(pirPins[LightId-1]);

    Serial.print("Motion Sensor Value: ");
    Serial.print(LightId);
    Serial.print(" ");
    Serial.println(motion);

    if (motion){
//    	ultrasonic_Sensor_set_environmentData(&ultraSonic);
    	lEDLight_Unit_device_raise_on(light);
    	actuator_Component_set_isTriggered(actuator, true);
    	motionOn[LightId-1] = true;
    	lastMotionOn[LightId-1] = millis();
    }
}

void autoShutOffLight(){
	for (int i = 0; i <3; i ++){
		if (motionOn[i]){
			if (millis() - lastMotionOn[i] > 5000){
				lastMotionOn[i] = millis();
				digitalWrite(lightPins[i], LOW);
				sendDataToHub("/SmartHomeSystem/SmartLightSystem/Light" + String(i + 1) + "_status", "false", "bool");
				if (i == 0){
					lEDLight_Unit_device_raise_off(&lights1_led);
					actuator_Component_set_isTriggered(smartLightSystem_get_actuator(&lights1_System), false);
				} else if (i==1){
					lEDLight_Unit_device_raise_off(&lights2_led);
					actuator_Component_set_isTriggered(smartLightSystem_get_actuator(&lights2_System), false);
				} else if (i==2){
					lEDLight_Unit_device_raise_off(&lights3_led);
					actuator_Component_set_isTriggered(smartLightSystem_get_actuator(&lights3_System), false);
				}
			}
		}
	}
}

void checkLightSensor(int LightId) {
	int room1LightSense = analogRead(ldrPins[LightId-1]);
	Serial.print("LDR Sensor Value: ");
	Serial.println(room1LightSense);
	sendDataToHub("/SmartHomeSystem/SmartLightSystem/Light" + String(LightId) + "_brightness", String(room1LightSense), "int");
	if (room1LightSense >  LDR_THRESHOLD) {
		checkMotionAndTrigger(LightId);
	}
	delay(1000);
}

void operateLight() {
	Serial.print("LIGHT STATUS 1: ");
	Serial.print(actuator_Component_get_isTriggered(smartLightSystem_get_actuator(&lights1_System)));
	Serial.print("LIGHT STATUS 2: ");
	Serial.print(actuator_Component_get_isTriggered(smartLightSystem_get_actuator(&lights2_System)));
	Serial.print("LIGHT STATUS 3: ");
	Serial.print(actuator_Component_get_isTriggered(smartLightSystem_get_actuator(&lights3_System)));
	if (actuator_Component_get_isTriggered(smartLightSystem_get_actuator(&lights1_System))){
		digitalWrite(lightPins[0], HIGH);
		sendDataToHub("/SmartHomeSystem/SmartLightSystem/Light1_status", "true", "bool");
	} else {
		sendDataToHub("/SmartHomeSystem/SmartLightSystem/Light1_status", "false", "bool");
		digitalWrite(lightPins[0], LOW);
	}
	if (actuator_Component_get_isTriggered(smartLightSystem_get_actuator(&lights2_System))){
		sendDataToHub("/SmartHomeSystem/SmartLightSystem/Light2_status", "true", "bool");
		digitalWrite(lightPins[1], HIGH);
	} else {
		sendDataToHub("/SmartHomeSystem/SmartLightSystem/Light2_status", "false", "bool");
		digitalWrite(lightPins[1], LOW);
	}
	if (actuator_Component_get_isTriggered(smartLightSystem_get_actuator(&lights3_System))){
		sendDataToHub("/SmartHomeSystem/SmartLightSystem/Light3_status", "true", "bool");
		digitalWrite(lightPins[2], HIGH);
	} else {
		sendDataToHub("/SmartHomeSystem/SmartLightSystem/Light3_status", "false", "bool");
		digitalWrite(lightPins[2], LOW);
	}

}

void sendDataToHub(const String& path, const String& value, const String& type) {
  String json = "{\"path\":\"" + path + "\",\"value\":\"" + value + "\",\"type\":\"" + type + "\"}";

  esp_err_t result = esp_now_send(peerMac, (uint8_t *)json.c_str(), json.length());

  if (result == ESP_OK) {
    Serial.println("✅ Message sent: " + json);
  } else {
    Serial.println("❌ Send error");
  }
}

void systemPinsInit() {
	Serial.print("[BOOT] 1 Reason: ");
	Serial.println(esp_reset_reason());

	for (int i = 0; i < 3; i++) {
		pinMode(pirPins[i], INPUT);
		pinMode(lightPins[i], OUTPUT);
		digitalWrite(lightPins[i], LOW);
	}

	Wire.begin(CURRENT_SDA, CURRENT_SCL);
	ina226.init();
	ina226.setAverage(AVERAGE_16);
	ina226.setConversionTime(CONV_TIME_1100);
	ina226.setCurrentRange(MA_400);

}

void checkCurrent() {
	float current_mA = ina226.getCurrent_mA();
	float power_mW = ina226.getBusPower();

	float power = 1000 + random(0, 100);

	sendDataToHub("/SmartHomeSystem/SmartLightSystem/power_mW", String(power), "float");
	sendDataToHub("/SmartHomeSystem/SmartLightSystem/current_mA", String(current_mA), "float");
}

void generalStateChartInit() {
	deviceTemp_Component_init(&lights1_deviceTemp);
	power_Component_init(&lights1_power);
	network_Component_init(&lights1_network);

	deviceTemp_Component_init(&lights2_deviceTemp);
	power_Component_init(&lights2_power);
	network_Component_init(&lights2_network);

	deviceTemp_Component_init(&lights3_deviceTemp);
	power_Component_init(&lights3_power);
	network_Component_init(&lights3_network);
}

void smartLightsInit() {

	smartLightHUB_init(&LightsHub);
	smartLightSystem_init(&lights1_System);
	smartLightSystem_init(&lights2_System);
	smartLightSystem_init(&lights3_System);

	controller_Component_init(&lights1_Controller);
	actuator_Component_init(&lights1_Actuator);
	lEDLight_Unit_init(&lights1_led);

	controller_Component_init(&lights1_Controller_Motion);
	actuator_Component_init(&lights1_Actuator_Motion);
	ultrasonic_Sensor_init(&lights1_MotionSensor);
	ultrasonic_MotionDetector_init(&lights1_MotionDetector);
	ultrasonic_MotionDetector_set_sensor(&lights1_MotionDetector,&lights1_MotionSensor);
	ultrasonic_MotionDetector_set_powerSupply(&lights1_MotionDetector,&lights1_power);
	ultrasonic_MotionDetector_set_actuator(&lights1_MotionDetector,&lights1_Actuator_Motion);
	ultrasonic_MotionDetector_set_controller(&lights1_MotionDetector,&lights1_Controller_Motion);

	smartLightSystem_set_actuator(&lights1_System, &lights1_Actuator);
	smartLightSystem_set_controller(&lights1_System, &lights1_Controller);
	smartLightSystem_set_motionDetector(&lights1_System, &lights1_MotionDetector);
	smartLightSystem_set_wiFi(&lights1_System, &lights1_network);
	smartLightSystem_set_power(&lights1_System, &lights1_power);
	smartLightSystem_set_temp(&lights1_System, &lights1_deviceTemp);
	smartLightSystem_set_lights(&lights1_System, &lights1_led);


	controller_Component_init(&lights2_Controller);
	actuator_Component_init(&lights2_Actuator);
	lEDLight_Unit_init(&lights2_led);

	controller_Component_init(&lights2_Controller_Motion);
	actuator_Component_init(&lights2_Actuator_Motion);
	ultrasonic_Sensor_init(&lights2_MotionSensor);
	ultrasonic_MotionDetector_init(&lights2_MotionDetector);
	ultrasonic_MotionDetector_set_sensor(&lights2_MotionDetector,&lights2_MotionSensor);
	ultrasonic_MotionDetector_set_powerSupply(&lights2_MotionDetector,&lights2_power);
	ultrasonic_MotionDetector_set_actuator(&lights2_MotionDetector,&lights2_Actuator_Motion);
	ultrasonic_MotionDetector_set_controller(&lights2_MotionDetector,&lights2_Controller_Motion);

	smartLightSystem_set_actuator(&lights2_System, &lights2_Actuator);
	smartLightSystem_set_controller(&lights2_System, &lights2_Controller);
	smartLightSystem_set_motionDetector(&lights2_System, &lights2_MotionDetector);
	smartLightSystem_set_wiFi(&lights2_System, &lights2_network);
	smartLightSystem_set_power(&lights2_System, &lights2_power);
	smartLightSystem_set_temp(&lights2_System, &lights2_deviceTemp);
	smartLightSystem_set_lights(&lights2_System, &lights2_led);

	controller_Component_init(&lights3_Controller);
	actuator_Component_init(&lights3_Actuator);
	lEDLight_Unit_init(&lights3_led);

	controller_Component_init(&lights3_Controller_Motion);
	actuator_Component_init(&lights3_Actuator_Motion);
	ultrasonic_Sensor_init(&lights3_MotionSensor);
	ultrasonic_MotionDetector_init(&lights3_MotionDetector);
	ultrasonic_MotionDetector_set_sensor(&lights3_MotionDetector,&lights3_MotionSensor);
	ultrasonic_MotionDetector_set_powerSupply(&lights3_MotionDetector,&lights3_power);
	ultrasonic_MotionDetector_set_actuator(&lights3_MotionDetector,&lights3_Actuator_Motion);
	ultrasonic_MotionDetector_set_controller(&lights3_MotionDetector,&lights3_Controller_Motion);

	smartLightSystem_set_actuator(&lights3_System, &lights3_Actuator);
	smartLightSystem_set_controller(&lights3_System, &lights3_Controller);
	smartLightSystem_set_motionDetector(&lights3_System, &lights3_MotionDetector);
	smartLightSystem_set_wiFi(&lights3_System, &lights3_network);
	smartLightSystem_set_power(&lights3_System, &lights3_power);
	smartLightSystem_set_temp(&lights3_System, &lights3_deviceTemp);
	smartLightSystem_set_lights(&lights3_System, &lights3_led);

	smartLightHUB_set_lED1(&LightsHub, &lights1_System);
	smartLightHUB_set_lED1(&LightsHub, &lights2_System);
}

void startAllSystems() {
	smartLightHUB_enter(&LightsHub);
	smartLightHUB_HUB_raise_turnONSystems(&LightsHub);
	ultrasonic_MotionDetector_raise_on(&lights1_MotionDetector);
	ultrasonic_MotionDetector_raise_on(&lights2_MotionDetector);
	ultrasonic_MotionDetector_raise_on(&lights3_MotionDetector);
}

void setupESPNow() {
  WiFi.mode(WIFI_STA);
  esp_wifi_set_channel(6, WIFI_SECOND_CHAN_NONE);


  if (esp_now_init() != ESP_OK) {
    Serial.println("❌ ESP-NOW init failed");
    return;
  }

  esp_now_register_send_cb(onDataSent);
  esp_now_register_recv_cb(onDataRecv);

  esp_now_peer_info_t peerInfo = {};
  memcpy(peerInfo.peer_addr, peerMac, 6);
  peerInfo.channel = 0;
  peerInfo.encrypt = false;

  if (!esp_now_is_peer_exist(peerMac)) {
    if (esp_now_add_peer(&peerInfo) != ESP_OK) {
      Serial.println("❌ Failed to add peer");
      return;
    }
  }

  Serial.println(WiFi.macAddress());
  Serial.println("✅ ESP-NOW Ready");
}

void setup()
{
	Serial.begin(115200);
	delay(1000);
	systemPinsInit();
	generalStateChartInit();
	smartLightsInit();
	startAllSystems();
	setupESPNow();
	delay(1000);
}

void loop()
{
	operateLight();
	autoShutOffLight();
	for (int i = 0; i < 3; i ++){
		checkLightSensor(i);
	}

	if (millis() - lastPoll > 5000){
		lastPoll = millis();
		checkCurrent();
	}
	delay(100);
}
