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
import com.amt.andalucismos.models.Comentario;
import com.amt.andalucismos.utils.MainViewModel;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FeedbackAdminAdapter extends RecyclerView.Adapter<FeedbackAdminAdapter.ViewHolder>{
    private final int iResource = R.layout.item_admin_feedback;
    private List<Comentario> alComentarios;
    private Context c;
    private MainViewModel mainViewModel;

    public FeedbackAdminAdapter(Context c, List<Comentario> alComentarios, MainViewModel mainViewModel) {
        this.c = c;
        this.alComentarios = alComentarios;
        this.mainViewModel = mainViewModel;
    }

    @NonNull
    @Override
    public FeedbackAdminAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("FeedbackAdminAdapter", "onCreateViewHolder llamado");
        View v = LayoutInflater.from(parent.getContext()).inflate(iResource, parent, false);
        return new FeedbackAdminAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackAdminAdapter.ViewHolder holder, int position) {
        Log.d("FeedbackAdminAdapter", "onBindViewHolder llamado para la posición: " + position);
        Comentario comentario = alComentarios.get(position);
        holder.txtComentario.setText(comentario.getComentario());
        holder.txtTipoComentario.setText("Tipo: " + comentario.getTipoComentario());
        obtenerNombreUsuario(comentario.getUsuarioId(), holder.txtUsuario);

        // Manejar clics en el botón OK
        holder.imgBtnAnadirComentario.setOnClickListener(v -> {
            Log.d("FeedbackAdminAdapter", "imgBtnAnadirComentario clicado para la posición: " + position);
            if (comentario != null) {
                boolean isRevisado = comentario.getRevisado();
                Notificaciones.makeDialog(c, "Marcar como leido", "¿Desea marcar el comentario como leido?", "SI", "NO", "", new Notificaciones.RespuestaDialog() {
                    @Override
                    public void onPositivo() {
                        mainViewModel.actualizarComentario(comentario, isRevisado);
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
                Log.e("FeedbackAdminAdapter", "Comentario es null");
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("FeedbackAdminAdapter", "getItemCount llamado, tamaño: " + alComentarios.size());
        return alComentarios.size();
    }

    public void setComentarios(List<Comentario> comentarios) {
        Log.d("FeedbackAdminAdapter", "setComentarios llamado, nuevo tamaño: " + comentarios.size());
        this.alComentarios = comentarios;
        notifyDataSetChanged();
    }

    private void obtenerNombreUsuario(String usuarioId, TextView textView) {
        if (usuarioId == null || usuarioId.isEmpty()) {
            Log.e("FeedbackAdminAdapter", "usuarioId es null o vacío");
            return;
        }

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("usuarios").child(usuarioId);
        database.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String nombreUsuario = snapshot.child("nombre").getValue(String.class);
                textView.setText("Comentario de: " + nombreUsuario);
            } else {
                textView.setText("Usuario no encontrado");
                Log.e("FeedbackAdminAdapter", "Usuario no encontrado para usuarioId: " + usuarioId);
            }
        }).addOnFailureListener(e -> {
            Log.e("FeedbackAdminAdapter", "Error al obtener el nombre de usuario", e);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtComentario, txtTipoComentario, txtUsuario;
        private ImageButton imgBtnAnadirComentario;
        public View v;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.v = itemView;
            this.txtComentario = itemView.findViewById(R.id.txtComentarioFeedbackRv);
            this.txtTipoComentario = itemView.findViewById(R.id.txtTipoComentarioFeedbackRv);
            this.imgBtnAnadirComentario = itemView.findViewById(R.id.imgBtnAnadirComentario);
            this.txtUsuario = itemView.findViewById(R.id.txtUsuarioFeedbackRv);
            Log.d("FeedbackAdminAdapter", "ViewHolder creado");
        }
    }
}
