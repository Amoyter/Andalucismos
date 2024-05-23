package com.amt.andalucismos.ui.privacidad;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amt.andalucismos.R;

public class PrivacidadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacidad);

        configurarToolbar();
        // Inicializar el WebView y cargar el archivo HTML de la política de privacidad
        WebView webView = findViewById(R.id.webViewActivity);
        webView.loadUrl("file:///android_asset/Politica_de_Privacidad_Andalucismos.html");
    }

    private void configurarToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarPrivacidad);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Política de privacidad");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Manejar el evento de retroceso en la Toolbar
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }
}