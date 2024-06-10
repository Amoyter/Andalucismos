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
import com.amt.andalucismos.adapters.FeedbackAdminAdapter;
import com.amt.andalucismos.models.Comentario;
import com.amt.andalucismos.utils.MainViewModel;

import java.util.ArrayList;

public class FeedbackAdminActivity extends AppCompatActivity {
    private MainViewModel mainViewModel;
    private RecyclerView rvAdminFeedback;
    private ArrayList<Comentario> alComentarios;
    private FeedbackAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback_admin);

        // Inicializar la Toolbar
        Toolbar toolbar = findViewById(R.id.tbFeedbackAdmin);
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
        rvAdminFeedback = findViewById(R.id.rvAdminFeedback);
        rvAdminFeedback.setLayoutManager(new LinearLayoutManager(this));
        alComentarios = new ArrayList<>();
    }

    private void inicializarAdapter(){
        adapter = new FeedbackAdminAdapter(this, alComentarios, mainViewModel);
        rvAdminFeedback.setAdapter(adapter);
    }

    private void inicializarMainViewModel() {
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.loadComentarios(); // Asegurarse de cargar los comentarios

        // Observar el LiveData de comentarios para actualizar el adaptador cuando cambien los datos
        mainViewModel.getComentarios().observe(this, comentarios -> {
            alComentarios.clear();
            if (comentarios != null) {
                alComentarios.addAll(comentarios);
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Volver a la actividad anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} // FeedbackAdminActivity