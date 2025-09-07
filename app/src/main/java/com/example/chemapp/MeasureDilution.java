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

import com.example.chemapp.Utils.CalculationRecord;
import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.Utils.DbHelper;
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

        CalculatorUtil util = CalculatorUtil.getInstance();

        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        DbHelper db = DbHelper.getInstance(MeasureDilution.this);

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


        binding.calculate.setOnClickListener(v -> {
            String stockConcentrationString = binding.stockConcentration.getText().toString();
            String reqConcentrationString = binding.requiredConcentration.getText().toString();
            String volumeString = binding.volume.getText().toString();

            String stockUnitString = binding.stockConcentrationUnit.getSelectedItem().toString();
            String reqUnitString = binding.requiredConcentrationUnit.getSelectedItem().toString();

            if (stockConcentrationString.isEmpty() ||
                reqConcentrationString.isEmpty() ||
                volumeString.isEmpty()) {
                return;
            }

            int stockUnit = (binding.stockConcentrationUnit.getSelectedItemPosition() % 3) + 1;
            int reqUnit = binding.requiredConcentrationUnit.getSelectedItemPosition() + 1;

            try {
                double stockConcentration = Double.parseDouble(stockConcentrationString);
                double reqConcentration = Double.parseDouble(reqConcentrationString);
                double volume = Double.parseDouble(volumeString);

                double result = util.getDilutionResultFrom(
                        stockConcentration, stockUnit,
                        reqConcentration, reqUnit,
                        volume
                );

                String title = getResultTitle(
                        reqConcentrationString, reqUnitString,
                        stockConcentrationString, stockUnitString
                );

                String[][] data = new String[2][2];
                data[0][0] = "Req stock volume (mL)";
                data[0][1] = "Req solvent volume (mL)";

                data[1][0] = String.valueOf(result);
                data[1][1] = String.valueOf(volume - result);


                String description = getDescription(data);

                try {
                    boolean res = db.addHistory(title, CalculationRecord.DILUTION_HISTORY_ITEM, description);
                } catch (Exception e) {

                }

                BottomSheetHelper.showExpandableBottomSheet(
                        MeasureDilution.this,
                        R.layout.sheet_layout,
                        title,
                        data,
                        () -> {
                            try {
                                db.addBookmark(title, CalculationRecord.DILUTION_HISTORY_ITEM, description);
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
    private String getResultTitle(String reqConc, String reqUnit, String stockConc, String stockUnit) {
        StringBuilder builder = new StringBuilder();
        builder.append("To measure ");
        builder.append(reqConc + reqUnit);
        builder.append(" from ");
        builder.append(stockConc + stockUnit);
        builder.append(" stock solution");

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