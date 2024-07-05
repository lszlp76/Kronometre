package com.lszlp.choronometre;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lszlp.choronometre.databinding.LaprowsBinding;

import java.util.ArrayList;

public class LapListAdapter extends RecyclerView.Adapter<LapListAdapter.LapListViewHolder>{
    ArrayList<Lap> lapArrayList;
    public LapListAdapter(ArrayList<Lap> lapArrayList) {
        this.lapArrayList = lapArrayList;
    }


    @NonNull
    @Override
    public LapListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LaprowsBinding laprowsBinding = LaprowsBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new LapListViewHolder(laprowsBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull LapListViewHolder holder, int position) {
        holder.itemView.laprow3.setText(lapArrayList.get(position).unit);
        holder.itemView.laprow1.setText(String.valueOf(lapArrayList.get(position).lapsayisi));
        holder.itemView.laprow2.setText(lapArrayList.get(position).lap);


    }
    @Override
    public int getItemCount() {
        return lapArrayList.size();
    }

    public class LapListViewHolder extends RecyclerView.ViewHolder{


private LaprowsBinding itemView;

        public LapListViewHolder(@NonNull LaprowsBinding itemView) {
            super(itemView.getRoot());
            this.itemView = itemView;
        }
    }


}