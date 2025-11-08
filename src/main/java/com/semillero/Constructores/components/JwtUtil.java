package com.semillero.Constructores.components;

import java.sql.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;

@Component
public class JwtUtil {
 private final Key SECRET_KEY = Keys.hmacShaKeyFor("claveSecretaSuperSegura1234567890".getBytes());
    public String generarToken(String username, String rol) {
        return Jwts.builder()
                .setSubject(username)
                .addClaims(Map.of("rol", rol)) 
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }
}
