package com.soincon.migrate;

import com.soincon.migrate.command.WarningUtil;
import com.soincon.migrate.logic.MigrateSystem;
import com.soincon.migrate.logic.ProgressIndicator;
import lombok.extern.log4j.Log4j2;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import picocli.CommandLine;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

/**
 * Introducir como variables:
 * Api nueva, api antigua
 */

@SpringBootApplication
@Log4j2
@CommandLine.Command(name = "migrate", mixinStandardHelpOptions = true, version = "1.0",
        description = "MigrateApplication, realiza tareas de migración.")
public class MigrateApplication implements CommandLineRunner, Runnable {
    public static int totalFilesAndDirectories;

    public static File f = new File("C:\\Soincon\\EMI\\Cross-Solutions\\Documents\\RepoTest");
    private File odl = new File("c:\\opt\\tools\\tomcat\\latest\\files\\clients");

    @CommandLine.Option(names = {"-a1", "--api1"}, description = "URL base para la API de la que se va a migrar", required = true)
    private String api1Url;

    @CommandLine.Option(names = {"-a2", "--api2"}, description = "URL base para la API a la que se quiere migrar", required = true)
    private String api2Url;

    @CommandLine.Option(names = {"-d", "--url"}, description = "URL de la base de datos (jdbc:mysql:// se da por hecho)", required = true)
    private String dbUrl;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Nombre del usuario en la base de datos", required = true)
    private String user;

    @CommandLine.Option(names = {"-p", "--pass"}, description = "Contraseña de la base de datos", required = true)
    private String password;

    @CommandLine.Option(names = {"-as", "--apis"}, description = "URL base de la API de seguridad", required = true)
    private String apiSUrl;

    @CommandLine.Option(names = {"-ps", "--passSecurity"}, description = "Contraseña de usuario", required = true)
    private String passwordSecurity;

    @CommandLine.Option(names = {"-us", "--userSecurity"}, description = "Usuario para acceder a la api de seguridad", required = true)
    private String userSecurity;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new MigrateApplication());

        // Comprobar si se solicita la ayuda o la versión antes de ejecutar Spring Boot
        if (commandLine.isUsageHelpRequested() || commandLine.isVersionHelpRequested()) {
            commandLine.execute(args);
            return; // Salir sin ejecutar la aplicación Spring Boot
        }

        // Lanzar la aplicación Spring Boot
        SpringApplication app = new SpringApplication(MigrateApplication.class);
        app.setBanner(new CustomBanner());
        app.run(args);


        // Ejecutar el comando PicoCLI
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    /**
     * Generar el banner de la aplicación
     */
    private static class CustomBanner implements Banner {
        @Override
        public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
            try {
                String bannerText = "";

                out.println(bannerText);
            } catch (Exception e) {
                out.println("Error al generar el banner: " + e.getMessage());
            }
        }
    }

    /**
     * ejecutar el programa en la la consola de picocli
     */
    @Override
    public void run() {
        WarningUtil.showAlert("ALERTA", "Este programa puede hacer cambios que no se podran desacer.");


        Properties properties = new Properties();
        String propertiesFilePath = "application.properties";

        // Cargar las propiedades desde un archivo en el sistema de archivos
        try (InputStream inputStream = MigrateApplication.class.getClassLoader().getResourceAsStream(propertiesFilePath)) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                log.error("File not found: {}", propertiesFilePath);
            }
        } catch (IOException e) {
            log.error("Error loading properties: {}", e.getMessage());
            return;
        }

        System.setProperty("api.base.url", api1Url);
        System.setProperty("api2.base.url", api2Url);
        System.setProperty("spring.datasource.url", "jdbc:mysql://" + dbUrl);
        System.setProperty("spring.datasource.username", user);
        System.setProperty("spring.datasource.password", password);
        System.setProperty("api.security.base.url", apiSUrl);
        System.setProperty("api.security.user", userSecurity);
        System.setProperty("api.security.password", passwordSecurity);

        MigrateSystem migrateSystem;
        try {
            migrateSystem = new MigrateSystem();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Guardar las propiedades en el archivo
        try (FileOutputStream propertiesOutputStream = new FileOutputStream(propertiesFilePath)) {
            properties.store(propertiesOutputStream, "");
        } catch (IOException e) {
            System.err.println("Error guardando las propiedades: " + e.getMessage());
        }

        Scanner src = new Scanner(System.in);

//        log.info("Enter the root location to clean (leave empty to use the default path):\n{}", odl);
        String pathroot = WarningUtil.showWarningAndReadInput("IMPORTANTE",
                "Introduce la localizacion root a migrar (dejalo vacio para usar el path por defecto):\n" + odl + "\n");
        if (pathroot.isEmpty()) {
            pathroot = odl.getAbsolutePath();
            log.info("Usando el path por defecto: {}", pathroot);
        }


        File root = new File(pathroot);
        if (!root.exists()) {
            try {
                throw new FileNotFoundException();
            } catch (FileNotFoundException e) {
                WarningUtil.showAlert("ERROR", "La ruta por defecto antigua no ha sido encontrada");
                return;
            }
        }


//        log.info("Enter the new root location (leave empty to use the default path):\n{}", f);
        String newRoot = WarningUtil.showWarningAndReadInput("IMPORTANTE",
                "Introduce la nueva localizacion root (dejalo vacio para usar el path por defecto):\n" + f + "\n");
        if (newRoot.isEmpty()) {
            newRoot = f.getAbsolutePath();
            log.info("Usando el path por defecto: {}", newRoot);
        }


        f = new File(newRoot);
        f.mkdirs();

        try {
            migrateSystem.cleanRoot(pathroot, newRoot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Modificando todo de: {}", pathroot);
        log.info("Esto llevara un rato...");

        File file = new File(pathroot);
        totalFilesAndDirectories = countFilesAndDirectories(file);
        try {
            ProgressIndicator progressIndicator = new ProgressIndicator(totalFilesAndDirectories);
            Thread progressThread = new Thread(progressIndicator);
            progressThread.start();
            migrateSystem.migrate(pathroot, null, f);
            progressIndicator.stop();

            System.out.println();
            migrateSystem.newMigration(f);

            migrateSystem.config();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println();
        log.info("Migracion completada.");

        WarningUtil.showAlert("ALERTA", "¿Quieres borrar todos los archivos del root antiguo? si lo borras " +
                "no lo podras recuperar mas tarde. Y/N");

        boolean t;
        do {
            String opc = WarningUtil.answer();
            switch (opc) {
                case ("y"):
                case ("Y"):
                    WarningUtil.showWarning("SEGURO", "¿Estas seguro? y/n");
                    src.nextLine();
                    opc = src.next();
                    switch (opc) {
                        case ("y"):
                        case ("Y"):
                            WarningUtil.showWarning("INFORMACION", "Borrando todo ");
                            migrateSystem.borrar(pathroot);
                            break;
                        default:
                            break;
                    }
                    t = false;
                    break;
                case ("n"):
                case ("N"):
                    log.info("Terminando el programa");
                    t = false;
                    break;
                default:
                    log.info("Elige una opcion Y/N");
                    t = true;
                    break;
            }
        } while (t);
        log.info("Programa terminado");
        AnsiConsole.systemUninstall();

    }

    /**
     * metodo que cuenta la cantidad de fichero y directorios de manera recursiva para
     * que funcione el progressbar
     *
     * @param file ubicacion en la que contar ficheros y directorios
     * @return numero de ficheros y directorios
     */
    private int countFilesAndDirectories(File file) {
        int count = 0;
        if (file.isDirectory()) {
            count++; // Contar el directorio actual
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                count += countFilesAndDirectories(subFile); // Recursivamente contar los archivos y directorios dentro del directorio actual
            }
        } else {
            count++; // Contar el archivo actual
        }
        return count;
    }

    @Override
    public void run(String... args) {
    }
}
