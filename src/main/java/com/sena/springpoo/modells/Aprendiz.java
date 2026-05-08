package com.sena.springpoo.modells;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class Aprendiz {

    private int id;

    @JsonProperty("primerNombre")
    private String primerNombre;

    @JsonProperty("segundoNombre")
    private String segundoNombre;

    @JsonProperty("primerApellido")
    private String primerApellido;

    @JsonProperty("segundoApellido")
    private String segundoApellido;

    @JsonProperty("tipoDocumento")
    private String tipoDocumento;

    @JsonProperty("documento")
    private String documento;

    @JsonProperty("celular")
    private String celular;

    @JsonProperty("grupoFormacion")
    private String grupoFormacion;

    @JsonProperty("correo")
    private String correo;

    @JsonProperty("contrasena")
    private String contrasena;

    @JsonProperty("rol")
    private String rol;

    @JsonProperty("tipoApoyo")
    private String tipoApoyo;

    @JsonProperty("foto")
    private String foto;

    private LocalDate fechaRegistro;

    public Aprendiz() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPrimerNombre() { return primerNombre; }
    public void setPrimerNombre(String primerNombre) { this.primerNombre = primerNombre; }

    public String getSegundoNombre() { return segundoNombre; }
    public void setSegundoNombre(String segundoNombre) { this.segundoNombre = segundoNombre; }

    public String getPrimerApellido() { return primerApellido; }
    public void setPrimerApellido(String primerApellido) { this.primerApellido = primerApellido; }

    public String getSegundoApellido() { return segundoApellido; }
    public void setSegundoApellido(String segundoApellido) { this.segundoApellido = segundoApellido; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }

    public String getGrupoFormacion() { return grupoFormacion; }
    public void setGrupoFormacion(String grupoFormacion) { this.grupoFormacion = grupoFormacion; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getTipoApoyo() { return tipoApoyo; }
    public void setTipoApoyo(String tipoApoyo) { this.tipoApoyo = tipoApoyo; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
}