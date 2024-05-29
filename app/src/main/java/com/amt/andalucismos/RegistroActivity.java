package com.amt.andalucismos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amt.andalucismos.ui.privacidad.PrivacidadActivity;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {
    // Declaración de variables de la clase para la UI y Firebase
    EditText txtNombreUsuario, txtCorreoElectronico, txtContrasena, txtRepetirContrasena, txtBiografia;
    TextView txtPoliticaPrivacidad;
    CheckBox chkPoliticaPrivacidad;
    Spinner spnrSexo;
    Button btnRegistrarse, btnVolver;
    ArrayAdapter<CharSequence> adapter;
    FirebaseAuth fbAuth;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // 1- Inicializar componentes de la interfaz de usuario
        inicializarVistas();
        // 2- Configurar FirebaseAuth y DatabaseReference
        configurarFirebase();
        // 3- Establecer adaptador para el spinner de selección de sexo
        configurarSpinner();
        // 4- Implementar la lógica de navegación para el botón Volver
        configurarBotonVolver();
        // 5- Establecer la lógica de registro para el botón Registrarse
        configurarBotonRegistrarse();
        // 6- Implementar la lógica de navegación para la política de privacidad
        configurarPoliticaPrivacidad();
    } // onCreate()

    // Método para inicializar las vistas
    private void inicializarVistas() {
        txtNombreUsuario = findViewById(R.id.txtNombreUsuario);
        txtCorreoElectronico = findViewById(R.id.txtCorreoElectronico);
        txtContrasena = findViewById(R.id.txtContrasena);
        txtRepetirContrasena = findViewById(R.id.txtRepetirContrasena);
        txtBiografia = findViewById(R.id.txtBiografia);
        spnrSexo = findViewById(R.id.spnrSexo);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        txtPoliticaPrivacidad = findViewById(R.id.txtPoliticaPrivacidad);
        btnVolver = findViewById(R.id.btnVolver);
        chkPoliticaPrivacidad = findViewById(R.id.chkPoliticaPrivacidad);
        setTextNegritaYSubrayado(txtPoliticaPrivacidad, getString(R.string.politica_privacidad_link));
    } // inicializarVistas()

    // Método para configurar Firebase
    private void configurarFirebase() {
        fbAuth = FirebaseAuth.getInstance(); // Instancia de FirebaseAuth
        database = FirebaseDatabase.getInstance().getReference(); // Referencia a la base de datos
    } // configurarFirebase()

    // Método para configurar el Spinner
    private void configurarSpinner() {
        adapter = ArrayAdapter.createFromResource(this, R.array.sexo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrSexo.setAdapter(adapter);
    } // configurarSpinner()

    private void configurarPoliticaPrivacidad() {
        txtPoliticaPrivacidad.setOnClickListener(view -> {
            // Abrir la nueva Activity para mostrar la política de privacidad
            Intent intent = new Intent(RegistroActivity.this, PrivacidadActivity.class);
            startActivity(intent);
        });
    }

    // Método para configurar el botón Volver
    private void configurarBotonVolver() {
        btnVolver.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(RegistroActivity.this, LoginActivity.class));
        });
    } // configurarBotonVolver()

    // Método para configurar el botón Registrarse
    private void configurarBotonRegistrarse() {
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
    } // configurarBotonRegistrarse()

    // Método para validar los campos de entrada
    private boolean validarFormulario(String sCorreoElectronico, String sContrasena, String sRepetirContrasena, String sNombreUsuario, String sBiografia) {
        boolean valido = true;

        // Validación del Email
        if (TextUtils.isEmpty(sCorreoElectronico) || !Patterns.EMAIL_ADDRESS.matcher(sCorreoElectronico).matches()) {
            Notificaciones.makeToast(this, "Ingresa un correo electrónico válido.", Toast.LENGTH_SHORT);
            valido = false;
        }
        // Validación de la Contraseña
        else if (TextUtils.isEmpty(sContrasena) || sContrasena.length() < 6) {
            Notificaciones.makeToast(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT);
            valido = false;
        }
        // Validación de la repetición de la contraseña
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
        // Validación de la política de privacidad
        else if (!chkPoliticaPrivacidad.isChecked()) {
            Notificaciones.makeToast(this, "Debes aceptar la política de privacidad.", Toast.LENGTH_SHORT);
            valido = false;
        }

        return valido;
    } // validarFormulario()

    // Método para registrar un nuevo usuario en Firebase Authentication y guardar su información adicional en Firebase Realtime Database
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
    } // registrarUsuario()

    public void setTextNegritaYSubrayado(TextView textView, String texto) {
        SpannableString spannableString = new SpannableString(texto);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, texto.length(), 0); // Negrita
        spannableString.setSpan(new UnderlineSpan(), 0, texto.length(), 0); // Subrayado
        textView.setText(spannableString);
    } // setTextNegritaYSubrayado()
} // RegistroActivity