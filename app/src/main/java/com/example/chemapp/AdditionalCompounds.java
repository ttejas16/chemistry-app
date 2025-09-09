package com.example.chemapp;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.Utils.Compound;
import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.Utils.UserCompoundAdapter;
import com.example.chemapp.databinding.AdditionalCompoundsBinding;

import java.util.List;

public class AdditionalCompounds extends AppCompatActivity {
    AdditionalCompoundsBinding binding;
    UserCompoundAdapter adapter;
    CalculatorUtil util;
    DbHelper db;
    private final RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {
        @Override public void onChanged() { checkIfEmpty(); }
        @Override public void onItemRangeInserted(int positionStart, int itemCount) { checkIfEmpty(); }
        @Override public void onItemRangeRemoved(int positionStart, int itemCount) { checkIfEmpty(); }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = AdditionalCompoundsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.navigation);
        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        util = CalculatorUtil.getInstance();
        db = DbHelper.getInstance(AdditionalCompounds.this);

        List<Compound> items = db.getAllUserCompounds();

        adapter = new UserCompoundAdapter(items);
        adapter.registerAdapterDataObserver(emptyObserver);

        adapter.setOnItemDeleteListener((compoundName, position) -> {
            if (position == RecyclerView.NO_POSITION) return;

            util.removeUserCompound(AdditionalCompounds.this, compoundName);
            adapter.removeAt(position);
        });

        RecyclerView recyclerView = findViewById(R.id.additionalCompoundRecyclerView);
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

    private void checkIfEmpty(){
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.additionalCompoundRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
