package com.soincon.migrate.updateLogic;

import com.soincon.migrate.dto.newDtos.DocumentDto;
import org.springframework.core.convert.Property;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DocumentService {

    private static String URL = System.getProperty("spring.datasource.url");
    private static String USER = System.getProperty("spring.datasource.username");
    private static String PASSWORD = System.getProperty("spring.datasource.password");

    public static DocumentDto updateDocument(DocumentDto documentDto) {
//        URL = System.getProperty("spring.datasource.url");
//        USER = System.getProperty("spring.datasource.username");
//        PASSWORD = System.getProperty("spring.datasource.password");

        String sql = "UPDATE dmr_documents SET uuid = ? WHERE idDocument = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, documentDto.getUuid());
            pstmt.setLong(2, documentDto.getIdDocument());

            int rowsAffected = pstmt.executeUpdate();
//            if (rowsAffected > 0) {
//                System.out.println("Update successful for document with ID: " + documentDto.getIdDocument());
//            } else {
//                System.out.println("No document found with ID: " + documentDto.getIdDocument());
//            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return documentDto;
    }

}