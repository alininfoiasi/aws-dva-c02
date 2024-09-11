package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.model.S3Metadata;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Service
public class AwsSqsService {
    // TODO: read arns / ids from Parameter Store
    private static final String QUEUE_NAME = "SQS-api_images";
    private static final Region REGION = Region.EU_NORTH_1;

    public boolean sendMessage(S3Metadata s3Metadata) {
        SqsClient sqsClient = getSqsClient();
        String message = getS3MetadataMessage(s3Metadata);
        try {
            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(QUEUE_NAME)
                    .build();
            String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .delaySeconds(5)
                    .build();
            sqsClient.sendMessage(sendMsgRequest);
            return true;
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return false;
        } finally {
            sqsClient.close();
        }
    }

    public List<Message> receiveMessages() {
        try {
            SqsClient sqsClient = getSqsClient();
            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(QUEUE_NAME)
                    .build();
            String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(5)
                    .build();
            List<Message> receivedMessages = sqsClient.receiveMessage(receiveMessageRequest).messages();
            deleteMessages(receivedMessages, queueUrl);
            return receivedMessages;
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
        return null;
    }

    private void deleteMessages(List<Message> messages, String queueUrl) {
        try {
            SqsClient sqsClient = getSqsClient();
            for (Message message : messages) {
                DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build();
                DeleteMessageResponse deleteMessageResponse = sqsClient.deleteMessage(deleteMessageRequest);
                System.out.println(deleteMessageResponse);
            }
        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

    private SqsClient getSqsClient() {
        return SqsClient.builder()
                .region(REGION)
                .build();
    }

    private String getS3MetadataMessage(S3Metadata s3Metadata) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("A new image has been uploaded.");
        messageBuilder.append(System.getProperty("line.separator"));
        messageBuilder.append(s3Metadata.toString());
        messageBuilder.append(System.getProperty("line.separator"));
        messageBuilder.append("Download link: <ec2-url>/api/s3/downloads?file=" + s3Metadata.name());
        return messageBuilder.toString();
    }
}
