package com.example.chemapp.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chemapp.R;
import com.example.chemapp.utils.Compound;

import java.util.List;

public class UserCompoundAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Compound> userCompoundList;
    private  OnItemDeleteListener onItemDeleteListener;
    public interface OnItemDeleteListener {
        void onItemDelete(String compoundName, int position);
    }

    public UserCompoundAdapter(List<Compound> userCompoundList) {
        this.userCompoundList = userCompoundList;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.added_compound_item, parent, false);
        return new UserCompoundItemHolder(view, onItemDeleteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Compound item = userCompoundList.get(position);
        ((UserCompoundItemHolder) holder).bind(item);
    }

    @Override
    public int getItemCount() {
        return userCompoundList.size();
    }

    public void removeAt(int position) {
        if (position < 0 || position >= userCompoundList.size()) return;
        userCompoundList.remove(position);
        notifyItemRemoved(position);
    }

    static class UserCompoundItemHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView compoundName,molecularFormula,molecularWeight,equivalentWeight,iupacName;
        String compound;

        private final OnItemDeleteListener onItemDeleteListener;

        UserCompoundItemHolder(@NonNull View itemView, OnItemDeleteListener listener) {
            super(itemView);

            compoundName = itemView.findViewById(R.id.newCompoundItemTitle);
            molecularFormula = itemView.findViewById(R.id.newCompoundItemMolecularFormula);
            molecularWeight = itemView.findViewById(R.id.newCompoundItemMolecularWeight);
            equivalentWeight = itemView.findViewById(R.id.newCompoundItemEquivalentWeight);
            iupacName = itemView.findViewById(R.id.newCompoundItemIupacName);

            this.onItemDeleteListener = listener;

            itemView.setOnCreateContextMenuListener(this);
        }
        void bind(Compound compound) {
            this.compound = compound.getName();
            compoundName.append(compound.getName());
            molecularFormula.append(compound.molecularFormula);
            iupacName.append(compound.iupacName);

            molecularWeight.append(String.valueOf(compound.molecularWeight));
            equivalentWeight.append(String.valueOf(compound.equivalentWeight));
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem delete = menu.add(0,0,0, "Delete");

            delete.setOnMenuItemClickListener(i -> {
                if (onItemDeleteListener != null) {
                    onItemDeleteListener.onItemDelete(compound, getAdapterPosition());
                }

                return true;
            });
        }
    }
}
