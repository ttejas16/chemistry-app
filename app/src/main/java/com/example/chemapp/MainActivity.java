package com.example.chemapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.utils.CalculatorUtil;
import com.example.chemapp.utils.DbHelper;
import com.example.chemapp.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityMainBinding binding;
    private ActionBarDrawerToggle toggle;

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

        DbHelper dbHelper = DbHelper.getInstance(this.getApplicationContext());
        dbHelper.getWritableDatabase();
        
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

        toggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        binding.drawerLayout.addDrawerListener(toggle);
        binding.navView.setNavigationItemSelectedListener(this);
        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Close drawer when an item is tapped
//        binding.drawerLayout.closeDrawers();

        int id = item.getItemId();

        if (id == R.id.nav_history) {
            startNewActivity(History.class);
        } else if (id == R.id.nav_bookmarks) {
            startNewActivity(Bookmarks.class);
        } else if (id == R.id.nav_add_new) {
            startNewActivity(AddCompound.class);
        } else if (id == R.id.nav_additional) {
            startNewActivity(AdditionalCompounds.class);
        }

        return true;
    }

    private void startNewActivity(Class<?> cls){
        Intent intent = new Intent(getApplicationContext(), cls);
        startActivity(intent);
    }

}