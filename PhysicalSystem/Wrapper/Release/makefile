################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -f

# All of the sources participating in the build are defined here
-include sources.mk
-include subdir.mk
-include libraries/dhtESP32-rmt/subdir.mk
-include libraries/Wire/src/subdir.mk
-include libraries/WiFi/src/subdir.mk
-include libraries/Update/src/subdir.mk
-include libraries/SPIFFS/src/subdir.mk
-include libraries/SPI/src/subdir.mk
-include libraries/SD/src/subdir.mk
-include libraries/NetworkClientSecure/src/subdir.mk
-include libraries/Network/src/subdir.mk
-include libraries/LittleFS/src/subdir.mk
-include libraries/INA226_WE/src/subdir.mk
-include libraries/HTTPClient/src/subdir.mk
-include libraries/Firebase_ESP32_Client/src/session/subdir.mk
-include libraries/Firebase_ESP32_Client/src/rtdb/stream/subdir.mk
-include libraries/Firebase_ESP32_Client/src/rtdb/subdir.mk
-include libraries/Firebase_ESP32_Client/src/message/subdir.mk
-include libraries/Firebase_ESP32_Client/src/json/MB_JSON/subdir.mk
-include libraries/Firebase_ESP32_Client/src/json/subdir.mk
-include libraries/Firebase_ESP32_Client/src/core/subdir.mk
-include libraries/Firebase_ESP32_Client/src/client/SSLClient/client/subdir.mk
-include libraries/Firebase_ESP32_Client/src/client/SSLClient/bssl/subdir.mk
-include libraries/Firebase_ESP32_Client/src/subdir.mk
-include libraries/FS/src/subdir.mk
-include libraries/ESP_NOW/src/subdir.mk
-include libraries/ESP32Servo/src/subdir.mk
-include libraries/Adafruit_INA219/subdir.mk
-include libraries/Adafruit_BusIO/subdir.mk
-include core/core/libb64/subdir.mk
-include core/core/subdir.mk
-include STL4IoT-V3/iot-sc-template-library/src-gen/subdir.mk
-include STL4IoT-V3/iot-sc-template-library/src/subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(CCM_DEPS)),)
-include $(CCM_DEPS)
endif
ifneq ($(strip $(C_UPPER_DEPS)),)
-include $(C_UPPER_DEPS)
endif
ifneq ($(strip $(PDE_DEPS)),)
-include $(PDE_DEPS)
endif
ifneq ($(strip $(CXXM_DEPS)),)
-include $(CXXM_DEPS)
endif
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
ifneq ($(strip $(CC_DEPS)),)
-include $(CC_DEPS)
endif
ifneq ($(strip $(SX_DEPS)),)
-include $(SX_DEPS)
endif
ifneq ($(strip $(C++_DEPS)),)
-include $(C++_DEPS)
endif
ifneq ($(strip $(CXX_DEPS)),)
-include $(CXX_DEPS)
endif
ifneq ($(strip $(ASM_DEPS)),)
-include $(ASM_DEPS)
endif
ifneq ($(strip $(INO_DEPS)),)
-include $(INO_DEPS)
endif
ifneq ($(strip $(S_UPPER_DEPS)),)
-include $(S_UPPER_DEPS)
endif
ifneq ($(strip $(C++M_DEPS)),)
-include $(C++M_DEPS)
endif
ifneq ($(strip $(CPP_DEPS)),)
-include $(CPP_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 
AR += \
/Users/sahilsalma/PersonalData/UniversityStudies/Graduate/Spring2025/Release/arduino.ar \

ELF += \
Arduino_Setup.elf \

HEX += \
Arduino_Setup.hex \



#bootloaderTest
BurnBootLoader: 
	@echo trying to burn bootloader ${bootloader.tool}
	${tools.${bootloader.tool}.erase.pattern}
	${tools.${bootloader.tool}.bootloader.pattern}

uploadWithBuild: all
	@echo trying to build and upload with upload tool ${upload.tool}
	${tools.${upload.tool}.upload.pattern}

uploadWithoutBuild: 
	@echo trying to upload without build with upload tool ${upload.tool}
	${tools.${upload.tool}.upload.pattern}
    
uploadWithProgrammerWithBuild: all
	@echo trying to build and upload with programmer ${program.tool}
	${tools.${program.tool}.program.pattern}

uploadWithProgrammerWithoutBuild: 
	@echo trying to upload with programmer ${program.tool} without build
	${tools.${program.tool}.program.pattern}

# All Target
all:
	-${recipe.hooks.prebuild.1.pattern}
	${recipe.hooks.prebuild.2.pattern}
	${recipe.hooks.prebuild.3.pattern}
	${recipe.hooks.prebuild.4.pattern}
	${recipe.hooks.prebuild.5.pattern}
	${recipe.hooks.prebuild.6.pattern}
	${recipe.hooks.prebuild.7.pattern} ||:
	$(MAKE) --no-print-directory main-build

# Main-build Target
main-build: Arduino_Setup

# Tool invocations
Arduino_Setup: $(HEX)
	@echo 'Building target: $@'
	@echo 'Printing size:'
	"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp-x32/2302/bin/xtensa-esp32-elf-size" -A "/Users/sahilsalma/PersonalData/UniversityStudies/Graduate/Spring2025/Release/Arduino_Setup.elf"
	@echo 'Finished building target: $@'
	@echo ' '

/Users/sahilsalma/PersonalData/UniversityStudies/Graduate/Spring2025/Release/arduino.ar: $(AR_OBJ)
	@echo 'Starting archiver'
	"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp-x32/2302/bin/xtensa-esp32-elf-gcc-ar" cr  "/Users/sahilsalma/PersonalData/UniversityStudies/Graduate/Spring2025/Release/arduino.ar" $(AR_OBJ)
	@echo 'Finished building: $@'
	@echo ' '

Arduino_Setup.elf: $(LINK_OBJ) $(AR)
	@echo 'Starting combiner'
	"/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp-x32/2302/bin/xtensa-esp32-elf-g++" "@/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/flags/ld_flags" "@/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/flags/ld_scripts" "-Wl,--Map=/Users/sahilsalma/PersonalData/UniversityStudies/Graduate/Spring2025/Release/Arduino_Setup.map" "-L/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/lib" "-L/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/ld" "-L/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/qio_qspi" "-Wl,--wrap=esp_panic_handler" -Wl,--start-group  $(LINK_OBJ) $(AR)   "@/Users/sahilsalma/eclipse/embedcpp-2024-09/Eclipse.app/Contents/Eclipse/arduinoPlugin/packages/esp32/tools/esp32-arduino-libs/idf-release_v5.1-b6b4727c58/esp32/flags/ld_libs"  -Wl,--end-group -Wl,-EL -o "/Users/sahilsalma/PersonalData/UniversityStudies/Graduate/Spring2025/Release/Arduino_Setup.elf"
	@echo 'Finished building: $@'
	@echo ' '

Arduino_Setup.hex: $(ELF)
	@echo 'Do all objcopy commands'
	${recipe.objcopy.bin.pattern}
	${recipe.objcopy.hex.pattern}
	${recipe.objcopy.partitions.bin.pattern}
	@echo 'Finished building: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(CCM_DEPS)$(C_UPPER_DEPS)$(PDE_DEPS)$(CXXM_DEPS)$(C_DEPS)$(AR)$(CC_DEPS)$(AR_OBJ)$(SX_DEPS)$(C++_DEPS)$(LINK_OBJ)$(CXX_DEPS)$(ASM_DEPS)$(HEX)$(INO_DEPS)$(SIZEDUMMY)$(S_UPPER_DEPS)$(C++M_DEPS)$(ELF)$(CPP_DEPS)
	-@echo ' '

.PHONY: all clean dependents main-build pre-build

-include ../makefile.targets
