package com.amt.andalucismos.ui.detallePalabra;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amt.andalucismos.R;
import com.amt.andalucismos.adapters.PalabraAdapter;
import com.amt.andalucismos.adapters.TagAdapter;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DetallePalabraFragment extends Fragment {
    private View v;
    private Context c;
    private DatabaseReference fbDatabase;
    private TextView txtPalabra, txtSignificado, txtEjemplo, txtUbicacion, txtUsarioAporte;
    private RecyclerView rvTags;
    private TagAdapter adapter;
    private double dLongitud = 0.0f;
    private double dLatitud = 0.0f;
    private String sPalabra = "";
    private String sSignificado = "";
    private String sComarca = "";
    private String sEjemplo = "";
    private String sExpresionId = "";
    private String sPoblacion = "";
    private String sProvincia = "";
    private String sUsuarioId = "";
    private String sNombreUsuario = "Sin datos";
    private List<String> lTags;

    public DetallePalabraFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_detalle_palabra, container, false);
        c = getContext();
        Bundle args = getArguments();
        // Se recogen los argumentos pasados con el bundle
        if (args != null) {
            sPalabra = args.getString("palabra");
            sSignificado = args.getString("significado");
            sComarca = args.getString("comarca");
            sEjemplo = args.getString("ejemplo");
            sExpresionId = args.getString("expresionId");
            sPoblacion = args.getString("poblacion");
            sProvincia = args.getString("provincia");
            sUsuarioId = args.getString("usuarioId");

            // Deserializar la lista de tags desde JSON
            String jsonTags = args.getString("tags");
            Type type = new TypeToken<List<String>>() {}.getType();
            lTags = new Gson().fromJson(jsonTags, type);

            if(lTags != null && !lTags.get(0).isEmpty() && getActivity() != null){
                LinearLayoutManager llm = new LinearLayoutManager(getContext());
                llm.setOrientation(RecyclerView.HORIZONTAL);
                rvTags = v.findViewById(R.id.rvTags);
                rvTags.setLayoutManager(llm);
                adapter = new TagAdapter(c, lTags);
                rvTags.setAdapter(adapter);
                getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
            }

        } else {
            Notificaciones.makeToast(getContext(), "Error al acceder a los datos de la palabra", Toast.LENGTH_SHORT);
            NavHostFragment.findNavController(this).navigate(R.id.action_nav_detalle_palabra_to_nav_home);
            return v;
        }
        fbDatabase = FirebaseDatabase.getInstance().getReference("usuarios").child(sUsuarioId).child("nombre");
        obtenerCoordenadas(sProvincia);

        // Aqui se asignan los R.id.
        txtPalabra = v.findViewById(R.id.txtDpPalabra);
        txtSignificado = v.findViewById(R.id.txtDpSignificado);
        txtEjemplo = v.findViewById(R.id.txtDpEjemplo);
        txtUbicacion = v.findViewById(R.id.txtDpUbicacion);
        txtUsarioAporte = v.findViewById(R.id.txtDpUsarioAporte);
        txtPalabra.setText(sPalabra);
        txtSignificado.setText(sSignificado);
        txtEjemplo.setText(sEjemplo);
        txtUbicacion.setText(obtenerUbicacion(sProvincia, sPoblacion, sComarca));

        obtenerNombreUsuario();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();

        mapFragment.getMapAsync(googleMap -> {
            // Configura tu mapa aquí
            Log.d("MapReady", "El mapa está listo para usarse.");
            LatLng coords = new LatLng(dLatitud, dLongitud);
            googleMap.setMinZoomPreference(10.0f);
            if(dLongitud != 0.0f && dLatitud != 0.0f){
                googleMap.addMarker(new MarkerOptions().position(coords).title(sProvincia));
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(coords));
        });
    }

    private void obtenerCoordenadas(String sProvincia){
        switch (sProvincia){
            case "Granada":
                dLatitud = 37.18817f;
                dLongitud = -3.60667f;
                break;
            case "Sevilla":
                dLatitud = 37.38283f;
                dLongitud = -5.97317f;
                break;
            case "Málaga":
                dLatitud = 36.72016f;
                dLongitud = -4.42034f;
                break;
            case "Cádiz":
                dLatitud = 36.5297800f;
                dLongitud = -6.2946500f;
                break;
            case "Córdoba":
                dLatitud = 36.51543;
                dLongitud = -4.88583f;
                break;
            case "Almería":
                dLatitud = 36.83814f;
                dLongitud = -2.45974f;
                break;
            case "Huelva":
                dLatitud = 37.26638f;
                dLongitud = -6.94004f;
                break;
            case "Jaén":
                dLatitud = 37.76922;
                dLongitud = -3.79028;
                break;
            default:
                dLatitud = 0.0f;
                dLongitud = 0.0f;
                break;
        }
    } // obtenerUbicacion()

    private String obtenerUbicacion(String sProvincia, String sPoblacion, String sComarca){
        String sUbicacion = "";
        if(sProvincia != null && !sProvincia.equals("Varias")){
            if(sPoblacion != null && !sPoblacion.isEmpty()){
                sUbicacion += sPoblacion;
                if(sComarca != null && !sComarca.isEmpty()){
                    sUbicacion += ", " + sComarca;
                }
                sUbicacion += ", " + sProvincia;
            } else if (sComarca != null && !sComarca.isEmpty()){
                sUbicacion += ", " + sComarca;
                sUbicacion += ", " + sProvincia;
            } else { sUbicacion = sProvincia; }
        } else { sUbicacion = "Varias provincias"; }

        return sUbicacion;
    }

    private void obtenerNombreUsuario() {
        if(hayConexion()){
            fbDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        Log.d("UsuarioId", sUsuarioId);
                        Log.d("Recibido", snapshot.getValue(String.class));
                        sNombreUsuario = snapshot.getValue(String.class);
                        txtUsarioAporte.setText(sNombreUsuario);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("getPalabras", "DatabaseError", error.toException());
                }
            });
        }else {
            Notificaciones.makeToast(c, "No hay conexión a Internet.", Toast.LENGTH_SHORT);
        }
    }

    private boolean hayConexion(){
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    } // hayConexion()

}