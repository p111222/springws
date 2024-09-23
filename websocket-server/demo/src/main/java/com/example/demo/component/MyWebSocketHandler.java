package com.example.demo.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.demo.modal.MyEntity;
import com.example.demo.service.DataChangeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new ArrayList<>();

     @Autowired
    private DataChangeService dataChangeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("session:-"+session);
        sessions.add(session);
        // Fetch data from Redis and send to the new client
        List<MyEntity> cachedData = dataChangeService.getCachedData();
        try {
            String jsonMessage = objectMapper.writeValueAsString(cachedData); // Use injected ObjectMapper
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen()) {
                try {
                    webSocketSession.sendMessage(new TextMessage(message.getPayload()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendMessageToClients(String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
