package com.amt.andalucismos.ui.detallePalabra;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amt.andalucismos.MainActivity;
import com.amt.andalucismos.R;
import com.amt.andalucismos.adapters.TagAdapter;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DetallePalabraFragment extends Fragment implements OnMapReadyCallback {
    private View v;
    private Context c;
    private MainActivity mainActivity;
    private TextView txtPalabra, txtSignificado, txtEjemplo, txtUbicacion, txtUsarioAporte, txtFechaAnadida;
    private RecyclerView rvTags;
    private TagAdapter adapter;
    private double dLongitud = 0.0f;
    private double dLatitud = 0.0f;
    private String sPalabra = "";
    private String sSignificado = "";
    private String sComarca = "";
    private String sEjemplo = "";
    private String sExpresionId = "";
    private String sFechaAnadida = "";
    private String sPoblacion = "";
    private String sProvincia = "";
    private String sUsuarioId = "";
    private String sNombreUsuario = "Sin datos";
    private List<String> lTags;

    public DetallePalabraFragment() {
        // Constructor vacío requerido
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    } // onAttach()

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_detalle_palabra, container, false);
        c = getContext();
        Bundle args = getArguments();

        if (args != null) {
            obtenerArgumentos(args);
            inicializarRecyclerView();
        } else {
            Notificaciones.makeToast(getContext(), "Error al acceder a los datos de la palabra", Toast.LENGTH_SHORT);
            NavHostFragment.findNavController(this).navigate(R.id.action_nav_detalle_palabra_to_nav_home);
            return v;
        }

        obtenerCoordenadas(sProvincia);
        inicializarVistas();
        obtenerNombreUsuario();

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
        mapFragment.getMapAsync((OnMapReadyCallback) this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng coords = new LatLng(dLatitud, dLongitud);
        googleMap.setMinZoomPreference(10.0f);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        if (dLongitud != 0.0f && dLatitud != 0.0f) {
            googleMap.addMarker(new MarkerOptions().position(coords).title(sProvincia));
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(coords));
    }

    // Método para inicializar las vistas
    private void inicializarVistas() {
        txtPalabra = v.findViewById(R.id.txtDpPalabra);
        txtSignificado = v.findViewById(R.id.txtDpSignificado);
        txtEjemplo = v.findViewById(R.id.txtDpEjemplo);
        txtUbicacion = v.findViewById(R.id.txtDpUbicacion);
        txtUsarioAporte = v.findViewById(R.id.txtDpUsarioAporte);
        txtFechaAnadida = v.findViewById(R.id.txtDpFechaAnadida);

        txtPalabra.setText(sPalabra);
        txtSignificado.setText(sSignificado);
        txtEjemplo.setText(sEjemplo);
        txtUbicacion.setText(obtenerUbicacion(sProvincia, sPoblacion, sComarca));
        txtFechaAnadida.setText(sFechaAnadida);
    }

    // Método para obtener los argumentos pasados al fragmento
    private void obtenerArgumentos(Bundle args) {
        sPalabra = args.getString("palabra");
        sSignificado = args.getString("significado");
        sComarca = args.getString("comarca");
        sEjemplo = args.getString("ejemplo");
        sExpresionId = args.getString("expresionId");
        sFechaAnadida = args.getString("fechaAnadida");
        if(sFechaAnadida == null || sFechaAnadida.isEmpty()){ sFechaAnadida = "Sin datos"; }
        else { sFechaAnadida = convertirFecha(sFechaAnadida); }
        sPoblacion = args.getString("poblacion");
        sProvincia = args.getString("provincia");
        sUsuarioId = args.getString("usuarioId");

        String jsonTags = args.getString("tags");
        Type type = new TypeToken<List<String>>() {}.getType();
        lTags = new Gson().fromJson(jsonTags, type);
    }

    // Método para inicializar el RecyclerView
    private void inicializarRecyclerView() {
        if (lTags != null && !lTags.get(0).isEmpty() && getActivity() != null) {
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(RecyclerView.HORIZONTAL);
            rvTags = v.findViewById(R.id.rvTags);
            rvTags.setLayoutManager(llm);
            adapter = new TagAdapter(c, lTags);
            rvTags.setAdapter(adapter);
            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }

    private void obtenerCoordenadas(String sProvincia){
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
    } // obtenerUbicacion()

    // Método para obtener la ubicación en formato cadena
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

    private void obtenerNombreUsuario() {
        if (mainActivity.hayConexion()) {
            mainActivity.obtenerDatos("usuarios/" + sUsuarioId + "/nombre", new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Log.d("UsuarioId", sUsuarioId);
                        Log.d("Recibido", snapshot.getValue(String.class));
                        sNombreUsuario = snapshot.getValue(String.class);
                        setTextNegritaYSubrayado(txtUsarioAporte, sNombreUsuario);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("obtenerNombreUsuario", "DatabaseError", error.toException());
                }
            });
        } else {
            Notificaciones.makeToast(c, "No hay conexión a Internet.", Toast.LENGTH_SHORT);
        }
    }

    public static String convertirFecha(String sFechaAnadida) {
        // Convertir el string a un long
        long fechaMilisegundos = Long.parseLong(sFechaAnadida);

        // Crear un objeto Date a partir del long
        Date fecha = new Date(fechaMilisegundos);

        // Crear un formateador de fecha
        SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");

        // Formatear la fecha y devolver el resultado
        return formateador.format(fecha);
    }

    public void setTextNegritaYSubrayado(TextView textView, String texto) {
        SpannableString spannableString = new SpannableString(texto);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, texto.length(), 0); // Negrita
        spannableString.setSpan(new UnderlineSpan(), 0, texto.length(), 0); // Subrayado
        textView.setText(spannableString);
    } // setTextNegritaYSubrayado()

} // DetallePalabraFragment