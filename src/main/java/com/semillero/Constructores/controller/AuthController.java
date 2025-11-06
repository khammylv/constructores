package com.semillero.Constructores.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.semillero.Constructores.domain.Usuario;
import com.semillero.Constructores.service.AuthService;
import com.semillero.Constructores.service.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
 private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> request) {
        authService.registrar(request.get("username"), request.get("password"));
        return Map.of("message", "Usuario registrado correctamente");
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
         Usuario user = authService.getUser(username);
        boolean valido = authService.validarCredenciales(username, password);
        if (!valido) {
            return Map.of("error", "Credenciales inválidas");
        }
        String token = jwtUtil.generarToken(username, user.getRol());
        return Map.of("token", token);
    }
}
