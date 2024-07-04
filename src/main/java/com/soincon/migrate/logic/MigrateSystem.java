package com.soincon.migrate.logic;

import com.soincon.migrate.command.WarningUtil;
import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.dto.newDtos.DocumentVersionDto;
import com.soincon.migrate.dto.oldDtos.DirectoryDto;
import com.soincon.migrate.dto.oldDtos.FileDto;
import com.soincon.migrate.dto.oldDtos.FileTypeDto;
import com.soincon.migrate.dto.oldDtos.PathDto;
import com.soincon.migrate.filter.Content;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.retroFit.ImplNew;
import com.soincon.migrate.retroFit.ImplOld;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;

import static com.soincon.migrate.MigrateApplication.f;
import static com.soincon.migrate.retroFit.ImplNew.Jwtoken;

@Log4j2
public class MigrateSystem {
    ImplOld implOld;
    ImplNew implNew;
    public static int fi = 0;
    public static int currentStep = 1;
    private static String uuid;

    public MigrateSystem() throws IOException {
        this.implNew = new ImplNew();
        this.implOld = new ImplOld();
    }

    /**
     * method that involves all the logic for the migration of files and folders
     *
     * @param path    path of the old root
     * @param parent  dto with the information from the parent of the current file
     * @param newPath path of the new root
     * @throws IOException          an exception by a possible error with the files
     * @throws InterruptedException an exception throw by the thread of the progress bar
     */
    public void migrate(String path, DocumentDto parent, File newPath) throws Exception {
        File file = new File(path);
        if (file.isDirectory()) {
            String name = file.getName();
            File directory = Paths.get(String.valueOf(newPath), name).toFile();
            if (folderExist(file)) {

                DocumentDto documentDto = new DocumentDto();
                documentDto.setName(name);
                documentDto.setTypeDoc("FOLDER");

                String pathToErase;
                if (directory.getParentFile().getAbsolutePath().equals(f.getAbsolutePath())) {
                    pathToErase = f.getAbsolutePath();
                } else {
                    pathToErase = f.getAbsolutePath() + File.separator;
                }

                String mkdPath = directory.getParentFile().getPath().replace(pathToErase, "");
                mkdPath = mkdPath.replace(File.separator, "/");

                documentDto = implNew.createDocument(documentDto, mkdPath);

                if (uuid != null) {
                    documentDto.setUuid(uuid);
                    DocumentService.updateDocument(documentDto);
                }
                for (File subFile : Objects.requireNonNull(file.listFiles())) {
                    ++currentStep;
                    migrate(subFile.getAbsolutePath(), documentDto, directory);
                }

            } else {
                DocFolderMigration docFolderMigration = new DocFolderMigration(directory, parent);
                File file2 = Paths.get(String.valueOf(f), "notfound").toFile();
                file2.mkdir();
                boolean t = true;
                int i = 0;
                do {
                    file2 = new File(file2 + File.separator + i + name);
                    if (file2.mkdir()) {
                        t = false;
                    } else {
                        i++;
                    }
                } while (t);
                for (File subFile : Objects.requireNonNull(file.listFiles())) {
                    ++currentStep;
                    migrate(subFile.getAbsolutePath(), docFolderMigration.getDocumentDto(), directory);
                }
            }
        } else {
            if (exists(file)) {
                File directory = Paths.get(String.valueOf(newPath), file.getName()).toFile();
                if (!directory.exists()) {
                    FileUtils.copyFile(file, directory);

                    DocFolderMigration docFolderMigration = new DocFolderMigration(directory, parent);
                    int i = 1;
                    boolean tr = false;

                    String extension = FilenameUtils.getExtension(directory.getAbsolutePath());

                    if (extension.isEmpty()) {
                        File file3 = giveExtension(directory);
                        if (directory.renameTo(file3)) {
                            directory = file3;
                        }
                        extension = FilenameUtils.getExtension(directory.getAbsolutePath());

                    } else {
                        extension = FilenameUtils.getExtension(giveExtension(directory).getAbsolutePath());
                    }

                    String name = removeExtension(directory);
                    do {
                        String fullName = name.toLowerCase() + "_V" + i + '.' + extension;
                        File file2 = new File(directory.getParentFile().getPath() + File.separator + fullName);
                        if (directory.renameTo(file2)) {
                            tr = true;
                            DocumentDto documentDto = new DocumentDto();
                            documentDto.setName(name.toLowerCase());
                            documentDto.setTypeDoc("DOC");
                            documentDto.setIdParent(docFolderMigration.getDocumentDto().getIdDocument());

                            documentDto = implNew.createDocument(documentDto, file2.getParentFile().getPath().toLowerCase());
                            if (uuid != null) {
                                documentDto.setUuid(uuid);
                                DocumentService.updateDocument(documentDto);
                            }
                            fi++;
                            if (documentDto != null) {
                                createVersion(documentDto, i, fullName, file2);
                                fi--;
                            } else {
                                log.info("You need to check this: {}", file);
                            }
                        }
                        i++;
                    } while (!tr);
                }
            } else {
                File directory = Paths.get(String.valueOf(f), "notfound").toFile();
                if(directory.mkdir()){
                    log.info("created {}", directory.getAbsolutePath());
                }
                boolean t = true;
                int i = 0;
                do {
                    File file2 = new File(directory + File.separator + i + "-" + file.getName());
                    if (!file2.exists()) {
                        try {
                            FileUtils.copyFile(file, file2);
                        } catch (IOException exception) {
                            log.error("Cannot create {}", file2);
                        }
                        if (file2.exists()) {
                            t = false;
                        }
                    }
                    i++;
                } while (t);

            }
        }
    }

    private boolean folderExist(File file) throws IOException {
        PathDto pathDto = new PathDto();
        pathDto.setPathBase(file.getAbsolutePath());

        List<DirectoryDto> directoryDtoList = implOld.getDirectoryPathBase(pathDto);

        if (directoryDtoList != null) {
            if (!directoryDtoList.isEmpty()) {
                for (DirectoryDto directoryDto : directoryDtoList) {
                    uuid = directoryDto.getId();
                    return true;
                }
            }
        }

        FilterDirectory filterDirectory = new FilterDirectory();
        Content content = new Content();
        content.setExactName(file.getName());

        filterDirectory.setContent(content);
        List<DirectoryDto> fileDtoList = implOld.searchDirectoryByFilterAll(filterDirectory);

        if (!fileDtoList.isEmpty()) {
            for (DirectoryDto fileDto : fileDtoList) {
                if (file.getParentFile().getAbsolutePath().equalsIgnoreCase(fileDto.getPathBase())) {
                    uuid = fileDto.getId();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Method that checks if a file exists in the old database.
     *
     * @param file The file you are checking for existence.
     * @return True if it exists, false otherwise.
     * @throws IOException Returns an error if there is any issue with the file.
     */
    private boolean exists(File file) throws IOException {
        PathDto pathDto = new PathDto();
        pathDto.setPathBase(file.getAbsolutePath());

        List<FileDto> directoryDtoList = implOld.getFilePathBase(pathDto);

        if (directoryDtoList != null) {
            if (!directoryDtoList.isEmpty()) {
                for (FileDto directoryDto : directoryDtoList) {
                    uuid = directoryDto.getId();
                    return true;
                }
            }
        }

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

    /**
     * Method to create the documentVersion in the new database.
     *
     * @param documentDto The document for which a version will be registered.
     * @param i           The number of the version.
     * @param fullName    The full name.
     * @throws IOException Returns an error if there is any issue with the file.
     */
    private void createVersion(DocumentDto documentDto, int i, String fullName, File file) throws IOException {
        DocumentVersionDto documentVersionDto = new DocumentVersionDto();
        documentVersionDto.setIdVersion(i);
        documentVersionDto.setIdDocument(documentDto.getIdDocument());
        documentVersionDto.setExternalCode(fullName);
        documentVersionDto.setIdUser(obtainToken());
        documentVersionDto.setReason("migrate");
        documentVersionDto.setVersionStatus("VALID");
        documentVersionDto.setUploadDate(ZonedDateTime.now().toString());
        Path path = file.toPath();

        String mimeType = Files.probeContentType(path);
        documentVersionDto.setMimeType(mimeType);

        implNew.documentCreateDto(documentVersionDto);
    }

    /**
     * method that obtains the user id with the token
     *
     * @return the id of the user
     */
    private Integer obtainToken() {
        int idUser = 1;

        String[] chunks = Jwtoken.getToken().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));

        String[] code = payload.split(",");

        for (String s : code) {
            if (s.contains("userId")) {
                String[] id = s.split(":");
                idUser = Integer.parseInt(id[1]);
            }
        }

        return idUser;
    }

    /**
     * Method that gives an extension to a file if it does not have one
     *
     * @param file the file without extension
     * @return file with extension
     * @throws IOException Returns an error if there is any issue with the file.
     */
    private File giveExtension(File file) throws IOException {
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
                        file = new File(file.getParentFile().getPath() + File.separator + file.getName() + fileTypeDto.getExtension());
                        break;
                    }
                }
            }
        }
        return file;
    }

    /**
     * Method used to obtain the name of a file without its extension
     *
     * @param file file with extension
     * @return name of the file without extension
     */
    private String removeExtension(File file) {
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

    /**
     *Method that checks the root folder and if it contains files, moves them to a new location
     *
     * @param pathroot Old root location
     * @param newRoot  New root location
     * @throws IOException Returns an error if there is any issue with the file.
     */
    public void cleanRoot(String pathroot, String newRoot) throws Exception {

        Path path = Paths.get(pathroot);
        File directory = path.toFile();

        File newDirectory = new File(newRoot);
        path = Paths.get(String.valueOf(newDirectory), "folderRootNew");
        File file2 = path.toFile();

        File[] files = directory.listFiles(File::isFile);

        if (files != null) {
            if (files.length != 0) {
                DocumentDto documentDto = createNew(file2);
                for (File file : files) {
                    if (file.isFile()) {
                        if (!exists(file)) {
                            path = Paths.get(String.valueOf(f), "notFound");
                            file2 = path.toFile();
                            file2.mkdirs();
                            file2 = new File(file2 + File.separator + file.getName());
                            if (!file2.exists()) {
                                FileUtils.copyFile(file, file2);
                            } else {
                                int i = 1;
                                boolean tr = false;

                                String extension = FilenameUtils.getExtension(file.getAbsolutePath());

                                if (extension.isEmpty()) {
                                    File file3 = giveExtension(directory);
                                    if (file.renameTo(file3)) {
                                        file = file3;
                                    }
                                    extension = FilenameUtils.getExtension(file.getAbsolutePath());

                                }
                                String name = removeExtension(directory);
                                do {
                                    String fullName = name.toLowerCase() + "_V" + i + '.' + extension;
                                    file2 = new File(file2 + File.separator + fullName);
                                    if (!file2.exists()) {
                                        FileUtils.copyFile(directory, file2);
                                        if (file2.exists()) {
                                            tr = true;
                                        }
                                    }
                                    i++;
                                } while (!tr);
                            }
                        } else {
                            file2.mkdir();
                            int i = 1;
                            boolean tr = false;

                            String extension = FilenameUtils.getExtension(file.getAbsolutePath());

                            if (extension.isEmpty()) {
                                File file3 = giveExtension(directory);
                                if (file.renameTo(file3)) {
                                    file = file3;
                                }
                                extension = FilenameUtils.getExtension(file.getAbsolutePath());

                            }

                            String name = removeExtension(directory);
                            do {
                                String fullName = name.toLowerCase() + "_V" + i + '.' + extension;
                                file2 = new File(file2 + File.separator + fullName);
                                if (!file2.exists()) {
                                    FileUtils.copyFile(directory, file2);
                                    if (file2.exists()) {
                                        tr = true;
                                        DocumentDto documentDto1 = new DocumentDto();
                                        documentDto1.setTypeDoc("DOC");
                                        documentDto1.setName(file.getName());
                                        documentDto1.setIdParent(documentDto.getIdDocument());
                                        documentDto1 = implNew.createDocument(documentDto1, file.getParentFile().getAbsolutePath());
                                        if (documentDto1 != null) {
                                            createVersion(documentDto, i, fullName, file2);
                                        }
                                    }
                                }
                            } while (!tr);

                        }
                    } else {
                        if (!exists(file)) {
                            path = Paths.get(String.valueOf(f), "notFound");
                            file2 = path.toFile();
                            file2.mkdirs();
                            file = new File(file2 + File.separator + file.getName());
                            file.mkdir();
                        } else {
                            file2.mkdir();

                            file2 = new File(file2 + File.separator + file.getName());
                            if (file2.mkdir()) {

                                DocumentDto documentDto1 = new DocumentDto();
                                documentDto1.setTypeDoc("FOLDER");
                                documentDto1.setName(file.getName());
                                documentDto1.setIdParent(documentDto.getIdParent());
                                implNew.createDocument(documentDto1, file.getParentFile().getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * method that creates a new document in the new database
     *
     * @param file folder you want to create in the database
     * @return the new document created
     * @throws IOException if something happens with the files
     */
    private DocumentDto createNew(File file) throws Exception {
        DocumentDto documentDto = new DocumentDto();
        documentDto.setName(file.getName());
        documentDto.setTypeDoc("FOLDER");
        return implNew.createDocument(documentDto, file.getParentFile().getAbsolutePath());
    }

    /**
     * method that deletes the old directory tree
     *
     * @param path location to be deleted
     */
    public void delete(String path) {
        File file = new File(path);
        if (file.listFiles() != null) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                delete(subFile.getPath());
            }
        }
        file.delete();
    }

    public void newMigration(File f) throws Exception {
        File file = new File(f.getAbsolutePath() + File.separator +"Emisuite");
        if (file.exists()) {
//            DocumentDto documentDto = findDocument(file);
            moveToEmisuite(f.getAbsolutePath() + File.separator +"clients", file);
        } else {
            DocumentDto documentDto = new DocumentDto();
            documentDto.setTypeDoc("FOLDER");
            documentDto.setName(file.getName());
            implNew.createDocument(documentDto, "");

            moveToEmisuite(f.getAbsolutePath() + File.separator + "clients", file);
        }
    }

    /**
     * @param file THE FILE TO FIND IN DATA BASE
     * @return THE DOCUMENT
     * @throws IOException IF ANY ERROR OCCUR WITH RETROFIT
     */
    private DocumentDto findDocument(File file) throws IOException {
        DocumentDto documentDto = new DocumentDto();

        documentDto.setName(file.getName());
        documentDto.setTypeDoc("FOLDER");

        List<DocumentDto> documentDtoList = implNew.search(documentDto);

        for (DocumentDto documentDto1 : documentDtoList) {
            if (documentDto1.getName().equals(file.getName()) && documentDto1.getIdParent() == null) {
                return documentDto1;
            }
        }

        return documentDto;
    }

    /**
     * METHOD TO MOVE THE MAIN FOLDERS TO EMISUITE
     *
     * @param s    PATH OF THE FOLDERS
     * @param file NEW PATH OF THE FOLDERS
     * @throws Exception IF ANY ERROR OCCUR WITH RETROFIT
     */
    private void moveToEmisuite(String s, File file) throws Exception {
        File old = new File(s + File.separator+ "1" + File.separator +"vt");
        File neu = new File(file.getAbsolutePath() + File.separator +"Visual Tracking");

        if (old.exists()) {
            long id = find(old);
            implNew.moveDocuments(Math.toIntExact(id), null, "Emisuite");

            old = new File(file.getAbsolutePath() + File.separator + "vt");
            if (old.renameTo(neu)) {
                updateNew((int) id, "Visual Tracking");
            }
        } else {
            DocumentDto documentDto1 = new DocumentDto();
            documentDto1.setName("Visual Tracking");
            documentDto1.setTypeDoc("FOLDER");

            implNew.createDocument(documentDto1, "Emisuite");
        }

        old = new File(s + File.separator+"1"+File.separator+"easy-gmao");
        neu = new File(file.getAbsolutePath() + File.separator+"Easy GMAO");

        if (old.exists()) {
            long id = find(old);
            implNew.moveDocuments(Math.toIntExact(id), null, "Emisuite");
            old = new File(file.getAbsolutePath() + File.separator +"easy-gmao");
            if (old.renameTo(neu)) {
                updateNew((int) id, "Easy GMAO");
            }
        } else {
            DocumentDto documentDto1 = new DocumentDto();
            documentDto1.setName("Easy GMAO");
            documentDto1.setTypeDoc("FOLDER");

            documentDto1 = implNew.createDocument(documentDto1, "Emisuite");

            documentDto1.setUuid("6d44b088-2324-4afd-b186-514f150205cb");

            DocumentService.updateDocument(documentDto1);
        }

        old = new File(s + File.separator+"clientid-1"+File.separator+"my-factory");
        neu = new File(file.getAbsolutePath() + File.separator+"My Factory");

        if (old.exists()) {
            long id = find(old);
            implNew.moveDocuments(Math.toIntExact(id), null, "Emisuite");
            old = new File(file.getAbsolutePath() + File.separator+"my-factory");
            if (old.renameTo(neu)) {
                updateNew((int) id, "My Factory");
            }
        } else {
            DocumentDto documentDto1 = new DocumentDto();
            documentDto1.setName("My Factory");
            documentDto1.setTypeDoc("FOLDER");

            implNew.createDocument(documentDto1, "Emisuite");
        }

        DocumentDto documentDto1 = new DocumentDto();
        documentDto1.setName("Visual SGA");
        documentDto1.setTypeDoc("FOLDER");

        implNew.createDocument(documentDto1, "Emisuite");

    }

    /**
     * METHOD TO FIND THE ID OF THE FOLDER THAT ARE GOING TO MOVE TO EMISUITE
     *
     * @param old THE FILE THAT ARE GOING TO BE MOVED
     * @return TE ID OF THE SPECIFIED FILE
     * @throws IOException IF ANY ERROR OCCUR WITH RETROFIT
     */
    private long find(File old) throws IOException {
        return implNew.searchByPathName(old).get(0).getIdDocument();
    }

    /**
     * METHOD TO UPDATE THE FOLDER IN EMISUITE
     *
     * @param id   OF THE FOLDER TO UPDATE
     * @param name NEW NAME OF THE FOLDER
     * @throws IOException IF ANY ERROR OCCUR WITH RETROFIT
     */
    private void updateNew(int id, String name) throws IOException {
        DocumentDto documentsDto = implNew.getDocument(id);

        documentsDto.setName(name);

        implNew.updateDocument(documentsDto);
    }

    /**
     * THIS METHOD ONLY NEED TO INITIALIZE THE CHECKS OS SGA AND DP
     *
     * @throws Exception IF OCCURS ANY PROBLEM IN ANY CHECK
     */
    public void config() throws Exception {
        WarningUtil.showWarning("IMPORTANTE", "Ten el archivo config.json abierto, se te va a preguntar por informacion contenida en el mismo");

        checkDP();
    }

    /**
     * METHOD TO DO ALL THE CHECKS OF VISUAL SGA
     *
     * @throws Exception IF ANY METHOD OF RETROFIT FAIL
     */
    public void checksSGA() throws Exception {
        String sgaUUID;
        do {
            WarningUtil.showWarning("IMPORTANTE",
                    "Introduce el UUID de Visual SGA:");
            sgaUUID = WarningUtil.answer();
        } while (!isUUID(sgaUUID));

        DocumentDto documentByUUIDDto = implNew.findByUUID(sgaUUID);


        if (documentByUUIDDto == null) {

            DocumentDto documentDto = new DocumentDto();
            documentDto.setName("Visual SGA");
            documentDto.setTypeDoc("FOLDER");
            documentDto = implNew.createDocument(documentDto, "Emisuite");
            documentDto.setUuid(sgaUUID);

            DocumentService.updateDocument(documentDto);
        } else {
            List<DocumentDto> documentDtoList = implNew.moveDocuments(documentByUUIDDto.getIdDoc(),
                    null, "Emisuite");

            documentByUUIDDto = implNew.getDocument(documentByUUIDDto.getIdDoc());

            File file = new File(System.getProperty("user.home")
                    +File.separator+"Soincon"
                    +File.separator+"EMI"
                    +File.separator+"Cross-Solutions"
                    +File.separator+"Documents"
                    +File.separator+"RepoTest"
                    +File.separator+"Emisuite"
                    +File.separator+ documentByUUIDDto.getName());

            if (file.renameTo(new File(file.getParentFile() + File.separator+"Visual SGA"))) {
                documentByUUIDDto.setName("Visual SGA");
                implNew.updateDocument(documentByUUIDDto);
            }
        }

    }

    /**
     * Method to copy content FROM SGA to the that are indicated previously
     */
    private void copySGA() {
        try {
            WarningUtil.showWarning("Importante".toUpperCase(), "Donde esta la carpeta con el contenido que quieres mover, " +
                    "ten en cuenta que ahora todo esta en: \n"
            +System.getProperty("user.home")
                    +File.separator+"Soincon"
                    +File.separator+"EMI"
                    +File.separator+"Cross-Solutions"
                    +File.separator+"Documents"
                    +File.separator+"RepoTest");
            String answer = WarningUtil.answer();
            List<DocumentDto> documentDtoList = implNew.searchByPath(answer.
                    replace(System.getProperty("user.home")
                            +File.separator+"Soincon"
                            +File.separator+"EMI"
                            +File.separator+"Cross-Solutions"
                            +File.separator+"Documents"
                            +File.separator+"RepoTest", ""));

            DocumentDto SGAdocument = new DocumentDto();
            SGAdocument.setName("Visual SGA");
            SGAdocument.setTypeDoc("FOLDER");
            SGAdocument = implNew.createDocument(SGAdocument, "Emisuite");

            implNew.moveDocuments(Math.toIntExact(documentDtoList.get(0).getIdDocument()),
                    Math.toIntExact(SGAdocument.getIdDocument()), null);
        } catch (Exception ex) {
            WarningUtil.showError();
            copySGA();
        }
    }

    /**
     * METHOD TO DO ALL CHECKS OF DIGITAL PEOPLE
     *
     * @throws Exception IF ANY METHOD OF RETROFIT FAIL
     */
    public void checkDP() throws Exception {
        List<String> uuids = new ArrayList<>();
        String answer;

        WarningUtil.showWarning("Importante".toUpperCase(), "Ahora tienes que introducir los uuids que " +
                "pertenezcan a digital people, si un algun UUID se repite no lo introduzcas varias veces, solo una vez," +
                " pulsa n para terminar");
        do {
            WarningUtil.showWarning("Importante".toUpperCase(), "Escribe un uuid, n para terminar");
            answer = WarningUtil.answer();

            if (!answer.equalsIgnoreCase("n")) {
                if (isUUID(answer)) {
                    if (!uuids.contains(answer)) {
                        uuids.add(answer);
                    } else {
                        WarningUtil.showWarning("ERROR", "El UUID ya está en la lista");
                    }
                } else {
                    WarningUtil.showWarning("ERROR", "introduce un UUID valido o n para terminar");
                }
            }

        } while (!answer.equalsIgnoreCase("n"));

        if (!findDP(uuids)) {
            File file = new File("Emisuite");
            DocumentDto documentDto = new DocumentDto();

            documentDto.setName("Digital People");
            documentDto.setTypeDoc("FOLDER");

            documentDto = implNew.createDocument(documentDto, file.getName());

            documentDto.setUuid("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c");

            DocumentService.updateDocument(documentDto);
        } else {
            int position = uuids.indexOf("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c");

            DocumentDto documentDto = implNew.findByUUID(uuids.get(position));
            implNew.moveDocuments(documentDto.getIdDoc(), null, "Emisuite");

            uuids.remove("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c");
        }

        if (uuids.size() == 1) {
            if (uuids.get(0).equals("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c")) {
                DocumentDto documentDto = implNew.findByUUID(uuids.get(0));
                implNew.moveDocuments(documentDto.getIdDoc(), null, "Emisuite");
            } else {
                DocumentDto documentByUUIDDto = implNew.findByUUID(uuids.get(0));

                if (documentByUUIDDto != null) {
                    implNew.moveDocuments(documentByUUIDDto.getIdDoc(), null, "Emisuite/Digital People");
                }
            }
        } else {
            int i = 1;
            for (String UUID : uuids) {
                if (UUID.equals("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c")) {
                    DocumentDto documentDto = implNew.findByUUID(UUID);
                    if (documentDto != null) {
                        implNew.moveDocuments(documentDto.getIdDoc(), null, "Emisuite");
                    }
                } else {

                    DocumentDto documentByUUIDDto = implNew.findByUUID(UUID);

                    if (documentByUUIDDto != null) {
                        List<DocumentDto> documentDtoList = implNew.moveDocuments(documentByUUIDDto.getIdDoc(),
                                null, "Emisuite/Digital People/dp" + i);
                    } else {
                        DocumentDto documentDto = new DocumentDto();
                        documentDto.setName("Digital People");
                        documentDto.setTypeDoc("FOLDER");

                        documentDto = implNew.createDocument(documentDto, "Emisuite/Digital People/dp" + i);

                        documentDto.setUuid(UUID);

                        DocumentService.updateDocument(documentDto);
                    }
                    i++;
                }
            }
        }
    }

    /**
     * METHOD TO CHECK IF THE PARAMETER PASED BY COMMAND LINE IS A VALID UUID
     *
     * @param answer THE STRING TO CHECK IS VALID
     * @return TRUE IF IS VALID
     */
    private boolean isUUID(String answer) {
        if (answer == null) {
            return false;
        }

        try {
            UUID.fromString(answer);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * METHOD CHECK IF 1 OF THE UUIDS RECEIVED IS THE MAIN UUID OF DIGITAL PEOPLE
     *
     * @param uuids THE LIST OF UUIDS RECEIVED
     * @return BOOLEAN IF ONE THE UUIDS IS THE SEARCHED
     */
    private boolean findDP(List<String> uuids) {
        for (String UUID : uuids) {
            if (UUID.equals("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c")) {
                return true;
            }
        }
        return false;
    }
}