package com.example.chemapp.Utils;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chemapp.R;

import java.util.Collections;
import java.util.List;

public class BookmarkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CalculationRecord> bookmarkItemList;
    private  OnItemDeleteListener onItemDeleteListener;
    public interface OnItemDeleteListener {
        void onItemDelete(long id, int position);
    }

    public BookmarkAdapter(List<CalculationRecord> bookmarkItemList) {
        this.bookmarkItemList = bookmarkItemList;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return bookmarkItemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case CalculationRecord.ELEMENT_HISTORY_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.element_history_item, parent, false);
                return new BookmarkItemHolder(view, onItemDeleteListener);
            }

            case CalculationRecord.MOLARITY_HISTORY_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.molarity_history_item, parent, false);
                return new BookmarkItemHolder(view, onItemDeleteListener);
            }

            case CalculationRecord.PPM_HISTORY_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.ppm_history_item, parent, false);
                return new BookmarkItemHolder(view, onItemDeleteListener);
            }

            case CalculationRecord.DILUTION_HISTORY_ITEM: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.dilution_history_item, parent, false);
                return new BookmarkItemHolder(view, onItemDeleteListener);
            }

            default: {
                View view = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.element_history_item, parent, false);
                return new BookmarkItemHolder(view, onItemDeleteListener);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CalculationRecord item = bookmarkItemList.get(position);
        ((BookmarkItemHolder) holder).bind(item);
    }

    @Override
    public int getItemCount() {
        return bookmarkItemList.size();
    }

    public void removeAt(int position) {
        if (position < 0 || position >= bookmarkItemList.size()) return;
        bookmarkItemList.remove(position);
        notifyItemRemoved(position);
    }

    public void sortByTitle() {
        bookmarkItemList.sort((b1, b2) -> b1.getTitle().compareToIgnoreCase(b2.getTitle()));
        notifyDataSetChanged();
    }

    public void sortByType() {
        bookmarkItemList.sort((b1, b2) -> b1.getType() - b2.getType());
        notifyDataSetChanged();
    }

    public void sortByTime() {
        bookmarkItemList.sort((b1, b2) -> {
            long res = b2.getId() - b1.getId();
            return Math.toIntExact(res);
        });
        notifyDataSetChanged();
    }

    public void clearItems(){
        int oldSize = bookmarkItemList.size();

        if (oldSize == 0) return;

        bookmarkItemList.clear();

        notifyItemRangeRemoved(0, oldSize);
    }

    static class BookmarkItemHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView title,description;

        private long id = -1;
        private final OnItemDeleteListener onItemDeleteListener;

        BookmarkItemHolder(@NonNull View itemView, OnItemDeleteListener listener) {
            super(itemView);

            title = itemView.findViewById(R.id.historyTitle);
            description = itemView.findViewById(R.id.historyDescription);
            this.onItemDeleteListener = listener;

            itemView.setOnCreateContextMenuListener(this);
        }
        void bind(CalculationRecord item) {
            title.setText(item.getTitle());
            description.setText(item.getDescription());
            this.id = item.getId();
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem delete = menu.add(0,0,0, "Delete");

            delete.setOnMenuItemClickListener(i -> {
                if (onItemDeleteListener != null) {
                    onItemDeleteListener.onItemDelete(id, getAdapterPosition());
                }

                return true;
            });
        }
    }
}
