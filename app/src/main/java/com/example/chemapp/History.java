package com.example.chemapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chemapp.utils.CalculationRecord;
import com.example.chemapp.utils.DbHelper;
import com.example.chemapp.adapters.HistoryAdapter;
import com.example.chemapp.data.repository.HistoryRepository;
import com.example.chemapp.databinding.HistoryBinding;

import java.util.List;

public class History extends AppCompatActivity {

    private HistoryBinding binding;
    private HistoryAdapter adapter;

    private HistoryRepository historyRepository;
    private DbHelper db;

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

        setSupportActionBar(binding.navigation);
        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        db = DbHelper.getInstance(History.this);
        historyRepository = HistoryRepository.getInstance(getApplicationContext());

        List<CalculationRecord> items = historyRepository.getHistoryCalculations();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.clearHistory) {
            AlertDialog.Builder builder = new AlertDialog.Builder(History.this);
            builder.setTitle("Clear History")
                    .setMessage("Are you sure you want to clear history?")
                    .setPositiveButton("Yes", (dialog,whichButton) -> {
                        boolean res = historyRepository.clearHistory();
                        if (res) {
                            adapter.clearItems();
                        }
                    })
                    .setNegativeButton("No", null);

            builder.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkIfEmpty(){
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.historyRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
