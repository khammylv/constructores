package com.semillero.Constructores.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.semillero.Constructores.DTO.UsuarioDTO;
import com.semillero.Constructores.domain.model.Usuario;
import com.semillero.Constructores.repository.UsuarioRepository;

@Service
public class AuthService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario registrar(Usuario usuario) {

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(UsuarioDTO userDto) {
        Usuario usuarioExistente = usuarioRepository.findById(userDto.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioExistente.setNombre(userDto.getNombre());
        usuarioExistente.setUsername(userDto.getUsername());
        usuarioExistente.setRol(userDto.getRol());
        usuarioExistente.setStatus(userDto.getStatus());

        return usuarioRepository.save(usuarioExistente);
    }

    public boolean validarCredenciales(String username, String password) {
        return usuarioRepository.findByUsername(username)
                .map(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElse(false);
    }

    public Usuario getUser(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public List<UsuarioDTO> getAllUser() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioDTO> usuariosDTO = usuarios.stream()
                .map(usuario -> new UsuarioDTO(
                        usuario.getId(),
                        usuario.getNombre(),
                        usuario.getUsername(),
                        usuario.getRol(),
                        usuario.getStatus()))
                .collect(Collectors.toList());

        return usuariosDTO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        var authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol());

        return new org.springframework.security.core.userdetails.User(
                usuario.getUsername(),
                usuario.getPassword(),
                List.of(authority));
    }
}
