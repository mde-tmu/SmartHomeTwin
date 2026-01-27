#!/bin/bash

# === USER CONFIGURATION ===
PROJECT_DIR="$(dirname "$0")/.."
RELEASE_DIR="$PROJECT_DIR/Release"
BIN_FILE_NAME="Arduino_Setup.bin"   # Name of .bin output file from build
ESP_IP="172.20.10.2"                    # Replace with your ESP32's IP
ESP_PORT=3232                            # OTA port
ESP_PASS=""                              # Optional OTA password

# === espota.py location ===
# Try to find espota.py in common locations (macOS, Linux, Windows)
ESPOTA_PY=$(find ~/Library/Arduino15/packages/esp32/hardware/esp32/*/tools/espota.py 2>/dev/null | head -n 1)
if [ -z "$ESPOTA_PY" ]; then
    ESPOTA_PY=$(find ~/.arduino15/packages/esp32/hardware/esp32/*/tools/espota.py 2>/dev/null | head -n 1)
fi

# === CHECKS ===

# Check for espota.py
if [ ! -f "$ESPOTA_PY" ]; then
    echo "❌ espota.py not found. Make sure ESP32 Arduino core is installed via arduino-cli or Arduino IDE."
    exit 1
fi

# Check for .bin file
BIN_FILE_PATH="$RELEASE_DIR/$BIN_FILE_NAME"
if [ ! -f "$BIN_FILE_PATH" ]; then
    echo "❌ Binary file not found at $BIN_FILE_PATH"
    echo "➡️  Ensure you have built the project in Release mode."
    exit 1
fi

# === UPLOAD ===

echo "📡 Uploading $BIN_FILE_NAME to $ESP_IP via OTA..."

# Build command array to avoid eval security issues
if [ -n "$ESP_PASS" ]; then
    python3 "$ESPOTA_PY" -i "$ESP_IP" -p "$ESP_PORT" -f "$BIN_FILE_PATH" -a "$ESP_PASS"
else
    python3 "$ESPOTA_PY" -i "$ESP_IP" -p "$ESP_PORT" -f "$BIN_FILE_PATH"
fi

if [ $? -eq 0 ]; then
    echo "✅ OTA Upload successful!"
else
    echo "❌ OTA Upload failed."
    exit 1
fi
