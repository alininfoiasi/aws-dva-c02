package com.example.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class S3MetadataRowMapper implements RowMapper<S3Metadata> {

    @SuppressWarnings("null")
    @Override
    public S3Metadata mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new S3Metadata(
                rs.getString("name"),
                rs.getString("lastUpdated"),
                rs.getString("extension"),
                rs.getDouble("size"));
    }
}