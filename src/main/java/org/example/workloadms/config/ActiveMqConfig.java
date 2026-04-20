package org.example.workloadms.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJms
public class ActiveMqConfig {

    @Value("${spring.activemq.user}")
    private String user;

    @Value("${spring.activemq.password}")
    private String password;

    @Value("${spring.activemq.broker-url}")
    private String broker;

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        return new ActiveMQConnectionFactory(user, password, broker);
    }

    @Bean
    public JmsTemplate jmsTemplate(MessageConverter messageConverter) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(activeMQConnectionFactory());
        jmsTemplate.setMessageConverter(messageConverter);
        return jmsTemplate;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(MessageConverter messageConverter) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory());
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("TrainerWorkloadRequest", TrainerWorkloadRequest.class);
        converter.setTypeIdMappings(typeIdMappings);
        return converter;
    }
}