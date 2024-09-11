package com.example.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

@Service
public class AwsLambdaService {
    private static final String FUNCTION_NAME = "DataConsistencyFunction";
    private static final Region REGION = Region.EU_NORTH_1;

    public String invokeLambda() {
        LambdaClient lambdaClient = LambdaClient.builder()
                .region(REGION)
                .build();

        InvokeResponse res = null;
        try {
            JSONObject jsonObj = new JSONObject();
            String json = jsonObj.toString();
            SdkBytes payload = SdkBytes.fromUtf8String(json);
            InvokeRequest request = InvokeRequest.builder()
                    .functionName(FUNCTION_NAME)
                    .payload(payload)
                    .build();
            res = lambdaClient.invoke(request);
            String value = res.payload().asUtf8String();
            return value;
        } catch (LambdaException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
