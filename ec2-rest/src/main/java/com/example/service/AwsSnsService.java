package com.example.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.UnsubscribeRequest;
import software.amazon.awssdk.services.sns.model.UnsubscribeResponse;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;

@Service public class AwsSnsService {
    // TODO: read arns / ids from Parameter Store
    private static final String TOPIC_ARN = "arn:aws:sns:eu-north-1:123456789012:SNS-api_images";
    private static final Region REGION = Region.EU_NORTH_1;

    public static final String FILTER_BY_EXTENSION = "extension";

    public String subscribe(String email) {
        SnsClient snsClient = getSnsClient();
        try {
            SubscribeRequest request = SubscribeRequest.builder()
                    .protocol("email")
                    .endpoint(email)
                    .returnSubscriptionArn(true)
                    .topicArn(TOPIC_ARN)
                    .build();
            SubscribeResponse result = snsClient.subscribe(request);
            return result.subscriptionArn();
        } catch (SnsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return null;
        } finally {
            snsClient.close();
        }
    }

    public boolean unsubscribe(String subscriptionArn) {
        SnsClient snsClient = getSnsClient();
        try {
            UnsubscribeRequest request = UnsubscribeRequest.builder()
                    .subscriptionArn(subscriptionArn)
                    .build();
            UnsubscribeResponse result = snsClient.unsubscribe(request);
            return result.sdkHttpResponse().statusCode() == HttpStatus.OK.value();
        } catch (SnsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return false;
        } finally {
            snsClient.close();
        }
    }

    public void publish(String message, String extension) {
        try {
            SnsClient snsClient = getSnsClient();
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put(FILTER_BY_EXTENSION, MessageAttributeValue.builder()
                    .dataType("String")
                    .stringValue(extension)
                    .build());
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .messageAttributes(messageAttributes)
                    .topicArn(TOPIC_ARN)
                    .build();
            snsClient.publish(request);
        } catch (SnsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

    private SnsClient getSnsClient() {
        SnsClient snsClient = SnsClient.builder()
                .region(REGION)
                .build();
        return snsClient;
    }

}
