package com.soincon.migrate;

import com.soincon.migrate.logic.MigrateSystem;
import lombok.extern.log4j.Log4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.util.Properties;

/**
 * Introducir como variables:
 *  Api nueva, api antigua, rutas a migrar
 */

@SpringBootApplication
@Log4j
public class MigrateApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(MigrateApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        if (args.length >= 3) {
            System.out.println("bien hecho");
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
            System.out.println("Propiedades actualizadas con nuevos valores."+properties.getProperty("api.base.url")+properties.getProperty("api2.base.url"));

            MigrateSystem migrateSystem = new MigrateSystem();

            // Guardar las propiedades en el archivo
            try (FileOutputStream propertiesOutputStream = new FileOutputStream(propertiesFilePath)) {
                properties.store(propertiesOutputStream, "Actualizada la base de datos");
            } catch (IOException e) {
                System.err.println("Error guardando las propiedades: " + e.getMessage());
            }


            File f = new File("C:\\opt\\tools\\tomcat\\latest\\files\\clients");
            f.mkdirs();

            log.info("Empezando a migrar todo a esta ubicacion "+f.getAbsolutePath());

            for (int i = 2; i < args.length; i++) {
                log.info("Modificando a migrar todo de esta ubicacion "+args[i]);
                migrateSystem.migrate(args[i]);
            }
        }
    }
}
