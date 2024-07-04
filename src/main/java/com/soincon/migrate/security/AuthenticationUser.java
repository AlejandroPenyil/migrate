package com.soincon.migrate.security;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthenticationUser {
    private final String username = System.getProperty("api.security.user"); ;
    private final String password = System.getProperty("api.security.password"); ;
}
