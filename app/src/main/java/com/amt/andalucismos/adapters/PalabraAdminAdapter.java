package com.amt.andalucismos.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amt.andalucismos.R;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.models.Usuario;
import com.amt.andalucismos.utils.MainViewModel;
import com.amt.andalucismos.utils.Notificaciones;

import java.util.List;

public class PalabraAdminAdapter extends RecyclerView.Adapter<PalabraAdminAdapter.ViewHolder> {
    private final int iResource = R.layout.item_admin_palabra;
    private List<Palabra> alPalabras;
    private Context c;
    private MainViewModel mainViewModel;

    public PalabraAdminAdapter(Context c, List<Palabra> alPalabras, MainViewModel mainViewModel) {
        this.c = c;
        this.alPalabras = alPalabras;
        this.mainViewModel = mainViewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(iResource, parent, false);
        return new PalabraAdminAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Palabra palabra = alPalabras.get(position);
        holder.txtPalabraAdmin.setText(palabra.getPalabra());
        holder.txtSignificadoAdmin.setText(palabra.getSignificado());
        holder.txtEjemploAdmin.setText(palabra.getEjemplo());
        holder.txtUbicacionAdminRv.setText(obtenerUbicacion(palabra.getProvincia(), palabra.getPoblacion(), palabra.getComarca()));

        // Obtener el usuario actual desde el ViewModel
        Usuario usuario = mainViewModel.getUsuario().getValue();

        // Manejar clics en el botón de añadir palabra
        holder.imgBtnAnadirPalabra.setOnClickListener(v -> {
            if (palabra != null) {
                boolean isRevisado = palabra.getRevisado();
                Notificaciones.makeDialog(c, "Aprobar palabra", "¿Desea aprobar la palabra y subirla a la app?", "SI", "NO", "", new Notificaciones.RespuestaDialog() {
                    @Override
                    public void onPositivo() {
                        mainViewModel.actualizarPalabrasNoRevisadas(palabra, isRevisado);
                        notifyItemChanged(position);// Notificar al adaptador que el elemento ha cambiado
                    }

                    @Override
                    public void onNegativo() {

                    }

                    @Override
                    public void onNeutral() {

                    }
                });
            } else {
                Log.e("FeedbackAdminAdapter", "Palabra es null");
            }
        });
        // Manejar clics en el botón de eliminar palabra
        holder.imgBtnEliminarPalabra.setOnClickListener(v -> {
            if (palabra != null) {
                Notificaciones.makeDialog(c, "Eliminar palabra", "¿Desea eliminar esta palabra para siempre?", "SI", "NO", "", new Notificaciones.RespuestaDialog() {
                    @Override
                    public void onPositivo() {
                        mainViewModel.eliminarPalabras(palabra);
                        notifyItemChanged(position);// Notificar al adaptador que el elemento ha cambiado
                    }

                    @Override
                    public void onNegativo() {

                    }

                    @Override
                    public void onNeutral() {

                    }
                });
            } else {
                Log.e("FeedbackAdminAdapter", "Palabra es null");
            }
        });
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

    @Override
    public int getItemCount() {
        return alPalabras.size();
    }


    /**
     * Actualiza la lista de palabras en el adaptador.
     *
     * @param palabras Nueva lista de palabras.
     */
    public void setPalabras(List<Palabra> palabras) {
        this.alPalabras = palabras;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder para mantener las vistas de cada elemento en el RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPalabraAdmin, txtSignificadoAdmin, txtEjemploAdmin, txtUbicacionAdminRv;
        private ImageButton imgBtnAnadirPalabra, imgBtnEliminarPalabra;
        public View v;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.v = itemView;
            this.txtPalabraAdmin = itemView.findViewById(R.id.txtPalabraAdminRv);
            this.txtSignificadoAdmin = itemView.findViewById(R.id.txtSignificadoAdminRv);
            this.txtEjemploAdmin = itemView.findViewById(R.id.txtEjemploAdminRv);
            this.txtUbicacionAdminRv = itemView.findViewById(R.id.txtUbicacionAdminRv);
            this.imgBtnAnadirPalabra = itemView.findViewById(R.id.imgBtnAnadirPalabra);
            this.imgBtnEliminarPalabra = itemView.findViewById(R.id.imgBtnEliminarAdmin);
        }
    } // ViewHolder
} // PalabraAdminAdapter
