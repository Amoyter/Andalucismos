package com.amt.andalucismos.ui.favoritos;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;

public class FavoritosFragment extends Fragment implements OnPalabrasClickListener, Ordenable {
    private Context c;
    private View v;
    private Gson gson;
    private Type listType;
    private RecyclerView rvFavoritos;
    private PalabraAdapter adapter;
    private ArrayList<Palabra> alFavoritas;
    private MainViewModel mainViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        //mainViewModel.loadPalabras(); // Asegurarse de cargar las palabras
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_favoritos, container, false);

        inicializarVariables();

        mainViewModel.getPalabras().observe(getViewLifecycleOwner(), this::actualizarListaFavoritas);

        mainViewModel.getUsuario().observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                actualizarListaFavoritas(mainViewModel.getPalabras().getValue());
            }
        });

        /*mainViewModel.getPalabras().observe(getViewLifecycleOwner(), palabras -> {
            alFavoritas.clear();
            List<String> favoritasIds = mainViewModel.getUsuario().getValue().getFavoritas();
            for (Palabra palabra : palabras) {
                if (favoritasIds != null && favoritasIds.contains(palabra.getExpresionId())) {
                    alFavoritas.add(palabra);
                }
            }
            adapter.notifyDataSetChanged();
        });*/

        return v;
    }

    private void actualizarListaFavoritas(List<Palabra> palabras) {
        if (palabras == null) return;

        alFavoritas.clear();
        List<String> favoritasIds = mainViewModel.getUsuario().getValue().getFavoritas();
        if (favoritasIds != null) {
            for (Palabra palabra : palabras) {
                if (favoritasIds.contains(palabra.getExpresionId())) {
                    alFavoritas.add(palabra);
                }
            }
        }
        adapter.setPalabras(alFavoritas);
    }

    private void inicializarVariables() {
        c = getContext();
        rvFavoritos = v.findViewById(R.id.rvFavoritos);
        gson = new Gson();
        alFavoritas = new ArrayList<>();
        listType = new TypeToken<List<String>>() {}.getType();

        rvFavoritos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PalabraAdapter(c, alFavoritas, this, mainViewModel);
        rvFavoritos.setAdapter(adapter);
    }

    @Override
    public void onPalabraClick(int position) {
        Palabra palabra = alFavoritas.get(position);
        DetallePalabraFragment detallePalabraFragment = new DetallePalabraFragment();

        Bundle args = new Bundle();
        args.putParcelable("palabra", palabra);

        detallePalabraFragment.setArguments(args);
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_favoritos_to_nav_detalle_palabra, args);
    }

    public void filtrarPalabras(String query) {
        adapter.getFilter().filter(query);
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

}