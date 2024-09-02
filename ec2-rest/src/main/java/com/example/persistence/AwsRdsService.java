package com.example.persistence;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.model.S3Metadata;
import com.example.model.S3MetadataRowMapper;
import com.example.model.SnsSubscription;
import com.example.model.SnsSubscriptionRowMapper;

@Service
public class AwsRdsService {

    // TODO: implement using JPA
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveMetadata(S3Metadata s3Metadata) {
        jdbcTemplate.update(
                "insert into api_images values (?, ?, ?, ?)",
                s3Metadata.name(),
                s3Metadata.lastUpdated(),
                s3Metadata.extension(),
                s3Metadata.size());
    }

    public S3Metadata getMetadata(String fileName) {
        return jdbcTemplate.queryForObject(
                "select * from api_images where name = ?",
                new S3MetadataRowMapper(), fileName);
    }

    public SnsSubscription getArnSubscription(String email) {
        return jdbcTemplate.queryForObject(
                "select * from api_subscriptions where email = ?",
                new SnsSubscriptionRowMapper(), email);
    }

    public void saveSubscription(String subscriptionArn, String email) {
        jdbcTemplate.update(
                "insert into api_subscriptions values (?, ?, ?)",
                subscriptionArn,
                email,
                Boolean.FALSE);
    }

    public void updateSubscription(String email, Boolean unsubscribed) {
        jdbcTemplate.update(
                "update api_subscriptions set unsubscribed = ? where email = ?",
                unsubscribed,
                email);
    }
}
