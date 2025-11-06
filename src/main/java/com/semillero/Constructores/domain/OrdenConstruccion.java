package com.semillero.Constructores.domain;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.semillero.Constructores.domain.model.Coordenada;
import com.semillero.Constructores.domain.model.EstadoOrden;

@Document(collection = "ordenes")
public class OrdenConstruccion {
    @Id
    private String id;

    @Field("tipo_construccion")
    private TipoConstruccion tipo;

    private Coordenada coordenada;
    private LocalDate fechaSolicitud;
    private LocalDate fechaInicioProgramada;
    private LocalDate fechaFinProgramada;
    private EstadoOrden estado;

    public OrdenConstruccion() {
    }

    public OrdenConstruccion(TipoConstruccion tipo, Coordenada coordenada, LocalDate fechaSolicitud) {
        this.tipo = tipo;
        this.coordenada = coordenada;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = EstadoOrden.PENDIENTE;
    }

    public String getId() {
        return id;
    }

    public TipoConstruccion getTipo() {
        return tipo;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public LocalDate getFechaInicioProgramada() {
        return fechaInicioProgramada;
    }

    public LocalDate getFechaFinProgramada() {
        return fechaFinProgramada;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setFechas(LocalDate inicio, LocalDate fin) {
        this.fechaInicioProgramada = inicio;
        this.fechaFinProgramada = fin;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "OrdenConstruccion{" +
                "id='" + id + '\'' +
                ", tipo=" + tipo +
                ", coordenada=" + coordenada +
                ", fechaSolicitud=" + fechaSolicitud +
                ", fechaInicioProgramada=" + fechaInicioProgramada +
                ", fechaFinProgramada=" + fechaFinProgramada +
                ", estado=" + estado +
                '}';
    }


}
