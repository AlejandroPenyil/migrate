package com.soincon.migrate.dto.oldDtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileExportDto {

	boolean active;
    DownloadDto download;
    String expirationDate; // Date
    String file;
    String id;
    String insertDate; // Date
    String modificationDate; // Date
    Integer versionLock;


//	{
//              "active": true,
//              "download": {
//                    "authority": "string",
//                    "content": {},
//                    "defaultPort": 0,
//                    "deserializedFields": {},
//                    "file": "string",
//                    "host": "string",
//                    "path": "string",
//                    "port": 0,
//                    "protocol": "string",
//                    "query": "string",
//                    "ref": "string",
//                    "serializedHashCode": 0,
//                    "userInfo": "string"
//              },
//            "expirationDate": "string",
//            "file": "string",
//            "id": "string",
//            "insertDate": "string",
//            "modificationDate": "string",
//            "versionLock": 0



}
