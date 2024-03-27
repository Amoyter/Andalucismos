package com.amt.andalucismos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amt.andalucismos.utils.Notificaciones;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity implements Notificaciones {
    // Declaración de variables de la clase para la UI y Firebase
    EditText txtNombreUsuario, txtCorreoElectronico, txtContrasena, txtRepetirContrasena, txtBiografia;
    Spinner spnrSexo;
    Button btnRegistrarse, btnVolver;
    ArrayAdapter<CharSequence> adapter;
    FirebaseAuth fbAuth;
    DatabaseReference database;

    /*TODO:
        RegistroActivity
    *   -----------------------------------------------------
    *   1- Inicializar componentes de la interfaz de usuario.
    *   2- Configurar FirebaseAuth y DatabaseReference.
    *   3- Establecer adaptador para el spinner de selección de sexo.
    *   4- Implementar la lógica de navegación para el botón Volver.
    *   5- Establecer la lógica de registro para el botón Registrarse.
    *       5.1- Recolectar datos de entrada del usuario.
    *       5.2- Validar los datos de entrada.
    *       5.3- Llamar a la función de registro si la validación es exitosa.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // 1- Inicializar componentes de la interfaz de usuario
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        txtCorreoElectronico = findViewById(R.id.txtCorreoElectronico);
        txtContrasena = findViewById(R.id.txtContrasena);
        txtRepetirContrasena = findViewById(R.id.txtRepetirContrasena);
        txtBiografia = findViewById(R.id.txtBiografia);
        spnrSexo = findViewById(R.id.spnrSexo);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        btnVolver = findViewById(R.id.btnVolver);

        // 2- Configurar FirebaseAuth y DatabaseReference
        fbAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // 3- Establecer adaptador para el spinner de selección de sexo
        adapter = ArrayAdapter.createFromResource(this, R.array.sexo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrSexo.setAdapter(adapter);

        // 4- Implementar la lógica de navegación para el botón Volver
        btnVolver.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
        });

        // 5- Establecer la lógica de registro para el botón Registrarse
        btnRegistrarse.setOnClickListener(view -> {
            // 5.1- Recolectar datos de entrada del usuario
            String sCorreoElectronico = txtCorreoElectronico.getText().toString().trim();
            String sContrasena = txtContrasena.getText().toString();
            String sRepetirContrasena = txtRepetirContrasena.getText().toString();
            String sNombreUsuario = txtNombreUsuario.getText().toString().trim();
            String sBiografia = txtBiografia.getText().toString().trim();
            String sSexo = spnrSexo.getSelectedItem().toString();

            // 5.2- Validar los datos de entrada
            if(validarFormulario(sCorreoElectronico, sContrasena, sRepetirContrasena, sNombreUsuario, sBiografia)){
                // 5.3- Llamar a la función de registro si la validación es exitosa
                registrarUsuario(sCorreoElectronico, sContrasena, sNombreUsuario, sBiografia, sSexo);
            }
        });
    }

    /**
     * Valida los datos del formulario de registro.
     *
     * Este método verifica que los campos del formulario de registro cumplan con los requisitos básicos:
     * - El email debe tener un formato válido y no estar vacío.
     * - La contraseña debe tener al menos 6 caracteres de longitud y no estar vacía.
     * - El nombre no debe estar vacío.
     *
     * @param sCorreoElectronico El correo electrónico ingresado por el usuario.
     * @param sContrasena La contraseña ingresada por el usuario.
     * @param sRepetirContrasena La contraseña repetida ingresada por el usuario.
     * @param sNombreUsuario El nombre ingresado por el usuario.
     * @param sBiografia La biografía ingresada por el usuario.
     * @return boolean Retorna true si todos los campos cumplen con los requisitos; de lo contrario, retorna false.
     */
    private boolean validarFormulario(String sCorreoElectronico, String sContrasena, String sRepetirContrasena, String sNombreUsuario, String sBiografia) {
        boolean valido = true;

        // Validación del Email
        if (TextUtils.isEmpty(sCorreoElectronico) || !Patterns.EMAIL_ADDRESS.matcher(sCorreoElectronico).matches()) {
            Notificaciones.makeToast(this, "Ingresa un correo electronico válido.", Toast.LENGTH_SHORT);
            valido = false;
        }
        // Validación de la Contraseña
        else if (TextUtils.isEmpty(sContrasena) || sContrasena.length() < 6) {
            Notificaciones.makeToast(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT);
            valido = false;
        }
        // Validacion de la repeticón de la contraseña
        else if (!sRepetirContrasena.equals(sContrasena)){
            Notificaciones.makeToast(this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT);
            valido = false;
        }
        // Validación del Nombre
        else if (TextUtils.isEmpty(sNombreUsuario)) {
            Notificaciones.makeToast(this, "Ingresa tu nombre de usuario.", Toast.LENGTH_SHORT);
            valido = false;
        }
        // Validación de la Biografía
        else if (TextUtils.isEmpty(sBiografia)) {
            txtBiografia.setText("");
        }

        return valido;
    }

    /**
     * Registra un nuevo usuario en Firebase Authentication y guarda su información adicional en Firebase Realtime Database.
     *
     * @param sCorreoElectronico Correo electrónico del usuario para el registro.
     * @param sContrasena        Contraseña del usuario para el registro.
     * @param sNombreUsuario     Nombre del usuario.
     * @param sBiografia         Biografía del usuario.
     * @param sSexo              Sexo del usuario.
     */
    private void registrarUsuario(final String sCorreoElectronico, String sContrasena, final String sNombreUsuario, final String sBiografia, final String sSexo) {
        // Intenta crear un usuario con email y contraseña en Firebase Auth
        fbAuth.createUserWithEmailAndPassword(sCorreoElectronico, sContrasena).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {

                // Registro exitoso, obtiene el ID del usuario recién creado y prepara sus datos para guardar
                String usuarioId = fbAuth.getCurrentUser().getUid();
                Map<String, Object> datosUsuario = new HashMap<>();
                datosUsuario.put("id", usuarioId);
                datosUsuario.put("bio", sBiografia);
                datosUsuario.put("nombre", sNombreUsuario);
                datosUsuario.put("sexo", sSexo);
                datosUsuario.put("email", sCorreoElectronico);

                // Guarda los datos adicionales del usuario en Realtime Database bajo el nodo "usuarios"
                database.child("usuarios").child(usuarioId).setValue(datosUsuario).addOnSuccessListener(unused -> {
                    Notificaciones.makeToast(this, "Registro exitoso", Toast.LENGTH_SHORT);
                    // Redirige al usuario a la MainActivity y finaliza la actividad actual
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    // En caso de fallo al guardar los datos, notifica al usuario
                    Notificaciones.makeToast(this, "Error al guardar datos del usuario.", Toast.LENGTH_SHORT);
                });
            } else if(task.getException() instanceof FirebaseAuthUserCollisionException){
                // Si el correo ya está registrado
                Notificaciones.makeToast(this, "El correo electrónico ya está registrado", Toast.LENGTH_SHORT);
            } else {
                // Si el registro falla, muestra un mensaje al usuario.
                Notificaciones.makeToast(this, "El registro ha fallado", Toast.LENGTH_SHORT);
            }
        });
    }


}