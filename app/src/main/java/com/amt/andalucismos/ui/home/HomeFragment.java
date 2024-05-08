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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amt.andalucismos.R;
import com.amt.andalucismos.adapters.PalabraAdapter;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.ui.detallePalabra.DetallePalabraFragment;
import com.amt.andalucismos.utils.Notificaciones;
import com.amt.andalucismos.utils.OnPalabrasClickListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnPalabrasClickListener {

    private Context c;
    private View v;
    private Gson gson;
    private Type listType;
    private RecyclerView rvPalabras;
    private PalabraAdapter adapter;
    private DatabaseReference fbDatabase;
    private ArrayList<Palabra> alPalabras;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);

        inicializarVariables();
        getPalabras();

        return v;
    } // onCreateView()

    private void inicializarVariables(){
        c = getContext();
        rvPalabras = v.findViewById(R.id.rvPalabras);
        fbDatabase = FirebaseDatabase.getInstance().getReference("contribuciones");
        gson = new Gson();
        alPalabras = new ArrayList<>();
        listType = new TypeToken<List<String>>() {}.getType();

        rvPalabras.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PalabraAdapter(c, alPalabras, this);
        rvPalabras.setAdapter(adapter);
    } // inicializarVariables()

    /**
     * Recupera las palabras de la base de datos Firebase y actualiza la lista local.
     *
     * <br><br>Este método intenta cargar la lista de palabras desde Firebase Database.
     * Verifica primero si hay una conexión a Internet disponible. Si los datos están disponibles,
     * los carga en la lista local y luego actualiza el adaptador del RecyclerView en el hilo de UI.
     * Las palabras se ordenan alfabéticamente antes de ser mostradas.
     */
    private void getPalabras() {
        if(hayConexion()){
            fbDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        alPalabras.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            String sComarca = getStringFromDataSnapshot(ds.child("comarca"));
                            String sSignificado = getStringFromDataSnapshot(ds.child("significado"));
                            String sEjemplo = getStringFromDataSnapshot(ds.child("ejemplo"));
                            String sExpresionId = getStringFromDataSnapshot(ds.child("expresionId"));
                            String sPalabra = getStringFromDataSnapshot(ds.child("palabra"));
                            String sPoblacion = getStringFromDataSnapshot(ds.child("poblacion"));
                            String sProvincia = getStringFromDataSnapshot(ds.child("provincia"));
                            boolean bRevisado = Boolean.TRUE.equals(ds.child("revisado").getValue());
                            String sTags = getStringFromDataSnapshot(ds.child("tags"));
                            List<String> lTags = sTags.isEmpty() ? new ArrayList<>() : gson.fromJson(sTags, listType);
                            String sUsuarioId = getStringFromDataSnapshot(ds.child("usuarioId"));
                            alPalabras.add(new Palabra(sComarca, sEjemplo, sExpresionId, sPalabra, sPoblacion, sProvincia, bRevisado, sSignificado, lTags, sUsuarioId));
                        }
                        alPalabras.sort((p1, p2) -> p1.getPalabra().compareToIgnoreCase(p2.getPalabra()));
                        if(getActivity() != null){
                            getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("getPalabras", "DatabaseError", error.toException());
                }
            });
        }else {
            Notificaciones.makeToast(c, "No hay conexión a Internet. No se pueden cargar las palabras.", Toast.LENGTH_SHORT);
        }
    } // getPalabras()

    /**
     * Extrae un valor de cadena de un DataSnapshot, devolviendo una cadena vacía si el valor es nulo.
     *
     * @param dataSnapshot El DataSnapshot que contiene el dato.
     * @return La cadena contenida en el DataSnapshot o una cadena vacía si el dato es nulo.
     */
    private String getStringFromDataSnapshot(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(String.class) != null ? dataSnapshot.getValue(String.class) : "";
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
        args.putString("poblacion", palabra.getPoblacion());
        args.putString("provincia", palabra.getProvincia());
        args.putString("usuarioId", palabra.getUsuarioId());
        args.putString("tags", gson.toJson(palabra.getTags()));  // Convierte la lista de etiquetas a JSON

        detallePalabraFragment.setArguments(args);
        // Realizar la transacción de fragmentos
        NavHostFragment.findNavController(this).navigate(R.id.action_nav_home_to_nav_detalle_palabra, args);
    }

    /**
     * Verifica si hay una conexión de red activa y disponible.
     * <br><br>Este método comprueba si el dispositivo está conectado a una red y si esa
     * red tiene acceso a Internet. Utiliza el {@link ConnectivityManager} para
     * obtener detalles de la red activa y comprobar si está conectada o en proceso
     * de conexión.
     *
     * @return true si hay una conexión a Internet disponible, false en caso contrario.
     */
    private boolean hayConexion(){
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    } // hayConexion()
}