package com.example.chemapp.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chemapp.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CalculationRecord> historyItemList;
    private AdapterView.OnItemClickListener listener;


    public HistoryAdapter(List<CalculationRecord> historyItemList) {
        this.historyItemList = historyItemList;
    }

    public void setItemOnClickListener(AdapterView.OnItemClickListener l){
        this.listener = l;
    }

    @Override
    public int getItemViewType(int position) {
        return historyItemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case CalculationRecord.ELEMENT_HISTORY_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.element_history_item, parent, false);
                return new ElementHistoryItemHolder(view);
            }

            case CalculationRecord.MOLARITY_HISTORY_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.molarity_history_item, parent, false);
                return new MolarityHistoryItemHolder(view);
            }

            case CalculationRecord.PPM_HISTORY_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.ppm_history_item, parent, false);
                return new PpmHistoryItemHolder(view);
            }

            case CalculationRecord.DILUTION_HISTORY_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.dilution_history_item, parent, false);
                return new DilutionHistoryItemHolder(view);
            }

            default: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.element_history_item, parent, false);
                return new ElementHistoryItemHolder(view);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CalculationRecord item = historyItemList.get(position);
        int type = item.getType();

        switch (type) {
            case CalculationRecord.ELEMENT_HISTORY_ITEM:
                ((ElementHistoryItemHolder) holder).bind(item);
                break;
            case CalculationRecord.MOLARITY_HISTORY_ITEM:
                ((MolarityHistoryItemHolder) holder).bind(item);
                break;
            case CalculationRecord.PPM_HISTORY_ITEM:
                ((PpmHistoryItemHolder) holder).bind(item);
                break;
            case CalculationRecord.DILUTION_HISTORY_ITEM:
                ((DilutionHistoryItemHolder) holder).bind(item);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return historyItemList.size();
    }

    public void clearItems(){
        int oldSize = historyItemList.size();

        if (oldSize == 0) return;

        historyItemList.clear();

        notifyItemRangeRemoved(0, oldSize);
    }

    static class ElementHistoryItemHolder extends RecyclerView.ViewHolder {
        TextView title,description;
        ElementHistoryItemHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.historyTitle);
            description = itemView.findViewById(R.id.historyDescription);

            itemView.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "type 1 clicked", Toast.LENGTH_SHORT).show();
            });
        }
        void bind(CalculationRecord item) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
        }
    }

    static class MolarityHistoryItemHolder extends RecyclerView.ViewHolder {
        TextView title,description;
        MolarityHistoryItemHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.historyTitle);
            description = itemView.findViewById(R.id.historyDescription);

            itemView.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "type 2 clicked", Toast.LENGTH_SHORT).show();
            });
        }
        void bind(CalculationRecord item) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
        }
    }

    static class PpmHistoryItemHolder extends RecyclerView.ViewHolder {
        TextView title,description;
        PpmHistoryItemHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.historyTitle);
            description = itemView.findViewById(R.id.historyDescription);

            itemView.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "type 3 clicked", Toast.LENGTH_SHORT).show();
            });
        }
        void bind(CalculationRecord item) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
        }
    }

    static class DilutionHistoryItemHolder extends RecyclerView.ViewHolder {
        TextView title,description;
        DilutionHistoryItemHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.historyTitle);
            description = itemView.findViewById(R.id.historyDescription);

            itemView.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "type 4 clicked", Toast.LENGTH_SHORT).show();
            });
        }
        void bind(CalculationRecord item) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
        }
    }
}
