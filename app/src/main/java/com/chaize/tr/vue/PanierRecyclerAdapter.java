package com.chaize.tr.vue;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chaize.tr.R;
import com.chaize.tr.modele.Produit;
import com.chaize.tr.outils.DbContract;

import java.util.ArrayList;

public class PanierRecyclerAdapter extends RecyclerView.Adapter<PanierRecyclerAdapter.MyViewHolder> {

    private ArrayList<ItemPanier> arrayList = new ArrayList<>();

    public PanierRecyclerAdapter(ArrayList<ItemPanier> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_panier_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.code.setText(arrayList.get(position).getProduit().getCode());
        holder.description.setText(arrayList.get(position).getProduit().getDescription());
        holder.prix.setText(Float.toString(arrayList.get(position).getPrix()));
        int flgTR = arrayList.get(position).getProduit().getFlgTR();
        switch (flgTR) {
            case DbContract.TR_ACCEPTE:
                holder.flgTR.setImageResource(R.drawable.tr_green);
                break;
            case DbContract.TR_REFUSE:
                holder.flgTR.setImageResource(R.drawable.tr_red);
                break;
            default:
                holder.flgTR.setImageResource(R.drawable.tr_grey);
                break;
        }
        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FAF8FD"));
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView flgTR;
        TextView code;
        TextView description;
        TextView prix;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            flgTR = itemView.findViewById(R.id.imgTR);
            code = itemView.findViewById(R.id.txtCode);
            description = itemView.findViewById(R.id.txtDesc);
            prix = itemView.findViewById(R.id.txtPrix);
        }
    }
}
