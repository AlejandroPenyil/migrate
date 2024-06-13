package com.soincon.migrate;

import com.soincon.migrate.dto.newDtos.DocumentDto;
import com.soincon.migrate.dto.oldDtos.DirectoryDto;
import com.soincon.migrate.dto.oldDtos.FileDto;
import com.soincon.migrate.dto.oldDtos.FileTypeDto;
import com.soincon.migrate.filter.Content;
import com.soincon.migrate.filter.FilterDirectory;
import com.soincon.migrate.logic.DocFolderMigration;
import com.soincon.migrate.retroFit.ImplNew;
import com.soincon.migrate.retroFit.ImplOld;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static com.soincon.migrate.MigrateApplication.f;
import static com.soincon.migrate.retroFit.ImplNew.Jwtoken;

@Log4j2
public class MigrateSystem1 {
    ImplOld implOld;
    ImplNew implNew;
    public static int fi = 0;
    public static int currentStep = 1;
    private static long startTime = -1;

    public MigrateSystem1() throws IOException {
        this.implNew = new ImplNew();
        this.implOld = new ImplOld();
    }

    /**
     * metodo que implica toda la logica para la migracion de ficheros y carpetas
     *
     * @param path    path of the old root
     * @param parent  dto with the information from the parent of the current file
     * @param newPath path of the new root
     * @throws IOException          an exception by a possible error with the files
     * @throws InterruptedException an exception throw by the thread of the progress bar
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

                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        ++currentStep;
                        migrate(subFile.getAbsolutePath(), documentDto, directory);
                    }
                } else {
                    for (File subFile : Objects.requireNonNull(file.listFiles())) {
                        ++currentStep;
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
                    ++currentStep;
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

                }
            } else {

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
     * Method that checks if a file exists in the old database.
     *
     * @param file The file you are checking for existence.
     * @return True if it exists, false otherwise.
     * @throws IOException Returns an error if there is any issue with the file.
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
     * Method to create the documentVersion in the new database.
     *
     * @param documentDto The document for which a version will be registered.
     * @param i           The number of the version.
     * @param fullName    The full name.
     * @throws IOException Returns an error if there is any issue with the file.
     */
    private void createVersion(DocumentDto documentDto, int i, String fullName, File file) throws IOException {

    }

    /**
     * metodo que obtiene el id de usuario con el tokem
     *
     * @return the id of the user
     */
    private Integer obtenerToken() {
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

//    /**
//     * Metodo para obtener el mimeType de un fichero
//     *
//     * @param fullName nombre completo del fichero
//     * @return el mimetype
//     * @throws IOException si ocurre algo con los ficheros
//     */
//    private String getMimeType(String fullName) throws IOException {
//        List<FileTypeDto> fileTypeDtos = implOld.getFileTypes();
//        for (FileTypeDto fileTypeDto : fileTypeDtos) {
//            File file = new File(fullName);
//            int lastIndex = file.getName().lastIndexOf('.');
//            if (lastIndex != -1) {
//                fullName = file.getName().substring(lastIndex, fullName.length());
//            }
//            if (fileTypeDto.getExtension().equals(fullName)) {
//                lastIndex = fileTypeDto.getMimeType().lastIndexOf('/');
//                if (lastIndex != -1) {
//                    fullName = fileTypeDto.getMimeType().substring(0, lastIndex) + "/" + fileTypeDto.getName();
//                }
//                return fullName;
//
//            } else if (fullName.equals(".jpeg")) {
//                if (fileTypeDto.getExtension().equals(".jpg")) {
//                    return fileTypeDto.getMimeType();
//                }
//            }
//        }
//        return "";
//    }

    /**
     * Metodo que le pone una extension a un fichero si este no tiene una
     *
     * @param file the file without extension
     * @return file with extension
     * @throws IOException Returns an error if there is any issue with the file.
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
     * @param file file with extension
     * @return name of the file without extension
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
     * @throws IOException Returns an error if there is any issue with the file.
     */
    public void cleanRoot(String pathroot, String newRoot) throws IOException {
    }

    /**
     * metodo que crea un documento nuevo en la nueva base de datos
     *
     * @param file carpeta que se quiere crear en la base de datos
     * @return el nuevo documento creado
     * @throws IOException si ocurre algo con loos ficheros
     */
    private void createNew(File file) throws IOException {

    }

    /**
     * metodo que borra el arbol de directorios antiguo
     *
     * @param path ubicacion que se va a borrar
     */
    public void borrar(String path) {

    }

}
