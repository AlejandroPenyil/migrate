package com.soincon.migrate.logic;

import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.retroFit.ImplNew;
import com.soincon.migrate.retroFit.ImplOld;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Getter
@Setter
public class DocFolderMigration {
    ImplNew implNew;
    private long idParent;
    private boolean exist;

    public DocFolderMigration(File file) throws IOException {
        this.implNew = new ImplNew();
        this.exist = isExist(file);
        this.idParent = parentId(file);
    }

    private long parentId(File file) throws IOException {
        DocumentDto documentDto = new DocumentDto();
        documentDto.setTypeDoc("FOLDER");
        documentDto.setName(file.getParentFile().getName());

        List<DocumentDto> documentDtos = implNew.search(documentDto);
        if (!documentDtos.isEmpty()) {
            for (DocumentDto documentDto2 : documentDtos) {
                String path = makePath(documentDto2);

                if (path.equals(file.getParentFile().getAbsolutePath())) {
                    return documentDto2.getIdDocument();
                }
            }
        }

        return 0;
    }

    private String makePath(DocumentDto documentDto2) throws IOException {
        Path path;

        if (documentDto2.getIdParent() != null) {
            DocumentDto dto = implNew.getDocument(documentDto2.getIdParent());
            path = Paths.get(makePath(dto),documentDto2.getName());
        } else {
            String currentDirectory = System.getProperty("user.dir");
            path = Paths.get(currentDirectory).getRoot();
        }

        return path.toString();
    }

    public boolean isExist(File file) throws IOException {
        DocumentDto documentDto = new DocumentDto();

        documentDto.setName(file.getName().toLowerCase());

        List<DocumentDto> documentDtos = implNew.search(documentDto);

        if (!documentDtos.isEmpty()) {
            for (DocumentDto documentDto2 : documentDtos) {
                String path = makePath(documentDto2);
                if (path.equals(file.getAbsolutePath())) {
                    return true;
                }
            }
        }

        return false;
    }
}
