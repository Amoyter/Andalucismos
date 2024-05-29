package com.amt.andalucismos.ui.nueva;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amt.andalucismos.MainActivity;
import com.amt.andalucismos.R;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Date;

public class NuevaFragment extends Fragment {
    private View v;
    private Context c;
    private ArrayAdapter<CharSequence> adapter;
    private EditText txtPalabra, txtSignificado, txtEjemplo, txtPoblacion, txtComarca, txtTags;
    private Spinner spnrProvincia;
    private Button btnAnadir;
    private MainActivity mainActivity;
    private FirebaseUser fbUser;
    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_nueva, container, false);
        c = getContext();
        mainActivity = (MainActivity) getActivity();
        database = FirebaseDatabase.getInstance().getReference();
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        inicializarVariables();
        return v;
    }

    private void inicializarVariables() {
        txtPalabra = v.findViewById(R.id.txtPalabra);
        txtSignificado = v.findViewById(R.id.txtSignificado);
        txtEjemplo = v.findViewById(R.id.txtEjemplo);
        txtPoblacion = v.findViewById(R.id.txtPoblacion);
        txtComarca = v.findViewById(R.id.txtComarca);
        txtTags = v.findViewById(R.id.txtTags);
        spnrProvincia = v.findViewById(R.id.spnrProvincia);
        btnAnadir = v.findViewById(R.id.btnAnadir);

        adapter = ArrayAdapter.createFromResource(c, R.array.provincias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrProvincia.setAdapter(adapter);

        btnAnadir.setOnClickListener(view -> anadirPalabra());
    }

    private boolean validarFormulario() {
        boolean isValid = true;
        String mensajeError = "";

        if (txtPalabra.getText().toString().trim().isEmpty()) {
            mensajeError = "La palabra no puede estar vacía";
            isValid = false;
        } else if (txtSignificado.getText().toString().trim().isEmpty()) {
            mensajeError = "Debes indicar el significado";
            isValid = false;
        } else if (txtEjemplo.getText().toString().trim().isEmpty()) {
            mensajeError = "Debes indicar un ejemplo de uso";
            isValid = false;
        }

        if (!isValid) {
            Notificaciones.makeToast(c, mensajeError, Toast.LENGTH_SHORT);
        }

        return isValid;
    }

    private void anadirPalabra() {
        if (!mainActivity.hayConexion()) {
            Notificaciones.makeToast(c, "No hay conexión a Internet. No se puede añadir la palabra.", Toast.LENGTH_SHORT);
            return;
        }

        if (!validarFormulario()) {
            return;
        }

        String sUsuarioId = fbUser.getUid();
        DatabaseReference dbrExpresionId = database.child("contribuciones").push();
        String sExpresionId = dbrExpresionId.getKey();
        String sTags = new Gson().toJson(txtTags.getText().toString().toLowerCase().split("\\s"));

        Palabra nuevaPalabra = new Palabra(
                capitalizarPrimeraLetra(txtComarca.getText().toString().trim()),
                capitalizarPrimeraLetra(txtEjemplo.getText().toString().trim()),
                sExpresionId,
                String.valueOf(new Date().getTime()),
                0,
                capitalizarPrimeraLetra(txtPalabra.getText().toString().trim()),
                capitalizarPrimeraLetra(txtPoblacion.getText().toString().trim()),
                capitalizarPrimeraLetra(spnrProvincia.getSelectedItem().toString()),
                false,
                capitalizarPrimeraLetra(txtSignificado.getText().toString().trim()),
                sTags,
                sUsuarioId
        );

        dbrExpresionId.setValue(nuevaPalabra).addOnSuccessListener(unused -> {
            limpiarCampos();
            Notificaciones.makeDialog(c, "", "¡Gracias! Tenemos que revisar tu palabra, pero no te procupes, enseguida estará disponible.",
                    "", "", "ACEPTAR", new Notificaciones.RespuestaDialog() {
                        @Override
                        public void onPositivo() {}

                        @Override
                        public void onNegativo() {}

                        @Override
                        public void onNeutral() {
                            Notificaciones.makeToast(c, "Palabra añadida", Toast.LENGTH_SHORT);
                        }
                    });
        }).addOnFailureListener(e -> {
            Log.e("anadirPalabra", "Error al guardar la palabra.", e);
            Notificaciones.makeToast(c, "Error al guardar la palabra. Por favor, inténtalo de nuevo.", Toast.LENGTH_SHORT);
        });
    }

    private void limpiarCampos() {
        txtPalabra.setText("");
        txtSignificado.setText("");
        txtEjemplo.setText("");
        txtPoblacion.setText("");
        txtComarca.setText("");
        txtTags.setText("");
    }

    private String capitalizarPrimeraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}