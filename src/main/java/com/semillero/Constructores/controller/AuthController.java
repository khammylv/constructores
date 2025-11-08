package com.semillero.Constructores.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.semillero.Constructores.DTO.UserLogin;

import com.semillero.Constructores.domain.model.StatusUsuario;
import com.semillero.Constructores.domain.model.Usuario;
import com.semillero.Constructores.service.AuthService;
import com.semillero.Constructores.service.JwtService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final AuthService authService;

    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;

        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLogin userLogin) {
        String username = userLogin.getUsername();
        String password = userLogin.getPassword();
        Usuario userVal = authService.getUser(username);
        boolean valido = authService.validarCredenciales(username, password);
       
        if (!valido || userVal.getStatus().equals(StatusUsuario.INACTIVO)) {
            String errMessage = userVal.getStatus().equals(StatusUsuario.INACTIVO)? "Usuario inactivo": "Credenciales inválidas";
            
            return ResponseEntity.status(401).body(Map.of("error", errMessage));
        }

        var user = authService.loadUserByUsername(username);
        var token = jwtService.generateToken(user, userVal.getRol().toString());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
