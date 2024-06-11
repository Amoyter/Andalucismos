package com.amt.andalucismos.models;

import android.os.Parcel;
import android.os.Parcelable;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;


public class Palabra implements Parcelable {
    private String comarca;
    private String ejemplo;
    private String expresionId;
    private String fechaAnadida;
    private int numFavoritas;
    private String palabra;
    private String poblacion;
    private String provincia;
    private boolean revisado;
    private String significado;
    private String tags;
    private String usuarioId;
    private List<String> lTags;

    // Constructor sin argumentos
    public Palabra() {
        // Inicializa los valores predeterminados si es necesario
        this.lTags = new ArrayList<>();
    }

    // Constructor con argumentos
    public Palabra(String comarca, String ejemplo, String expresionId, String fechaAnadida, int numFavoritas, String palabra, String poblacion, String provincia, boolean revisado, String significado, String tags, String usuarioId) {
        this.comarca = comarca;
        this.ejemplo = ejemplo;
        this.expresionId = expresionId;
        this.fechaAnadida = fechaAnadida;
        this.numFavoritas = numFavoritas;
        this.palabra = palabra;
        this.poblacion = poblacion;
        this.provincia = provincia;
        this.revisado = revisado;
        this.significado = significado;
        this.tags = tags;
        this.usuarioId = usuarioId;

        if (tags != null && !tags.isEmpty() && isValidJson(tags)) {
            try {
                this.lTags = new Gson().fromJson(tags, new TypeToken<List<String>>() {}.getType());
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                this.lTags = new ArrayList<>();
            }
        } else {
            this.lTags = new ArrayList<>();
        }
    }

    protected Palabra(Parcel in) {
        comarca = in.readString();
        ejemplo = in.readString();
        expresionId = in.readString();
        fechaAnadida = in.readString();
        numFavoritas = in.readInt();
        palabra = in.readString();
        poblacion = in.readString();
        provincia = in.readString();
        revisado = in.readByte() != 0;
        significado = in.readString();
        tags = in.readString();
        usuarioId = in.readString();
        lTags = in.createStringArrayList();
    }

    public static final Parcelable.Creator<Palabra> CREATOR = new Creator<Palabra>() {
        @Override
        public Palabra createFromParcel(Parcel in) {
            return new Palabra(in);
        }

        @Override
        public Palabra[] newArray(int size) {
            return new Palabra[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comarca);
        dest.writeString(ejemplo);
        dest.writeString(expresionId);
        dest.writeString(fechaAnadida);
        dest.writeInt(numFavoritas);
        dest.writeString(palabra);
        dest.writeString(poblacion);
        dest.writeString(provincia);
        dest.writeByte((byte) (revisado ? 1 : 0));
        dest.writeString(significado);
        dest.writeString(tags);
        dest.writeString(usuarioId);
        dest.writeStringList(lTags);
    }

    private boolean isValidJson(String json) {
        try {
            new Gson().fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException ex) {
            return false;
        }
    }

    // Getters y setters para cada campo
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

    public String getFechaAnadida() {
        return fechaAnadida;
    }

    public void setFechaAnadida(String fechaAnadida) {
        this.fechaAnadida = fechaAnadida;
    }

    public int getNumFavoritas() {
        return numFavoritas;
    }

    public void setNumFavoritas(int numFavoritas) {
        this.numFavoritas = numFavoritas;
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

    public boolean getRevisado() {
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<String> getLTags() {
        return lTags;
    }

    public void setLTags(List<String> lTags) {
        this.lTags = lTags;
    }
}



