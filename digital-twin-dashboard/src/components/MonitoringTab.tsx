"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { getEnabledModels, type ReasoningModelConfig } from "@/config/reasoningConfig";
import RealAnalyticsService from "@/lib/realAnalyticsService";
import RealReasoningService, { type ReasoningResult } from "@/lib/realReasoningService";
import {
  AlertTriangle,
  CheckCircle,
  XCircle,
  Clock,
  Loader2,
  RefreshCw,
  Brain,
  Bell,
  BellOff,
  Flame,
  Zap,
  Activity
} from "lucide-react";

interface AlertItem {
  id: string;
  type: 'critical' | 'warning' | 'info';
  title: string;
  description: string;
  timestamp: number;
  systemName?: string;
  acknowledged: boolean;
  source: string;
}

export function MonitoringTab() {
  const [alerts, setAlerts] = useState<AlertItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [enabledModels, setEnabledModels] = useState<ReasoningModelConfig[]>([]);
  const [lastAnalysis, setLastAnalysis] = useState<number>(Date.now());
  const [reasoningResults, setReasoningResults] = useState<ReasoningResult[]>([]);
  const [isRunningAnalysis, setIsRunningAnalysis] = useState(false);

  useEffect(() => {
    const loadMonitoringData = async () => {
      try {
        setIsLoading(true);

        // Initialize reasoning service
        RealReasoningService.initialize();

        // Get enabled reasoning models
        const models = getEnabledModels();
        setEnabledModels(models);

        // Get reasoning results
        const results = RealReasoningService.getAllResults();
        setReasoningResults(results);

        // Generate some sample alerts based on real system data
        const analytics = await RealAnalyticsService.getCurrentAnalytics();
        const generatedAlerts = await generateAlertsFromAnalytics(analytics);
        setAlerts(generatedAlerts);

        const lastAnalysisTime = RealReasoningService.getLastAnalysisTime();
        setLastAnalysis(lastAnalysisTime || Date.now());
      } catch (error) {
        console.error('Error loading monitoring data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    loadMonitoringData();

    // Refresh monitoring data every 30 seconds
    const interval = setInterval(loadMonitoringData, 30000);

    return () => clearInterval(interval);
  }, []);

  const generateAlertsFromAnalytics = async (analytics: any): Promise<AlertItem[]> => {
    const alerts: AlertItem[] = [];
    const now = Date.now();

    // Check for high power usage
    if (analytics.totalPowerUsage > 500) {
      alerts.push({
        id: `power-high-${now}`,
        type: 'warning',
        title: 'High Power Usage Detected',
        description: `Total power usage is ${analytics.totalPowerUsage}W, which exceeds the recommended threshold of 500W.`,
        timestamp: now,
        acknowledged: false,
        source: 'Power Anomaly Detector'
      });
    }

    if (analytics.totalPowerUsage < 200) {
      alerts.push({
        id: `power-low-${now}`,
        type: 'warning',
        title: 'Low Power Usage Detected',
        description: `Total power usage is ${analytics.totalPowerUsage}W, which is below the recommended threshold of 200W.`,
        timestamp: now,
        acknowledged: false,
        source: 'Power Anomaly Detector'
      });
    }

    // Check for low uptime systems
    // analytics.systemPowerData.forEach((system: any) => {
    //   if (!system.isOn) {
    //     alerts.push({
    //       id: `system-offline-${system.systemName}-${now}`,
    //       type: 'critical',
    //       title: 'System Offline',
    //       description: `${system.systemName.replace('Smart', '').replace('System', '')} is currently offline.`,
    //       timestamp: now,
    //       systemName: system.systemName,
    //       acknowledged: false,
    //       source: 'System Health Monitor'
    //     });
    //   }
    // });

    // // Check average uptime
    // if (analytics.averageUptime < 95) {
    //   alerts.push({
    //     id: `uptime-low-${now}`,
    //     type: 'warning',
    //     title: 'Low System Uptime',
    //     description: `Average system uptime is ${analytics.averageUptime.toFixed(1)}%, below the recommended 95%.`,
    //     timestamp: now,
    //     acknowledged: false,
    //     source: 'System Health Monitor'
    //   });
    // }

    // Add some informational alerts
    if (analytics.activeSystems === analytics.totalSystems) {
      alerts.push({
        id: `all-systems-online-${now}`,
        type: 'info',
        title: 'All Systems Online',
        description: `All ${analytics.totalSystems} systems are currently operational.`,
        timestamp: now,
        acknowledged: false,
        source: 'System Health Monitor'
      });
    }

    return alerts.sort((a, b) => b.timestamp - a.timestamp);
  };

  const acknowledgeAlert = (alertId: string) => {
    setAlerts(prev => prev.map(alert => 
      alert.id === alertId ? { ...alert, acknowledged: true } : alert
    ));
  };

  const getAlertIcon = (type: AlertItem['type']) => {
    switch (type) {
      case 'critical':
        return <XCircle className="h-4 w-4 text-red-500" />;
      case 'warning':
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      case 'info':
        return <CheckCircle className="h-4 w-4 text-blue-500" />;
      default:
        return <AlertTriangle className="h-4 w-4" />;
    }
  };

  const getAlertBadgeVariant = (type: AlertItem['type']) => {
    switch (type) {
      case 'critical':
        return 'destructive' as const;
      case 'warning':
        return 'secondary' as const;
      case 'info':
        return 'default' as const;
      default:
        return 'outline' as const;
    }
  };

  const criticalAlerts = alerts.filter(alert => alert.type === 'critical' && !alert.acknowledged);
  const warningAlerts = alerts.filter(alert => alert.type === 'warning' && !alert.acknowledged);
  const infoAlerts = alerts.filter(alert => alert.type === 'info' && !alert.acknowledged);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin mr-3" />
        <span className="text-lg">Loading monitoring data...</span>
      </div>
    );
  }

  return (
    <div className="space-y-8 p-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">System Monitoring</h2>
          <p className="text-muted-foreground">Real-time alerts and system health monitoring</p>
        </div>
        <div className="flex items-center space-x-2">
          <Badge variant="outline" className="text-sm">
            Last analysis: {new Date(lastAnalysis).toLocaleTimeString()}
          </Badge>
          <Button
            variant="outline"
            size="sm"
            disabled={isRunningAnalysis}
            onClick={async () => {
              setIsRunningAnalysis(true);
              try {
                await RealReasoningService.runAllAnalysis();
                const results = RealReasoningService.getAllResults();
                setReasoningResults(results);
                const lastAnalysisTime = RealReasoningService.getLastAnalysisTime();
                setLastAnalysis(lastAnalysisTime || Date.now());
              } catch (error) {
                console.error('Error running analysis:', error);
              } finally {
                setIsRunningAnalysis(false);
              }
            }}
          >
            {isRunningAnalysis ? (
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <Brain className="h-4 w-4 mr-2" />
            )}
            {isRunningAnalysis ? 'Running...' : 'Run Analysis'}
          </Button>
          <Button variant="outline" size="sm" onClick={() => window.location.reload()}>
            <RefreshCw className="h-4 w-4 mr-2" />
            Refresh
          </Button>
        </div>
      </div>

      {/* Alert Summary */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Critical Alerts</p>
                <p className="text-2xl font-bold text-red-600">{criticalAlerts.length}</p>
              </div>
              <XCircle className="h-8 w-8 text-red-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Warning Alerts</p>
                <p className="text-2xl font-bold text-yellow-600">{warningAlerts.length}</p>
              </div>
              <AlertTriangle className="h-8 w-8 text-yellow-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Info Alerts</p>
                <p className="text-2xl font-bold text-blue-600">{infoAlerts.length}</p>
              </div>
              <CheckCircle className="h-8 w-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Active Models</p>
                <p className="text-2xl font-bold">{enabledModels.length}</p>
              </div>
              <Brain className="h-8 w-8 text-green-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Active Reasoning Models */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Brain className="h-5 w-5 mr-2" />
            Active Reasoning Models
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {enabledModels.map((model) => (
              <Card key={model.id} className="border">
                <CardContent className="p-4">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-medium text-sm">{model.name}</h4>
                    <Badge variant="default" className="text-xs">
                      Active
                    </Badge>
                  </div>
                  <p className="text-xs text-muted-foreground mb-2">{model.description}</p>
                  <div className="flex items-center text-xs text-muted-foreground">
                    <Clock className="h-3 w-3 mr-1" />
                    Every {Math.floor(model.interval / (60 * 1000))} minutes
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Reasoning Results */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Brain className="h-5 w-5 mr-2" />
            Reasoning Analysis Results
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {reasoningResults.map((result) => {
              const getResultDisplay = () => {
                if (!result.success) {
                  return (
                    <div className="text-red-500 text-sm">
                      <XCircle className="h-4 w-4 inline mr-1" />
                      Error: {result.error}
                    </div>
                  );
                }

                switch (result.modelId) {
                  case 'early-fire-detection':
                    const fireData = result.data;
                    return (
                      <div className="space-y-2">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center">
                            <Flame className="h-4 w-4 text-orange-500 mr-2" />
                            <span className="text-sm font-medium">Fire Probability:</span>
                          </div>
                          <Badge variant={fireData.probability > 0.5 ? "destructive" : "default"}>
                            {(fireData.probability * 100).toFixed(1)}%
                          </Badge>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium">Status:</span>
                          <span className="text-sm font-medium">{fireData.status}</span>
                        </div>
                      </div>
                    );
                  case 'power-anomaly-detector':
                    const powerData = result.data;
                    return (
                      <div className="space-y-2">
                        <div className="flex items-center mb-2">
                          <Zap className="h-4 w-4 text-yellow-500 mr-2" />
                          <span className="text-sm font-medium">Power Analysis</span>
                        </div>
                        <div className="space-y-1 text-xs">
                          <div><strong>Most Power:</strong> {powerData.mostPowerUsedSystemName}</div>
                          <div><strong>Least Power:</strong> {powerData.leastPowerUsedSystemName}</div>
                          <div><strong>Idle System:</strong> {powerData.idleSystemName}</div>
                        </div>
                      </div>
                    );
                  case 'system-health-monitor':
                    const healthData = result.data;
                    return (
                      <div className="flex items-center">
                        <Activity className="h-4 w-4 text-green-500 mr-2" />
                        <span className="text-sm font-medium">Status: {healthData.status}</span>
                      </div>
                    );
                  default:
                    return <span className="text-sm text-muted-foreground">No data available</span>;
                }
              };

              return (
                <Card key={result.modelId} className="border">
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between mb-3">
                      <h4 className="font-medium text-sm">{result.modelName}</h4>
                      <Badge variant={result.success ? "default" : "destructive"} className="text-xs">
                        {result.success ? "Success" : "Failed"}
                      </Badge>
                    </div>
                    {getResultDisplay()}
                    <div className="flex items-center text-xs text-muted-foreground mt-3">
                      <Clock className="h-3 w-3 mr-1" />
                      {new Date(result.timestamp).toLocaleTimeString()}
                    </div>
                  </CardContent>
                </Card>
              );
            })}
            {reasoningResults.length === 0 && !isRunningAnalysis && (
              <div className="col-span-full text-center py-8 text-muted-foreground">
                <Brain className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>No reasoning results available</p>
                <p className="text-sm">Click "Run Analysis" to start reasoning models</p>
              </div>
            )}
            {isRunningAnalysis && (
              <div className="col-span-full text-center py-8">
                <Loader2 className="h-12 w-12 mx-auto mb-4 animate-spin text-primary" />
                <p className="text-lg font-medium">Running AI Analysis...</p>
                <p className="text-sm text-muted-foreground">This may take a few moments</p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Recent Alerts */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Bell className="h-5 w-5 mr-2" />
            Recent Alerts
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {alerts.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                <BellOff className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>No alerts at this time</p>
                <p className="text-sm">All systems are operating normally</p>
              </div>
            ) : (
              alerts.map((alert) => (
                <Alert key={alert.id} className={alert.acknowledged ? 'opacity-50' : ''}>
                  <div className="flex items-start justify-between">
                    <div className="flex items-start space-x-3">
                      {getAlertIcon(alert.type)}
                      <div className="flex-1">
                        <div className="flex items-center space-x-2 mb-1">
                          <h4 className="font-medium">{alert.title}</h4>
                          <Badge variant={getAlertBadgeVariant(alert.type)} className="text-xs">
                            {alert.type}
                          </Badge>
                          {alert.systemName && (
                            <Badge variant="outline" className="text-xs">
                              {alert.systemName.replace('Smart', '').replace('System', '')}
                            </Badge>
                          )}
                        </div>
                        <AlertDescription className="text-sm">
                          {alert.description}
                        </AlertDescription>
                        <div className="flex items-center space-x-4 mt-2 text-xs text-muted-foreground">
                          <span className="flex items-center">
                            <Clock className="h-3 w-3 mr-1" />
                            {new Date(alert.timestamp).toLocaleString()}
                          </span>
                          <span className="flex items-center">
                            <Brain className="h-3 w-3 mr-1" />
                            {alert.source}
                          </span>
                        </div>
                      </div>
                    </div>
                    {!alert.acknowledged && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => acknowledgeAlert(alert.id)}
                        className="text-xs"
                      >
                        Acknowledge
                      </Button>
                    )}
                  </div>
                </Alert>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
