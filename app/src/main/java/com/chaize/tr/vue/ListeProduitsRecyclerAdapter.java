package com.chaize.tr.vue;

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

public class ListeProduitsRecyclerAdapter extends RecyclerView.Adapter<ListeProduitsRecyclerAdapter.MyViewHolder> {

    private ArrayList<Produit> arrayList = new ArrayList<>();

    public ListeProduitsRecyclerAdapter(ArrayList<Produit> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_produit_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.code.setText(arrayList.get(position).getCode());
        holder.description.setText(arrayList.get(position).getDescription());
        int syncStatus = arrayList.get(position).getSyncStatus();
        if (syncStatus== DbContract.SYNC_STATUS_OK) {
            holder.syncStatus.setImageResource(R.drawable.sync_ok);
        } else {
            holder.syncStatus.setImageResource(R.drawable.sync);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView syncStatus;
        TextView code;
        TextView description;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            syncStatus = itemView.findViewById(R.id.imgSync);
            code = itemView.findViewById(R.id.txtCode);
            description = itemView.findViewById(R.id.txtDesc);
        }
    }
}
