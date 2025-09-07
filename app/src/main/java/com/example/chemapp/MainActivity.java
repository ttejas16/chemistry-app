package com.example.chemapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.databinding.ActivityMainBinding;
import com.google.android.material.appbar.MaterialToolbar;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        setSupportActionBar(binding.toolbar);

        binding.featureSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (binding.solidrb.isChecked()) {
                    binding.solid.setVisibility(View.VISIBLE);
                    binding.liquid.setVisibility(View.GONE);
                }
                else if (binding.liquidrb.isChecked()) {
                    binding.solid.setVisibility(View.GONE);
                    binding.liquid.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.measureMolarity.setOnClickListener(v -> {
            startNewActivity(MeasureMolarity.class);
        });

        binding.measurePpm.setOnClickListener(v -> {
            startNewActivity(MeasureMass.class);
        });

        binding.measureElement.setOnClickListener(v -> {
            startNewActivity(MeasureSolid.class);
        });

        binding.measureDilution.setOnClickListener(v -> {
            startNewActivity(MeasureDilution.class);
        });


        CalculatorUtil.init(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.history == item.getItemId()) {
            startNewActivity(History.class);
        }
        else if(R.id.bookmarks == item.getItemId()) {
            startNewActivity(Bookmarks.class);
        }

        return super.onOptionsItemSelected(item);
    }

    private void startNewActivity(Class<?> cls){
        Intent intent = new Intent(getApplicationContext(), cls);
        startActivity(intent);
    }

}