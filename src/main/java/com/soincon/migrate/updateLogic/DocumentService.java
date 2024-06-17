package com.soincon.migrate.updateLogic;

import com.soincon.migrate.dto.newDtos.DocumentDto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DocumentService {

    private static final String URL = System.getProperty("spring.datasource.url");
    private static final String USER = System.getProperty("spring.datasource.username");
    private static final String PASSWORD = System.getProperty("spring.datasource.password");

    public static DocumentDto updateDocument(DocumentDto documentDto) {
        String sql = "UPDATE dmr_documents SET uuid = ? WHERE idDocument = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, documentDto.getUuid());
            pstmt.setLong(2, documentDto.getIdDocument());

            int rowsAffected = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documentDto;
    }

}