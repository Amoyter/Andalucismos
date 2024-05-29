package com.amt.andalucismos.models;

import java.util.List;

public class Usuario {
    private String bio;
    private String email;
    private List<String> favoritas;
    private String id;
    private String nombre;
    private String sexo;

    // Constructor vacío necesario para Firebase
    public Usuario() {
    }

    // Constructor completo
    public Usuario(String bio, String email, List<String> favoritas, String id, String nombre, String sexo) {
        this.bio = bio;
        this.email = email;
        this.favoritas = favoritas;
        this.id = id;
        this.nombre = nombre;
        this.sexo = sexo;
    }

    // Getters y Setters
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getFavoritas() {
        return favoritas;
    }

    public void setFavoritas(List<String> favoritas) {
        this.favoritas = favoritas;
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

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
}

