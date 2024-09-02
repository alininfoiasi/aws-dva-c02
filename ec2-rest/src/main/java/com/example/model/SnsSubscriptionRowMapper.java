package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class SnsSubscriptionRowMapper implements RowMapper<SnsSubscription> {

    @SuppressWarnings("null")
    @Override
    public SnsSubscription mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new SnsSubscription(
                rs.getString("arn"),
                rs.getString("email"),
                rs.getBoolean("unsubscribed"));
    }
}