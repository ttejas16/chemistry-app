package com.example.chemapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chemapp.Utils.BookmarkAdapter;
import com.example.chemapp.Utils.CalculationRecord;
import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.data.repository.BookmarkRepository;
import com.example.chemapp.databinding.BookmarksBinding;

import java.util.List;

public class Bookmarks extends AppCompatActivity {
    private BookmarksBinding binding;

    private BookmarkAdapter adapter;

    private final RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override public void onChanged() { checkIfEmpty(); }
        @Override public void onItemRangeInserted(int positionStart, int itemCount) { checkIfEmpty(); }
        @Override public void onItemRangeRemoved(int positionStart, int itemCount) { checkIfEmpty(); }
    };

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

        setSupportActionBar(binding.navigation);
        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        DbHelper db = DbHelper.getInstance(getApplicationContext());
        BookmarkRepository bookmarkRepository = BookmarkRepository.getInstance(getApplicationContext());

        List<CalculationRecord> items = bookmarkRepository.getBookmarks();

        adapter = new BookmarkAdapter(items);
        adapter.registerAdapterDataObserver(emptyObserver);
        adapter.setOnItemDeleteListener((id, position) -> {
            if (position == RecyclerView.NO_POSITION) return;

            boolean res = bookmarkRepository.deleteBookmark(String.valueOf(id));
            if (res) {
                adapter.removeAt(position);
            }

        });


        RecyclerView recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
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
        getMenuInflater().inflate(R.menu.bookmark_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sort_by_title) {
            adapter.sortByTitle();
        } else if (id == R.id.sort_by_type) {
            adapter.sortByType();
        } else if (id == R.id.sort_by_created) {
            adapter.sortByTime();
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkIfEmpty(){
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.historyRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
