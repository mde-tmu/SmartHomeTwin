"use client";

import { useState } from "react";
import { SystemSelector } from "@/components/SystemSelector";
import { Dashboard } from "@/components/Dashboard";
import { SystemStatusMonitor } from "@/components/SystemStatusMonitor";
import { ModeToggle } from "@/components/ModeToggle";

export default function Home() {
  const [selectedSystems, setSelectedSystems] = useState<string[]>([]);
  const [mode, setMode] = useState<"real" | "simulated">("real");

  return (
    <div className="min-h-screen bg-background grid-background">
      {/* Futuristic Header */}
      <header className="border-b border-primary/20 bg-surface/80 backdrop-blur-md">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <h1 className="text-2xl font-bold neon-text text-primary">
            ⚡ Smart Home Digital Twin Dashboard
          </h1>
          <div className="flex items-center gap-4">
            <ModeToggle mode={mode} onModeChange={setMode} />
          </div>
        </div>
      </header>

      <main className="container mx-auto px-6 py-8">
        {selectedSystems.length === 0 ? (
          <div className="max-w-7xl mx-auto">
            <div className="text-center mb-16">
              <h2 className="text-5xl font-bold mb-8 bg-gradient-to-r from-primary via-blue-600 to-purple-600 bg-clip-text text-transparent">
                Digital Twin Control Center
              </h2>
              <p className="text-muted-foreground text-xl leading-relaxed max-w-4xl mx-auto mb-12">
                Experience next-generation digital twin technology with real-time monitoring,
                advanced analytics, and intelligent reasoning. Control smart systems in real-time
                or simulate complex behaviors with our statechart engine.
              </p>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-4xl mx-auto mb-12">
                <div className="p-6 bg-gradient-to-br from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-xl border border-green-200 dark:border-green-800">
                  <div className="w-12 h-12 rounded-full bg-green-500 flex items-center justify-center mx-auto mb-4">
                    <div className="w-6 h-6 rounded-full bg-white animate-pulse"></div>
                  </div>
                  <h3 className="font-semibold text-lg mb-2">Real-time Monitoring</h3>
                  <p className="text-sm text-muted-foreground">Live system status and component health tracking</p>
                </div>

                <div className="p-6 bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-xl border border-blue-200 dark:border-blue-800">
                  <div className="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center mx-auto mb-4">
                    <div className="w-6 h-6 rounded-full bg-white animate-pulse"></div>
                  </div>
                  <h3 className="font-semibold text-lg mb-2">Advanced Analytics</h3>
                  <p className="text-sm text-muted-foreground">Power usage trends and system performance insights</p>
                </div>

                <div className="p-6 bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-xl border border-purple-200 dark:border-purple-800">
                  <div className="w-12 h-12 rounded-full bg-purple-500 flex items-center justify-center mx-auto mb-4">
                    <div className="w-6 h-6 rounded-full bg-white animate-pulse"></div>
                  </div>
                  <h3 className="font-semibold text-lg mb-2">Intelligent Reasoning</h3>
                  <p className="text-sm text-muted-foreground">AI-powered alerts and system optimization</p>
                </div>
              </div>
            </div>

            <div className="bg-card/50 backdrop-blur-sm rounded-2xl border border-border/50 p-8 shadow-xl">
              <SystemSelector
                onSystemsSelected={setSelectedSystems}
                mode={mode}
              />
            </div>
          </div>
        ) : (
          <div className="max-w-7xl mx-auto">
            <Dashboard
              selectedSystems={selectedSystems}
              mode={mode}
              onBackToSelection={() => setSelectedSystems([])}
            />
          </div>
        )}
      </main>
    </div>
  );
}
