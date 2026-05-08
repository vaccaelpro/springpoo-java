package com.sena.springpoo.modells;

import java.time.LocalDate;

public class Gasto {

    private int idGasto;
    private String categoria;
    private double monto;
    private LocalDate fechaRegistro;

    public Gasto() {}

    public Gasto(int idGasto, String categoria, double monto, LocalDate fechaRegistro) {
        this.idGasto = idGasto;
        this.categoria = categoria;
        this.monto = monto;
        this.fechaRegistro = fechaRegistro;
    }

    public int getIdGasto() { return idGasto; }
    public void setIdGasto(int idGasto) { this.idGasto = idGasto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}