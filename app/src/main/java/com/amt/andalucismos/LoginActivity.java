package com.amt.andalucismos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amt.andalucismos.utils.Notificaciones;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final int REQ_ONE_TAP = 0;

    // Variables de la clase
    private Button btnIniciarSesion, btnRegistrarse;
    private SignInButton btnIniciarSesionGoogle;
    private EditText txtCorreoElectronico, txtContrasena;
    private FirebaseAuth fbAuth; // Maneja la autenticación con Firebase
    private FirebaseUser fbUsuario; // Maneja el usuario
    private GoogleSignInClient fbSignInClient; // Maneja el inicio de sesión con Google
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Inicializar vistas
        inicializarVistas();
        // Configurar Google Sign-In
        configurarGoogleSignIn();
        // Configurar listeners
        configurarListeners();
    } // onCreate()

    // Método para inicializar las vistas
    private void inicializarVistas(){
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnIniciarSesionGoogle = findViewById(R.id.btnIniciarSesionGoogle);
        txtCorreoElectronico = findViewById(R.id.txtCorreoElectronico);
        txtContrasena = findViewById(R.id.txtContrasena);
        btnIniciarSesionGoogle.setSize(SignInButton.SIZE_WIDE); // Cambia el tamaño del botón para que sea más grande
        fbAuth = FirebaseAuth.getInstance(); // Instancia de FirebaseAuth
        database = FirebaseDatabase.getInstance().getReference(); // Referencia a la base de datos
    } // inicializarVistas()

    // Método para configurar Google Sign-In
    private void configurarGoogleSignIn(){
        GoogleSignInOptions fbSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        fbSignInClient = GoogleSignIn.getClient(this, fbSignInOptions);
    }

    // Método para configurar los listeners
    private void configurarListeners() {
        btnIniciarSesion.setOnClickListener(view -> {
            String email = txtCorreoElectronico.getText().toString().trim();
            String password = txtContrasena.getText().toString().trim();
            if (validarInputs(email, password)) {
                iniciarSesion(email, password);
            }
        });

        btnRegistrarse.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
            finish();
        });

        btnIniciarSesionGoogle.setOnClickListener(view -> iniciarSesionGoogle());
    }

    // Método para validar los inputs
    private boolean validarInputs(String email, String password) {
        if (email.isEmpty()) {
            Notificaciones.makeToast(getApplicationContext(), "El correo electrónico no puede estar vacío", Toast.LENGTH_SHORT);
            return false;
        }
        if (password.isEmpty()) {
            Notificaciones.makeToast(getApplicationContext(), "La contraseña no puede estar vacía", Toast.LENGTH_SHORT);
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Hacer que si el usuario no ha cerrado sesión, al entrar de nuevo no necesite identificarse
        fbUsuario = fbAuth.getCurrentUser();
        if(fbUsuario != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    } // onStart()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if(requestCode==REQ_ONE_TAP) {
           Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
           manejarResultadoInicioSesion(task);
       }
    } // onActivityResult()

    private void manejarResultadoInicioSesion(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            fbAuth.signInWithCredential(credential).addOnCompleteListener(this, task1 -> {
                if (task1.isSuccessful()) {
                    fbUsuario = fbAuth.getCurrentUser();
                    guardarUsuarioEnBaseDeDatos();
                } else {
                    Notificaciones.makeToast(getApplicationContext(), "¡Algo ha ido mal!", Toast.LENGTH_SHORT);
                }
            });
        } catch (ApiException e) {
            Notificaciones.makeDialog(this, "ERROR", e.getMessage(), "ACEPTAR", null, null, new Notificaciones.RespuestaDialog() {
                @Override
                public void onPositivo() {
                    Notificaciones.makeToast(getApplicationContext(), "Error al iniciar sesión", Toast.LENGTH_SHORT);
                }

                @Override
                public void onNegativo() {}

                @Override
                public void onNeutral() {}
            });
        }
    } // manejarResultadoInicioSesion()

    private void guardarUsuarioEnBaseDeDatos() {
        HashMap<String, Object> datosUsuario = new HashMap<>();
        datosUsuario.put("id", fbUsuario.getUid());
        datosUsuario.put("bio", "");
        datosUsuario.put("nombre", fbUsuario.getDisplayName());
        datosUsuario.put("sexo", "Otro");
        datosUsuario.put("email", fbUsuario.getEmail());

        database.child("usuarios").child(fbUsuario.getUid()).setValue(datosUsuario)
                .addOnSuccessListener(unused -> {
                    Notificaciones.makeToast(getApplicationContext(), "Registro exitoso.", Toast.LENGTH_SHORT);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Notificaciones.makeToast(getApplicationContext(), "Error al guardar datos del usuario.", Toast.LENGTH_SHORT));
    } // guardarUsuarioEnBaseDeDatos()

    private void iniciarSesion(String email, String password) {
        fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                Notificaciones.makeToast(getApplicationContext(), "Inicio de sesión correcto", Toast.LENGTH_SHORT);
            }
        }).addOnFailureListener(e -> {
            if (Objects.requireNonNull(e.getMessage()).startsWith("A network")) {
                Notificaciones.makeToast(getApplicationContext(), "Error: Sin conexión a internet", Toast.LENGTH_SHORT);
            } else {
                Notificaciones.makeToast(getApplicationContext(), "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT);
                txtContrasena.setText("");
                txtCorreoElectronico.setText("");
            }
        });
    } // iniciarSesion()

    private void iniciarSesionGoogle() {
        fbSignInClient.signOut();
        Intent intent = fbSignInClient.getSignInIntent();
        startActivityForResult(intent, REQ_ONE_TAP);
    } // iniciarSesionGoogle()

} // LoginActivity