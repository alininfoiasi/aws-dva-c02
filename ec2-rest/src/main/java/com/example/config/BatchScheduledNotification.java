package com.example.config;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.service.AwsSnsService;
import com.example.service.AwsSqsService;

import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class BatchScheduledNotification {

    private AwsSnsService awsSnsService;
    private AwsSqsService awsSqsService;

    public BatchScheduledNotification(AwsSnsService awsSnsService, AwsSqsService awsSqsService) {
		this.awsSnsService = awsSnsService;
		this.awsSqsService = awsSqsService;
	}

    public void sendNotifications() {
        List<Message> messages = awsSqsService.receiveMessages();
        messages.forEach(message -> {
            awsSnsService.publish(message.body());
        });
    }
}
