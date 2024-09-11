package example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.regions.Region;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RdsLambdaHandler implements RequestHandler<Object, APIGatewayProxyResponseEvent> {

    private static final String BUCKET_NAME = "alinr-bucket";
    private static final Region REGION = Region.EU_NORTH_1;

    private static final Connection connection;
    private static final S3Client s3client;

    static {
        try {
            // define connection configuration
            String connectionString = String.format("jdbc:postgresql://%s:%s/%s",
                    System.getenv("RDS_PROXY_HOST"),
                    System.getenv("PORT"),
                    System.getenv("DB_NAME"));

            // establish a connection to the database
            connection = DriverManager.getConnection(connectionString, System.getenv("USER_NAME"),
                    System.getenv("PASSWORD"));

            s3client = S3Client.builder()
                    .region(REGION)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("Could not init class: ", e);
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Object event, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        LambdaLogger logger = context.getLogger();

        logger.log(
            "EVENT: lambda triggered by event = " + event + " in context = " + context + System.getProperty("line.separator"));

        logger.log(
                "EVENT: db connection - " + connection + System.getProperty("line.separator"));

        if (connection != null) {
            try {
                PreparedStatement statement = connection.prepareStatement("select * from api_images");
                try (ResultSet resultSet = statement.executeQuery()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    while (resultSet.next()) {
                        logger.log(
                                "EVENT: db resultSet - " + resultSet.getRow() + System.getProperty("line.separator"));
                        S3Metadata metadata = new S3Metadata(
                                resultSet.getString("name"),
                                resultSet.getString("lastUpdated"),
                                resultSet.getString("extension"),
                                resultSet.getDouble("size"));
                        S3Metadata s3metadata = getS3Metadata(metadata.name(), logger);
                        logger.log(
                                "EVENT: rds - " + metadata + System.getProperty("line.separator"));
                        logger.log(
                                "EVENT: s3 - " + s3metadata + System.getProperty("line.separator"));
 
                        if (!s3metadata.equals(metadata)) {
                            stringBuilder.append("Inconsistency: ");
                            stringBuilder.append(System.getProperty("line.separator"));
                            stringBuilder.append("rds: " + metadata);
                            stringBuilder.append(System.getProperty("line.separator"));
                            stringBuilder.append("s3: " + s3metadata);
                            stringBuilder.append(System.getProperty("line.separator"));
                            stringBuilder.append("------------------------------------");
                        }
                    }
                    response.setStatusCode(200);
                    response.setBody("Metadata in db: " + stringBuilder.toString());
                }
            } catch (Exception e) {
                response.setStatusCode(500);
                response.setBody("Error: " + e.getMessage());
                logger.log("EVENT: db error - " + e.getMessage() + System.getProperty("line.separator"));
            }
            logger.log("EVENT: queried db successfully" + System.getProperty("line.separator"));
            return response;
        } else {
            response.setStatusCode(500);
            response.setBody("Error: null db connection");
            logger.log("EVENT: db connection is null");
            return response;
        }
    }

    private S3Metadata getS3Metadata(String key, LambdaLogger logger) {
        logger.log("EVENT: s3Client = " + s3client.toString() + System.getProperty("line.separator"));
        HeadObjectResponse headObjectResponse = getMetadataFromS3(key, logger);
        logger.log("EVENT: s3 HeadObjectResponse - " + headObjectResponse.toString()
                + System.getProperty("line.separator"));
        var metadata = new S3Metadata(
                key,
                headObjectResponse.lastModified().toString(),
                getExtension(key),
                headObjectResponse.contentLength() / 1024.0);
        return metadata;
    }

    private String getExtension(String key) {
        int i = key.lastIndexOf('.');
        if (i > 0) {
            return key.substring(i + 1);
        }
        return "";
    }

    private HeadObjectResponse getMetadataFromS3(String key, LambdaLogger logger) {
        logger.log("EVENT: s3 key = " + key + System.getProperty("line.separator"));
        try {
            return s3client.headObject(HeadObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build());
        } catch (Exception e) {
            logger.log("EVENT: getMetadataFromS3 error - " + e.getMessage()
                    + System.getProperty("line.separator"));
            return null;
        }
    }
}

record S3Metadata(String name, String lastUpdated, String extension, double size) {
};
