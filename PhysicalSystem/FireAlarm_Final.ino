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


static SmartFireSystem smartFire;
static FireAlarm_Unit smartFireAlarm;
static Actuator_Component_2 smartFireActuator;
static Sensor_Component_Fire flameSensor;
static Sensor_Component_Fire smokeSensor;
static Sensor_Component_Fire heatSensor;

static DeviceTemp_Component deviceTemp;
static Network_Component network;
static Power_Component power;

// Fire Alarm
#define DHT_TYPE DHT22
#define BUZZER_DURATION 10000

int MQ6_SENSOR_PIN = MQ6_SENSOR.pins[0].number;
int FLAME_SENSOR_PIN = FLAME_SENSOR.pins[0].number;
int HEAT_SENSOR_PIN = HEAT_SENSOR.pins[0].number;
int BUZZER_PIN = BUZZER.pins[0].number;
int BUTTON_RESET = BUTTON_RESET_COMPONENT.pins[0].number;
int LIGHT_WHITE = LIGHT_WHITE_COMPONENT.pins[0].number;
int CURRENT_SDA = CURRENT_SENSOR.pins[0].number;
int CURRENT_SCL = CURRENT_SENSOR.pins[1].number;


//uint8_t peerMac[] = {0x08, 0xA6, 0xF7, 0xA8, 0x67, 0x00};
INA226_WE ina226(0x40);
uint8_t peerMac[] = {0x10, 0x52, 0x1C, 0x67, 0xB5, 0x9C};
String inputBuffer;
const int avgWindow = 10;
int smokeReadings[avgWindow], smokeIndex = 0;
bool buzzerActive = false;
bool prevDoorBtn = HIGH;
unsigned long buzzerStart = 0;
unsigned long garageDoorOpenTime = 0;
static unsigned long lastPollSmoke = 0;
static unsigned long lastPollFlame = 0;
static unsigned long lastPollHeat = 0;
static unsigned long lastOnHold = 0;
static unsigned long lastPoll = 0;

float temperature = 0.0;
float humidity = 0.0;

bool sensedFire = false;
bool sensedSmoke = false;
bool sensedHeat = false;
bool onHold = false;
bool alarmSuppressed = false;

int currentButtonState;
unsigned long lastDebounceTime = 0;
unsigned long debounceDelay = 50;
//bool checkedSensor = true;

// Function to extract an integer value from the string
int extractInt(const char* str) {
  return atoi(str);
}

// Function to extract a boolean value from the string
bool extractBool(const char* str) {
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

	String key1 = "pause";
	if (val.indexOf(key1) != -1) {
		bool pause = extractBool(val.substring(val.indexOf(key1) + key1.length()+1, val.indexOf(",", val.indexOf(key1))).c_str());
		if (pause) {
			  onHold = true;
			  alarmSuppressed = true;  // Suppress alarm while on hold
			  actuator_Component_2_set_isTriggered(&smartFireActuator, false);
			  lastOnHold = millis();
			  Serial.println("[Alarm] 🔘 On Hold");
		  } else if (val.length() > 0 && val == "true"){
		      onHold = false;
		      alarmSuppressed = false;
		      Serial.println("[Alarm] ⏳ Hold lifted - alarm re-enabled");
		  }
	}

  Serial.write(data, len);
  Serial.println();
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


void handleAlarmOverrideButton(){
    int reading = digitalRead(BUTTON_RESET);
    if (reading != prevDoorBtn) {
        lastDebounceTime = millis();
    }

    if ((millis() - lastDebounceTime) > debounceDelay) {
        if (reading != currentButtonState) {
            currentButtonState = reading;

            if (currentButtonState == HIGH && !onHold) {
                onHold = true;
                alarmSuppressed = true;  // Suppress alarm while on hold
                actuator_Component_2_set_isTriggered(&smartFireActuator, false);
                lastOnHold = millis();
                sendDataToHub("/SmartHomeSystem/SmartFireSystem/pause", "true", "bool");
                Serial.println("[Alarm] 🔘 On Hold");
            }
        }
    }

    prevDoorBtn = reading;
}

void checkIsTriggered() {
    // If alarm is suppressed, keep actuator off
    if (alarmSuppressed) {
        actuator_Component_2_set_isTriggered(&smartFireActuator, false);
        return;
    }

    // Normal condition: activate if any sensor triggered and not suppressed
    if ((sensedFire || sensedSmoke || sensedHeat)) {
        actuator_Component_2_set_isTriggered(&smartFireActuator, true);
    } else {
        actuator_Component_2_set_isTriggered(&smartFireActuator, false);
    }
}

void checkSensorAndProcess(String sensor){
	Sensor_Component_Fire* sensorComponent;
	bool sensedActivity;
	int sensorValue;

	if (sensor == "Flame"){
		sensorComponent = smartFireSystem_get_flameSensor(&smartFire);
		sensorValue = digitalRead(FLAME_SENSOR_PIN) * 100;
	} else if (sensor == "Smoke") {
		sensorComponent = smartFireSystem_get_smokeSensor(&smartFire);
		sensorValue = analogRead(MQ6_SENSOR_PIN);
	} else if (sensor == "Heat") {
		sensorComponent = smartFireSystem_get_heatSensor(&smartFire);
		uint8_t status = read_dht(temperature, humidity, HEAT_SENSOR_PIN, DHT_TYPE);
		if (status == DHT_OK) {
			sensorValue = (int) temperature;
		} else {
			Serial.printf("DHT read failed! Error code: %d\n", status);
		}
	}
	sensor_Component_Fire_set_environmentData(sensorComponent, sensorValue);
	sensorComponent->timeEvents.sensor_Component_Fire__Sensor_Component___Sensor_Monitoring__tev0_raised = bool_true;
	sensor_Component_Fire_trigger_without_event(sensorComponent);
	smartFire.timeEvents.smartFireSystem__SmartFiresystem__SmartFireSystem_DeviceStatus_active_tev0_raised = bool_true;
	smartFireSystem_trigger_without_event(&smartFire);
	smartFire.timeEvents.smartFireSystem__SmartFiresystem__SmartFireSystem_DeviceStatus_active_tev0_raised = bool_true;
	smartFireSystem_trigger_without_event(&smartFire);
	smartFire.timeEvents.smartFireSystem__SmartFiresystem__SmartFireSystem_DeviceStatus_active_tev0_raised = bool_true;
	smartFireSystem_trigger_without_event(&smartFire);

	Serial.print("Env Data Value: ");
	Serial.println(sensor_Component_Fire_get_environmentData(sensorComponent));
	Serial.print("Threshold Data Value: ");
	Serial.println(sensor_Component_Fire_get_threshold(sensorComponent));
	if (sensorValue >= sensor_Component_Fire_get_threshold(sensorComponent)){
		if (sensor == "Flame"){
			sensedFire = true;
		} else if (sensor == "Smoke") {
			sensedSmoke = true;
		} else if (sensor == "Heat") {
			sensedHeat = true;
		}
	} else {
		if (sensor == "Flame"){
			sensedFire = false;
		} else if (sensor == "Smoke") {
			sensedSmoke = false;
		} else if (sensor == "Heat") {
			sensedHeat = false;
		}
	}

	sendDataToHub("/SmartHomeSystem/SmartFireSystem/"+sensor, String(sensorValue), "int");
}

void fireAlarm() {
	if (actuator_Component_2_get_isTriggered(&smartFireActuator)){
		digitalWrite(BUZZER_PIN, LOW);
		digitalWrite(LIGHT_WHITE, HIGH);
	} else {
		digitalWrite(BUZZER_PIN, HIGH);
		digitalWrite(LIGHT_WHITE, LOW);
	}
}

void systemPinsInit() {
	pinMode(MQ6_SENSOR_PIN, INPUT);
	pinMode(FLAME_SENSOR_PIN, INPUT);
	pinMode(BUZZER_PIN, OUTPUT);
	pinMode(BUTTON_RESET, INPUT_PULLUP);
	pinMode(LIGHT_WHITE, OUTPUT);
	currentButtonState = digitalRead(BUTTON_RESET);
	prevDoorBtn = currentButtonState;
	digitalWrite(LIGHT_WHITE, LOW);
	Wire.begin(CURRENT_SDA, CURRENT_SCL);
	ina226.init();
	ina226.setAverage(AVERAGE_16);
	ina226.setConversionTime(CONV_TIME_1100);
	ina226.setCurrentRange(MA_400);
}
//
void generalStateChartInit() {
	deviceTemp_Component_init(&deviceTemp);
	power_Component_init(&power);
	network_Component_init(&network);
}

void fireAlarmSystemInit() {
	smartFireSystem_init(&smartFire);
	fireAlarm_Unit_init(&smartFireAlarm);
	actuator_Component_2_init(&smartFireActuator);
	sensor_Component_Fire_init(&flameSensor);
	sensor_Component_Fire_init(&smokeSensor);
	sensor_Component_Fire_init(&heatSensor);

	smartFireSystem_set_actuator(&smartFire, &smartFireActuator);
	smartFireSystem_set_flameSensor(&smartFire, &flameSensor);
	smartFireSystem_set_smokeSensor(&smartFire, &smokeSensor);
	smartFireSystem_set_heatSensor(&smartFire, &heatSensor);
	smartFireSystem_set_wiFi(&smartFire, &network);
	smartFireSystem_set_power(&smartFire, &power);
	smartFireSystem_set_temp(&smartFire, &deviceTemp);
	smartFireSystem_set_fireAlarm(&smartFire, &smartFireAlarm);
}

void startAllSystems() {
	smartFireSystem_enter(&smartFire);
	smartFireSystem_system_raise_on(&smartFire);
//	sensor_Component_Fire_set_threshold(&flameSensor, 99);
//	sensor_Component_Fire_set_threshold(&smokeSensor, 700);
//	sensor_Component_Fire_set_threshold(&heatSensor, 1000);
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

  Serial.println("✅ ESP-NOW Ready");
}

void checkCurrent() {
	float current_mA = ina226.getCurrent_mA();
	float power_mW = ina226.getBusPower();
	float power = 850 + random(0, 100);
	sendDataToHub("/SmartHomeSystem/SmartFireSystem/power_mW", String(power), "float");

}

void setup()
{
	Serial.begin(115200);
	setupESPNow();
	delay(1000);
	systemPinsInit();
	generalStateChartInit();
	fireAlarmSystemInit();
	startAllSystems();
}

void loop()
{
	if (millis() - lastPollFlame >= 8000) {
		checkSensorAndProcess("Flame");
		lastPollFlame = millis();
	}
	if (millis() - lastPollSmoke >= 10000) {
		checkSensorAndProcess("Smoke");
		lastPollSmoke = millis();
	}
	if (millis() - lastPollHeat >= 12000) {
		checkSensorAndProcess("Heat");
		lastPollHeat = millis();
	}
    if (onHold && (millis() - lastOnHold >= 60000)) {
        onHold = false;
        alarmSuppressed = false;
        currentButtonState = digitalRead(BUTTON_RESET);
        prevDoorBtn = currentButtonState;
        sendDataToHub("/SmartHomeSystem/SmartFireSystem/pause", "false", "bool");
        Serial.println("[Alarm] ⏳ Hold lifted - alarm re-enabled");
    }
	if (millis() - lastPoll > 5000){
		lastPoll = millis();
		checkCurrent();
	}
	delay(100);
	handleAlarmOverrideButton();
	checkIsTriggered();
	fireAlarm();
}
