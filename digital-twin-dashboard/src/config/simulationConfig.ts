// Simulation configuration for simulated mode
export interface SimulationConfig {
  apiUrl: string;
  timeout?: number;
}

// Default simulation configuration
export const DEFAULT_SIMULATION_CONFIG: SimulationConfig = {
  apiUrl: process.env.NEXT_PUBLIC_JAVA_BRIDGE_URL || 'http://localhost:8080',
  timeout: 5000 // 5 seconds
};

// System-specific simulation configurations (if needed)
export const SYSTEM_SIMULATION_CONFIGS: Record<string, Partial<SimulationConfig>> = {
  // Override specific configs per system if needed
};

// Helper function to get simulation config for a system
export function getSimulationConfig(systemName?: string): SimulationConfig {
  if (systemName && SYSTEM_SIMULATION_CONFIGS[systemName]) {
    return {
      ...DEFAULT_SIMULATION_CONFIG,
      ...SYSTEM_SIMULATION_CONFIGS[systemName]
    };
  }
  return DEFAULT_SIMULATION_CONFIG;
}
