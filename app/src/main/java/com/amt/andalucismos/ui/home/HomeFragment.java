package com.amt.andalucismos.ui.home;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amt.andalucismos.R;
import com.amt.andalucismos.adapters.PalabraAdapter;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.ui.detallePalabra.DetallePalabraFragment;
import com.amt.andalucismos.utils.MainViewModel;
import com.amt.andalucismos.utils.OnPalabrasClickListener;
import com.amt.andalucismos.utils.Ordenable;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment implements OnPalabrasClickListener, Ordenable {
    private Context c;
    private View v;
    private Gson gson;
    private Type listType;
    private RecyclerView rvPalabras;
    private PalabraAdapter adapter;
    private ArrayList<Palabra> alPalabras;
    private MainViewModel mainViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.loadPalabrasRevisadas(); // Asegurarse de cargar las palabras
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);

        inicializarVariables();

        mainViewModel.getPalabrasRevisadas().observe(getViewLifecycleOwner(), palabras -> {
            adapter.setPalabras(palabras);
        });

        mainViewModel.getUsuario().observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                adapter.notifyDataSetChanged();
            }
        });

        return v;
    }

    private void inicializarVariables() {
        c = getContext();
        rvPalabras = v.findViewById(R.id.rvPalabras);
        gson = new Gson();
        alPalabras = new ArrayList<>();
        listType = new TypeToken<List<String>>() {}.getType();

        rvPalabras.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PalabraAdapter(c, alPalabras, this, mainViewModel);
        rvPalabras.setAdapter(adapter);
    }

    public void filtrarPalabras(String query) {
        Log.d("HomeFragment", "Query a filtrar: " + query); // LLega el query bien
        adapter.getFilter().filter(query);
    }

    @Override
    public void onPalabraClick(int position) {
        Palabra palabra = adapter.getPalabraEnPosicion(position);
        DetallePalabraFragment detallePalabraFragment = new DetallePalabraFragment();

        Bundle args = new Bundle();
        args.putParcelable("palabra", palabra);

        detallePalabraFragment.setArguments(args);
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_home_to_nav_detalle_palabra, args);
    }

    @Override
    public void ordenarAZ() {
        List<Palabra> listaFiltrada = new ArrayList<>(adapter.getAlPalabrasFiltro());
        Collections.sort(listaFiltrada, (p1, p2) -> p1.getPalabra().compareToIgnoreCase(p2.getPalabra()));
        adapter.setPalabras(listaFiltrada);
    }

    @Override
    public void ordenarZA() {
        List<Palabra> listaFiltrada = new ArrayList<>(adapter.getAlPalabrasFiltro());
        Collections.sort(listaFiltrada, (p1, p2) -> p2.getPalabra().compareToIgnoreCase(p1.getPalabra()));
        adapter.setPalabras(listaFiltrada);
    }

    @Override
    public void ordenarFavoritas() {
        List<Palabra> listaFiltrada = new ArrayList<>(adapter.getAlPalabrasFiltro());
        Collections.sort(listaFiltrada, (p1, p2) -> Integer.compare(p2.getNumFavoritas(), p1.getNumFavoritas()));
        adapter.setPalabras(listaFiltrada);
    }

    @Override
    public void ordenarMasRecientes() {
        List<Palabra> listaFiltrada = new ArrayList<>(adapter.getAlPalabrasFiltro());
        Collections.sort(listaFiltrada, (p1, p2) -> {
            Long fecha1 = parseFechaSegura(p1.getFechaAnadida(), Long.MIN_VALUE); // Nulo -> MIN_VALUE
            Long fecha2 = parseFechaSegura(p2.getFechaAnadida(), Long.MIN_VALUE); // Nulo -> MIN_VALUE
            return fecha2.compareTo(fecha1);
        });
        adapter.setPalabras(listaFiltrada);
    }

    @Override
    public void ordenarMasAntiguas() {
        List<Palabra> listaFiltrada = new ArrayList<>(adapter.getAlPalabrasFiltro());
        Collections.sort(listaFiltrada, (p1, p2) -> {
            Long fecha1 = parseFechaSegura(p1.getFechaAnadida(), Long.MAX_VALUE); // Nulo -> MAX_VALUE
            Long fecha2 = parseFechaSegura(p2.getFechaAnadida(), Long.MAX_VALUE); // Nulo -> MAX_VALUE
            return fecha1.compareTo(fecha2);
        });
        adapter.setPalabras(listaFiltrada);
    }

    /**
     * Parsea la fecha de String a Long y controla cuando es nula.
     * @param fecha {@link String} Fecha a parsear
     * @param valorPorDefecto {@link Long} Valor por defecto si la fecha es nula
     * @return {@link Long} Fecha parseada
     */
    private Long parseFechaSegura(String fecha, Long valorPorDefecto) {
        try { return Long.parseLong(fecha); }
        catch (NumberFormatException e) { return valorPorDefecto; }
    } // parseFechaSegura

}

