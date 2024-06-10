package com.amt.andalucismos.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.amt.andalucismos.LoginActivity;
import com.amt.andalucismos.MainActivity;
import com.amt.andalucismos.R;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {
    Button btnAdminPalabras, btnAdminFeedback, btnAdminCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        inicializarVistas();
        inicializarListeners();
    } // onCreate

    private void inicializarVistas() {
        btnAdminPalabras = findViewById(R.id.btnAdminPalabras);
        btnAdminFeedback = findViewById(R.id.btnAdminFeedback);
        btnAdminCerrarSesion = findViewById(R.id.btnAdminCerrarSesion);

    } // inicializarVistas

    private void inicializarListeners() {
        btnAdminPalabras.setOnClickListener(v -> {
            Intent intent = new Intent(this, PalabrasAdminActivity.class);
            startActivity(intent);
        });
        btnAdminFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(this, FeedbackAdminActivity.class);
            startActivity(intent);
        });
        btnAdminCerrarSesion.setOnClickListener(v -> {
            cerrarSesion();
        });
    } // inicializarListeners

    private void cerrarSesion() {
        Notificaciones.makeDialog(this, null, "¿Quieres cerrar sesión?", "SI", "CANCELAR", "", new Notificaciones.RespuestaDialog() {
            @Override
            public void onPositivo() {
                FirebaseAuth.getInstance().signOut();
                Notificaciones.makeToast(getApplicationContext(), "Sesión cerrada", Toast.LENGTH_SHORT);
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onNegativo() {}

            @Override
            public void onNeutral() {}
        });
    }
} // AdminActivity