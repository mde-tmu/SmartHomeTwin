#ifndef CONFIGURATION_H
#define CONFIGURATION_H

#ifdef __cplusplus
extern "C" {
#endif

// Define pin types
typedef enum {
    PIN_TYPE_INPUT,
    PIN_TYPE_OUTPUT
} PinType;

// Structure to represent a single pin
typedef struct {
    const char* name;
    PinType type;
    int number;
} Pin;

// Structure to represent a component with pins
typedef struct {
    const char* name;
    Pin* pins;
    int pin_count;
} Component;

// --- Define Pins ---
static Pin mq6_pins[] = {
    { "mq6_in", PIN_TYPE_INPUT, 34 }
};

static Pin flame_pins[] = {
    { "flame_in", PIN_TYPE_INPUT, 13 }
};

static Pin heat_pins[] = {
    { "heat_in", PIN_TYPE_INPUT, 17 }
};

static Pin buzzer_pins[] = {
    { "buzzer_out", PIN_TYPE_OUTPUT, 21 }
};

static Pin button_pins[] = {
    { "reset_button", PIN_TYPE_INPUT, 15 },
	{ "open_button", PIN_TYPE_INPUT, 16 }
};

static Pin light_pins[] = {
    { "white_light", PIN_TYPE_OUTPUT, 26 },
};

static Pin blue_light_pins[] = {
    { "blue_light", PIN_TYPE_OUTPUT, 26 }
};

static Pin current_pins[] = {
    { "sda", PIN_TYPE_INPUT, 21 },
    { "scl", PIN_TYPE_INPUT, 22 }
};

static Pin servo_pins[] = {
	{ "servo_out", PIN_TYPE_OUTPUT, 13 }
};

static Pin ultra_sonic_pins[] = {
	{ "trig", PIN_TYPE_INPUT, 27 },
	{ "echo", PIN_TYPE_INPUT, 33 },
};

static Pin pir_pins[] = {
	{ "pir_in", PIN_TYPE_INPUT, 12 }
};

// --- Define Components ---
static Component MQ6_SENSOR = {
    "MQ6_SENSOR",
    mq6_pins,
    sizeof(mq6_pins) / sizeof(Pin)
};

static Component FLAME_SENSOR = {
    "FLAME_SENSOR",
    flame_pins,
    sizeof(flame_pins) / sizeof(Pin)
};

static Component HEAT_SENSOR = {
    "HEAT_SENSOR",
    heat_pins,
    sizeof(heat_pins) / sizeof(Pin)
};

static Component BUZZER = {
    "BUZZER",
    buzzer_pins,
    sizeof(buzzer_pins) / sizeof(Pin)
};

static Component BUTTON_RESET_COMPONENT = {
    "BUTTON_RESET",
    button_pins,
    sizeof(button_pins) / sizeof(Pin)
};

static Component BUTTON_OPEN_COMPONENT = {
    "BUTTON_OPEN",
    button_pins,
    sizeof(button_pins) / sizeof(Pin)
};

static Component LIGHT_WHITE_COMPONENT = {
    "LIGHT_WHITE",
    light_pins,
    sizeof(light_pins) / sizeof(Pin)
};

static Component CURRENT_SENSOR = {
    "CURRENT_SENSOR",
	current_pins,
    sizeof(current_pins) / sizeof(Pin)
};

static Component SERVO = {
    "GARAGE_DOOR_GATE",
	servo_pins,
    sizeof(servo_pins) / sizeof(Pin)
};

static Component ULTRASONIC_SENSOR = {
    "MOTION_SENSOR",
	ultra_sonic_pins,
    sizeof(ultra_sonic_pins) / sizeof(Pin)
};

static Component PIR_SENSOR = {
    "MOTION_SENSOR_2",
	pir_pins,
    sizeof(pir_pins) / sizeof(Pin)
};


// --- DHT Sensor Type ---
#define DHT_TYPE DHT22

#ifdef __cplusplus
}
#endif

#endif /* CONFIGURATION_H */
