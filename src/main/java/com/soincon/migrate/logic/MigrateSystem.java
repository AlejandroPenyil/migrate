package com.soincon.migrate.logic;

import emisuite.documentmanager.enums.TypeDoc;
import emisuite.documentmanager.dto.DocumentDto;
import emisuite.documentmanager.dto.DocumentVersionDto;
import emisuite.documentmanager.enums.VersionStatus;
import es.snc.common.persistence.filter.Filter;
import es.snc.document.manager.dto.DirectoryDto;
import es.snc.document.manager.dto.PathDto;
import es.snc.document.manager.dto.FileDto;
import es.snc.document.manager.dto.FileTypeDto;
import es.snc.document.manager.persistence.filter.DirectoryFilter;
import es.snc.document.manager.persistence.filter.FileFilter;

import com.soincon.migrate.command.WarningUtil;
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
                documentDto.setTypeDoc(TypeDoc.FOLDER);

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
                    documentDto.setUuid(UUID.fromString(uuid));
                    DocumentService.updateDocument(documentDto);
                }
                for (File subFile : Objects.requireNonNull(file.listFiles())) {
                    ++currentStep;
                    migrate(subFile.getAbsolutePath(), documentDto, directory);
                }
            } else {
                DocFolderMigration docFolderMigration = new DocFolderMigration(directory, parent);
                File file2 = Paths.get(String.valueOf(f), "notfound").toFile();
                if (file2.mkdir()) {
                    log.info("crated {}", file2.getAbsolutePath());
                }

                boolean t = true;
                int i = 0;
                do {
                    File nfDirectory = new File(file2 + File.separator + i + "-" + name);
                    if (nfDirectory.mkdir()) {
                        log.info("{} added to not found", nfDirectory.getName());
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
                            documentDto.setTypeDoc(TypeDoc.DOC);
                            documentDto.setIdParent(docFolderMigration.getDocumentDto().getIdDocument());

                            documentDto = implNew.createDocument(documentDto, file2.getParentFile().getPath().toLowerCase());
                            if (uuid != null) {
                                documentDto.setUuid(UUID.fromString(uuid));
                                DocumentService.updateDocument(documentDto);
                            }
                            fi++;
                            if (documentDto != null) {
                                createVersion(documentDto, (long) i, fullName, file2);
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
                if (directory.mkdir()) {
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
                            log.info("copy file: {}", file2.getAbsolutePath());
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
                    uuid = String.valueOf(directoryDto.getId());
                    return true;
                }
            }
        }

        Filter<DirectoryFilter> filter = new Filter<>();
        DirectoryFilter directoryFilter = new DirectoryFilter();
        directoryFilter.setExactName(file.getName());

        filter.setContent(directoryFilter);

        List<DirectoryDto> fileDtoList = implOld.searchDirectoryByFilterAll(filter);

        if (!fileDtoList.isEmpty()) {
            for (DirectoryDto fileDto : fileDtoList) {
                if (file.getParentFile().getAbsolutePath().equalsIgnoreCase(fileDto.getPathBase())) {
                    uuid = String.valueOf(fileDto.getId());
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
                    uuid = String.valueOf(directoryDto.getId());
                    return true;
                }
            }
        }

        Filter<FileFilter> filter = new Filter<>();
        FileFilter filterFile = new FileFilter();
        filterFile.setExactName(file.getName());
        filter.setContent(filterFile);

        List<FileDto> fileDtoList = implOld.searchFileByFilterAll(filter);

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
    private void createVersion(DocumentDto documentDto, Long i, String fullName, File file) throws IOException {
        DocumentVersionDto documentVersionDto = new DocumentVersionDto();
        documentVersionDto.setIdVersion(i);
        documentVersionDto.setIdDocument(documentDto.getIdDocument());
        documentVersionDto.setExternalCode(fullName);
        documentVersionDto.setIdUser(obtainToken());
        documentVersionDto.setReason("migrate");
        documentVersionDto.setVersionStatus(VersionStatus.VALID);
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
    private Long obtainToken() {
        long idUser = 1L;

        String[] chunks = Jwtoken.getToken().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));

        String[] code = payload.split(",");

        for (String s : code) {
            if (s.contains("userId")) {
                String[] id = s.split(":");
                idUser = Long.parseLong(id[1]);
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
        Filter<FileFilter> filter = new Filter<>();
        FileFilter filterFile = new FileFilter();
        filterFile.setExactName(file.getName());

        filter.setContent(filterFile);

        List<FileDto> fileDtoList = implOld.searchFileByFilterAll(filter);
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
     * Method that checks the root folder and if it contains files, moves them to a new location
     *
     * @param pathRoot Old root location
     * @param newRoot  New root location
     * @throws IOException Returns an error if there is any issue with the file.
     */
    public void cleanRoot(String pathRoot, String newRoot) throws Exception {
        Path path = Paths.get(pathRoot);
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
                            if (file2.mkdirs()) {
                                log.info("created: {}", file2.getAbsolutePath());
                            }
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
                            if (file2.mkdir()) {
                                log.info("created: {}", file2.getPath());
                            }
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
                                        documentDto1.setTypeDoc(TypeDoc.DOC);
                                        documentDto1.setName(file.getName());
                                        documentDto1.setIdParent(documentDto.getIdDocument());
                                        documentDto1 = implNew.createDocument(documentDto1, file.getParentFile().getAbsolutePath());
                                        if (documentDto1 != null) {
                                            createVersion(documentDto, (long) i, fullName, file2);
                                        }
                                    }
                                }
                            } while (!tr);
                        }
                    } else {
                        if (!exists(file)) {
                            path = Paths.get(String.valueOf(f), "notFound");
                            file2 = path.toFile();
                            if (file2.mkdirs()) {
                                log.info("created: {}", file2.getPath());
                            }
                            file = new File(file2 + File.separator + file.getName());
                            if (file.mkdir()) {
                                log.info("created: {}", file.getPath());
                            }
                        } else {
                            if (file2.mkdir()) {
                                log.info("created: {}", file2.getPath());
                            }

                            file2 = new File(file2 + File.separator + file.getName());
                            if (file2.mkdir()) {

                                DocumentDto documentDto1 = new DocumentDto();
                                documentDto1.setTypeDoc(TypeDoc.FOLDER);
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
        documentDto.setTypeDoc(TypeDoc.FOLDER);
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
        if (file.delete()) {
            log.info("delete has been completed successfully");
        } else {
            log.error("An error has occurred and the deletion of {} could not be completed", file.getAbsolutePath());
        }
    }

    public void newMigration(File f) throws Exception {
        File file = new File(f.getAbsolutePath() + File.separator + "Emisuite");
        if (file.exists()) {
            moveToEmisuite(f.getAbsolutePath() + File.separator + "clients", file);
        } else {
            DocumentDto documentDto = new DocumentDto();
            documentDto.setTypeDoc(TypeDoc.FOLDER);
            documentDto.setName(file.getName());
            implNew.createDocument(documentDto, "");

            moveToEmisuite(f.getAbsolutePath() + File.separator + "clients", file);
        }
    }

    /**
     * METHOD TO MOVE THE MAIN FOLDERS TO EMISUITE
     *
     * @param s    PATH OF THE FOLDERS
     * @param file NEW PATH OF THE FOLDERS
     * @throws Exception IF ANY ERROR OCCUR WITH RETROFIT
     */
    private void moveToEmisuite(String s, File file) throws Exception {
        File old = new File(s + File.separator + "1" + File.separator + "vt");
        File neu = new File(file.getAbsolutePath() + File.separator + "Visual Tracking");

        if (old.exists()) {
            long id = find(old);
            log.info("{} find, now moving to {}", old, neu);
            implNew.moveDocuments(id, null, "Emisuite");

            old = new File(file.getAbsolutePath() + File.separator + "vt");
            if (old.renameTo(neu)) {
                updateNew((int) id, "Visual Tracking");
            }
        } else {
            log.info("{} not find, now creating to {}", old, neu);
            DocumentDto documentDto1 = new DocumentDto();
            documentDto1.setName("Visual Tracking");
            documentDto1.setTypeDoc(TypeDoc.FOLDER);

            implNew.createDocument(documentDto1, "Emisuite");
        }

        old = new File(System.getProperty("local.gmao.folder"));
        neu = new File(file.getAbsolutePath() + File.separator + "Easy GMAO");

        if (old.exists()) {
            long id = find(old);
            log.info("{} find, now moving to {}", old, neu);
            implNew.moveDocuments(id, null, "Emisuite");
            old = new File(file.getAbsolutePath() + File.separator + old.getName());
            if (old.renameTo(neu)) {
                updateNew((int) id, "Easy GMAO");
            }
        } else {
            log.info("{} not find, now creating to {}", old, neu);
            DocumentDto documentDto1 = new DocumentDto();
            documentDto1.setName("Easy GMAO");
            documentDto1.setTypeDoc(TypeDoc.FOLDER);

            documentDto1 = implNew.createDocument(documentDto1, "Emisuite");

            documentDto1.setUuid(UUID.fromString("6d44b088-2324-4afd-b186-514f150205cb"));

            DocumentService.updateDocument(documentDto1);
        }

        old = new File(s + File.separator + "clientid-1" + File.separator + "my-factory");
        neu = new File(file.getAbsolutePath() + File.separator + "My Factory");

        if (old.exists()) {
            long id = find(old);
            log.info("{} find, now moving to {}", old, neu);
            implNew.moveDocuments(id, null, "Emisuite");
            old = new File(file.getAbsolutePath() + File.separator + "my-factory");
            if (old.renameTo(neu)) {
                updateNew((int) id, "My Factory");
            }
        } else {
            log.info("{} not find, now creating to {}", old, neu);
            DocumentDto documentDto1 = new DocumentDto();
            documentDto1.setName("My Factory");
            documentDto1.setTypeDoc(TypeDoc.FOLDER);

            implNew.createDocument(documentDto1, "Emisuite");
        }

        DocumentDto documentDto1 = new DocumentDto();
        documentDto1.setName("Visual SGA");
        documentDto1.setTypeDoc(TypeDoc.FOLDER);

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
            documentDto.setTypeDoc(TypeDoc.FOLDER);

            documentDto = implNew.createDocument(documentDto, file.getName());

            documentDto.setUuid(UUID.fromString("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c"));

            DocumentService.updateDocument(documentDto);
        } else {
            int position = uuids.indexOf("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c");

            DocumentDto documentDto = implNew.findByUUID(uuids.get(position));
            implNew.moveDocuments(documentDto.getIdDocument(), null, "Emisuite");

            uuids.remove("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c");
        }
        if (uuids.size() == 1) {
            if (uuids.get(0).equals("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c")) {
                DocumentDto documentDto = implNew.findByUUID(uuids.get(0));
                implNew.moveDocuments(documentDto.getIdDocument(), null, "Emisuite");
            } else {
                DocumentDto documentByUUIDDto = implNew.findByUUID(uuids.get(0));

                if (documentByUUIDDto != null) {
                    implNew.moveDocuments(documentByUUIDDto.getIdDocument(), null, "Emisuite/Digital People");
                }
            }
        } else {
            int i = 1;
            for (String UUID : uuids) {
                if (UUID.equals("3791fbdb-cb62-4742-9cf3-fc8f79d8a51c")) {
                    DocumentDto documentDto = implNew.findByUUID(UUID);
                    if (documentDto != null) {
                        implNew.moveDocuments(documentDto.getIdDocument(), null, "Emisuite");
                    }
                } else {

                    DocumentDto documentByUUIDDto = implNew.findByUUID(UUID);

                    if (documentByUUIDDto != null) {
                        implNew.moveDocuments(documentByUUIDDto.getIdDocument(),
                                null, "Emisuite/Digital People/dp" + i);
                    } else {
                        DocumentDto documentDto = new DocumentDto();
                        documentDto.setName("Digital People");
                        documentDto.setTypeDoc(TypeDoc.FOLDER);

                        documentDto = implNew.createDocument(documentDto, "Emisuite/Digital People/dp" + i);

                        documentDto.setUuid(java.util.UUID.fromString(UUID));

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