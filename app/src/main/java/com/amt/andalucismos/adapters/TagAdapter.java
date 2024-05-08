package com.amt.andalucismos.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amt.andalucismos.R;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder>{
    private int iResource = R.layout.item_tags;
    private List<String> lTags;
    private Context c;


    @NonNull
    @Override
    public TagAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = new View(c);
        v = LayoutInflater.from(parent.getContext()).inflate(iResource, parent, false);

        return new TagAdapter.ViewHolder(v);
    } // onCreateViewHolder()

    @Override
    public void onBindViewHolder(@NonNull TagAdapter.ViewHolder holder, int position) {
        String sTag = lTags.get(position);
        Log.d("Tamaño tags", "Tamaño: " + lTags.size() + " - Contenido: " + lTags.toString());
        holder.txtTag.setText("#" + sTag);
    }

    @Override
    public int getItemCount() {
        return lTags.size();
    } // getItemCount()

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtTag;
        public View v;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.v = itemView;
            this.txtTag = itemView.findViewById(R.id.txtTagsRv);

        } // Constructor

    } // CLASS ViewHolder

    public TagAdapter(Context c, List<String> lTags) {
        this.c = c;
        this.lTags = lTags;
    } // Constructor
}
