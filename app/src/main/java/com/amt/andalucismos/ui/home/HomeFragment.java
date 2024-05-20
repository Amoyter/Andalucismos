package com.amt.andalucismos.ui.home;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amt.andalucismos.MainActivity;
import com.amt.andalucismos.R;
import com.amt.andalucismos.adapters.PalabraAdapter;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.ui.detallePalabra.DetallePalabraFragment;
import com.amt.andalucismos.utils.Notificaciones;
import com.amt.andalucismos.utils.OnPalabrasClickListener;
import com.amt.andalucismos.utils.Ordenable;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements OnPalabrasClickListener, Ordenable {

    private Context c;
    private View v;
    private Gson gson;
    private Type listType;
    private RecyclerView rvPalabras;
    private PalabraAdapter adapter;
    private ArrayList<Palabra> alPalabras;
    private MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);

        inicializarVariables();
        cargarPalabras();

        return v;
    }

    private void inicializarVariables(){
        c = getContext();
        rvPalabras = v.findViewById(R.id.rvPalabras);
        gson = new Gson();
        alPalabras = new ArrayList<>();
        listType = new TypeToken<List<String>>() {}.getType();

        rvPalabras.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PalabraAdapter(c, alPalabras, this);
        rvPalabras.setAdapter(adapter);
    }

    private void cargarPalabras() {
        if (mainActivity.hayConexion()) {
            mainActivity.obtenerDatos("contribuciones", new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        alPalabras.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String sComarca = getStringFromDataSnapshot(ds.child("comarca"));
                            String sSignificado = getStringFromDataSnapshot(ds.child("significado"));
                            String sEjemplo = getStringFromDataSnapshot(ds.child("ejemplo"));
                            String sExpresionId = getStringFromDataSnapshot(ds.child("expresionId"));
                            String sFechaAnadida = getStringFromDataSnapshot(ds.child("fechaAnadida"));
                            String sPalabra = getStringFromDataSnapshot(ds.child("palabra"));
                            String sPoblacion = getStringFromDataSnapshot(ds.child("poblacion"));
                            String sProvincia = getStringFromDataSnapshot(ds.child("provincia"));
                            boolean bRevisado = Boolean.TRUE.equals(ds.child("revisado").getValue());
                            String sTags = getStringFromDataSnapshot(ds.child("tags"));
                            List<String> lTags = sTags.isEmpty() ? new ArrayList<>() : gson.fromJson(sTags, listType);
                            String sUsuarioId = getStringFromDataSnapshot(ds.child("usuarioId"));
                            alPalabras.add(new Palabra(sComarca, sEjemplo, sExpresionId, sFechaAnadida, sPalabra, sPoblacion, sProvincia, bRevisado, sSignificado, lTags, sUsuarioId));
                        }
                        alPalabras.sort((p1, p2) -> p1.getPalabra().compareToIgnoreCase(p2.getPalabra()));
                        adapter.notifyDataSetChanged();
                        adapter.alPalabrasFull = new ArrayList<>(alPalabras);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("cargarPalabras", "DatabaseError", error.toException());
                }
            });
        } else {
            Notificaciones.makeToast(c, "No hay conexión a Internet. No se pueden cargar las palabras.", Toast.LENGTH_SHORT);
        }
    }

    private String getStringFromDataSnapshot(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(String.class) != null ? dataSnapshot.getValue(String.class) : "";
    }

    public void filtrarPalabras(String query) {
        Log.d("HomeFragment", "filtrarPalabras called with: " + query);
        adapter.getFilter().filter(query);
    }

    @Override
    public void onPalabraClick(int position) {
        Palabra palabra = alPalabras.get(position);
        DetallePalabraFragment detallePalabraFragment = new DetallePalabraFragment();

        Bundle args = new Bundle();
        args.putString("palabra", palabra.getPalabra());
        args.putString("significado", palabra.getSignificado());
        args.putString("comarca", palabra.getComarca());
        args.putString("ejemplo", palabra.getEjemplo());
        args.putString("expresionId", palabra.getExpresionId());
        args.putString("fechaAnadida", palabra.getFechaAnadida());
        args.putString("poblacion", palabra.getPoblacion());
        args.putString("provincia", palabra.getProvincia());
        args.putString("usuarioId", palabra.getUsuarioId());
        args.putString("tags", gson.toJson(palabra.getTags()));

        detallePalabraFragment.setArguments(args);
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_home_to_nav_detalle_palabra, args);
    }

    @Override
    public void ordenarAZ() {
        Collections.sort(alPalabras, (p1, p2) -> p1.getPalabra().compareToIgnoreCase(p2.getPalabra()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void ordenarZA() {
        Collections.sort(alPalabras, (p1, p2) -> p2.getPalabra().compareToIgnoreCase(p1.getPalabra()));
        adapter.notifyDataSetChanged();
    }
}