package com.amt.andalucismos.ui.feedback;

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
import com.amt.andalucismos.models.Comentario;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Date;

public class FeedbackFragment extends Fragment {
    private View v;
    private Context c;
    private ArrayAdapter<CharSequence> adapter;
    private EditText txtComentario;
    private Spinner spnrTipoComentario;
    private Button btnEnviar;
    private MainActivity mainActivity;
    private FirebaseUser fbUser;
    private DatabaseReference database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_feedback, container, false);
        c = getContext();
        mainActivity = (MainActivity) getActivity();
        database = FirebaseDatabase.getInstance().getReference();
        fbUser = FirebaseAuth.getInstance().getCurrentUser();
        inicializarVariables();
        return v;
    }

    private void inicializarVariables() {
        txtComentario = v.findViewById(R.id.txtComentario);
        spnrTipoComentario = v.findViewById(R.id.spnrTipoComentario);
        btnEnviar = v.findViewById(R.id.btnEnviar);

        adapter = ArrayAdapter.createFromResource(c, R.array.tipo_comentario, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrTipoComentario.setAdapter(adapter);

        btnEnviar.setOnClickListener(view -> enviarComentario());
    }

    private boolean validarFormulario() {
        boolean isValid = true;
        String mensajeError = "";

        if (txtComentario.getText().toString().trim().isEmpty()) {
            mensajeError = "El comentario no puede estar vacío";
            isValid = false;
        }

        if (!isValid) {
            Notificaciones.makeToast(c, mensajeError, Toast.LENGTH_SHORT);
        }

        return isValid;
    }

    private void enviarComentario() {
        if (!mainActivity.hayConexion()) {
            Notificaciones.makeToast(c, "No hay conexión a Internet. No se puede enviar el comentario.", Toast.LENGTH_SHORT);
            return;
        }

        if (!validarFormulario()) {
            return;
        }

        String sUsuarioId = fbUser.getUid();
        DatabaseReference dbrComentarioId = database.child("comentarios").push();
        String sComentarioId = dbrComentarioId.getKey();

        Comentario nuevoComentario = new Comentario(
                capitalizarPrimeraLetra(txtComentario.getText().toString().trim()),
                sComentarioId,
                capitalizarPrimeraLetra(spnrTipoComentario.getSelectedItem().toString()),
                false,
                sUsuarioId
        );

        dbrComentarioId.setValue(nuevoComentario).addOnSuccessListener(unused -> {
            limpiarCampos();
            Notificaciones.makeDialog(c, "", "¡Gracias por tu comentario!",
                    "", "", "ACEPTAR", new Notificaciones.RespuestaDialog() {
                        @Override
                        public void onPositivo() {}

                        @Override
                        public void onNegativo() {}

                        @Override
                        public void onNeutral() {
                            Notificaciones.makeToast(c, "Comentario enviado", Toast.LENGTH_SHORT);
                        }
                    });
        }).addOnFailureListener(e -> {
            Log.e("enviarComentario", "Error al enviar el comentario", e);
            Notificaciones.makeToast(c, "Error al enviar el comentario. Por favor, inténtalo de nuevo.", Toast.LENGTH_SHORT);
        });
    }

    private void limpiarCampos() {
        txtComentario.setText("");
    }

    private String capitalizarPrimeraLetra(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
}