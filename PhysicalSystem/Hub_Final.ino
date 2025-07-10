#include <WiFi.h>
#include <esp_now.h>
#include <FirebaseESP32.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>


// === CONFIGURATION ===
#define FIREBASE_STREAM_PATH "/SmartHomeSystem"
#define API_KEY "API_KEY_FOR_FIREBASE_RTDB"
#define DATABASE_URL "https://smarthubsystem-394c6-default-rtdb.firebaseio.com"
//OLD DB
// #define DATABASE_URL "https://smart-fire-system-684fb-default-rtdb.firebaseio.com"
#define USER_EMAIL "FIREBASE_USER_EMAIL"
#define USER_PASSWORD "FIREBASE_USER_PASSWORD"
#define WIFI_SSID "WIFI_SSID"
#define WIFI_PASSWORD "WIFI_PASSWORD"
#define LOG_SERVER_URL "http://192.168.0.13:3000/log"

enum FirebaseValueType { TYPE_STRING, TYPE_INT, TYPE_FLOAT, TYPE_BOOL };

// === GLOBALS ===
FirebaseData firebaseStream, firebaseWrite;
FirebaseAuth auth;
FirebaseConfig config;
// uint8_t peerMac[] = {0x14, 0x2B, 0x2F, 0xC4, 0xE4, 0x80}; /homeGarageDoor
// uint8_t peerMac[] = {0x08, 0xA6, 0xF7, 0x21, 0xB8, 0xC4}; //garageDoor


uint8_t fireESP[]     = {0x08, 0xA6, 0xF7, 0x22, 0xDC, 0x88};
uint8_t garageESP[]   = {0x08, 0xA6, 0xF7, 0x21, 0xB8, 0xC4};
uint8_t lightingESP[] = {0x08, 0xA6, 0xF7, 0x22, 0x6B, 0xE0};

String bufferedCommand = "";
bool hasBufferedCommand = false;

void sendLog(const String& message) {
  // if (WiFi.status() == WL_CONNECTED) {
  //   HTTPClient http;
  //   http.begin(LOG_SERVER_URL);
  //   http.addHeader("Content-Type", "text/plain");
  //   int httpResponseCode = http.POST(message);
  //   http.end();
  // }
  Serial.println(message);
}

// === CALLBACKS ===
void onFirebaseStream(StreamData data) {
  String val = data.stringData();
  String path = data.dataPath();     // Get the path of the update
  Serial.print("Stream Path: ");
  Serial.println(path);

  Serial.print("Stream Value: ");
  Serial.println(val);

  Serial.print("Full Stream JSON: ");
  Serial.println(data.jsonString());

  sendLog("📡 Firebase update: " + val);

  String command = path.substring(path.lastIndexOf("/") + 1); // Extract command after last "/"
  String message = command + ":" + val;
  if (path.indexOf("SmartFireSystem") != -1){
    esp_now_send(fireESP, (uint8_t *)message.c_str(), message.length()); // Relay to peer
  } else if (path.indexOf("SmartLightSystem") != -1){
    esp_now_send(lightingESP, (uint8_t *)message.c_str(), message.length()); // Relay to peer
  } else if (path.indexOf("SmartGarageDoorSystem")) {
    esp_now_send(garageESP, (uint8_t *)message.c_str(), message.length()); // Relay to peer
  }
} 

void onFirebaseTimeout(bool timeout) {
  if (timeout) {
    sendLog("⚠️ Stream timeout. Reconnecting...");
    Firebase.beginStream(firebaseStream, FIREBASE_STREAM_PATH);
  }
}

bool updateFirebaseValue(const String& path, const String& value, FirebaseValueType type = TYPE_STRING) {
  if (!Firebase.ready()) {
    sendLog("❌ Firebase not ready");
    return false;
  }

  bool success = false;

  if (type == TYPE_STRING) {
    success = Firebase.setString(firebaseWrite, path, value);
  } else if (type == TYPE_INT) {
    success = Firebase.setInt(firebaseWrite, path, value.toInt());
  } else if (type == TYPE_FLOAT) {
    success = Firebase.setFloat(firebaseWrite, path, value.toFloat());
  } else if (type == TYPE_BOOL) {
    success = Firebase.setBool(firebaseWrite, path, (value == "true" || value == "1"));
  } else {
    sendLog("❌ Unsupported Firebase data type");
    return false;
  }

  if (success) {
    sendLog("✅ Firebase updated:");
    sendLog("📍 Path: " + path);
    sendLog("📦 Value: " + value);
  } else {
    sendLog("❌ Firebase update failed:");
    sendLog(firebaseWrite.errorReason());
  }

  return success;
}

void onDataRecv(const esp_now_recv_info_t *recv_info, const uint8_t *data, int len) {
  char macStr[18];
  snprintf(macStr, sizeof(macStr), "%02X:%02X:%02X:%02X:%02X:%02X",
           recv_info->src_addr[0], recv_info->src_addr[1], recv_info->src_addr[2],
           recv_info->src_addr[3], recv_info->src_addr[4], recv_info->src_addr[5]);

  char buffer[251];  // ESP-NOW max is 250
  memcpy(buffer, data, len);
  buffer[len] = '\0';  // Null terminate
  String msg = String(buffer);
  
  bufferedCommand = msg;
  hasBufferedCommand = true;
}

void onDataSent(const uint8_t *mac, esp_now_send_status_t status) {
  sendLog("📤 Send status: ");
  sendLog(status == ESP_NOW_SEND_SUCCESS ? "Success" : "Fail");
}

void connectToWiFiPrompt() {
  sendLog("🔍 Scanning Wi-Fi...");
  int n = WiFi.scanNetworks();
  if (n == 0) return;

  for (int i = 0; i < n; ++i) {
    Serial.printf("%d: %s (%d dBm)\n", i + 1, WiFi.SSID(i).c_str(), WiFi.RSSI(i));
  }

  sendLog("\nEnter network number:");
  while (Serial.available() == 0);
  int choice = Serial.parseInt() - 1;
  if (choice < 0 || choice >= n) return;

  String ssid = WiFi.SSID(choice);

  WiFi.begin(ssid, WIFI_PASSWORD);
  sendLog("Connecting to ");
  sendLog(ssid);

  int tries = 0;
  while (WiFi.status() != WL_CONNECTED && tries++ < 30) {
    delay(500);
    sendLog(".");
  }

  if (WiFi.status() == WL_CONNECTED) {
    sendLog("\n✅ Wi-Fi connected");
    // sendLog("📡 IP: ");
    // sendLog(WiFi.localIP());
  } else {
    sendLog("\n❌ Wi-Fi failed");
  }
}

void setupFirebase() {
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  unsigned long start = millis();
  while (!Firebase.ready() && millis() - start < 10000) {
    delay(100);
  }

  if (Firebase.ready()) {
    Firebase.beginStream(firebaseStream, FIREBASE_STREAM_PATH);
    Firebase.setStreamCallback(firebaseStream, onFirebaseStream, onFirebaseTimeout);
    sendLog("✅ Firebase ready");
  } else {
    sendLog("❌ Firebase init failed");
  }
}

void setupESPNow(uint8_t channel) {
  // WiFi.mode(WIFI_STA);
  if (esp_now_init() != ESP_OK) {
    sendLog("❌ ESP-NOW init failed");
    return;
  }

  esp_now_register_recv_cb(onDataRecv);
  esp_now_register_send_cb(onDataSent);

  // Add the first peer (fireESP)
  esp_now_peer_info_t peerInfo1 = {};
  memcpy(peerInfo1.peer_addr, fireESP, 6);
  peerInfo1.channel = channel;
  peerInfo1.encrypt = false;
  if (esp_now_add_peer(&peerInfo1) != ESP_OK) {
    sendLog("❌ Failed to add fireESP peer");
  }

  // Add the second peer (garageESP)
  esp_now_peer_info_t peerInfo2 = {};
  memcpy(peerInfo2.peer_addr, garageESP, 6);
  peerInfo2.channel = channel;
  peerInfo2.encrypt = false;
  if (esp_now_add_peer(&peerInfo2) != ESP_OK) {
    sendLog("❌ Failed to add garageESP peer");
  }

  // Add the third peer (lightingESP)
  esp_now_peer_info_t peerInfo3 = {};
  memcpy(peerInfo3.peer_addr, lightingESP, 6);
  peerInfo3.channel = channel;
  peerInfo3.encrypt = false;
  if (esp_now_add_peer(&peerInfo3) != ESP_OK) {
    sendLog("❌ Failed to add lightingESP peer");
  }

  sendLog("✅ ESP-NOW peers added");
}

void syncTimeWithNTP() {
  sendLog("⏳ Syncing time with NTP...");

  // Configure time zone offset and NTP servers (adjust if needed)
  configTime(0, 0, "pool.ntp.org", "time.nist.gov");

  time_t now = time(nullptr);
  int retry = 0;
  const int maxRetries = 20;

  while (now < 8 * 3600 * 2 && retry < maxRetries) {  // Wait until time is valid (after Jan 1, 1970 + 16 hours)
    delay(500);
    sendLog(".");
    now = time(nullptr);
    retry++;
  }

  if (retry == maxRetries) {
    sendLog("\n❌ Failed to sync time with NTP");
  } else {
    sendLog("\n✅ Time synchronized: " + String(ctime(&now)));
  }
}

void checkBufferAndUpdate() {
  if (hasBufferedCommand) {
    sendLog("📨 Message from peer: " + bufferedCommand);
    bufferedCommand.replace("\\\"", "\"");
    StaticJsonDocument<256> doc;
    DeserializationError error = deserializeJson(doc, bufferedCommand);

    if (!error) {
      String path = doc["path"] | "";
      String value = doc["value"] | "";
      String typeStr = doc["type"] | "";
      FirebaseValueType type;

      if (typeStr == "bool") {
        type = TYPE_BOOL;
      } else if (typeStr == "int") {
        type = TYPE_INT;
      } else if (typeStr == "float") {
        type = TYPE_FLOAT;
      } else {
        type = TYPE_STRING;
      }

      if (path != "") {
        sendLog("Updating Firebase: ");
        updateFirebaseValue(path, value, type);
      } else {
        sendLog("❌ JSON missing 'path'");
      }

    } else {
      sendLog("❌ Failed to parse buffered JSON");
    }

    hasBufferedCommand = false;
    bufferedCommand = "";
  }
}

void setup() {
  Serial.begin(115200);
  WiFi.mode(WIFI_STA);
  connectToWiFiPrompt();

  if (WiFi.status() != WL_CONNECTED) {
    sendLog("❌ Wi-Fi not connected, cannot continue");
    return;
  }

  syncTimeWithNTP();

  uint8_t wifiChannel = WiFi.channel(); // Get Wi-Fi channel after connection
  sendLog("Wi-Fi channel: ");
  sendLog(String(wifiChannel));
  sendLog(String(WiFi.macAddress()));

  setupFirebase();

  setupESPNow(wifiChannel);  // Pass the Wi-Fi channel here!
}


// === MAIN LOOP ===
void loop() {

  checkBufferAndUpdate();
}
