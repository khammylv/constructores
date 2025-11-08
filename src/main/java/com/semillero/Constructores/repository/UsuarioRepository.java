package com.semillero.Constructores.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.semillero.Constructores.domain.model.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
Optional<Usuario> findByUsername(String username);
}
