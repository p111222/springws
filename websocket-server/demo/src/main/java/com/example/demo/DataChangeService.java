package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataChangeService {

    @Autowired
    private MyWebSocketHandler webSocketHandler;

    @Autowired
    private MyRepository myRepository;

    @Autowired
    private ObjectMapper objectMapper; // Add ObjectMapper bean

    @Scheduled(fixedRate = 5000)  // Poll every 5 seconds
    public void checkForChanges() {
        List<MyEntity> updatedData = myRepository.findAll();
        try {
            String jsonData = objectMapper.writeValueAsString(updatedData); // Serialize data to JSON
            System.out.println("Sending JSON data: " + jsonData); // Log data
            webSocketHandler.sendMessageToClients(jsonData);  // Send JSON data to UI
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    
}
