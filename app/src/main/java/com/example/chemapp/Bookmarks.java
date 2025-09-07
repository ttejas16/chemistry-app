package com.example.chemapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chemapp.Utils.HistoryAdapter;
import com.example.chemapp.Utils.HistoryItem;
import com.example.chemapp.databinding.BookmarksBinding;
import com.example.chemapp.databinding.HistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class Bookmarks extends AppCompatActivity {
    private BookmarksBinding binding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = BookmarksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        List<HistoryItem> items = new ArrayList<>();

        for (int i = 0;i < 4;i++) {
            HistoryItem historyItem = new HistoryItem(i, i + 1, "Item Title", "Item description");
            items.add(historyItem);
        }

        RecyclerView recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new HistoryAdapter(items));
    }
}
