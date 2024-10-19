package com.example.config;

import com.zaxxer.hikari.HikariDataSource;

/*
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.RdsUtilities;
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest;
import software.amazon.awssdk.services.rds.model.RdsException;
*/

//@Component
public class RdsIamHikariDataSource extends HikariDataSource {

    @Override
    public String getPassword() {
        return "getToken()";
    }

    /*
    private String getToken() {
        String dbInstanceIdentifier = "";
        String masterUsername = "db_userx";
        Region region = Region.EU_NORTH_1;
        RdsClient rdsClient = RdsClient.builder()
                .region(region)
                .build();
        
        RdsUtilities utilities = rdsClient.utilities();
        try {
            GenerateAuthenticationTokenRequest tokenRequest = GenerateAuthenticationTokenRequest.builder()
                .credentialsProvider(ProfileCredentialsProvider.create())
                .username(masterUsername)
                .port(3306)
                .hostname(dbInstanceIdentifier)
                .build();

                return utilities.generateAuthenticationToken(tokenRequest);

        } catch (RdsException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return "";
    }
 */
}