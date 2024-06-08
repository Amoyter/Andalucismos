package com.amt.andalucismos.ui.historial;

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
import com.amt.andalucismos.models.Usuario;
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

public class HistorialFragment extends Fragment implements OnPalabrasClickListener, Ordenable {
    private Context c;
    private View v;
    private Gson gson;
    private Type listType;
    private RecyclerView rvHistorial;
    private PalabraAdapter adapter;
    private ArrayList<Palabra> alHistorial;
    private MainViewModel mainViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.loadPalabras(); // Asegurarse de cargar las palabras
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_historial, container, false);

        inicializarVariables();

        mainViewModel.getPalabras().observe(getViewLifecycleOwner(), palabras -> {
            alHistorial.clear();
            Usuario usuario = mainViewModel.getUsuario().getValue();
            if (usuario != null) {
                List<String> historialIds = usuario.getHistorial();
                if (historialIds != null) {
                    for (int i = historialIds.size() - 1; i >= 0; i--) {
                        String id = historialIds.get(i);
                        for (Palabra palabra : palabras) {
                            if (palabra.getExpresionId().equals(id)) {
                                alHistorial.add(palabra);
                                break;
                            }
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });

        return v;
    }

    public void filtrarPalabras(String query) {
        adapter.getFilter().filter(query);
    }

    private void inicializarVariables() {
        c = getContext();
        rvHistorial = v.findViewById(R.id.rvHistorial);
        gson = new Gson();
        alHistorial = new ArrayList<>();
        listType = new TypeToken<List<String>>() {}.getType();

        rvHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PalabraAdapter(c, alHistorial, this, mainViewModel);
        rvHistorial.setAdapter(adapter);
    }

    @Override
    public void onPalabraClick(int position) {
        Palabra palabra = alHistorial.get(position);
        DetallePalabraFragment detallePalabraFragment = new DetallePalabraFragment();

        Bundle args = new Bundle();
        args.putParcelable("palabra", palabra);

        detallePalabraFragment.setArguments(args);
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_historial_to_nav_detalle_palabra, args);
    }

    @Override
    public void ordenarAZ() {
        Collections.sort(alHistorial, (p1, p2) -> p1.getPalabra().compareToIgnoreCase(p2.getPalabra()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void ordenarZA() {
        Collections.sort(alHistorial, (p1, p2) -> p2.getPalabra().compareToIgnoreCase(p1.getPalabra()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void ordenarFavoritas() {
        Collections.sort(alHistorial, (p1, p2) -> Integer.compare(p2.getNumFavoritas(), p1.getNumFavoritas()));
        adapter.notifyDataSetChanged();
    }

}