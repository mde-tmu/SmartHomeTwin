"use client";

import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { getSystemByName } from "@/config/systemConfig";
import { SystemCard } from "@/components/SystemCard";
import { MonitoringTab } from "@/components/MonitoringTab";
import { AnalyticsTab } from "@/components/AnalyticsTab";
import { SettingsPage } from "@/components/SettingsPage";
import { useDataTracking } from "@/hooks/useDataTracking";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ArrowLeft, Settings } from "lucide-react";

interface DashboardProps {
  selectedSystems: string[];
  mode: "real" | "simulated";
  onBackToSelection: () => void;
}

type DashboardView = 'main' | 'settings';

export function Dashboard({ selectedSystems, mode, onBackToSelection }: DashboardProps) {
  const [currentView, setCurrentView] = useState<DashboardView>('main');

  // Initialize data tracking for power usage and uptime
  useDataTracking();

  if (currentView === 'settings') {
    return <SettingsPage onBack={() => setCurrentView('main')} />;
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between p-6 bg-card/50 backdrop-blur-sm rounded-2xl border border-border/50 shadow-lg">
        <div className="flex items-center space-x-6">
          <Button variant="outline" onClick={onBackToSelection} className="hover:bg-primary/10">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Selection
          </Button>
          <div className="h-8 w-px bg-border/50"></div>
          <div>
            <h2 className="text-3xl font-bold bg-gradient-to-r from-primary to-blue-600 bg-clip-text text-transparent">
              Dashboard
            </h2>
            <p className="text-muted-foreground mt-1">
              {selectedSystems.length} system{selectedSystems.length > 1 ? 's' : ''} in {mode} mode
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-4">
          <Badge variant={mode === "real" ? "default" : "secondary"} className="px-4 py-2 text-sm font-medium">
            {mode === "real" ? "Real System" : "Simulated"}
          </Badge>
          {mode === "real" && <Button
            variant="outline"
            size="sm"
            className="hover:bg-primary/10"
            onClick={() => setCurrentView('settings')}
          >
            <Settings className="h-4 w-4 mr-2" />
            Settings
          </Button>}
        </div>
      </div>

      {/* Main Dashboard Tabs */}
      <div className="bg-card/30 backdrop-blur-sm rounded-2xl border border-border/50 overflow-hidden">
        <Tabs defaultValue="systems" className="w-full">
          <TabsList className="grid w-full grid-cols-4 h-14 bg-muted/50 rounded-none border-b border-border/50">
            <TabsTrigger value="systems" className="text-base font-medium h-12">
              Systems
            </TabsTrigger>
            {mode === "real" && <><TabsTrigger value="monitoring" className="text-base font-medium h-12">
              Monitoring
            </TabsTrigger>
            <TabsTrigger value="analytics" className="text-base font-medium h-12">
              Analytics
            </TabsTrigger>
            <TabsTrigger value="configuration" className="text-base font-medium h-12">
              Configuration
            </TabsTrigger></>}
          </TabsList>

          <TabsContent value="systems" className="p-8 space-y-8">
            {/* Systems Grid - 2 cards per row */}
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
              {selectedSystems.map(systemName => {
                const system = getSystemByName(systemName);

                if (!system) return null;

                return (
                  <SystemCard
                    key={systemName}
                    system={system}
                    mode={mode}
                  />
                );
              })}
            </div>
          </TabsContent>

          <TabsContent value="monitoring" className="p-8 space-y-8">
            <MonitoringTab
              systems={selectedSystems.map(name => getSystemByName(name)).filter(Boolean) as any[]}
              systemStates={{}} // This would be populated with actual system states
              mode={mode}
              onRefreshSystem={(systemName) => {
                console.log(`Refreshing system: ${systemName}`);
                // Implement system refresh logic
              }}
            />
          </TabsContent>

          <TabsContent value="analytics" className="p-8 space-y-8">
            <AnalyticsTab />
          </TabsContent>

          <TabsContent value="configuration" className="p-8">
            <div className="text-center py-16">
              <div className="max-w-md mx-auto">
                <Settings className="h-16 w-16 mx-auto mb-6 text-muted-foreground opacity-50" />
                <h3 className="text-2xl font-bold mb-4">Configuration</h3>
                <p className="text-muted-foreground mb-6">
                  Advanced system configuration and customization options will be available here.
                </p>
                <Badge variant="outline" className="text-sm">
                  Coming Soon
                </Badge>
              </div>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
