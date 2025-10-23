package com.example.chemapp.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaltOptionAdapter extends ArrayAdapter<String> {
    private final List<String> itemsAll;
    private final List<String> itemsAllNormalized;
    private final List<String> suggestions;
    public SaltOptionAdapter(Context ctx, int resource, String[] items) {
        super(ctx, resource, new ArrayList<>(Arrays.asList(items)));

        this.itemsAll = new ArrayList<>(Arrays.asList(items));
        this.itemsAllNormalized = new ArrayList<>(items.length);
        for (String i : itemsAll) {
            this.itemsAllNormalized.add(normalize(i));
        }
        this.suggestions = new ArrayList<>();
    }

    public void updateItems(String[] items) {
        List<String> newItems = Arrays.asList(items);

        itemsAll.clear();
        itemsAll.addAll(newItems);

        suggestions.clear();

        itemsAllNormalized.clear();
        for (String i : itemsAll) {
            this.itemsAllNormalized.add(normalize(i));
        }

        clear();
        addAll(newItems);
        notifyDataSetChanged();
    }

    public boolean isValidSelection(String input) {
        if (input == null || input.trim().isEmpty()) return false;

        return itemsAll.contains(input);
    }

    public boolean hasMatchingSuggestions(CharSequence constraint) {
        if (constraint == null || constraint.length() == 0) {
            return true;
        }

        String cNorm = normalize(constraint.toString());
        for (String norm : itemsAllNormalized) {
            if (norm.contains(cNorm)) return true;
        }

        return false;
    }

    // Normalize: convert subscript digits to normal digits
    private static String normalize(String s) {
        if (s == null) return "";
        s = s.trim();

        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (ch >= '₀' && ch <= '₉') {
                char normalDigit = (char) ('0' + (ch - '₀'));
                out.append(normalDigit);
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return optionFilter;
    }

    private final Filter optionFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            suggestions.clear();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(itemsAll);
            } else {
                String cNorm = normalize(constraint.toString());
                for (int i = 0; i < itemsAll.size(); i++) {
                    if (itemsAllNormalized.get(i).contains(cNorm)) {
                        suggestions.add(itemsAll.get(i));
                    }
                }
            }

            results.values = new ArrayList<>(suggestions);
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                //noinspection unchecked
                addAll((List<String>) results.values);
            }
            notifyDataSetChanged();
        }
    };
}
