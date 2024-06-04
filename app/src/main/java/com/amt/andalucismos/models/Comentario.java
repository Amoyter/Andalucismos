package com.amt.andalucismos.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class Comentario implements Parcelable {
    private String comentario;
    private String comentarioId;
    private String tipoComentario;
    private boolean revisado;
    private String usuarioId;


    // Constructor sin argumentos
    public Comentario() {}

    // Constructor con argumentos
    public Comentario(String comentario, String comentarioId, String tipoComentario, boolean revisado, String usuarioId) {
        this.comentario = comentario;
        this.comentarioId = comentarioId;
        this.tipoComentario = tipoComentario;
        this.revisado = revisado;
        this.usuarioId = usuarioId;
    }

    protected Comentario(Parcel in) {
        comentario = in.readString();
        comentarioId = in.readString();
        tipoComentario = in.readString();
        revisado = in.readByte() != 0;
        usuarioId = in.readString();
    }

    public static final Parcelable.Creator<Comentario> CREATOR = new Parcelable.Creator<Comentario>() {
        @Override
        public Comentario createFromParcel(Parcel in) {
            return new Comentario(in);
        }

        @Override
        public Comentario[] newArray(int size) {
            return new Comentario[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comentario);
        dest.writeString(comentarioId);
        dest.writeString(tipoComentario);
        dest.writeByte((byte) (revisado ? 1 : 0));
        dest.writeString(usuarioId);
    }

    // Getters y setters para cada campo


    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getComentarioId() {
        return comentarioId;
    }

    public void setComentarioId(String comentarioId) {
        this.comentarioId = comentarioId;
    }

    public String getTipoComentario() {
        return tipoComentario;
    }

    public void setTipoComentario(String tipoComentario) {
        this.tipoComentario = tipoComentario;
    }

    public boolean isRevisado() {
        return revisado;
    }

    public void setRevisado(boolean revisado) {
        this.revisado = revisado;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
}
