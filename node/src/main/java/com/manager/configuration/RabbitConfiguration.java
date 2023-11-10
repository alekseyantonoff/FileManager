package com.manager.configuration;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

@Configuration
public class RabbitConfiguration {
    @Bean
    public MessageConverter jasonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
