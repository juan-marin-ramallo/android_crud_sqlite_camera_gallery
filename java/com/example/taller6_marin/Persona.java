package com.example.taller6_marin;

import androidx.annotation.NonNull;

public class Persona {
    private int id;
    private String nombre;
    private int edad;
    private String rutaFoto;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getRutaFoto() {
        return rutaFoto;
    }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = rutaFoto;
    }

    @NonNull
    @Override
    public String toString() {
        return nombre;
    }
}
