package com.amt.andalucismos.models;

import androidx.annotation.NonNull;

import java.util.List;

public class Palabra {
    private String comarca;
    private String ejemplo;
    private String expresionId;
    private String palabra;
    private String poblacion;
    private String provincia;
    private boolean revisado;
    private String significado;
    private List<String> tags;
    private String usuarioId;

    // Constructor vacío es necesario para Firebase
    public Palabra() {
    }

    // Constructor completo
    public Palabra(String comarca, String ejemplo, String expresionId, String palabra, String poblacion, String provincia, boolean revisado, String significado, List<String> tags, String usuarioId) {
        this.comarca = comarca;
        this.ejemplo = ejemplo;
        this.expresionId = expresionId;
        this.palabra = palabra;
        this.poblacion = poblacion;
        this.provincia = provincia;
        this.revisado = revisado;
        this.significado = significado;
        this.tags = tags;
        this.usuarioId = usuarioId;
    }

    // Getters y setters para cada propiedad
    public String getComarca() {
        return comarca;
    }

    public void setComarca(String comarca) {
        this.comarca = comarca;
    }

    public String getEjemplo() {
        return ejemplo;
    }

    public void setEjemplo(String ejemplo) {
        this.ejemplo = ejemplo;
    }

    public String getExpresionId() {
        return expresionId;
    }

    public void setExpresionId(String expresionId) {
        this.expresionId = expresionId;
    }

    public String getPalabra() {
        return palabra;
    }

    public void setPalabra(String palabra) {
        this.palabra = palabra;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public boolean isRevisado() {
        return revisado;
    }

    public void setRevisado(boolean revisado) {
        this.revisado = revisado;
    }

    public String getSignificado() {
        return significado;
    }

    public void setSignificado(String significado) {
        this.significado = significado;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    @NonNull
    @Override
    public String toString() {
        return "Palabra{" +
                "comarca='" + comarca + '\'' +
                ", ejemplo='" + ejemplo + '\'' +
                ", expresionId='" + expresionId + '\'' +
                ", palabra='" + palabra + '\'' +
                ", poblacion='" + poblacion + '\'' +
                ", provincia='" + provincia + '\'' +
                ", revisado=" + revisado +
                ", significado='" + significado + '\'' +
                ", tags=" + tags +
                ", usuarioId='" + usuarioId + '\'' +
                '}';
    }
}
