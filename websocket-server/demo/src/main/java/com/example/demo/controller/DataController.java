// package com.example.demo.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.demo.DataChangeService;
// import com.example.demo.MyEntity;
// import com.example.demo.service.RedisService;

// @RestController
// @RequestMapping("/api") 
// public class DataController {

//     @Autowired
//     private RedisService redisService;

//     @Autowired
//     private DataChangeService dataChangeService;

//     @GetMapping("/redis-data")
//     public Object getRedisData() {
//         return redisService.getData("myRedisEntityData");
//     }

//     @DeleteMapping("/entities/{id}")
//     public void deleteEntity(@PathVariable Long id) {
//         dataChangeService.deleteEntityById(id);
//     }

//     @PostMapping("/entities")
//     public ResponseEntity<MyEntity> saveEntity(@RequestBody MyEntity entity) {
//         return ResponseEntity.ok(dataChangeService.saveEntity(entity));
//     }

//     @PutMapping("/entities/{id}")
//     public ResponseEntity<MyEntity> updateEntity(@PathVariable Long id, @RequestBody MyEntity entity) {
//         return ResponseEntity.ok(dataChangeService.updateEntity(id, entity));
//     }

// }

package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.component.MyWebSocketHandler;
import com.example.demo.modal.MyEntity;
import com.example.demo.service.DataChangeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api")
public class DataController {

    @Autowired
    private DataChangeService dataChangeService;

    @Autowired
    private MyWebSocketHandler webSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/redis-data")
    public Object getRedisData() {
        return dataChangeService.getCachedData();
    }

    @DeleteMapping("/entities/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable Long id) {
        dataChangeService.deleteEntityById(id);

        MyEntity deletedEntity = new MyEntity();
        deletedEntity.setId(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/entities")
    public ResponseEntity<MyEntity> saveEntity(@RequestBody MyEntity entity) {
        MyEntity savedEntity = dataChangeService.saveEntity(entity);
        return ResponseEntity.ok(savedEntity);
    }

    @PutMapping("/entities/{id}")
    public ResponseEntity<MyEntity> updateEntity(@PathVariable Long id, @RequestBody MyEntity updatedEntity) {
        MyEntity entity = dataChangeService.updateEntity(id, updatedEntity);
        return ResponseEntity.ok(entity);
    }
}
