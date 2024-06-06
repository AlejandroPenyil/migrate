package com.soincon.migrate.updateLogic;

import com.soincon.migrate.dto.newDtos.DocumentDto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DocumentService {
    private static final String URL = "jdbc:mysql://localhost:3306/documentmanager";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static DocumentDto updateDocument(DocumentDto documentDto) {
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
