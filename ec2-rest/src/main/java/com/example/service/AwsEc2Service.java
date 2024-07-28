package com.example.service;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.document.Document;
import software.amazon.awssdk.imds.Ec2MetadataClient;
import software.amazon.awssdk.imds.Ec2MetadataResponse;

@Service
public class AwsEc2Service {

    public String getRegion() {
       return retrieveMetadata("region");
    }

    public String getAZ() {
        return retrieveMetadata("availabilityZone");
    }

    private String retrieveMetadata(String key) {
        Ec2MetadataClient client = Ec2MetadataClient.create();
        Ec2MetadataResponse response = 
        client.get("/latest/dynamic/instance-identity/document");
        Document instanceInfo = response.asDocument();
        return instanceInfo.asMap().get(key).asString();
    }

}
