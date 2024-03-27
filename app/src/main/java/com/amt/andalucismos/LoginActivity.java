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

public class LoginActivity extends AppCompatActivity implements Notificaciones {
    private static final int REQ_ONE_TAP = 0;

    // Variables de la clase
    private Button btnIniciarSesion, btnRegistrarse;
    private SignInButton btnIniciarSesionGoogle;
    private EditText txtCorreoElectronico, txtContrasena;
    private FirebaseAuth fbAuth; // Maneja la autenticación con Firebase
    private FirebaseUser fbUsuario; // Maneja el usuario
    private GoogleSignInClient fbSignInClient; // Maneja el inicio de sesión con Google
    private GoogleSignInOptions fbSignInOptions;
    private DatabaseReference database;


    /*TODO:
       LoginActivity
    *  -----------------------------------------------------
    *  1- Inicializar objetos de la vista y listeners
    *       1.1- Se enlazan las variables creadas a los objetos de la vista
    *       1.2- Se crean los listeners pertinentes
    *  2- Métodos
    *       2.1- Definicion del método encargado del inicio de sesión sin Google
    *  3- Hacer que si el usuario no ha cerrado sesión, al entrar de nuevo no necesite identificarse
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1- Instanciar los objetos de la vista
        // 1.1- Se enlazan las variables creadas a los objetos de la vista
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnIniciarSesionGoogle = findViewById(R.id.btnIniciarSesionGoogle);
        txtCorreoElectronico = findViewById(R.id.txtCorreoElectronico);
        txtContrasena = findViewById(R.id.txtContrasena);
        // Cambia el tamaño del botón para que sea más grande
        btnIniciarSesionGoogle.setSize(SignInButton.SIZE_WIDE);
        // Creamos una instancia de FirebaseAuth
        fbAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();

        fbSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        fbSignInClient = GoogleSignIn.getClient(this, fbSignInOptions);

        //1.2- Se crean los listeners pertinentes
        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprueba que ni el correo ni la contraseña estén vacíos
                if(txtCorreoElectronico.getText() == null || txtCorreoElectronico.getText().toString().trim().equals("")){
                    Notificaciones.makeToast(getApplicationContext(), "El correo electrónico no puede estar vacío", Toast.LENGTH_SHORT);
                }else if(txtContrasena.getText() == null || txtContrasena.getText().toString().trim().equals("")){
                    Notificaciones.makeToast(getApplicationContext(), "La contraseña no puede estar vacía", Toast.LENGTH_SHORT);
                }else {
                    String sEmail = txtCorreoElectronico.getText().toString().trim();
                    String sContrasena = txtContrasena.getText().toString().trim();
                    iniciarSesion(sEmail, sContrasena);
                }
            } // onClick()
        }); // btnIniciarSesion.setOnClickListener()
        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class));
                finish();
            } // onClick()
        }); // btnRegistrarse.setOnClickListener()
        btnIniciarSesionGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarSesionGoogle();
            }
        });
    } // onCreate()

    @Override
    protected void onStart() {
        super.onStart();
        // 3- Hacer que si el usuario no ha cerrado sesión, al entrar de nuevo no necesite identificarse
        fbUsuario = fbAuth.getCurrentUser();
        if(fbUsuario != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if(requestCode==REQ_ONE_TAP){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try{
                GoogleSignInAccount cuenta = task.getResult(ApiException.class);
                AuthCredential credenciales = GoogleAuthProvider.getCredential(cuenta.getIdToken(), null);
                fbAuth.signInWithCredential(credenciales).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            HashMap<String, Object> datosUsuario = new HashMap<>();
                            fbUsuario = fbAuth.getCurrentUser();

                            datosUsuario.put("id", fbUsuario.getUid());
                            datosUsuario.put("bio", "");
                            datosUsuario.put("nombre", fbUsuario.getDisplayName());
                            datosUsuario.put("sexo", "Otro");
                            datosUsuario.put("email", fbUsuario.getEmail());

                            database.child("usuarios").child(fbUsuario.getUid()).setValue(datosUsuario).addOnSuccessListener(unused -> {
                                Notificaciones.makeToast(getApplicationContext(), "Registro exitoso.", Toast.LENGTH_SHORT);
                                // Redirige al usuario a la MainActivity y finaliza la actividad actual
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }).addOnFailureListener(e -> {
                                // En caso de fallo al guardar los datos, notifica al usuario
                                Notificaciones.makeToast(getApplicationContext(), "Error al guardar datos del usuario.", Toast.LENGTH_SHORT);
                            });
                        } else {
                            Notificaciones.makeToast(getApplicationContext(), "¡Algo ha ido mal!", Toast.LENGTH_SHORT);
                        }
                    }
                });
            } catch (Exception e){
                Notificaciones.makeDialog(this, "ERROR", e.getMessage(), "ACEPTAR", null, null, new RespuestaDialog() {
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
        }
    }

    // 2- Métodos
    // 2.1- Definicion del método encargado del inicio de sesión sin Google

    /**
     * Intenta iniciar sesión con el correo electrónico y la contraseña proporcionados.
     * Si la autenticación es exitosa, finaliza la actividad actual e inicia `MainActivity`.
     * Si falla la autenticación o hay algún error, muestra un toast con el mensaje de error adecuado.
     *
     * @param sEmail El correo electrónico del usuario que intenta iniciar sesión.
     * @param sContrasena La contraseña del usuario que intenta iniciar sesión.
     */
    private void iniciarSesion(String sEmail, String sContrasena) {
        fbAuth.signInWithEmailAndPassword(sEmail, sContrasena).addOnCompleteListener(new OnCompleteListener<AuthResult>() { // En caso de que las credenciales sean correctas
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // Si se inicia sesion correctamente, se cierra la actividad y se abre la MainActivity
                    finish();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    Notificaciones.makeToast(getApplicationContext(), "Inicio de sesión correcto", Toast.LENGTH_SHORT);
                }
            }
        }).addOnFailureListener(new OnFailureListener() { // En caso de que las credenciales no sean correctas o haya algún error
            @Override
            public void onFailure(@NonNull Exception e) {
                if(Objects.requireNonNull(e.getMessage()).startsWith("A network")){
                    Notificaciones.makeToast(getApplicationContext(), "Error: Sin conexión a internet", Toast.LENGTH_SHORT);
                } else {
                    Notificaciones.makeToast(getApplicationContext(), "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT);
                    txtContrasena.setText("");
                    txtCorreoElectronico.setText("");
                }
            }
        });
    } // iniciarSesion()

    private void iniciarSesionGoogle(){
        fbSignInClient.signOut();
        Intent intent = fbSignInClient.getSignInIntent();
        startActivityForResult(intent, REQ_ONE_TAP); // En la documentación recomiendan este aunque esté "deprecated"
    } // iniciarSesionGoogle()

} // LoginActivity