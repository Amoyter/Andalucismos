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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_historial, container, false);

        inicializarVariables();

        mainViewModel.getPalabras().observe(getViewLifecycleOwner(), this::actualizarListaHistorial);

        mainViewModel.getUsuario().observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                actualizarListaHistorial(mainViewModel.getPalabras().getValue());
            }
        });

        /*mainViewModel.getPalabras().observe(getViewLifecycleOwner(), palabras -> {
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
        });*/

        return v;
    }

    public void filtrarPalabras(String query) {
        adapter.getFilter().filter(query);
    }

    private void actualizarListaHistorial(List<Palabra> palabras) {
        if (palabras == null) return;

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
        adapter.setPalabras(alHistorial);
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
        Palabra palabra = adapter.getPalabraEnPosicion(position);
        DetallePalabraFragment detallePalabraFragment = new DetallePalabraFragment();

        Bundle args = new Bundle();
        args.putParcelable("palabra", palabra);

        detallePalabraFragment.setArguments(args);
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_historial_to_nav_detalle_palabra, args);
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