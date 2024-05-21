package com.soincon.migrate.logic;

import com.soincon.migrate.dto.DocumentDto;
import com.soincon.migrate.dto.FileDto;
import com.soincon.migrate.security.AutentecationUser;
import com.soincon.migrate.retroFit.ImplNew;
import com.soincon.migrate.retroFit.ImplOld;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.IOException;

@Log4j
public class MigrateSystem {
    ImplOld implOld;
    ImplNew implNew;

    public MigrateSystem() throws IOException {
//        this.implNew = new ImplNew();
        this.implOld = new ImplOld();
    }

    public void migrate(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    migrate(subFile.getAbsolutePath());
                }
            }else{
                log.info("migrating " + file.getAbsolutePath());
            }
        }
    }
}
