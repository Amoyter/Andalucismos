package com.amt.andalucismos.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amt.andalucismos.R;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.utils.OnPalabrasClickListener;

import java.util.ArrayList;

public class PalabraAdapter extends RecyclerView.Adapter<PalabraAdapter.ViewHolder> {
    private final OnPalabrasClickListener onPalabrasClickListener;
    private int iResource = R.layout.item_palabra;
    private ArrayList<Palabra> alPalabras;
    private Context c;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(iResource, parent, false);
        return new ViewHolder(v, onPalabrasClickListener);
    } // onCreateViewHolder()

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Palabra palabra = alPalabras.get(position);

        holder.txtPalabra.setText(palabra.getPalabra());
        holder.txtSignificado.setText(palabra.getSignificado());
    } // onBindViewHolder()

    @Override
    public int getItemCount() {
        return alPalabras.size();
    } // getItemCount()

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtPalabra, txtSignificado;
        private ImageButton imgBtnFavoritos;
        public View v;

        public ViewHolder(@NonNull View itemView, OnPalabrasClickListener onPalabrasClickListener) {
            super(itemView);

            this.v = itemView;
            this.txtPalabra = itemView.findViewById(R.id.txtPalabraRv);
            this.txtSignificado = itemView.findViewById(R.id.txtSignificadoRv);
            this.imgBtnFavoritos = itemView.findViewById(R.id.imgBtnFavoritos);

            itemView.setOnClickListener(view -> {
                if(onPalabrasClickListener != null){
                    int pos = getAbsoluteAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        onPalabrasClickListener.onPalabraClick(pos);
                    }
                }
            });
        } // Constructor

    } // CLASS ViewHolder

    public PalabraAdapter(Context c, ArrayList<Palabra> alPalabras, OnPalabrasClickListener onPalabrasClickListener) {
        this.onPalabrasClickListener = onPalabrasClickListener;
        this.c = c;
        this.alPalabras = alPalabras;
    } // Constructor
}
