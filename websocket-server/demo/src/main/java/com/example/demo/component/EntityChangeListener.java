// package com.example.demo.component;
// package com.example.demo;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationListener;
// import org.springframework.stereotype.Component;

// @Component
// public class EntityChangeListener implements ApplicationListener<EntityChangeEvent> {

//     @Autowired
//     private DataChangeService dataChangeService;

//     @Autowired
//     private MyWebSocketHandler webSocketHandler;

//     @Autowired
//     private ObjectMapper objectMapper;  

//     @Override
//     public void onApplicationEvent(EntityChangeEvent event) {
//         dataChangeService.initializeCache();  

//         try {
//             String jsonMessage = objectMapper.writeValueAsString(event.getEntity());

//             webSocketHandler.sendMessageToClients(jsonMessage);
//         } catch (Exception e) {
//             e.printStackTrace();  
//         }
//     }
// }
