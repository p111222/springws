// package com.example.demo;

// import com.example.demo.service.RedisService;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.concurrent.TimeUnit;

// @Service
// public class DataChangeService {

//     @Autowired
//     private MyWebSocketHandler webSocketHandler;

//     @Autowired
//     private MyRepository myRepository;

//     @Autowired
//     private ObjectMapper objectMapper; // Add ObjectMapper bean

//     @Autowired
//     private RedisService redisService;

//     @Scheduled(fixedRate = 5000) // Poll every 5 seconds
//     public void checkForChanges() {
//         List<MyEntity> updatedData = myRepository.findAll();
//         try {
//             String jsonData = objectMapper.writeValueAsString(updatedData); // Serialize data to JSON

//             redisService.saveData("myRedisEntityData", jsonData, 10, TimeUnit.MINUTES);

//             System.out.println("Sending JSON data: " + jsonData); // Log data
//             webSocketHandler.sendMessageToClients(jsonData); // Send JSON data to UI
//         } catch (JsonProcessingException e) {
//             e.printStackTrace();
//         }
//     }

// }

// package com.example.demo;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.data.redis.core.RedisTemplate;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.List;
// import java.util.concurrent.TimeUnit;

// @Service
// public class DataChangeService {

//     @Autowired
//     private MyWebSocketHandler webSocketHandler;

//     @Autowired
//     private MyRepository myRepository;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Autowired
//     private RedisTemplate<String, String> redisTemplate;

//     private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

//     @Scheduled(fixedRate = 5000)
//     public void checkForChanges() {
//         String cachedTimestamp = redisTemplate.opsForValue().get("myRedisEntityDataTimestamp");
//         LocalDateTime cachedLastUpdated = (cachedTimestamp != null) ? parseCustomFormat(cachedTimestamp) : null;

//         if (cachedLastUpdated == null || isSignificantChangeDetected(cachedLastUpdated)) {
//             List<MyEntity> updatedData = myRepository.findAll();

//             try {
//                 String jsonData = objectMapper.writeValueAsString(updatedData);
//                 redisTemplate.opsForValue().set("myRedisEntityData", jsonData, 10, TimeUnit.MINUTES);

//                 LocalDateTime latestUpdated = updatedData.stream()
//                     .map(MyEntity::getLastUpdated)
//                     .max(LocalDateTime::compareTo)
//                     .orElse(LocalDateTime.now());

//                 redisTemplate.opsForValue().set("myRedisEntityDataTimestamp", latestUpdated.format(CUSTOM_FORMATTER), 10, TimeUnit.MINUTES);

//                 webSocketHandler.sendMessageToClients(jsonData);
//             } catch (JsonProcessingException e) {
//                 e.printStackTrace();
//             }
//         } else {
//             String cachedData = redisTemplate.opsForValue().get("myRedisEntityData");
//             webSocketHandler.sendMessageToClients(cachedData);
//         }
//     }

//     private boolean isSignificantChangeDetected(LocalDateTime cachedLastUpdated) {
//         List<MyEntity> updatedData = myRepository.findAll();
//         LocalDateTime latestUpdated = updatedData.stream()
//             .map(MyEntity::getLastUpdated)
//             .max(LocalDateTime::compareTo)
//             .orElse(LocalDateTime.now());

//         return latestUpdated.isAfter(cachedLastUpdated);
//     }

//     private LocalDateTime parseCustomFormat(String customTimestamp) {
//         return LocalDateTime.parse(customTimestamp, CUSTOM_FORMATTER);
//     }
// }

package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

@Service
public class DataChangeService {

    @Autowired
    private MyWebSocketHandler webSocketHandler;

    @Autowired
    private MyRepository myRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Scheduled(fixedRate = 5000)
    public void checkForChanges() {
        String cachedTimestamp = redisTemplate.opsForValue().get("myRedisEntityDataTimestamp");
        LocalDateTime cachedLastUpdated = (cachedTimestamp != null) ? parseCustomFormat(cachedTimestamp) : null;

        if (cachedLastUpdated == null || isSignificantChangeDetected(cachedLastUpdated)) {
            List<MyEntity> currentData = myRepository.findAll();

            try {
                String cachedDataJson = redisTemplate.opsForValue().get("myRedisEntityData");
                List<MyEntity> cachedData = cachedDataJson != null ? parseJsonToList(cachedDataJson) : List.of();

                List<Long> currentIds = currentData.stream().map(MyEntity::getId).collect(Collectors.toList());
                List<MyEntity> deletedEntities = cachedData.stream()
                        .filter(entity -> !currentIds.contains(entity.getId()))
                        .collect(Collectors.toList());

                String jsonData = objectMapper.writeValueAsString(currentData);
                redisTemplate.opsForValue().set("myRedisEntityData", jsonData, 10, TimeUnit.MINUTES);

                // Update the latest timestamp for Redis
                LocalDateTime latestUpdated = currentData.stream()
                        .map(MyEntity::getLastUpdated)
                        .max(LocalDateTime::compareTo)
                        .orElse(LocalDateTime.now());
                redisTemplate.opsForValue().set("myRedisEntityDataTimestamp", latestUpdated.format(CUSTOM_FORMATTER),
                        10, TimeUnit.MINUTES);

                webSocketHandler.sendMessageToClients(jsonData);

                if (!deletedEntities.isEmpty()) {
                    String deletedDataJson = objectMapper.writeValueAsString(deletedEntities);
                    webSocketHandler.sendMessageToClients("Deleted: " + deletedDataJson);
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            String cachedData = redisTemplate.opsForValue().get("myRedisEntityData");
            webSocketHandler.sendMessageToClients(cachedData);
        }
    }

    private boolean isSignificantChangeDetected(LocalDateTime cachedLastUpdated) {
        List<MyEntity> currentData = myRepository.findAll();
        LocalDateTime latestUpdated = currentData.stream()
                .map(MyEntity::getLastUpdated)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        return latestUpdated.isAfter(cachedLastUpdated);
    }

    private List<MyEntity> parseJsonToList(String json) throws JsonProcessingException {
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MyEntity.class));
    }

    private LocalDateTime parseCustomFormat(String customTimestamp) {
        return LocalDateTime.parse(customTimestamp, CUSTOM_FORMATTER);
    }

    @Transactional
    public void deleteEntityById(Long id) {
        MyEntity entityToDelete = myRepository.findById(id).orElse(null);
        if (entityToDelete != null) {
            myRepository.deleteById(id);

            updateCacheAndNotifyClients();
        }
    }

    private void updateCacheAndNotifyClients() {
        List<MyEntity> updatedData = myRepository.findAll();

        try {
            String jsonData = objectMapper.writeValueAsString(updatedData);

            redisTemplate.opsForValue().set("myRedisEntityData", jsonData, 10, TimeUnit.MINUTES);

            webSocketHandler.sendMessageToClients(jsonData);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public MyEntity saveEntity(MyEntity entity) {
        entity.setLastUpdated(LocalDateTime.now());
        return myRepository.save(entity);
    }

     @Transactional
    public MyEntity updateEntity(Long id, MyEntity updatedEntity) {
        MyEntity existingEntity = myRepository.findById(id).orElse(null);

        if (existingEntity != null) {
            existingEntity.setData(updatedEntity.getData()); 
            existingEntity.setLastUpdated(LocalDateTime.now());

            MyEntity savedEntity = myRepository.save(existingEntity);

            updateCacheAndNotifyClients();

            return savedEntity;
        } else {
            throw new EntityNotFoundException("Entity with id " + id + " not found");
        }
    }

}
