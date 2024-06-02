package com.amt.andalucismos.ui.detallePalabra;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amt.andalucismos.R;
import com.amt.andalucismos.adapters.TagAdapter;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.utils.MainViewModel;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Fragmento para mostrar los detalles de una palabra.
 * Implementa OnMapReadyCallback para manejar el mapa.
 */
public class DetallePalabraFragment extends Fragment implements OnMapReadyCallback {
    private View v;
    private Context c;
    private MainViewModel mainViewModel;
    private TextView txtPalabra, txtSignificado, txtEjemplo, txtUbicacion, txtUsuarioAporte, txtFechaAnadida;
    private RecyclerView rvTags;
    private ImageView imgFavoritos;
    private TagAdapter adapter;
    private double dLongitud = 0.0;
    private double dLatitud = 0.0;
    private Palabra palabra;
    private boolean esFavorita;

    public DetallePalabraFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_detalle_palabra, container, false);
        c = getContext();
        Bundle args = getArguments();

        if (args != null) {
            palabra = args.getParcelable("palabra");
            obtenerCoordenadas(palabra.getProvincia());
            inicializarRecyclerView();
        } else {
            Notificaciones.makeToast(getContext(), "Error al acceder a los datos de la palabra", Toast.LENGTH_SHORT);
            NavHostFragment.findNavController(this).navigate(R.id.action_nav_detalle_palabra_to_nav_home);
            return v;
        }

        inicializarVistas();
        mainViewModel.getUsuario().observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                setTextNegritaYSubrayado(txtUsuarioAporte, usuario.getNombre());
                esFavorita = usuario.getFavoritas().contains(palabra.getExpresionId());
                actualizarIconoFavorito(esFavorita);
            }
        });

        imgFavoritos.setOnClickListener(view -> {
            esFavorita = !esFavorita;
            actualizarFavorito(palabra, esFavorita);
        });

        mainViewModel.actualizarHistorial(palabra);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng coords = new LatLng(dLatitud, dLongitud);
        googleMap.setMinZoomPreference(10.0f);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        if (dLongitud != 0.0 && dLatitud != 0.0) {
            googleMap.addMarker(new MarkerOptions().position(coords).title(palabra.getProvincia()));
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(coords));
    }

    private void inicializarVistas() {
        txtPalabra = v.findViewById(R.id.txtDpPalabra);
        txtSignificado = v.findViewById(R.id.txtDpSignificado);
        txtEjemplo = v.findViewById(R.id.txtDpEjemplo);
        txtUbicacion = v.findViewById(R.id.txtDpUbicacion);
        txtUsuarioAporte = v.findViewById(R.id.txtDpUsarioAporte);
        txtFechaAnadida = v.findViewById(R.id.txtDpFechaAnadida);
        imgFavoritos = v.findViewById(R.id.imgDpBtnFavoritos);

        txtPalabra.setText(palabra.getPalabra());
        txtSignificado.setText(palabra.getSignificado());
        txtEjemplo.setText(palabra.getEjemplo());
        txtUbicacion.setText(obtenerUbicacion(palabra.getProvincia(), palabra.getPoblacion(), palabra.getComarca()));
        txtFechaAnadida.setText(convertirFecha(palabra.getFechaAnadida()));
    }

    /**
     * Actualiza el icono de favoritos según el estado actual.
     *
     * @param esFavorita Indica si la palabra es favorita.
     */
    private void actualizarIconoFavorito(boolean esFavorita) {
        if (esFavorita) {
            imgFavoritos.setImageResource(R.drawable.ic_favoritos_relleno);
        } else {
            imgFavoritos.setImageResource(R.drawable.ic_favoritos_vacio);
        }
    }

    /**
     * Actualiza el estado de favorito de una palabra.
     *
     * @param palabra   La palabra a actualizar.
     * @param esFavorita Nuevo estado de favorito.
     */
    private void actualizarFavorito(Palabra palabra, boolean esFavorita) {
        mainViewModel.actualizarFavorito(palabra, esFavorita);
        actualizarIconoFavorito(esFavorita);
    }

    /**
     * Inicializa el RecyclerView para mostrar las etiquetas (tags) de la palabra.
     */
    private void inicializarRecyclerView() {
        if (palabra.getLTags() != null && !palabra.getLTags().isEmpty() && getActivity() != null) {
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(RecyclerView.HORIZONTAL);
            rvTags = v.findViewById(R.id.rvTags);
            rvTags.setLayoutManager(llm);
            adapter = new TagAdapter(c, palabra.getLTags());
            rvTags.setAdapter(adapter);
        }
    }

    /**
     * Obtiene las coordenadas de una provincia.
     *
     * @param sProvincia La provincia de la palabra.
     */
    private void obtenerCoordenadas(String sProvincia) {
        switch (sProvincia) {
            case "Granada":
                dLatitud = 37.18817;
                dLongitud = -3.60667;
                break;
            case "Sevilla":
                dLatitud = 37.38283;
                dLongitud = -5.97317;
                break;
            case "Málaga":
                dLatitud = 36.72016;
                dLongitud = -4.42034;
                break;
            case "Cádiz":
                dLatitud = 36.52978;
                dLongitud = -6.29465;
                break;
            case "Córdoba":
                dLatitud = 37.88818;
                dLongitud = -4.77938;
                break;
            case "Almería":
                dLatitud = 36.83814;
                dLongitud = -2.45974;
                break;
            case "Huelva":
                dLatitud = 37.26638;
                dLongitud = -6.94004;
                break;
            case "Jaén":
                dLatitud = 37.76922;
                dLongitud = -3.79028;
                break;
            default:
                dLatitud = 0.0;
                dLongitud = 0.0;
                break;
        }
    }

    /**
     * Obtiene la ubicación completa combinando provincia, población y comarca.
     *
     * @param sProvincia La provincia de la palabra.
     * @param sPoblacion La población de la palabra.
     * @param sComarca   La comarca de la palabra.
     * @return La ubicación completa en formato de cadena.
     */
    private String obtenerUbicacion(String sProvincia, String sPoblacion, String sComarca) {
        StringBuilder sUbicacion = new StringBuilder();
        if (sProvincia != null && !sProvincia.equals("Varias")) {
            if (sPoblacion != null && !sPoblacion.isEmpty()) {
                sUbicacion.append(sPoblacion);
                if (sComarca != null && !sComarca.isEmpty()) {
                    sUbicacion.append(", ").append(sComarca);
                }
                sUbicacion.append(", ").append(sProvincia);
            } else if (sComarca != null && !sComarca.isEmpty()) {
                sUbicacion.append(sComarca).append(", ").append(sProvincia);
            } else {
                sUbicacion = new StringBuilder(sProvincia);
            }
        } else {
            sUbicacion = new StringBuilder("Varias provincias");
        }
        return sUbicacion.toString();
    }

    public static String convertirFecha(String sFechaAnadida) {
        String sFechaParseada = "Sin fecha de creación";
        if (sFechaAnadida != null && !sFechaAnadida.isEmpty()) {
            long fechaMilisegundos = Long.parseLong(sFechaAnadida);
            Date fecha = new Date(fechaMilisegundos);
            SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");
            sFechaParseada = formateador.format(fecha);
        }
        return sFechaParseada;
    }

    public void setTextNegritaYSubrayado(TextView textView, String texto) {
        SpannableString spannableString = new SpannableString(texto);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, texto.length(), 0);
        spannableString.setSpan(new UnderlineSpan(), 0, texto.length(), 0);
        textView.setText(spannableString);
    }
}

