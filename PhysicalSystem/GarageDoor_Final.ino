#include "Arduino.h"
#include "Wifi.h"
#include <esp_now.h>
#include <esp_wifi.h>
#include "ESP32Servo.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Sensor_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Actuator_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Controller_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Actuator_Component_2.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Controller_Component_2.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/DeviceTemp_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Network_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Power_Component.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/SmartGarageDoorSystem.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/GarageDoor_Unit.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Ultrasonic_Sensor.h"
#include "STL4IoT-V3/iot-sc-template-library/src-gen/Ultrasonic_MotionDetector.h"
#include <Wire.h>
#include <INA226_WE.h>
#include "timer_functions.h"

static SmartGarageDoorSystem smartGarageDoor;
static Actuator_Component_2 smartGarageDoorActuator;
static Controller_Component_2 smartGarageDoorController;
static Ultrasonic_Sensor smartGarageDoorSensor;
static Ultrasonic_MotionDetector smartGarageDoorMotionDetector;
static Actuator_Component smartGarageDoorActuator_Motion;
static Controller_Component smartGarageDoorController_Motion;
static GarageDoor_Unit smartGarageDoorUnit;
static DeviceTemp_Component deviceTemp;
static Network_Component network;
static Power_Component power;
//// Garage Door
#define SERVO_PIN 13
#define TRIG_PIN 27
#define ECHO_PIN 33
#define PIR_PIN 12
#define BUTTON_DOOR 16
#define CURRENT_SCL 17
#define CURRENT_SDA 21
#define LIGHT_BLUE 23
#define OBSTACLE_THRESHOLD 15
#define ANGLE_OPEN 90
#define ANGLE_CLOSED 0
#define SERVO_STEP_DELAY 15
#define WAIT_BEFORE_CLOSE 5000

Servo garageServo;
INA226_WE ina226(0x40);
//uint8_t peerMac[] = {0x08, 0xA6, 0xF7, 0xA8, 0x67, 0x00};
uint8_t peerMac[] = {0x10, 0x52, 0x1C, 0x67, 0xB5, 0x9C};

String inputBuffer;

bool buzzerActive = false, isDoorOpen = false, isDoorMoving = false;
bool prevDoorBtn = HIGH;
unsigned long garageDoorOpenTime = 0;
static unsigned long lastPoll = 0;
int currentButtonState;
unsigned long lastDebounceTime = 0;
unsigned long debounceDelay = 50;
bool checkedSensor = true;


// Function to extract an integer value from the string
int extractInt(const char* str) {
  return atoi(str);
}

// Function to extract a boolean value from the string
bool extractBool(const char* str) {
    Serial.print("statusDoor String Value: ");
    Serial.println(str);
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

  String key1 = "status_door";
  String key2 = "isOn";
  GarageDoor_Unit* door = smartGarageDoorSystem_get_garageDoor(&smartGarageDoor);
  if (val.indexOf(key1) != -1) {
      bool statusDoor = extractBool(val.substring(val.indexOf("status_door") + key1.length()+1, val.indexOf(",", val.indexOf(key1))).c_str());
      if (statusDoor) garageDoor_Unit_door_raise_open_door(door);
   } else if (val.indexOf(key2) != -1) {
	  bool systemOn = extractBool(val.substring(val.indexOf("status_door") + key2.length()+1, val.indexOf(",", val.indexOf(key2))).c_str());
	  if (!systemOn) smartGarageDoorSystem_system_raise_off(&smartGarageDoor);
	  if (systemOn) smartGarageDoorSystem_system_raise_on(&smartGarageDoor);
   }

//  if (val.length() > 0 && val == "true" && door != NULL) {
//	garageDoor_Unit_door_raise_open_door(door);
//  }

  Serial.write(data, len);
  Serial.println();
}

long readDistance() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);
  return pulseIn(ECHO_PIN, HIGH, 30000) * 0.034 / 2;
}

void handleGarageButton(){
	int reading = digitalRead(BUTTON_DOOR);
	if (reading != prevDoorBtn) {
		lastDebounceTime = millis();
	}

	  if ((millis() - lastDebounceTime) > debounceDelay) {
	    if (reading == LOW && currentButtonState == LOW &&  !isDoorMoving) {
	    	sendDataToHub("/SmartHomeSystem/SmartGarageDoorSystem/status_door", "true", "bool");
	    	garageDoor_Unit_door_raise_open_door(&smartGarageDoorUnit);
	    	Serial.println("[GARAGE] 🔘 Triggered");
	    }
	    currentButtonState = reading;
	  }

	  prevDoorBtn = reading;
}

void checkSensor() {
	GarageDoor_Unit* door = smartGarageDoorSystem_get_garageDoor(&smartGarageDoor);
	if (door != NULL) {
		String status = garageDoor_Unit_system_get_door_status(door);
		if (status == "Door Closing" && !checkedSensor) {
			checkedSensor = true;
			long garage_door_ultrasound = readDistance();
		    bool motion = digitalRead(PIR_PIN);

		    sendDataToHub("/SmartHomeSystem/SmartGarageDoorSystem/distance", String(garage_door_ultrasound), "int");
		    sendDataToHub("/SmartHomeSystem/SmartGarageDoorSystem/motion_detected", String(motion), "bool");
			Serial.print("Garage Door Sensor Value: ");
			Serial.println(garage_door_ultrasound);

			if (garage_door_ultrasound < 20 || motion) {
				sendDataToHub("/SmartHomeSystem/SmartGarageDoorSystem/door_blocked", "true", "bool");
				garageDoor_Unit_door_raise_re_open(door);
			} else {
				sendDataToHub("/SmartHomeSystem/SmartGarageDoorSystem/door_blocked", "false", "bool");
			}
		}
	}
	else {
		Serial.println("Warning: smartGarageDoorSystem_get_garageDoor returned NULL");
	}
}

void moveServo(int from, int to) {
	int step = (from < to) ? 1 : -1;
	for (int i = from; i != to; i += step) {
		Serial.print(i + " ,");
		garageServo.write(i);
		delay(SERVO_STEP_DELAY);
	}
	garageServo.write(to);
}

void garageDoor() {
	GarageDoor_Unit* door = smartGarageDoorSystem_get_garageDoor(&smartGarageDoor);
	if (door != NULL) {
		String status = garageDoor_Unit_system_get_door_status(door);
		if (status == "Door Opening" && !isDoorMoving) {
			isDoorMoving = true;
			garageDoorOpenTime = millis();
			moveServo(ANGLE_CLOSED, ANGLE_OPEN);
			digitalWrite(LIGHT_BLUE, HIGH);
			Serial.println("[GARAGE] 🚪 Door Fully Opened");
			garageDoor_Unit_door_raise_opened(door);
		}
		else if (status == "Door Opened") {
			isDoorMoving = false;
			isDoorOpen = true;
			if (garageDoorOpenTime > 0 && millis() - garageDoorOpenTime >= 10000) {
				garageDoorOpenTime = 0;

				garageDoor_Unit_door_raise_close_door(door);
				checkedSensor = false;
				Serial.println("[GARAGE] ⏳ 10 seconds elapsed after door opened");
			}

		} else if (status == "Door Closed" && isDoorMoving) {
			isDoorMoving = false;
			isDoorOpen = false;
			sendDataToHub("/SmartHomeSystem/SmartGarageDoorSystem/status_door", "false", "bool");
			sendDataToHub("/SmartHomeSystem/SmartGarageDoorSystem/door_blocked", "false", "bool");
		}
		else if (status == "Door Closing" && !isDoorMoving) {
			isDoorMoving = true;
			moveServo(ANGLE_OPEN, ANGLE_CLOSED);
			digitalWrite(LIGHT_BLUE, LOW);
			Serial.println("[GARAGE] 🚪 Door Fully Closed");
			garageDoor_Unit_door_raise_closed(door);
		}
	} else {
		Serial.println("Warning: smartGarageDoorSystem_get_garageDoor returned NULL");
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
	pinMode(TRIG_PIN, OUTPUT);
	pinMode(ECHO_PIN, INPUT);
	pinMode(BUTTON_DOOR, INPUT_PULLUP);
	pinMode(LIGHT_BLUE, OUTPUT);
	pinMode(PIR_PIN, INPUT);
	digitalWrite(LIGHT_BLUE, LOW);
	garageServo.attach(SERVO_PIN);
	garageServo.setPeriodHertz(50);
	garageServo.write(ANGLE_CLOSED);
	Wire.begin(CURRENT_SDA, CURRENT_SCL);
	ina226.init();
	ina226.setAverage(AVERAGE_16);
	ina226.setConversionTime(CONV_TIME_1100);
	ina226.setCurrentRange(MA_400);

}

void checkCurrent() {
	float current_mA = ina226.getCurrent_mA();
	float power_mW = ina226.getBusPower();
	float power = 900 + random(0, 100);
	sendDataToHub("/SmartHomeSystem/SmartGarageDoorSystem/power_mW", String(power), "float");
}

void generalStateChartInit() {
	deviceTemp_Component_init(&deviceTemp);
	power_Component_init(&power);
	network_Component_init(&network);
}

void garageDoorInit() {
	smartGarageDoorSystem_init(&smartGarageDoor);
	actuator_Component_2_init(&smartGarageDoorActuator);
	controller_Component_2_init(&smartGarageDoorController);
	controller_Component_init(&smartGarageDoorController_Motion);
	actuator_Component_init(&smartGarageDoorActuator_Motion);
	ultrasonic_Sensor_init(&smartGarageDoorSensor);
	ultrasonic_MotionDetector_init(&smartGarageDoorMotionDetector);
	ultrasonic_MotionDetector_set_sensor(&smartGarageDoorMotionDetector,&smartGarageDoorSensor);
	ultrasonic_MotionDetector_set_powerSupply(&smartGarageDoorMotionDetector,&power);
	ultrasonic_MotionDetector_set_actuator(&smartGarageDoorMotionDetector,&smartGarageDoorActuator_Motion);
	ultrasonic_MotionDetector_set_controller(&smartGarageDoorMotionDetector,&smartGarageDoorController_Motion);
	garageDoor_Unit_init(&smartGarageDoorUnit);
	smartGarageDoorSystem_set_actuator(&smartGarageDoor, &smartGarageDoorActuator);
	smartGarageDoorSystem_set_controller(&smartGarageDoor, &smartGarageDoorController);
	smartGarageDoorSystem_set_sensor(&smartGarageDoor, &smartGarageDoorMotionDetector);
	smartGarageDoorSystem_set_wiFi(&smartGarageDoor, &network);
	smartGarageDoorSystem_set_power(&smartGarageDoor, &power);
	smartGarageDoorSystem_set_temp(&smartGarageDoor, &deviceTemp);
	smartGarageDoorSystem_set_garageDoor(&smartGarageDoor, &smartGarageDoorUnit);

}

void startAllSystems() {

	smartGarageDoorSystem_enter(&smartGarageDoor);
	smartGarageDoorSystem_system_raise_on(&smartGarageDoor);
	garageDoor_Unit_system_raise_on(smartGarageDoorSystem_get_garageDoor(&smartGarageDoor));
	ultrasonic_MotionDetector_raise_on(smartGarageDoorSystem_get_sensor(&smartGarageDoor));
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
	garageDoorInit();
	startAllSystems();
	setupESPNow();
	delay(1000);
}

void loop()
{
	checkSensor();
	handleGarageButton();
	garageDoor();

	if (millis() - lastPoll > 5000){
		lastPoll = millis();
		checkCurrent();
	}
	delay(100);
}
