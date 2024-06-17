package com.soincon.migrate.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Getter
@NoArgsConstructor
public class AutentecationUser {
    private final String username = System.getProperty("api.security.user"); ;
    private final String password = System.getProperty("api.security.password"); ;
}
