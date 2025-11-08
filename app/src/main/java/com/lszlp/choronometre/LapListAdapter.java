package com.lszlp.choronometre;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lszlp.choronometre.databinding.LaprowsBinding;

import java.util.ArrayList;

public class LapListAdapter extends RecyclerView.Adapter<LapListAdapter.LapListViewHolder> {
    ArrayList<Lap> lapArrayList;

    private OnItemClickListener mlistener;

    private OnItemLongClickListener mlongClickListener;

    public LapListAdapter(ArrayList<Lap> lapArrayList) {
        this.lapArrayList = lapArrayList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mlistener = listener;
    }


    @NonNull
    @Override
    public LapListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LaprowsBinding laprowsBinding = LaprowsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LapListViewHolder(laprowsBinding, mlistener, mlongClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull LapListViewHolder holder, int position) {
        ;

        Context context = holder.itemView.lapline.getContext();
        holder.itemView.laprow3.setText(lapArrayList.get(position).unit);
        holder.itemView.laprow1.setText(String.valueOf(lapArrayList.get(position).lapsayisi));
        holder.itemView.laprow2.setText(lapArrayList.get(position).lap);

//listedeki her bir satırın rengini değiştiriyor
//if (position % 2 == 0) {
//           holder.itemView.lapline.setBackgroundColor(
//               ContextCompat.getColor(
//                   holder.itemView.lapline.getContext(),
//                   R.color.colorDisable
//               )
//           );
//       } else {
//           holder.itemView.lapline.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDisableq));
//       }

    }

    @Override
    public int getItemCount() {
        return lapArrayList.size();
    }


    public interface OnItemClickListener {

        void onAddMessage(int position);


    }

public interface  OnItemLongClickListener{
    void onItemLongClick(int position);
}

public void setOnItemLongClickListener(OnItemLongClickListener listener){
    this.mlongClickListener = listener;
}



    public class LapListViewHolder extends RecyclerView.ViewHolder {

        //Note kısmı ekleme
        ImageView addNote;

        private LaprowsBinding itemView;

        public LapListViewHolder(@NonNull LaprowsBinding itemView, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
            super(itemView.getRoot());

            this.itemView = itemView;
            addNote = itemView.addnote.findViewById(R.id.addnote);
            addNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onAddMessage(position);

                        }
                    }

                }
            });

            // --> ADD THIS: Set the long click listener on the entire item view
            itemView.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (longClickListener != null) {
                        int position = getBindingAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            // Trigger the interface method
                            longClickListener.onItemLongClick(position);
                            return true; // Return true to indicate the click was consumed
                        }
                    }
                    return false; // Return false if not consumed
                }
            });
        }
    }
}


