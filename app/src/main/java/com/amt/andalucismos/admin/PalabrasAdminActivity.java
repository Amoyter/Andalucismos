package com.amt.andalucismos.admin;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amt.andalucismos.R;
import com.amt.andalucismos.adapters.PalabraAdminAdapter;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.utils.MainViewModel;

import java.util.ArrayList;

public class PalabrasAdminActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private RecyclerView rvAdminPalabras;
    private ArrayList<Palabra> alPalabras;
    private PalabraAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_palabras_admin);

        // Inicializar la Toolbar
        Toolbar toolbar = findViewById(R.id.tbPalabraAdmin);
        setSupportActionBar(toolbar);

        // Habilitar el botón de volver atrás
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        inicializarVistas();
        inicializarMainViewModel();
        inicializarAdapter();
    } // onCreate

    private void inicializarVistas() {
        rvAdminPalabras = findViewById(R.id.rvAdminPalabras);
        rvAdminPalabras.setLayoutManager(new LinearLayoutManager(this));
        alPalabras = new ArrayList<>();
    } // inicializarVistas

    private void inicializarAdapter(){
        adapter = new PalabraAdminAdapter(this, alPalabras, mainViewModel);
        rvAdminPalabras.setAdapter(adapter);
    } // inicializarAdapter

    private void inicializarMainViewModel() {
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.loadPalabrasNoRevisadas(); // Asegurarse de cargar las palabras

        // Observar el LiveData de palabras para actualizar el adaptador cuando cambien los datos
        mainViewModel.getPalabrasNoRevisadas().observe(this, palabras -> {
            alPalabras.clear();
            if (palabras != null) {
                alPalabras.addAll(palabras);
            }
            adapter.notifyDataSetChanged();
        });
    } // inicializarMainViewModel
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Volver a la actividad anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    } // onOptionsItemSelected
} // PalabrasAdminActivity