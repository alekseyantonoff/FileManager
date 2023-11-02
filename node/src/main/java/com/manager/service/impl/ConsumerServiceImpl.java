package com.manager.service.impl;

import com.manager.service.ConsumerService;
import com.manager.service.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.manager.model.RabbitQueue.TEXT_MESSAGE_UPDATE;

@Service
@Slf4j
public class ConsumerServiceImpl implements ConsumerService {

    private final ProducerService producerService;

    public ConsumerServiceImpl(ProducerService producerService) {
        this.producerService = producerService;
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.info("NODE: Text message is received");


        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from NODE");
        producerService.producerAnswer(sendMessage);
    }

    @Override
    public void consumeDocMessageUpdates(Update update) {
        log.info("NODE: Doc message is received");
    }

    @Override
    public void consumePhotoMessageUpdates(Update update) {
        log.info("NODE: Photo message is received");
    }
}
