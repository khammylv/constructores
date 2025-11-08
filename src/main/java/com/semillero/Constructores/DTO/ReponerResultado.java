package com.semillero.Constructores.DTO;

import java.util.List;
import java.util.Map;

import com.semillero.Constructores.domain.Inventario;
import com.semillero.Constructores.domain.model.InventarioFaltante;
import com.semillero.Constructores.domain.model.Material;

public class ReponerResultado {
private final Inventario inventario;
    private final Map<Material, Long> materialesRepuestos;
    private final List<InventarioFaltante> faltantesActualizados;

    public ReponerResultado(Inventario inventario, Map<Material, Long> materialesRepuestos, List<InventarioFaltante> faltantesActualizados) {
        this.inventario = inventario;
        this.materialesRepuestos = materialesRepuestos;
        this.faltantesActualizados = faltantesActualizados;
    }

    public Inventario getInventario() { return inventario; }
    public Map<Material, Long> getMaterialesRepuestos() { return materialesRepuestos; }
    public List<InventarioFaltante> getFaltantesActualizados() { return faltantesActualizados; }

    @Override
    public String toString() {
        return "ReponerResultado{" +
                "inventario=" + inventario +
                ", materialesRepuestos=" + materialesRepuestos +
                ", faltantesActualizados=" + faltantesActualizados +
                '}';
    }
}
