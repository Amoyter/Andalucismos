package com.amt.andalucismos.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amt.andalucismos.R;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.utils.OnPalabrasClickListener;

import java.util.ArrayList;
import java.util.List;

public class PalabraAdapter extends RecyclerView.Adapter<PalabraAdapter.ViewHolder> implements Filterable {
    private final OnPalabrasClickListener onPalabrasClickListener;
    private int iResource = R.layout.item_palabra;
    private ArrayList<Palabra> alPalabras;
    public ArrayList<Palabra> alPalabrasFull;
    private Context c;

    public PalabraAdapter(Context c, ArrayList<Palabra> alPalabras, OnPalabrasClickListener onPalabrasClickListener) {
        this.onPalabrasClickListener = onPalabrasClickListener;
        this.c = c;
        this.alPalabras = alPalabras;
        this.alPalabrasFull = new ArrayList<>(alPalabras);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(iResource, parent, false);
        return new ViewHolder(v, onPalabrasClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Palabra palabra = alPalabras.get(position);
        holder.txtPalabra.setText(palabra.getPalabra());
        holder.txtSignificado.setText(palabra.getSignificado());
    }

    @Override
    public int getItemCount() {
        return alPalabras.size();
    }

    @Override
    public Filter getFilter() {
        return palabraFilter;
    }

    private Filter palabraFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d("PalabraAdapter", "performFiltering called with constraint: " + constraint);
            List<Palabra> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(alPalabrasFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Palabra palabra : alPalabrasFull) {
                    if (palabra.getPalabra().toLowerCase().contains(filterPattern)) {
                        filteredList.add(palabra);
                    }
                    Log.d("PalabraAdapter", "Checking palabra: " + palabra.getPalabra() + " against pattern: " + filterPattern);
                }
            }
            Log.d("PalabraAdapter", "performFiltering result size: " + filteredList.size());
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.d("PalabraAdapter", "publishResults called");
            alPalabras.clear();
            alPalabras.addAll((List<Palabra>) results.values);
            notifyDataSetChanged();
        }
    };

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
                if (onPalabrasClickListener != null) {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onPalabrasClickListener.onPalabraClick(pos);
                    }
                }
            });
        }
    }
}
