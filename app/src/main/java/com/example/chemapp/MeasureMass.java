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

import com.example.chemapp.Utils.CalculationRecord;
import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.Utils.NumberFormatter;
import com.example.chemapp.databinding.MeasureMassBinding;
import com.google.gson.Gson;


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

        Gson gson = new Gson();
        CalculatorUtil util = CalculatorUtil.getInstance();
        DbHelper db = DbHelper.getInstance(MeasureMass.this);

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

                String[][] data = new String[2][3];
                data[0][0] = "Volume (mL)";
                data[0][1] = "Req mass (g)";
                data[0][2] = "Req mass (mg)";

                data[1][0] = String.valueOf(volume);
                data[1][1] = NumberFormatter.formatNumber(result / 1000);
                data[1][2] = NumberFormatter.formatNumber(result);


                String description = gson.toJson(data);

                try {
                    boolean res = db.addHistory(title, CalculationRecord.PPM_HISTORY_ITEM, description);
                } catch (Exception e) {

                }

                BottomSheetHelper.showExpandableBottomSheet(
                        MeasureMass.this,
                        R.layout.sheet_layout,
                        title,
                        data,
                        () -> {
                            try {
                                db.addBookmark(title, CalculationRecord.PPM_HISTORY_ITEM, description);
                            } catch (Exception e) {

                            }
                        }
                );


            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Invalid inputs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getDescription(String[][] data){
        StringBuilder builder = new StringBuilder();

        for(int i = 0;i < data.length;i++) {
            builder.append(data[i][0]).append("\t").append(data[i][1]);

            if (i != data.length - 1) {
                builder.append("\n");
            }
        }

        return builder.toString();
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