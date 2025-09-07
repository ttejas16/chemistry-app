package com.example.chemapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.databinding.MeasureDilutionBinding;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Stream;


public class MeasureDilution extends AppCompatActivity {

    private MeasureDilutionBinding binding;
    private final String[] concentrationByPartsUnits = {"ppm", "ppb", "ppt"};
    private final String[] concentrationByMoleUnits = {"M", "mM", "μM"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = MeasureDilutionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        String[] unitsConcatenated = Stream
                .concat(Arrays.stream(concentrationByPartsUnits), Arrays.stream(concentrationByMoleUnits))
                .toArray(String[]::new);


        setSpinnerItems(binding.stockConcentrationUnit, unitsConcatenated);
        binding.stockConcentrationUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUnit = parent.getItemAtPosition(position).toString();

                boolean containsParts = Arrays.asList(concentrationByPartsUnits).contains(selectedUnit);
                if (containsParts) {
                    setSpinnerItems(binding.requiredConcentrationUnit, concentrationByPartsUnits);
                }
                else {
                    setSpinnerItems(binding.requiredConcentrationUnit, concentrationByMoleUnits);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        setSpinnerItems(binding.requiredConcentrationUnit, concentrationByPartsUnits);
    }

    public void setSpinnerItems(Spinner spinner, String[] options){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }
}