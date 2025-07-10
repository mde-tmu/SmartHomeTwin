# Digital Twin Java Bridge

This is a Spring Boot application that provides a REST API bridge to the actual Java statechart implementations for the Digital Twin Dashboard.

## Overview

The Java Bridge allows the TypeScript frontend to interact with the real Java statechart classes instead of using mock implementations. It provides:

- **System Management**: Create, start, stop, and remove statechart systems
- **Event Handling**: Raise events on components (e.g., open/close garage door)
- **Environment Data**: Set sensor environment data for simulation
- **State Reading**: Get current system and component states
- **Statechart Execution**: Run statechart cycles

## Supported Systems

- SmartGarageDoorSystem
- SmartLightSystem  
- SmartMicrowaveSystem
- SmartFireSystem
- SmartTrafficLightSystem

## API Endpoints

### System Management
- `POST /api/statechart/initialize` - Initialize all system types (creates and starts all available systems)
- `POST /api/statechart/systems/{systemId}?systemType={type}` - Create system
- `POST /api/statechart/systems/{systemId}/start` - Start system
- `POST /api/statechart/systems/{systemId}/stop` - Stop system
- `DELETE /api/statechart/systems/{systemId}` - Remove system
- `GET /api/statechart/systems` - Get all systems and their states

### Execution
- `POST /api/statechart/systems/{systemId}/runCycle` - Execute statechart cycle
- `GET /api/statechart/systems/{systemId}/state` - Get system state

### Interaction
- `POST /api/statechart/systems/{systemId}/setEnvironmentData?sensorType={type}&value={value}` - Set sensor data
- `POST /api/statechart/systems/{systemId}/raiseEvent?componentName={component}&eventName={event}` - Raise event

## Building and Running

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Quick Start
```bash
# Make script executable (if not already)
chmod +x run.sh

# Build and run
./run.sh
```

### Manual Build
```bash
# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

## Integration with Frontend

The TypeScript `StatechartEngine` class automatically connects to this Java bridge when running. Make sure the Java bridge is running before starting the frontend application.

## Example Usage

1. **Create a garage door system:**
   ```
   POST /api/statechart/systems/garage1?systemType=SmartGarageDoorSystem
   ```

2. **Start the system:**
   ```
   POST /api/statechart/systems/garage1/start
   ```

3. **Set obstacle distance:**
   ```
   POST /api/statechart/systems/garage1/setEnvironmentData?sensorType=ultrasonic&value=30
   ```

4. **Open the garage door:**
   ```
   POST /api/statechart/systems/garage1/raiseEvent?componentName=GarageDoor_Unit&eventName=raiseOpen_door
   ```

5. **Get system state:**
   ```
   GET /api/statechart/systems/garage1/state
   ```

## Architecture

The bridge works by:
1. Instantiating actual Java statechart classes from the generated code
2. Setting up proper component relationships and timer services
3. Exposing REST endpoints for interaction
4. Managing system lifecycle and state

This ensures that the simulation uses the exact same Java statechart logic that would run in the real system.
