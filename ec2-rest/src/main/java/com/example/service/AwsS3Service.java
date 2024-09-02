package com.example.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3ResponseMetadata;

import com.example.model.S3Metadata;

@Service
public class AwsS3Service {

    private static final String BUCKET_NAME = "alinr-bucket";
    private static final String PREFIX = "api_images";
    private static final Region REGION = Region.EU_NORTH_1;

    public S3Metadata getMetadata(String key) {
        S3Client s3client = getS3Client();
        var prefixedKey = getPrefixedKey(key);
        HeadObjectResponse headObjectResponse = getMetadataFromS3(prefixedKey, s3client);
        if (headObjectResponse != null) {
            var metadata = new S3Metadata(
                    prefixedKey,
                    headObjectResponse.lastModified().toString(),
                    getExtension(key),
                    headObjectResponse.contentLength() / 1024.0);
            return metadata;
        }
        s3client.close();
        return null;
    }

    public boolean upload(String key, MultipartFile multipartFile) {
        var prefixedKey = getPrefixedKey(key);
        try {
            writeToS3(prefixedKey, multipartFile.getBytes());
            return true;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }

    public byte[] download(String key) {
        var prefixedKey = getPrefixedKey(key);
        return readFromS3(prefixedKey);
    }

    private String getPrefixedKey(String key) {
        return PREFIX + "/" + key;
    }

    private String getExtension(String key) {
        int i = key.lastIndexOf('.');
        if (i > 0) {
            return key.substring(i + 1);
        }
        return "";
    }

    private S3Client getS3Client() {
        return S3Client.builder()
                .region(REGION)
                .build();
    }

    private HeadObjectResponse getMetadataFromS3(String key, S3Client s3client) {
        try {
            return s3client.headObject(HeadObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return null;
        }
    }

    private S3ResponseMetadata writeToS3(String key, byte[] fileContents) {
        S3Client s3client = getS3Client();
        try {
            PutObjectResponse response = s3client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(fileContents));
            return response.responseMetadata();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return null;
        } finally {
            s3client.close();
        }
    }

    private byte[] readFromS3(String key) {
        S3Client s3client = getS3Client();
        try {
            ResponseBytes<GetObjectResponse> response = s3client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(key)
                            .build());
            return response.asByteArray();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            return null;
        } finally {
            s3client.close();
        }
    }
}
