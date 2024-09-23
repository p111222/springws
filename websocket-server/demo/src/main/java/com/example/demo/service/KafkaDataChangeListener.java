package com.example.demo.service;

// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;

// import java.util.Map;

// @Service
// public class KafkaDataChangeListener {

//     @KafkaListener(topics = "mysql-db-connector3.mydatabase.mytable", groupId = "mysql")
//     public void consume(Map<String, Object> message) {
//         // Example: Extracting the 'after' field from the message
//         Map<String, Object> payload = (Map<String, Object>) message.get("payload");

//         Map<String, Object> after = (Map<String, Object>) payload.get("after");
//         if (after != null) {
//             System.out.println("Received New Record: ");
//             System.out.println("ID: " + after.get("id"));
//             System.out.println("Data: " + after.get("data"));
//             System.out.println("Last Updated: " + after.get("last_updated"));
//         } else {
//             System.out.println("No new data");
//         }

//         // You can also extract metadata from the 'source' field
//         Map<String, Object> source = (Map<String, Object>) payload.get("source");
//         if (source != null) {
//             System.out.println("Source DB: " + source.get("db"));
//             System.out.println("Table: " + source.get("table"));
//         }
//     }
// }

import com.example.demo.modal.MyEntity;
import com.example.demo.repository.MyRepository;
import com.example.demo.component.MyWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaDataChangeListener {

    @Autowired
    private DataChangeService dataChangeService;

    @Autowired
    private MyWebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "mysql-db-connector3.mydatabase.mytable", groupId = "mysql")
    @Transactional
    public void consume(Map<String, Object> message) {
        Map<String, Object> payload = (Map<String, Object>) message.get("payload");

        // If payload is missing, log the message and return to avoid further processing
        if (payload == null) {
            System.out.println("Payload is missing in the Kafka message: " + message);
            return;
        }

        Map<String, Object> after = (Map<String, Object>) payload.get("after");
        Map<String, Object> before = (Map<String, Object>) payload.get("before");

        // Handle the missing "before" and "after" data more gracefully
        if (after == null && before == null) {
            System.out.println("Both 'after' and 'before' are missing in the Kafka message: " + message);
            return;
        }

        // Extract the ID from either the "after" or "before" object
        Object idObject = (after != null) ? after.get("id") : (before != null) ? before.get("id") : null;

        // If ID is missing, print an error message and return
        if (idObject == null) {
            System.out.println("ID is missing in the Kafka message: " + message);
            return;
        }

        // Convert the ID to String for further processing
        String id;
        if (idObject instanceof Integer) {
            id = String.valueOf(idObject); // Convert Integer to String
        } else if (idObject instanceof String) {
            id = (String) idObject; // Already a String
        } else {
            throw new IllegalArgumentException("Unexpected type for id: " + idObject.getClass().getName());
        }

        // Handle entity creation or update
        if (after != null) {
            Long longId = Long.valueOf(id);
            String data = (String) after.get("data");
            String lastUpdated = (String) after.get("last_updated");

            // Convert the last_updated timestamp to LocalDateTime
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(lastUpdated);
            LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();

            // Create and save the entity
            MyEntity entity = new MyEntity(longId, data, localDateTime);
            dataChangeService.saveEntity(entity);

            // Send the saved entity details via WebSocket
            try {
                String jsonMessage = objectMapper.writeValueAsString(entity);
                webSocketHandler.sendMessageToClients(jsonMessage);
                System.out.println("Entity saved and message sent: " + jsonMessage);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            // Handle entity deletion
        } else if (before != null) { // Handle entity deletion
            Long longId = Long.valueOf(id);
            String deleteMessage = dataChangeService.deleteEntity(longId);
            System.out.println("Deleted message in Kafka listener: " + deleteMessage);

            // Send deletion message
            try {
                Map<String, Object> responseMessage = new HashMap<>();
                responseMessage.put("action", "delete");
                responseMessage.put("id", longId);
                responseMessage.put("message", deleteMessage);
                String jsonMessage = objectMapper.writeValueAsString(responseMessage);
                webSocketHandler.sendMessageToClients(jsonMessage);
                System.out.println("Delete message sent: " + jsonMessage);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    // @KafkaListener(topics = "mysql-db-connector3.mydatabase.mytable", groupId =
    // "mysql")
    // @Transactional
    // public void consume(Map<String, Object> message) {
    // // Extract the operation type
    // String operation = (String) message.get("op");

    // if (operation == null) {
    // System.out.println("Operation type is missing in the Kafka message: " +
    // message);
    // return;
    // }

    // // Extract the payload and "after" and "before" objects
    // Map<String, Object> payload = (Map<String, Object>) message.get("payload");
    // Map<String, Object> after = (Map<String, Object>) payload.get("after");
    // Map<String, Object> before = (Map<String, Object>) payload.get("before");

    // switch (operation) {
    // case "c": // Insert or Create operation
    // if (after != null) {
    // // Extract 'id' from the 'after' payload
    // Object afterIdObject = after.get("id");

    // if (afterIdObject == null) {
    // System.out.println("ID is missing in the 'after' section of the Kafka
    // message");
    // return;
    // }

    // Long longId = Long.valueOf(afterIdObject.toString()); // Convert to Long
    // String data = (String) after.get("data");
    // String lastUpdated = (String) after.get("last_updated");

    // // Create a new entity using the extracted 'id'
    // MyEntity entity = new MyEntity(longId, data,
    // LocalDateTime.parse(lastUpdated));
    // dataChangeService.saveEntity(entity);

    // try {
    // String jsonMessage = objectMapper.writeValueAsString(entity);
    // webSocketHandler.sendMessageToClients(jsonMessage);
    // } catch (JsonProcessingException e) {
    // e.printStackTrace();
    // }
    // } else {
    // System.out.println("After object is null in the Kafka message");
    // }
    // break;

    // case "u": // Update operation
    // if (after != null) {
    // // Extract 'id' from the 'after' payload
    // Object afterIdObject = after.get("id");

    // if (afterIdObject == null) {
    // System.out.println("ID is missing in the 'after' section of the Kafka
    // message");
    // break;
    // }

    // Long longId = Long.valueOf(afterIdObject.toString());
    // String data = (String) after.get("data");
    // String lastUpdated = (String) after.get("last_updated");

    // // Create updated entity
    // MyEntity updatedEntity = new MyEntity(longId, data,
    // LocalDateTime.parse(lastUpdated));

    // // Call updateEntity with both id and updated entity
    // dataChangeService.updateEntity(longId, updatedEntity);

    // try {
    // String jsonMessage = objectMapper.writeValueAsString(updatedEntity);
    // webSocketHandler.sendMessageToClients("Entity with ID " + longId + "
    // updated");
    // } catch (JsonProcessingException e) {
    // e.printStackTrace();
    // }
    // } else {
    // System.out.println("After object is null in the Kafka message");
    // }
    // break;

    // case "d": // Delete operation
    // if (before != null) {
    // Object beforeIdObject = before.get("id");

    // if (beforeIdObject == null) {
    // System.out.println("ID is missing in the 'before' section of the Kafka
    // message");
    // return;
    // }

    // Long longId = Long.valueOf(beforeIdObject.toString());
    // dataChangeService.deleteEntity(longId);
    // webSocketHandler.sendMessageToClients("Entity with ID " + longId + "
    // deleted");
    // } else {
    // System.out.println("Before object is null in the Kafka message");
    // }
    // break;

    // default:
    // System.out.println("Unhandled operation type: " + operation);
    // break;
    // }

    // // Process other metadata like ts_ms, server_id, etc., if needed
    // Long timestamp = (Long) message.get("ts_ms");
    // System.out.println("Message processed with timestamp: " + timestamp);
    // }

}
