package com.amt.andalucismos.ui.perfil;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amt.andalucismos.R;

public class PerfilFragment extends Fragment {
    private Context c;
    private String fotoHint, fotoId;
    private View v;
    private EditText txtBiografiaPerfil;
    private ImageButton imgBtnPerfil;
    private TextView txtUsuarioPerfil, txtCorreoPerfil;
    private Button btnGuardarCambios, btnEliminarCuenta, btnCerrarSesion;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_perfil, container, false);
        inicializarComponentes();
        confTxtBiografiaPerfil();
        inicializarEventosClick();
        return v;
    }

    private void inicializarComponentes() {
        c = getContext();
        txtUsuarioPerfil = v.findViewById(R.id.txtUsuarioPerfil);
        txtCorreoPerfil = v.findViewById(R.id.txtCorreoPerfil);
        txtBiografiaPerfil = v.findViewById(R.id.txtBiografiaPerfil);
        imgBtnPerfil = v.findViewById(R.id.imgBtnPerfil);
        btnGuardarCambios = v.findViewById(R.id.btnGuardarCambios);
        btnEliminarCuenta = v.findViewById(R.id.btnEliminarCuenta);
        btnCerrarSesion = v.findViewById(R.id.btnCerrarSesion);
    }

    private void confTxtBiografiaPerfil() {
        txtBiografiaPerfil.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                txtBiografiaPerfil.setMaxHeight(Integer.MAX_VALUE); // Expandir al seleccionar
            } else {
                txtBiografiaPerfil.setMaxHeight(dpToPx(150)); // Colapsar al deseleccionar
            }
        });

    } // confTxtBiografiaPerfil()

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void inicializarEventosClick(){
        txtBiografiaPerfil.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int lineas = txtBiografiaPerfil.getLineCount();
                if (lineas < 4) {
                    txtBiografiaPerfil.setLines(lineas);
                } else {
                    txtBiografiaPerfil.setLines(4);
                }
            }
        }); // btnBiografiaFerfil

        imgBtnPerfil.setOnClickListener(view -> {
            subirFoto();
        });
    } // inicializarEventosClick()

    private boolean subirFoto() {
        boolean haCargado = false;
        return haCargado;
    } // subirFoto()
}