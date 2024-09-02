package com.example.config;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.model.S3Metadata;
import com.example.persistence.AwsRdsService;
import com.example.service.AwsSnsService;
import com.example.service.AwsSqsService;

import software.amazon.awssdk.services.sqs.model.Message;

@Service
public class BatchScheduledNotification {

    private AwsSnsService awsSnsService;
    private AwsSqsService awsSqsService;
    private AwsRdsService awsRdsService;

    public BatchScheduledNotification(AwsSnsService awsSnsService, AwsSqsService awsSqsService, AwsRdsService awsRdsService) {
		this.awsSnsService = awsSnsService;
		this.awsSqsService = awsSqsService;
        this.awsRdsService = awsRdsService;
	}

    @Scheduled(fixedRate = 180000)
    public void sendNotifications() {
        List<Message> messages = awsSqsService.receiveMessages();
        messages.forEach(message -> {
            StringBuilder messageBuilder = new StringBuilder();
            String fileName = message.body();
            S3Metadata s3Metadata = awsRdsService.getMetadata(fileName);
            messageBuilder.append("A new image has been uploaded.");
            messageBuilder.append(System.getProperty("line.separator"));
            messageBuilder.append(s3Metadata.toString());
            messageBuilder.append(System.getProperty("line.separator"));
            messageBuilder.append("Download link: <ec2-url>/api/s3/downloads?file=" + fileName);
            awsSnsService.publish(messageBuilder.toString(), s3Metadata.extension());
        });
    }
}
