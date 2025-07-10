# Digital Twin Dashboard

A comprehensive Digital Twin Dashboard built with Next.js that can switch between Real System mode (direct Firebase connection) and Simulated mode (state chart logic). The dashboard allows users to select and monitor various smart home systems including garage doors, lights, TVs, microwaves, fire detection, and traffic lights.

## Features

### 🏠 Smart Home Systems
- **Smart Garage Door System** - Automated garage door with ultrasonic motion detection
- **Smart Light System** - LED lighting with motion detection and brightness control
- **Smart TV System** - Television control with remote functionality
- **Smart Microwave System** - Microwave control with timer and safety features
- **Smart Fire Detection System** - Fire detection with smoke and heat sensors
- **Smart Traffic Light System** - Intelligent traffic light with vehicle detection

### 🔄 Dual Operation Modes
- **Real System Mode**: Direct connection to Firebase Realtime Database for actual IoT devices
- **Simulated Mode**: State chart-based simulation engine for testing and demonstration

### 🎛️ Dynamic Dashboard
- **System Selection**: Choose which systems to include in your dashboard
- **Component Control**: Interactive controls for sensors, actuators, and controllers
- **Real-time Monitoring**: Live data visualization and status indicators
- **Responsive Design**: Works on desktop, tablet, and mobile devices

## Technology Stack

- **Frontend**: Next.js 15, React 18, TypeScript
- **UI Components**: Shadcn/ui, Tailwind CSS, Lucide React icons
- **Backend**: Firebase Realtime Database
- **State Management**: React hooks with real-time subscriptions
- **Simulation**: Custom state chart engine based on generated Java files

## Getting Started

### Prerequisites
- Node.js 18+
- npm or yarn
- Firebase project (for real system mode)

### Installation

1. Install dependencies:
```bash
npm install
```

2. Configure Firebase (optional for demo mode):
```bash
cp .env.local.example .env.local
# Edit .env.local with your Firebase configuration
```

3. Start the development server:
```bash
npm run dev
```

4. Open [http://localhost:3000](http://localhost:3000) in your browser

## System Architecture

### Component Hierarchy
Each system follows a consistent pattern:
- **System**: Main system control (on/off, network connection)
- **Components**: Individual functional units
  - **Sensors**: Motion, ultrasonic, smoke, heat, vehicle detection
  - **Actuators**: Garage doors, lights, alarms, sprinklers
  - **Controllers**: Logic processing and decision making
  - **Utilities**: Network, power, temperature monitoring

### State Management
- **Real Mode**: Direct Firebase Realtime Database integration
- **Simulated Mode**: Local state chart engine with Firebase persistence
- **Real-time Updates**: WebSocket-based subscriptions for live data

## Usage

### Selecting Systems
1. Choose between Real System or Simulated mode
2. Select one or more systems from the available options
3. Click "Create Dashboard" to proceed

### Controlling Systems
1. Toggle system power using the main switch
2. Interact with individual components:
   - **Garage Door**: Open/Close buttons
   - **Lights**: On/Off switch with brightness display
   - **TV**: Power control with volume/channel info
   - **Microwave**: Start/Stop with timer
   - **Sensors**: View readings and simulate detection
