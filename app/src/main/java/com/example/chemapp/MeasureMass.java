package com.example.chemapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.databinding.MeasureMassBinding;


public class MeasureMass extends AppCompatActivity {

    private MeasureMassBinding binding;
    private final String[] concentrationUnits = {"ppm", "ppb", "ppt"};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = MeasureMassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        setSpinnerItems(binding.concentrationUnit, concentrationUnits);

        CalculatorUtil util = CalculatorUtil.getInstance();

        binding.calculate.setOnClickListener(v -> {
            String concentrationString = binding.concentration.getText().toString();
            String volumeString = binding.volume.getText().toString();
            String selectedUnit = binding.concentrationUnit.getSelectedItem().toString();
            int cUnit = binding.concentrationUnit.getSelectedItemPosition() + 1;

            if (concentrationString.isEmpty() || volumeString.isEmpty()) {
                return;
            }

            try {
                double concentration = Double.parseDouble(concentrationString);
                double volume = Double.parseDouble(volumeString);

                double result = util.calculateSoluteMassForPartsPerConcentration(
                        concentration,
                        volume,
                        cUnit
                );

                String title = "For " + concentrationString + " " + selectedUnit + " of solute";

                String[][] data = new String[2][2];
                data[0][0] = "Volume (mL)";
                data[0][1] = "Mass required (mg)";

                data[1][0] = String.valueOf(volume);
                data[1][1] = String.valueOf(result);


                BottomSheetHelper.showExpandableBottomSheet(
                        MeasureMass.this,
                        R.layout.sheet_layout,
                        title,
                        data
                );


            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Invalid inputs", Toast.LENGTH_SHORT).show();
            }
        });
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