package com.digitaltwin.bridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast system state change to all connected clients
     */
    public void broadcastSystemStateChange(String systemId, Map<String, Object> systemState) {
        // Send to topic for all subscribers
        messagingTemplate.convertAndSend("/topic/systems/" + systemId + "/state", systemState);
        
        // Also send to general system updates topic
        Map<String, Object> updateMessage = Map.of(
            "systemId", systemId,
            "type", "STATE_UPDATE",
            "timestamp", System.currentTimeMillis(),
            "data", systemState
        );
        messagingTemplate.convertAndSend("/topic/system-updates", updateMessage);
    }

    /**
     * Broadcast system event to all connected clients
     */
    public void broadcastSystemEvent(String systemId, String eventType, Map<String, Object> eventData) {
        Map<String, Object> eventMessage = Map.of(
            "systemId", systemId,
            "type", eventType,
            "timestamp", System.currentTimeMillis(),
            "data", eventData
        );
        messagingTemplate.convertAndSend("/topic/systems/" + systemId + "/events", eventMessage);
        messagingTemplate.convertAndSend("/topic/system-updates", eventMessage);
    }

    /**
     * Broadcast component state change
     */
    public void broadcastComponentStateChange(String systemId, String componentName, Map<String, Object> componentState) {
        Map<String, Object> updateMessage = Map.of(
            "systemId", systemId,
            "componentName", componentName,
            "type", "COMPONENT_UPDATE",
            "timestamp", System.currentTimeMillis(),
            "data", componentState
        );
        messagingTemplate.convertAndSend("/topic/systems/" + systemId + "/components/" + componentName, updateMessage);
        messagingTemplate.convertAndSend("/topic/system-updates", updateMessage);
    }
}
