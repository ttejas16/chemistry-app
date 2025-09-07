package com.example.chemapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chemapp.Utils.CalculationRecord;
import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.Utils.HistoryAdapter;
import com.example.chemapp.Utils.HistoryItem;
import com.example.chemapp.databinding.HistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    private HistoryBinding binding;
    private HistoryAdapter adapter;


    private final RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override public void onChanged() { checkIfEmpty(); }
        @Override public void onItemRangeInserted(int positionStart, int itemCount) { checkIfEmpty(); }
        @Override public void onItemRangeRemoved(int positionStart, int itemCount) { checkIfEmpty(); }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = HistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        DbHelper db = DbHelper.getInstance(History.this);

        List<CalculationRecord> items = db.getHistoryCalculations();
        adapter = new HistoryAdapter(items);
        adapter.registerAdapterDataObserver(emptyObserver);

        RecyclerView recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        checkIfEmpty();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister observer to avoid leaks
        adapter.unregisterAdapterDataObserver(emptyObserver);
    }

    private void checkIfEmpty(){
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.historyRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
