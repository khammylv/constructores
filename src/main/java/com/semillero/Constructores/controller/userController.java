package com.semillero.Constructores.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.semillero.Constructores.DTO.UsuarioDTO;
import com.semillero.Constructores.domain.model.Usuario;
import com.semillero.Constructores.service.AuthService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200")
public class userController {
    @Autowired
    private AuthService authService;

    @PostMapping()
    public ResponseEntity<Map<String, String>> register(@RequestBody Usuario usuario) {
        System.out.println(usuario.getRol());
        authService.registrar(usuario);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado correctamente"));
    }

    @PutMapping
    public ResponseEntity<Map<String, String>> update(@RequestBody UsuarioDTO usuario) {
        authService.actualizarUsuario(usuario);
        return ResponseEntity.ok(Map.of("message", "Usuario actualizado correctamente"));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAll() {
        List<UsuarioDTO> usuarios = authService.getAllUser();
        return ResponseEntity.ok(usuarios);
    }
}
