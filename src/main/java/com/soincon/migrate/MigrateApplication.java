package com.soincon.migrate;

import com.soincon.migrate.command.WarningUtil;
import com.soincon.migrate.logic.MigrateSystem;
import lombok.extern.log4j.Log4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;

import static com.soincon.migrate.logic.MigrateSystem.fi;

/**
 * Introducir como variables:
 * Api nueva, api antigua
 */

@SpringBootApplication
@Log4j
@CommandLine.Command(name = "migrate", mixinStandardHelpOptions = true, version = "1.0",
        description = "MigrateApplication realiza tareas de migración.")
public class MigrateApplication implements CommandLineRunner, Runnable {

    public static File f = new File("C:\\soincon\\EMI\\Cross-Solutions\\Documents\\RepoTest");

    @CommandLine.Option(names = {"-a1", "--api1"}, description = "URL base para la API de la que se va a migrar", required = true)
    private String api1Url;

    @CommandLine.Option(names = {"-a2", "--api2"}, description = "URL base para la API a la que se quiere migrar", required = true)
    private String api2Url;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new MigrateApplication());

        // Comprobar si se solicita la ayuda o la versión antes de ejecutar Spring Boot
        if (commandLine.isUsageHelpRequested() || commandLine.isVersionHelpRequested()) {
            commandLine.execute(args);
            return; // Salir sin ejecutar la aplicación Spring Boot
        }

        // Lanzar la aplicación Spring Boot
        SpringApplication.run(MigrateApplication.class, args);

        // Ejecutar el comando PicoCLI
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }


    @Override
    public void run() {
        WarningUtil.showWarning("IMPORTANTE", "Este programa hará cambios en el sistema de archivos y no se podrán recuperar");

        Properties properties = new Properties();
        String propertiesFilePath = "src/main/resources/application.properties";

        // Cargar las propiedades desde un archivo en el sistema de archivos
        try (FileInputStream propertiesStream = new FileInputStream(propertiesFilePath)) {
            properties.load(propertiesStream);
        } catch (IOException e) {
            System.err.println("Error cargando las propiedades: " + e.getMessage());
            return;
        }

        // Establecer nuevas propiedades
        properties.setProperty("api.base.url", api1Url);
        properties.setProperty("api2.base.url", api2Url);
        System.out.println("Propiedades actualizadas con nuevos valores: " + properties.getProperty("api.base.url") + ", " + properties.getProperty("api2.base.url"));

        MigrateSystem migrateSystem = null;
        try {
            migrateSystem = new MigrateSystem();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Guardar las propiedades en el archivo
        try (FileOutputStream propertiesOutputStream = new FileOutputStream(propertiesFilePath)) {
            properties.store(propertiesOutputStream, "Actualizada la base de datos");
        } catch (IOException e) {
            System.err.println("Error guardando las propiedades: " + e.getMessage());
        }

        log.info("Empezando a migrar todo a esta ubicacion " + f.getAbsolutePath());

        Scanner src = new Scanner(System.in);
        System.out.println("Pasa la ubicacion de root para limpiar");
        String pathroot = src.nextLine();
        System.out.println("Escribe la nueva ubicacion de root");
        String newRoot = src.nextLine();

        f = new File(newRoot);
        f.mkdir();

        try {
            migrateSystem.cleanRoot(pathroot, newRoot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Modificando a migrar todo de esta ubicacion " + pathroot);
        log.info("Esto llevara un rato...");
        try {
            migrateSystem.migrate(pathroot, null, f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Migración completada.");

        WarningUtil.showWarning("ALERTA", "Quieres borrar las carpetas del root antiguo. Si lo borras no lo podrás recuperar más tarde. S/N");
        String opc = src.next();
        boolean t = false;
        do {
            switch (opc) {
                case ("s"):
                case ("S"):
                    migrateSystem.borrar(pathroot);
                    break;
                case ("n"):
                case ("N"):
                    System.out.println("Terminando el programa");
                    break;
                default:
                    System.out.println("Elige una opcion S/N");
                    t = true;
                    break;
            }
        } while (t);
        System.out.println("Programa terminado");
    }

    @Override
    public void run(String... args) throws Exception {
/*
        if (args.length >= 2) {
            System.out.print("---------------------------------" +
                    "\n|  *******IMPORTANTE*******     |" +
                    "\n| Este programa hara cambios en |" +
                    "\n| el sitema de archivos y no se |" +
                    "\n| podran recuperar              |" +
                    "\n---------------------------------\n");
            Properties properties = new Properties();
            String propertiesFilePath = "src/main/resources/application.properties";

            // Cargar las propiedades desde un archivo en el sistema de archivos
            try (FileInputStream propertiesStream = new FileInputStream(propertiesFilePath)) {
                properties.load(propertiesStream);
            } catch (IOException e) {
                System.err.println("Error cargando las propiedades: " + e.getMessage());
                return;
            }

            // Establecer nuevas propiedades
            properties.setProperty("api.base.url", args[1]);
            properties.setProperty("api2.base.url", args[0]);
            System.out.println("Propiedades actualizadas con nuevos valores." + properties.getProperty("api.base.url") + properties.getProperty("api2.base.url"));

            MigrateSystem migrateSystem = new MigrateSystem();

            // Guardar las propiedades en el archivo
            try (FileOutputStream propertiesOutputStream = new FileOutputStream(propertiesFilePath)) {
                properties.store(propertiesOutputStream, "Actualizada la base de datos");
            } catch (IOException e) {
                System.err.println("Error guardando las propiedades: " + e.getMessage());
            }

            log.info("Empezando a migrar todo a esta ubicacion " + f.getAbsolutePath());

            Scanner src = new Scanner(System.in);
            System.out.println("Pasa la ubicacion de root para limpiar");
            String pathroot = src.nextLine();
            System.out.println("Escribe la nueva ubicacion de root");
            String newRoot = src.nextLine();

            f = new File(newRoot);

            f.mkdir();

            migrateSystem.cleanRoot(pathroot, newRoot);


            log.info("Modificando a migrar todo de esta ubicacion " + pathroot);
            log.info("Esto llevara un rato...");
            migrateSystem.migrate(pathroot, null, f);
            log.info(fi);



            System.out.println("-------------------------------------------------------------");
            System.out.println("|                       ***ALERTA***                        |");
            System.out.println("| Quieres borrar las carpetas del root Antiguo antiguas S/N |");
            System.out.println("|       Si lo borras no lo podras recuperar más tarde       |");
            System.out.println("-------------------------------------------------------------");
            String opc = src.next();
            boolean t = false;
            do {
                switch (opc) {
                    case ("s"):
                    case ("S"):
                        migrateSystem.borrar(pathroot);
                        break;
                    case ("n"):
                    case ("N"):
                        System.out.println("Terminando el programa");
                        break;
                    default:
                        System.out.println("Elige una opcion S/N");
                        t = true;
                        break;
                }
            } while (t);
        }
        System.out.println("Programa terminado");*/
    }
}
