package com.semillero.Constructores.DTO;

import com.semillero.Constructores.domain.model.RoleUsuario;
import com.semillero.Constructores.domain.model.StatusUsuario;

public class UsuarioDTO {

    private String id;
    private String nombre;

    private String username;
    private StatusUsuario status;
    private RoleUsuario rol;

    public UsuarioDTO(String id, String nombre, String username, RoleUsuario rol, StatusUsuario status) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.rol = rol;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public RoleUsuario getRol() {
        return rol;
    }

    public void setRol(RoleUsuario rol) {
        this.rol = rol;
    }

}
