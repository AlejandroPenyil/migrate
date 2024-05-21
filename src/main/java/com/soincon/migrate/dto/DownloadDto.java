package com.soincon.migrate.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DownloadDto {

    String authority;
    Object content;
    Integer defaultPort;
    Object deserializedFields;
    String file;
    String host;
    String path;
    Integer port;
    String protocol;
    String query;
    String ref;
    Integer serializedHashCode;
    String serInfo;


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

}
