package com.soincon.migrate.logic;

import emisuite.documentmanager.dto.DocumentDto;
import com.soincon.migrate.retroFit.ImplNew;
import emisuite.documentmanager.enums.TypeDoc;
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
    private DocumentDto documentDto;

    public DocFolderMigration(File file, DocumentDto parent) throws IOException {
        this.implNew = new ImplNew();
        this.documentDto = parent;
        this.idParent = parentId(file);
    }

    private long parentId(File file) throws IOException {
        DocumentDto documentDto = new DocumentDto();
        documentDto.setTypeDoc(TypeDoc.FOLDER);
        documentDto.setName(file.getParentFile().getName());

        if (this.documentDto != null) {
            documentDto.setIdDocument(this.documentDto.getIdDocument());
        }

        List<DocumentDto> documentDtos = implNew.search(documentDto);

        if (!documentDtos.isEmpty()) {
            if (documentDtos.size() > 1) {
                for (DocumentDto documentDto2 : documentDtos) {
                    String path = makePath(documentDto2);

                    if (path.equals(file.getParentFile().getAbsolutePath())) {
                        return documentDto2.getIdDocument();
                    }
                }
            } else {
                return documentDtos.get(0).getIdDocument();
            }
        }

        return 0;
    }

    private String makePath(DocumentDto documentDto2) throws IOException {
        Path path;

        if (documentDto2.getIdParent() != null) {
            DocumentDto dto = implNew.getDocument(documentDto2.getIdParent());
            path = Paths.get(makePath(dto), documentDto2.getName());
        } else {
            String currentDirectory = System.getProperty("user.dir");
            path = Paths.get(currentDirectory).getRoot();
        }

        return path.toString();
    }
}
