################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../Emulator/qemu2/esp-idf/components/esp_driver_cam/isp_dvp/src/esp_cam_ctlr_isp_dvp.c 

C_DEPS += \
./Emulator/qemu2/esp-idf/components/esp_driver_cam/isp_dvp/src/esp_cam_ctlr_isp_dvp.c.d 

LINK_OBJ += \
./Emulator/qemu2/esp-idf/components/esp_driver_cam/isp_dvp/src/esp_cam_ctlr_isp_dvp.c.o 


# Each subdirectory must supply rules for building sources it contributes
Emulator/qemu2/esp-idf/components/esp_driver_cam/isp_dvp/src/esp_cam_ctlr_isp_dvp.c.o: ../Emulator/qemu2/esp-idf/components/esp_driver_cam/isp_dvp/src/esp_cam_ctlr_isp_dvp.c
	@echo 'Building file: $<'
	@echo 'Starting C compile'
	"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp-x32/2302/bin/xtensa-esp32-elf-gcc" -MMD -c "@/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/flags/c_flags" -w -Os -DF_CPU=240000000L -DARDUINO=10812 -DARDUINO_ESP32_WROOM_DA -DARDUINO_ARCH_ESP32 "-DARDUINO_BOARD=\"ESP32_WROOM_DA\"" -DARDUINO_VARIANT="esp32da" -DARDUINO_PARTITION_default -DARDUINO_HOST_OS="" -DARDUINO_FQBN="" -DESP32 -DCORE_DEBUG_LEVEL=0 -DARDUINO_RUNNING_CORE=1 -DARDUINO_EVENT_RUNNING_CORE=1  -DARDUINO_USB_CDC_ON_BOOT=0  "@/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/flags/defines" "-I/Users/sahilsalma/PersonalData/UniversityStudies/Graduate/Spring2025" -iprefix "/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/include/" "@/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/flags/includes" "-I/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/qio_qspi/include" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/cores/esp32" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/variants/esp32da" -I"/Users/sahilsalma/Documents/Arduino/libraries/Firebase_ESP32_Client/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/WiFi/src" -I"/Users/sahilsalma/Documents/Arduino/libraries/ESP32Servo/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/SPI/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/LittleFS/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/Update/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/Network/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/SD/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/FS/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/HTTPClient/src" -I"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/hardware/esp32/3.0.4/libraries/NetworkClientSecure/src" -I"/Users/sahilsalma/Documents/Arduino/libraries/ArduinoJson/src" -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -D__IN_ECLIPSE__=1 "@/Users/sahilsalma/PersonalData/UniversityStudies/Graduate/Spring2025/Release/file_opts" "$<" -o "$@"
	@echo 'Finished building: $<'
	@echo ' '


