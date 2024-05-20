package com.soincon.migrate;

import lombok.extern.log4j.Log4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Properties;

@SpringBootApplication
//@Log4j
public class MigrateApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MigrateApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        if (args.length >= 2) {
            System.out.println("bien hecho");
            Properties p = new Properties();
            InputStream propertiesStream = getClass().getClassLoader().getResourceAsStream("application.properties");
            p.load(propertiesStream);
            propertiesStream.close();

            p.setProperty("api.base.url",args[0]);
            p.store(new FileWriter("application.properties"),"Actualizada la base de datos");

            File f = new File("C:\\\\soincon\\\\EMI\\\\Cross-Solutions\\\\Documents\\\\RepoTest");
            f.mkdirs();
        }
    }
}
