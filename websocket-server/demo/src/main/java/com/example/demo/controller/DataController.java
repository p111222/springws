package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DataChangeService;
import com.example.demo.MyEntity;
import com.example.demo.service.RedisService;

@RestController
public class DataController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private DataChangeService dataChangeService;

    @GetMapping("/redis-data")
    public Object getRedisData() {
        return redisService.getData("myRedisEntityData");
    }

    @DeleteMapping("/entities/{id}")
    public void deleteEntity(@PathVariable Long id) {
        dataChangeService.deleteEntityById(id);
    }

    @PostMapping("/entities")
    public ResponseEntity<MyEntity> saveEntity(@RequestBody MyEntity entity) {
        return ResponseEntity.ok(dataChangeService.saveEntity(entity));
    }

    @PutMapping("/entities/{id}")
    public ResponseEntity<MyEntity> updateEntity(@PathVariable Long id, @RequestBody MyEntity entity) {
        return ResponseEntity.ok(dataChangeService.updateEntity(id, entity));
    }

}
