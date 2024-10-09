// // package com.example.demo;

// // import com.example.demo.service.RedisService;
// // import com.fasterxml.jackson.core.JsonProcessingException;
// // import com.fasterxml.jackson.databind.ObjectMapper;
// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.scheduling.annotation.Scheduled;
// // import org.springframework.stereotype.Service;

// // import java.util.List;
// // import java.util.concurrent.TimeUnit;

// // @Service
// // public class DataChangeService {

// //     @Autowired
// //     private MyWebSocketHandler webSocketHandler;

// //     @Autowired
// //     private MyRepository myRepository;

// //     @Autowired
// //     private ObjectMapper objectMapper; // Add ObjectMapper bean

// //     @Autowired
// //     private RedisService redisService;

// //     @Scheduled(fixedRate = 5000) // Poll every 5 seconds
// //     public void checkForChanges() {
// //         List<MyEntity> updatedData = myRepository.findAll();
// //         try {
// //             String jsonData = objectMapper.writeValueAsString(updatedData); // Serialize data to JSON

// //             redisService.saveData("myRedisEntityData", jsonData, 10, TimeUnit.MINUTES);

// //             System.out.println("Sending JSON data: " + jsonData); // Log data
// //             webSocketHandler.sendMessageToClients(jsonData); // Send JSON data to UI
// //         } catch (JsonProcessingException e) {
// //             e.printStackTrace();
// //         }
// //     }

// // }

// // package com.example.demo;

// // import com.fasterxml.jackson.core.JsonProcessingException;
// // import com.fasterxml.jackson.databind.ObjectMapper;
// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.scheduling.annotation.Scheduled;
// // import org.springframework.stereotype.Service;
// // import org.springframework.data.redis.core.RedisTemplate;

// // import java.time.LocalDateTime;
// // import java.time.format.DateTimeFormatter;
// // import java.util.List;
// // import java.util.concurrent.TimeUnit;

// // @Service
// // public class DataChangeService {

// //     @Autowired
// //     private MyWebSocketHandler webSocketHandler;

// //     @Autowired
// //     private MyRepository myRepository;

// //     @Autowired
// //     private ObjectMapper objectMapper;

// //     @Autowired
// //     private RedisTemplate<String, String> redisTemplate;

// //     private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

// //     @Scheduled(fixedRate = 5000)
// //     public void checkForChanges() {
// //         String cachedTimestamp = redisTemplate.opsForValue().get("myRedisEntityDataTimestamp");
// //         LocalDateTime cachedLastUpdated = (cachedTimestamp != null) ? parseCustomFormat(cachedTimestamp) : null;

// //         if (cachedLastUpdated == null || isSignificantChangeDetected(cachedLastUpdated)) {
// //             List<MyEntity> updatedData = myRepository.findAll();

// //             try {
// //                 String jsonData = objectMapper.writeValueAsString(updatedData);
// //                 redisTemplate.opsForValue().set("myRedisEntityData", jsonData, 10, TimeUnit.MINUTES);

// //                 LocalDateTime latestUpdated = updatedData.stream()
// //                     .map(MyEntity::getLastUpdated)
// //                     .max(LocalDateTime::compareTo)
// //                     .orElse(LocalDateTime.now());

// //                 redisTemplate.opsForValue().set("myRedisEntityDataTimestamp", latestUpdated.format(CUSTOM_FORMATTER), 10, TimeUnit.MINUTES);

// //                 webSocketHandler.sendMessageToClients(jsonData);
// //             } catch (JsonProcessingException e) {
// //                 e.printStackTrace();
// //             }
// //         } else {
// //             String cachedData = redisTemplate.opsForValue().get("myRedisEntityData");
// //             webSocketHandler.sendMessageToClients(cachedData);
// //         }
// //     }

// //     private boolean isSignificantChangeDetected(LocalDateTime cachedLastUpdated) {
// //         List<MyEntity> updatedData = myRepository.findAll();
// //         LocalDateTime latestUpdated = updatedData.stream()
// //             .map(MyEntity::getLastUpdated)
// //             .max(LocalDateTime::compareTo)
// //             .orElse(LocalDateTime.now());

// //         return latestUpdated.isAfter(cachedLastUpdated);
// //     }

// //     private LocalDateTime parseCustomFormat(String customTimestamp) {
// //         return LocalDateTime.parse(customTimestamp, CUSTOM_FORMATTER);
// //     }
// // }

// package com.example.demo;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.http.ResponseEntity;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.List;
// import java.util.concurrent.TimeUnit;
// import java.util.stream.Collectors;

// import javax.persistence.EntityNotFoundException;

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
//             List<MyEntity> currentData = myRepository.findAll();

//             try {
//                 String cachedDataJson = redisTemplate.opsForValue().get("myRedisEntityData");
//                 List<MyEntity> cachedData = cachedDataJson != null ? parseJsonToList(cachedDataJson) : List.of();

//                 List<Long> currentIds = currentData.stream().map(MyEntity::getId).collect(Collectors.toList());
//                 List<MyEntity> deletedEntities = cachedData.stream()
//                         .filter(entity -> !currentIds.contains(entity.getId()))
//                         .collect(Collectors.toList());

//                 String jsonData = objectMapper.writeValueAsString(currentData);
//                 redisTemplate.opsForValue().set("myRedisEntityData", jsonData, 10, TimeUnit.MINUTES);

//                 // Update the latest timestamp for Redis
//                 LocalDateTime latestUpdated = currentData.stream()
//                         .map(MyEntity::getLastUpdated)
//                         .max(LocalDateTime::compareTo)
//                         .orElse(LocalDateTime.now());
//                 redisTemplate.opsForValue().set("myRedisEntityDataTimestamp", latestUpdated.format(CUSTOM_FORMATTER),
//                         10, TimeUnit.MINUTES);

//                 webSocketHandler.sendMessageToClients(jsonData);

//                 if (!deletedEntities.isEmpty()) {
//                     String deletedDataJson = objectMapper.writeValueAsString(deletedEntities);
//                     webSocketHandler.sendMessageToClients("Deleted: " + deletedDataJson);
//                 }

//             } catch (JsonProcessingException e) {
//                 e.printStackTrace();
//             }
//         } else {
//             String cachedData = redisTemplate.opsForValue().get("myRedisEntityData");
//             webSocketHandler.sendMessageToClients(cachedData);
//         }
//     }

//     private boolean isSignificantChangeDetected(LocalDateTime cachedLastUpdated) {
//         List<MyEntity> currentData = myRepository.findAll();
//         LocalDateTime latestUpdated = currentData.stream()
//                 .map(MyEntity::getLastUpdated)
//                 .max(LocalDateTime::compareTo)
//                 .orElse(LocalDateTime.now());

//         return latestUpdated.isAfter(cachedLastUpdated);
//     }

//     private List<MyEntity> parseJsonToList(String json) throws JsonProcessingException {
//         return objectMapper.readValue(json,
//                 objectMapper.getTypeFactory().constructCollectionType(List.class, MyEntity.class));
//     }

//     private LocalDateTime parseCustomFormat(String customTimestamp) {
//         return LocalDateTime.parse(customTimestamp, CUSTOM_FORMATTER);
//     }

//     @Transactional
//     public void deleteEntityById(Long id) {
//         MyEntity entityToDelete = myRepository.findById(id).orElse(null);
//         if (entityToDelete != null) {
//             myRepository.deleteById(id);

//             updateCacheAndNotifyClients();
//         }
//     }

//     private void updateCacheAndNotifyClients() {
//         List<MyEntity> updatedData = myRepository.findAll();

//         try {
//             String jsonData = objectMapper.writeValueAsString(updatedData);

//             redisTemplate.opsForValue().set("myRedisEntityData", jsonData, 10, TimeUnit.MINUTES);

//             webSocketHandler.sendMessageToClients(jsonData);

//         } catch (JsonProcessingException e) {
//             e.printStackTrace();
//         }
//     }

//     public MyEntity saveEntity(MyEntity entity) {
//         entity.setLastUpdated(LocalDateTime.now());
//         return myRepository.save(entity);
//     }

//      @Transactional
//     public MyEntity updateEntity(Long id, MyEntity updatedEntity) {
//         MyEntity existingEntity = myRepository.findById(id).orElse(null);

//         if (existingEntity != null) {
//             existingEntity.setData(updatedEntity.getData()); 
//             existingEntity.setLastUpdated(LocalDateTime.now());

//             MyEntity savedEntity = myRepository.save(existingEntity);

//             updateCacheAndNotifyClients();

//             return savedEntity;
//         } else {
//             throw new EntityNotFoundException("Entity with id " + id + " not found");
//         }
//     }

// }

package com.example.demo.service;

import com.example.demo.modal.MyEntity;
import com.example.demo.repository.MyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// import org.apache.logging.log4j.util.PropertySource.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Comparator; 

@Service
public class DataChangeService {

    @Autowired
    private MyRepository myRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final String REDIS_KEY = "myRedisEntityData";

    @Transactional
    public MyEntity updateEntity(Long id, MyEntity updatedEntity) {
        MyEntity existingEntity = myRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found"));

        existingEntity.setId(updatedEntity.getId());
        existingEntity.setData(updatedEntity.getData());
        existingEntity.setLastUpdated(LocalDateTime.now());

        MyEntity savedEntity = myRepository.save(existingEntity);

        // eventPublisher.publishEvent(new EntityChangeEvent(this, savedEntity));

        return savedEntity;
    }

    @Transactional
    public MyEntity saveEntity(MyEntity entity) {
        entity.setLastUpdated(LocalDateTime.now());
        MyEntity savedEntity = myRepository.save(entity);

        List<MyEntity> allEntities = myRepository.findAll();
        updateCache(allEntities);

        // eventPublisher.publishEvent(new EntityChangeEvent(this, savedEntity));

        return savedEntity;
    }

    @Transactional
    public void deleteEntityById(Long id) {
        MyEntity entity = myRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Entity not found"));
        myRepository.delete(entity);

        // eventPublisher.publishEvent(new EntityChangeEvent(this, null));
    }

    // public void deleteEntity(Long id) {
    // redisTemplate.opsForValue().getOperations().delete(REDIS_KEY,id);
    // System.out.println("deletionnnn");
    // // myRepository.deleteById(id);
    // }

    @Transactional
    public String deleteEntity(Long id) {
        // Fetch the current data from Redis
        String jsonData = redisTemplate.opsForValue().get(REDIS_KEY);

        if (jsonData != null) {
            try {
                // Deserialize the cached JSON data into a list of MyEntity objects
                List<MyEntity> entities = objectMapper.readValue(jsonData, new TypeReference<List<MyEntity>>() {
                });

                // Remove the entity with the given ID from the list
                boolean removed = entities.removeIf(entity -> entity.getId().equals(id));

                if (removed) {
                    // Serialize the updated list back to JSON and update the cache
                    String updatedData = objectMapper.writeValueAsString(entities);
                    redisTemplate.opsForValue().set(REDIS_KEY, updatedData);
                    System.out.println("Entity with ID " + id + " removed from Redis cache.");

                    return "Entity with ID " + id + " deleted."; // Return the success message
                } else {
                    System.out.println("Entity with ID " + id + " not found in Redis cache.");
                    return "Entity with ID " + id + " not found.";
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return "Error processing JSON."; // Handle JSON processing errors
            }
        } else {
            System.out.println("No data found in Redis to delete.");
            return "No data found in Redis."; // Return a message indicating no data
        }
    }

    public void initializeCache() {
        List<MyEntity> currentData = myRepository.findAll();
        updateCache(currentData);
    }

    private void updateCache(List<MyEntity> data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(REDIS_KEY, jsonData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // public List<MyEntity> getCachedData() {
    // String jsonData = redisTemplate.opsForValue().get(REDIS_KEY);
    // System.out.println("jsonData"+jsonData);
    // if (jsonData != null) {
    // try {
    // return objectMapper.readValue(jsonData, new TypeReference<List<MyEntity>>() {
    // });
    // } catch (JsonProcessingException e) {
    // e.printStackTrace();
    // }
    // }
    // return new ArrayList<>();
    // }

    public List<MyEntity> getCachedData() {
        String jsonData = redisTemplate.opsForValue().get(REDIS_KEY);
        if (jsonData != null) {
            try {
                // Deserialize JSON into a list of MyEntity objects
                List<MyEntity> entityList = objectMapper.readValue(jsonData, new TypeReference<List<MyEntity>>() {
                });

                // Sort the list in reverse order by id
                entityList.sort(Comparator.comparing(MyEntity::getId).reversed());

                return entityList;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

}
