package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveData(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object getData(String key) {
        Object data = redisTemplate.opsForValue().get(key);
        System.out.println("Retrieved data from Redis: " + data); // Debugging line
        return data;
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
