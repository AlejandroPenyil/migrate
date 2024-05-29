package com.soincon.migrate.logic;

import com.soincon.migrate.dto.newDtos.DocumentVersionDto;
import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.dto.oldDtos.DirectoryDto;
import com.soincon.migrate.dto.oldDtos.FileDto;
import com.soincon.migrate.dto.oldDtos.FileTypeDto;
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
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static com.soincon.migrate.MigrateApplication.f;
import static com.soincon.migrate.MigrateApplication.totalFilesAndDirectories;
import static com.soincon.migrate.retroFit.ImplNew.Jwtoken;

@Log4j2
public class MigrateSystem {
    ImplOld implOld;
    ImplNew implNew;
    public static int fi = 0;
    static int currentStep = 1;
    private long startTime = -1;

    public MigrateSystem() throws IOException {
        this.implNew = new ImplNew();
        this.implOld = new ImplOld();
    }

    /**
     * metodo que implica toda la logica para la migracion de ficheros y carpetas
     *
     * @param path
     * @param parent
     * @param newPath
     * @throws IOException
     * @throws InterruptedException
     */
    public void migrate(String path, DocumentDto parent, File newPath) throws IOException, InterruptedException {
        File file = new File(path);
        if (file.isDirectory()) {
            File directory = Paths.get(String.valueOf(newPath), file.getName()).toFile();
            DocFolderMigration docFolderMigration = new DocFolderMigration(directory, parent);
            if (folderExist(file)) {

                directory.mkdirs();

                if (!docFolderMigration.isExist()) {
                    DocumentDto documentDto = new DocumentDto();
                    documentDto.setName(file.getName().toLowerCase());
                    documentDto.setTypeDoc("FOLDER");
                    if (documentDto.getIdParent() == null) {
                        documentDto.setIdParent(docFolderMigration.getIdParent());
                    }

                    documentDto = implNew.createDocument(documentDto, file.getParentFile().getPath().toLowerCase());
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        showProgressIndicator(++currentStep, totalFilesAndDirectories);
                        migrate(subFile.getAbsolutePath(), documentDto, directory);
                    }
                } else {
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        showProgressIndicator(++currentStep, totalFilesAndDirectories);
                        migrate(subFile.getAbsolutePath(), docFolderMigration.getDocumentDto(), directory);
                    }
                }
            } else {
                File file2 = Paths.get(String.valueOf(f), "notfound").toFile();
                file2.mkdir();
                boolean t = true;
                int i = 0;
                do {
                    file2 = new File(file2 + "\\" + i + file.getName());
                    if (file2.mkdir()) {
                        t = false;
                    } else {
                        i++;
                    }
                } while (t);
                for (File subFile : Objects.requireNonNull(file.listFiles())) {
                    showProgressIndicator(++currentStep, totalFilesAndDirectories);
                    migrate(subFile.getAbsolutePath(), docFolderMigration.getDocumentDto(), directory);
                }
            }
        } else {
            if (exists(file)) {
                File directory = Paths.get(String.valueOf(newPath), file.getName()).toFile();
                if (!directory.exists()) {
                    FileUtils.copyFile(file, directory);
//                    file.renameTo(directory);


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

                    } else {
                        extension = FilenameUtils.getExtension(asignarExtension(directory).getAbsolutePath());
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
                File file2 = Paths.get(String.valueOf(f), "notfound").toFile();
                file2.mkdir();
                boolean t = true;
                int i = 0;
                do {
                    file2 = new File(file2 + "\\" + i + file.getName());
                    if (!file2.exists()) {
                        FileUtils.copyFile(file, file2);
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
        FilterDirectory filterDirectory = new FilterDirectory();
        Content content = new Content();
        content.setExactName(file.getName());
        filterDirectory.setContent(content);

        List<DirectoryDto> fileDtoList = implOld.searchDirectoryByFilterAll(filterDirectory);

        if (!fileDtoList.isEmpty()) {
            for (DirectoryDto fileDto : fileDtoList) {
                if (file.getParentFile().getAbsolutePath().equalsIgnoreCase(fileDto.getPathBase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Metodo que pinta la barra de progreso
     *
     * @param currentStep
     * @param totalSteps
     */
    private void showProgressIndicator(int currentStep, int totalSteps) {
        // Obtener el tiempo de inicio solo la primera vez que se llama al método
        if (startTime == -1) {
            startTime = System.currentTimeMillis();
        }

        int progressBarLength = 85; // Longitud total de la barra de progreso
        double completedBlocks = (double) currentStep * progressBarLength / totalSteps;
        int remainingBlocks = progressBarLength - (int) completedBlocks;

        int percentage = (int) (currentStep * 100.0 / totalSteps); // División como punto flotante

        // Calcular el tiempo transcurrido
        long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        long elapsedTimeSeconds = elapsedTimeMillis / 1000;
        long hours = elapsedTimeSeconds / 3600;
        long minutes = (elapsedTimeSeconds % 3600) / 60;
        long seconds = elapsedTimeSeconds % 60;

        StringBuilder progressBar = new StringBuilder("[");
        for (int i = 0; i < (int) completedBlocks; i++) {
            progressBar.append("\u2588"); // Carácter Unicode para un bloque lleno
        }
        for (int i = 0; i < remainingBlocks; i++) {
            progressBar.append("\u2591"); // Carácter Unicode para un bloque vacío
        }
        progressBar.append("] ").append(percentage).append("%");

        // Añadir el cronómetro
        progressBar.append(" [");
        if (hours > 0) {
            progressBar.append(String.format("%02d:", hours));
        }
        progressBar.append(String.format("%02d:%02d]", minutes, seconds));

        // Imprimir la barra de progreso
        System.out.print("\r" + progressBar.toString());
    }

    /**
     * Metodo que comprueba si un fichero existe en la base de datos antigua
     *
     * @param file
     * @return
     * @throws IOException
     */
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

    /**
     * metodo para crear el documentVersion en la nueva base de datos
     *
     * @param documentDto
     * @param i
     * @param fullName
     * @throws IOException
     */
    private void createVersion(DocumentDto documentDto, int i, String fullName, File file) throws IOException {
        DocumentVersionDto documentVersionDto = new DocumentVersionDto();
        documentVersionDto.setIdVersion(i);
        documentVersionDto.setIdDocument(documentDto.getIdDocument());
        documentVersionDto.setExternalCode(fullName);
        obtenerToken();
        documentVersionDto.setIdUser(114);
        documentVersionDto.setReason("migrate");
        documentVersionDto.setVersionStatus("VALID");
        documentVersionDto.setUploadDate(ZonedDateTime.now().toString());
        Path path = file.toPath();

        String mimeType = Files.probeContentType(path);
        documentVersionDto.setMimeType(mimeType);

        implNew.documentCreateDto(documentVersionDto);
    }

    /**
     * metodo que obtiene el id de usuario con el tokem
     *
     * @return
     */
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

    /**
     * Metodo para obtener el mimeType de un fichero
     *
     * @param fullName
     * @return
     * @throws IOException
     */
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

    /**
     * Metodo que le pone una extension a un fichero si este no tiene una
     *
     * @param file
     * @return
     * @throws IOException
     */
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

    /**
     * Metodo que sirve para obtener el nombre de un fichero sin su extension
     *
     * @param file
     * @return
     */
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

    /**
     * metodo que revisa la carpeta root y si esta contiene ficheros los mueve a una nueva ubicacion
     *
     * @param pathroot Ubicacion root antigua
     * @param newRoot  Nueva ubicacion de root
     * @throws IOException
     */
    public void cleanRoot(String pathroot, String newRoot) throws IOException {

        Path path = Paths.get(pathroot);
        File directory = path.toFile();

        File newDirectory = new File(newRoot);
        path = Paths.get(String.valueOf(newDirectory), "folderRootNew");
        File file2 = path.toFile();

        DocumentDto documentDto = createNew(file2);

        File[] files = directory.listFiles(File::isFile);

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!exists(file)) {
                        path = Paths.get(String.valueOf(f), "notFound");
                        file2 = path.toFile();
                        file2.mkdirs();
                        file2 = new File(file2 + "\\" + file.getName());
                        if (!file2.exists()) {
                            FileUtils.copyFile(file, file2);
                        } else {
                            int i = 1;
                            boolean tr = false;

                            String extension = FilenameUtils.getExtension(file.getAbsolutePath());

                            if (extension.isEmpty()) {
                                File file3 = asignarExtension(directory);
                                if (file.renameTo(file3)) {
                                    file = file3;
                                }
                                extension = FilenameUtils.getExtension(file.getAbsolutePath());

                            }
                            String name = quitarExtension(directory);
                            do {
                                String fullName = name.toLowerCase() + "_V" + i + '.' + extension;
                                file2 = new File(file2 + "\\" + fullName);
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
                            File file3 = asignarExtension(directory);
                            if (file.renameTo(file3)) {
                                file = file3;
                            }
                            extension = FilenameUtils.getExtension(file.getAbsolutePath());

                        }

                        String name = quitarExtension(directory);
                        do {
                            String fullName = name.toLowerCase() + "_V" + i + '.' + extension;
                            file2 = new File(file2 + "\\" + fullName);
                            if (!file2.exists()) {
                                FileUtils.copyFile(directory, file2);
                                if (file2.exists()) {
                                    tr = true;
                                    DocumentDto documentDto1 = new DocumentDto();
                                    documentDto1.setTypeDoc("DOC");
                                    documentDto1.setName(file.getName());
                                    documentDto1.setIdParent(documentDto.getIdParent());
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
                        file = new File(file2 + "\\" + file.getName());
                        file.mkdir();
                    } else {
                        file2.mkdir();
                        boolean tr = false;

                        file2 = new File(file2 + "\\" + file.getName());
                        if (file2.mkdir()) {

                            DocumentDto documentDto1 = new DocumentDto();
                            documentDto1.setTypeDoc("FOLDER");
                            documentDto1.setName(file.getName());
                            documentDto1.setIdParent(documentDto.getIdParent());
                            documentDto1 = implNew.createDocument(documentDto1, file.getParentFile().getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * metodo que crea un documento nuevo en la nueva base de datos
     *
     * @param file
     * @return
     * @throws IOException
     */
    private DocumentDto createNew(File file) throws IOException {
        DocumentDto documentDto = new DocumentDto();
        documentDto.setName(file.getName());
        documentDto.setTypeDoc("FOLDER");
        return implNew.createDocument(documentDto, file.getParentFile().getAbsolutePath());
    }

    /**
     * metodo que borra el arbol de directorios antiguo
     *
     * @param path
     */
    public void borrar(String path) {
        File file = new File(path);
        if (file.listFiles() != null) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                borrar(subFile.getPath());
            }
        }
        file.delete();
    }
}