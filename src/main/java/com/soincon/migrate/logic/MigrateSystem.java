package com.soincon.migrate.logic;

import com.soincon.migrate.dto.newDtos.DocumentVersionDto;
import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.dto.oldDtos.FileDto;
import com.soincon.migrate.dto.oldDtos.FileTypeDto;
import com.soincon.migrate.filter.Content;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.retroFit.ImplNew;
import com.soincon.migrate.retroFit.ImplOld;
//import io.jsonwebtoken.security.SignatureAlgorithm;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static com.soincon.migrate.MigrateApplication.totalFilesAndDirectories;
import static com.soincon.migrate.retroFit.ImplNew.Jwtoken;

@Log4j
public class MigrateSystem {
    ImplOld implOld;
    ImplNew implNew;
    public static int fi = 0;
    static int currentStep = 0;

    public MigrateSystem() throws IOException {
        this.implNew = new ImplNew();
        this.implOld = new ImplOld();
    }

    public void migrate(String path, DocumentDto parent, File newPath) throws IOException, InterruptedException {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                File directory = Paths.get(String.valueOf(newPath), file.getName()).toFile();
                directory.mkdirs();
                DocFolderMigration docFolderMigration = new DocFolderMigration(directory, parent);
                if (!docFolderMigration.isExist()) {
                    DocumentDto documentDto = new DocumentDto();
                    documentDto.setName(file.getName().toLowerCase());
                    documentDto.setTypeDoc("FOLDER");
                    if (documentDto.getIdParent() == null) {
                        documentDto.setIdParent(docFolderMigration.getIdParent());
                    }

                    documentDto = implNew.createDocument(documentDto, file.getParentFile().getPath().toLowerCase());
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        showProgressIndicator(currentStep++, totalFilesAndDirectories);
                        migrate(subFile.getAbsolutePath(), documentDto, directory);
                    }
                } else {
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        showProgressIndicator(currentStep++, totalFilesAndDirectories);
                        migrate(subFile.getAbsolutePath(), docFolderMigration.getDocumentDto(), directory);
                    }
                }
            } else {
                if (exists(file)) {
                    File directory = Paths.get(String.valueOf(newPath), file.getName()).toFile();
                    file.renameTo(directory);

                    DocFolderMigration docFolderMigration = new DocFolderMigration(directory, parent);
                    int i = 1;
                    boolean tr = false;

                    String extension = FilenameUtils.getExtension(directory.getAbsolutePath());

                    if (extension.isEmpty()) {
                        File file3 = asignarExtension(directory);
                        if (directory.renameTo(file3)) {
                            directory = file3;
                        }
                        extension = FilenameUtils.getExtension(directory.getAbsolutePath());

                    }

                    String name = quitarExtension(directory);
                    do {
                        String fullName = name.toLowerCase() + "_V" + i + '.' + extension;
                        File file2 = new File(directory.getParentFile().getPath() + "\\" + fullName);
                        if (directory.renameTo(file2)) {
                            tr = true;
                            DocumentDto documentDto = new DocumentDto();
                            documentDto.setName(name.toLowerCase());
                            documentDto.setTypeDoc("DOC");
                            documentDto.setIdParent(docFolderMigration.getDocumentDto().getIdParent());

                            documentDto = implNew.createDocument(documentDto, file2.getParentFile().getPath().toLowerCase());
                            fi++;
                            if (documentDto != null) {
                                createVersion(documentDto, i, fullName);
                                fi--;
                            }
                        }
                        i++;
                    } while (!tr);
                } else {
                    File file2 = Paths.get(String.valueOf(newPath), "notfound").toFile();
                    file2.mkdir();
                    file.renameTo(new File(file2 + "\\" + file.getName()));
                }
            }
        }
    }

    private void showProgressIndicator(int currentStep, int totalSteps) throws InterruptedException {
        int progressBarLength = 20; // Longitud total de la barra de progreso
        int completed = currentStep * progressBarLength / totalSteps; // Calcula la longitud completada de la barra de progreso
        StringBuilder progressBar = new StringBuilder("[");
        for (int i = 0; i < progressBarLength; i++) {
            if (i < completed) {
                progressBar.append("="); // Carácter para representar la parte completada de la barra
            } else {
                progressBar.append(" "); // Carácter para representar la parte incompleta de la barra
            }
        }
        progressBar.append("] ").append(currentStep * 100 / totalSteps).append("%"); // Añadir el porcentaje de progreso
        System.out.print("\r" + progressBar.toString()); // Imprimir la barra de progreso en la misma línea
    }


    private boolean exists(File file) throws IOException {
        FilterDirectory filterDirectory = new FilterDirectory();
        Content content = new Content();
        content.setExactName(file.getName());
        filterDirectory.setContent(content);

        List<FileDto> fileDtoList = implOld.searchFileByFilterAll(filterDirectory);

        if (!fileDtoList.isEmpty()) {
            for (FileDto fileDto : fileDtoList) {
                if (file.getParentFile().getAbsolutePath().equalsIgnoreCase(fileDto.getPathBase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void createVersion(DocumentDto documentDto, int i, String fullName) throws IOException {
        DocumentVersionDto documentVersionDto = new DocumentVersionDto();
        documentVersionDto.setIdVersion(i);
        documentVersionDto.setIdDocument(documentDto.getIdDocument());
        documentVersionDto.setExternalCode(fullName);
        obtenerToken();
        documentVersionDto.setIdUser(114);
        documentVersionDto.setReason("migrate");
        documentVersionDto.setVersionStatus("VALID");
        documentVersionDto.setUploadDate(ZonedDateTime.now().toString());
        documentVersionDto.setMimeType(getMimeType(fullName));

        implNew.documentCreateDto(documentVersionDto);
    }

    private Integer obtenerToken() {
        Integer idUser = 1;

        String[] chunks = Jwtoken.getToken().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        String signature = new String(decoder.decode(chunks[2]));

        String[] code = payload.split(",");

        for (String s : code) {
            if (s.contains("userId")) {
                String[] id = s.split(":");
                idUser = Integer.valueOf(id[1]);
            }
        }

        return idUser;
    }

    private String getMimeType(String fullName) throws IOException {
        List<FileTypeDto> fileTypeDtos = implOld.getFileTypes();
        for (FileTypeDto fileTypeDto : fileTypeDtos) {
            File file = new File(fullName);
            int lastIndex = file.getName().lastIndexOf('.');
            if (lastIndex != -1) {
                fullName = file.getName().substring(lastIndex, fullName.length());
            }
            if (fileTypeDto.getExtension().equals(fullName)) {
                lastIndex = fileTypeDto.getMimeType().lastIndexOf('/');
                if (lastIndex != -1) {
                    fullName = fileTypeDto.getMimeType().substring(0, lastIndex) + "/" + fileTypeDto.getName();
                }
                return fullName;

            } else if (fullName.equals(".jpeg")) {
                if (fileTypeDto.getExtension().equals(".jpg")) {
                    return fileTypeDto.getMimeType();
                }
            }
        }
        return "";
    }

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

    public void cleanRoot(String pathroot, String newRoot) throws IOException {

        Path path = Paths.get(pathroot);
        File directory = path.toFile();

        File newDirectory = new File(newRoot);
        path = Paths.get(newDirectory.getAbsolutePath(), "folderRootNew");
        File file2 = path.toFile();
        file2.mkdir();

        DocumentDto documentDto = createNew(file2);

        File[] files = directory.listFiles(File::isFile);

        if (files != null) {
            for (File file : files) {
                if (!exists(file)) {
                    path = Paths.get(path.toFile().getAbsolutePath(), file.getName());
                    file2 = path.toFile();
                    file.renameTo(file2);
                } else {
                    int i = 1;
                    boolean tr = false;

                    String extension = FilenameUtils.getExtension(directory.getAbsolutePath());

                    if (extension.isEmpty()) {
                        File file3 = asignarExtension(directory);
                        if (directory.renameTo(file3)) {
                            directory = file3;
                        }
                        extension = FilenameUtils.getExtension(directory.getAbsolutePath());

                    }

                    String name = quitarExtension(directory);
                    do {
                        String fullName = name.toLowerCase() + "_V" + i + '.' + extension;
                        file2 = new File(directory.getParentFile().getPath() + "\\" + fullName);
                        if (directory.renameTo(file2)) {

                            DocumentDto documentDto1 = new DocumentDto();
                            documentDto1.setTypeDoc("DOC");
                            documentDto1.setName(file.getName());
                            documentDto1.setIdParent(documentDto.getIdParent());
                            documentDto1 = implNew.createDocument(documentDto1, file.getParentFile().getAbsolutePath());
                            if (documentDto1 != null) {
                                log.info(documentDto.toString());
                                createVersion(documentDto, i, fullName);
                            } else {
                                log.info(documentDto);
                            }
                        }
                    } while (!tr);
                }
            }
        }else{
            log.info("No hay ficheros en root");
        }
    }

    private DocumentDto createNew(File file) throws IOException {
        DocumentDto documentDto = new DocumentDto();
        documentDto.setName(file.getName());
        documentDto.setTypeDoc("FOLDER");
        return implNew.createDocument(documentDto, file.getParentFile().getAbsolutePath());
    }

    public void borrar(String path) {
        File file = new File(path);
        if(file.listFiles() != null) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                borrar(subFile.getPath());
            }
        }
        file.delete();
    }
}