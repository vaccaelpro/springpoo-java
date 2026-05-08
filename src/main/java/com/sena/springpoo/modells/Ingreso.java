package com.sena.springpoo.modells;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class Ingreso {

    @JsonProperty("idIngreso")
    private int idIngreso;

    @JsonProperty("monto")
    private double monto;

    @JsonProperty("fecha")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    public Ingreso() {}

    public Ingreso(int idIngreso, double monto, LocalDate fecha) {
        this.idIngreso = idIngreso;
        this.monto = monto;
        this.fecha = fecha;
    }

    public int getIdIngreso() { return idIngreso; }
    public void setIdIngreso(int idIngreso) { this.idIngreso = idIngreso; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}