package com.soincon.migrate.logic;

import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.dto.oldDtos.FileDto;
import com.soincon.migrate.dto.oldDtos.FileTypeDto;
import com.soincon.migrate.filter.Content;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.retroFit.ImplNew;
import com.soincon.migrate.retroFit.ImplOld;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Log4j
public class MigrateSystem {
    ImplOld implOld;
    ImplNew implNew;

    private String pathBase = "C:\\opt\\tools\\tomcat\\latest\\files\\clients";
//    private long idParent = 51;

    public MigrateSystem() throws IOException {
        this.implNew = new ImplNew();
        this.implOld = new ImplOld();
    }

    public void migrate(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                DocFolderMigration docFolderMigration = new DocFolderMigration(file);
                if (!docFolderMigration.isExist()) {
                    DocumentDto documentDto = new DocumentDto();
                    documentDto.setName(file.getName().toLowerCase());
                    documentDto.setTypeDoc("FOLDER");
                    documentDto.setIdParent(docFolderMigration.getIdParent());

                    documentDto = implNew.createDocument(documentDto, file.getParentFile().getPath().toLowerCase());
                    log.info(documentDto.toString());
                }
                for (File subFile : Objects.requireNonNull(file.listFiles())) {
                    migrate(subFile.getAbsolutePath());
                }
            } else {
                DocFolderMigration docFolderMigration = new DocFolderMigration(file);
                int i = 1;
                boolean tr = false;

                String extension = FilenameUtils.getExtension(file.getAbsolutePath());

                if (extension.isEmpty()) {
                    File file3 = asignarExtension(file);
                    if (file.renameTo(file3)) {
                        file = file3;
                    }
                    extension = FilenameUtils.getExtension(file.getAbsolutePath());

                }

                String name = quitarExtension(file);
                do {
                    File file2 = new File(file.getParentFile().getPath() + "\\" + name.toLowerCase() + "_V" + i + '.' + extension);
                    if (file.renameTo(file2)) {
                        tr = true;
                        DocumentDto documentDto = new DocumentDto();
                        documentDto.setName(name.toLowerCase());
                        documentDto.setTypeDoc("DOC");
                        documentDto.setIdParent(docFolderMigration.getIdParent());

                        documentDto = implNew.createDocument(documentDto, file2.getParentFile().getPath().toLowerCase());
                        if (documentDto != null) {
                            log.info(documentDto.toString());
                        }
                    }
                    i++;
                } while (!tr);
            }
        }
    }

    /**
     * Method to obtain the parentId in case of you need to create a folder in the database
     */
//    private long parentId(File file) throws IOException {
//        long id = idParent;
//        DocumentDto documentDto = new DocumentDto();
//
//        documentDto.setName(file.getParentFile().getName().toLowerCase());
//        documentDto.setTypeDoc("FOLDER");
//
//        //TODO mejorar la manera de comprobar el parent id
//        List<DocumentDto> documentDtos = implNew.search(documentDto);
//        if (documentDtos != null) {
//            if (documentDtos.size() > 1) {
//                id = parentId(file.getParentFile());
//                for (DocumentDto document : documentDtos) {
//                    if(document.getIdParent() != null) {
//                        if (document.getIdParent() == id) {
//                            return document.getIdDocument();
//                        }
//                    }
//                }
//            } else if (documentDtos.size() == 1) {
//                documentDto.setName(file.getParentFile().getParentFile().getName());
//
//                documentDtos = implNew.search(documentDto);
//                if (documentDtos.size() == 1) {
//                    id = documentDtos.get(0).getIdDocument();
//                    return id;
//                }
//            }
//        }
//        return id;
//    }
//
//    private boolean comprobar(File file) throws IOException {
//        DocumentDto documentDto = new DocumentDto();
//
//        documentDto.setName(file.getName().toLowerCase());
//
//        List<DocumentDto> documentDtos = implNew.search(documentDto);
//
//        if (documentDtos != null) {
//            if (documentDtos.size() > 1) {
//                for (DocumentDto document : documentDtos) {
//                    if (document.getIdParent() == idParent) {
//                        idParent = document.getIdDocument();
//                        return true;
//                    }
//                }
//            } else if (documentDtos.size() == 1) {
//                if (documentDtos.get(0).getIdParent() == idParent) {
//                    idParent = documentDtos.get(0).getIdDocument();
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private File asignarExtension(File file) throws IOException {
        FilterDirectory filterDirectory = new FilterDirectory();
        Content content = new Content();
        content.setExactName(file.getName());

        filterDirectory.setContent(content);

        List<FileDto> fileDtoList = implOld.searchFileByFilterAll(filterDirectory);
        if (!fileDtoList.isEmpty()) {
            for (FileDto fileDto : fileDtoList) {
                List<FileTypeDto> fileTypeDtos = implOld.getFileTypes();
                for (FileTypeDto fileTypeDto : fileTypeDtos) {
                    if (fileTypeDto.getMimeType().equals(fileDto.getMimeType())) {
                        file = new File(file.getParentFile().getPath() + "\\" + file.getName() + fileTypeDto.getExtension());
                        break;
                    }
                }
            }
        }
        return file;
    }

    private String quitarExtension(File file) {
        String fileName = "";
        int lastIndex = file.getName().lastIndexOf('.');
        if (lastIndex != -1) {
            fileName = file.getName().substring(0, lastIndex);
        }
        lastIndex = fileName.lastIndexOf("_v");
        if (lastIndex != -1) {
            fileName = fileName.substring(0, lastIndex);
        }
        return fileName;
    }
}
