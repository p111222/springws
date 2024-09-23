package com.example.demo.config;

// import org.apache.kafka.clients.consumer.ConsumerConfig;
// import org.apache.kafka.common.serialization.StringDeserializer;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.kafka.annotation.EnableKafka;
// import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
// import org.springframework.kafka.core.ConsumerFactory;
// import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
// import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
// import org.springframework.kafka.support.serializer.JsonDeserializer;

// import com.example.demo.modal.MyEntity;

// import org.springframework.kafka.listener.ContainerProperties.AckMode;

// import java.util.HashMap;
// import java.util.Map;

// @Configuration
// @EnableKafka
// public class KafkaConfig {

//     private static final String BOOTSTRAP_SERVERS = "3.108.54.64:9096";
//     private static final String GROUP_ID = "real-time-data-mysql-1";

//     @Bean
//     public ConsumerFactory<String, MyEntity> consumerFactory() {
//         Map<String, Object> props = new HashMap<>();
//         props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
//         props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
//         props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//         props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
//         props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
//         props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // Adjust based on your needs
//         // props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, MyEntity.class.getName()); // Set to MyEntity

//         props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//         props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
//         return new DefaultKafkaConsumerFactory<>(props);
//     }

//     @Bean
//     public ConcurrentKafkaListenerContainerFactory<String, MyEntity> kafkaListenerContainerFactory() {
//         ConcurrentKafkaListenerContainerFactory<String, MyEntity> factory =
//                 new ConcurrentKafkaListenerContainerFactory<>();
//         factory.setConsumerFactory(consumerFactory());
//         // Set acknowledgment mode (MANUAL)
//         factory.getContainerProperties().setAckMode(AckMode.MANUAL);
//         return factory;
//     }
// }


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, Map<String, Object>> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "3.108.54.64:9096");
        // props.put(ConsumerConfig.GROUP_ID_CONFIG, "mysql-real-time-data-1");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "mysql");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Configure the JsonDeserializer to deserialize nested structures
        JsonDeserializer<Map<String, Object>> deserializer = new JsonDeserializer<>(Map.class);
        deserializer.addTrustedPackages("*"); // Adjust this to secure your deserialization packages
        
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
