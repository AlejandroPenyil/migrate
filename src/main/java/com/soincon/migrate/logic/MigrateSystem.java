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
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FilenameUtils;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static com.soincon.migrate.MigrateApplication.f;
import static com.soincon.migrate.retroFit.ImplNew.Jwtoken;

@Log4j
public class MigrateSystem {
    ImplOld implOld;
    ImplNew implNew;
    public static int fi=0;

    public MigrateSystem() throws IOException {
        this.implNew = new ImplNew();
        this.implOld = new ImplOld();
    }

    public void migrate(String path, DocumentDto parent, File newPath) throws IOException {
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
                    log.info(documentDto.toString());
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        migrate(subFile.getAbsolutePath(), documentDto, directory);
                    }
                }else {
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        migrate(subFile.getAbsolutePath(), docFolderMigration.getDocumentDto(), directory);
                    }
                }
            } else {
                if(exists(file)) {
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
                                log.info(documentDto.toString());
                                createVersion(documentDto, i, fullName);
                                fi--;
                            } else {
                                log.info(documentDto);
                            }
                        }
                        i++;
                    } while (!tr);
                }else{
                    File file2 = Paths.get(String.valueOf(newPath), "notfound").toFile();
                    file2.mkdir();
                    file.renameTo(new File(file2 + "\\" + file.getName()));
                }
            }
        }
    }

    private boolean exists(File file) throws IOException {
        FilterDirectory filterDirectory = new FilterDirectory();
        Content content = new Content();
        content.setExactName(file.getName());
        filterDirectory.setContent(content);

        List<FileDto> fileDtoList = implOld.searchFileByFilterAll(filterDirectory);

        if (!fileDtoList.isEmpty()) {
            for (FileDto fileDto : fileDtoList) {
                if(file.getParentFile().getAbsolutePath().equalsIgnoreCase(fileDto.getPathBase())){
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
        documentVersionDto.setIdUser(114);
        documentVersionDto.setReason("migrate");
        documentVersionDto.setVersionStatus("VALID");
        documentVersionDto.setUploadDate(ZonedDateTime.now().toString());
        documentVersionDto.setMimeType(getMimeType(fullName));

        implNew.documentCreateDto(documentVersionDto);
    }

    private Integer obtenerToken() {
        //TODO no puedo recoger la secret key del token,
        String[] chunks = Jwtoken.getToken().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

        SignatureAlgorithm sa = SignatureAlgorithm.HS256;
//        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), sa.getJcaName());
        return 114;
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
                    fullName = fileTypeDto.getMimeType().substring(0, lastIndex)+"/"+fileTypeDto.getName();
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

    public void cleanRoot() {
        //TODO preguntar si todos los documentos que estan en root se meten en la base de datos o solo los que esten en la base de datos antigua
        String currentDirectory = System.getProperty("user.dir");
        Path path = Paths.get(currentDirectory).getRoot();
        File directory = path.toFile();

        for (File file : Objects.requireNonNull(directory.listFiles(File::isFile))) {
            path = Paths.get(f.getAbsolutePath(),"new");
            File file2 = path.toFile();
            file2.mkdir();
            path = Paths.get(path.toFile().getAbsolutePath(),file.getName());
            file2 = path.toFile();
            file.renameTo(file2);
        }
    }
}