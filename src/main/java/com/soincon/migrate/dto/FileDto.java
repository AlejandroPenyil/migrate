package com.soincon.migrate.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileDto {

    Boolean active;
    byte[] fileData;
    List<FileExportDto> fileExports;
    String id;
    String insertDate; //Date
    String mimeType;
    String modificationDate; //Date
    String name;
    String parentDirectoryId;
    String pathBase;
    int version;
    Integer versionLock;


//    {
//        "active": true,
//        "fileData": "string",
//        "fileExports": [
//       ],
//       "id": "string",
//       "insertDate": "string",
//       "mimeType": "string",
//       "modificationDate": "string",
//       "name": "string",
//       "parentDirectoryId": "string",
//       "pathBase": "string",
//       "version": 0,
//       "versionLock": 0
//    }

}
