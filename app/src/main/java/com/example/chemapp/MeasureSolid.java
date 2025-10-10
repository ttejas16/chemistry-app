package com.example.chemapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.Utils.CalculationRecord;
import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.Utils.Element;
import com.example.chemapp.Utils.NumberFormatter;
import com.example.chemapp.databinding.MeasureSolidBinding;
import com.google.gson.Gson;

import java.util.Arrays;

public class MeasureSolid extends AppCompatActivity {
    private MeasureSolidBinding binding;
    private final String[] concentrationUnits = {"ppm", "ppb", "ppt"};
    String[] elements;

    CalculatorUtil util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = MeasureSolidBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.navigation);
        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Gson gson = new Gson();
        DbHelper db = DbHelper.getInstance(MeasureSolid.this);

        util = CalculatorUtil.getInstance();
        elements = util.getElementsMap().keySet().toArray(new String[0]);
        setSpinnerItems(binding.element, elements);

        setSpinnerItems(binding.concentrationUnit, concentrationUnits);

        binding.element.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedElement = parent.getItemAtPosition(position).toString();
                binding.element.setError(null);
                binding.salt.setText("");

                Element element = util.getElementsMap().get(selectedElement);
                String[] elementSalts = util.getFormattedDisplayName(element);
                setSpinnerItems(binding.salt, elementSalts);
            }
        });
        binding.element.setOnDismissListener(() -> {
            String selectedElement = binding.element.getText().toString();
            if (selectedElement.isEmpty()) {
                binding.element.setError("please select a element");
                setSpinnerItems(binding.salt, new String[0]);
                return;
            };

            if (!util.getElementsMap().containsKey(selectedElement)) {
                setSpinnerItems(binding.salt, new String[0]);
                binding.element.setError("invalid element");
                return;
            }

            Element element = util.getElementsMap().get(selectedElement);
            String[] elementSalts = util.getFormattedDisplayName(element);
            setSpinnerItems(binding.salt, elementSalts);
            binding.element.setError(null);
        });
        binding.element.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!hasMatchingSuggestions(s.toString(), elements)) {
                    binding.element.setError("invalid element");
                }
                else {
                    binding.element.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.calculate.setOnClickListener(v -> {
            String element = binding.element.getText().toString();
            String formattedSaltName = binding.salt.getText().toString();
            String concentrationString = binding.concentration.getText().toString();
            String volumeString = binding.volume.getText().toString();

            if (element.isEmpty() || formattedSaltName.isEmpty() ||
                    concentrationString.isEmpty() || volumeString.isEmpty()) {
                Toast.makeText(
                        MeasureSolid.this,
                        "please fill required fields",
                        Toast.LENGTH_LONG
                        ).show();
                return;
            }

            String salt = formattedSaltName;

            int splitIndex = formattedSaltName.lastIndexOf(" (");
            if(splitIndex != -1){
                salt = formattedSaltName.substring(0,splitIndex).trim();
            }

            if (!util.getElementsMap().containsKey(element) ||
                !util.getCompoundsMap().containsKey(salt)
            ) {
                return;
            }

            double concentration,volume;

            try {
                concentration = Double.parseDouble(concentrationString);
                volume = Double.parseDouble(volumeString);
            } catch (NumberFormatException e) {
                Toast.makeText(MeasureSolid.this, "Invalid inputs", Toast.LENGTH_SHORT).show();
                return;
            }

            double[] sizes = new double[]{volume};

            int concentrationUnit = binding.concentrationUnit.getSelectedItemPosition() + 1;

            String[][] data = new String[2][3];
            data[0][0] = "Volume (mL)";
            data[0][1] = "Req weight (g)";
            data[0][2] = "Req weight (mg)";

            int i = 1;

            for (double size:sizes) {
                try {
                    double result = util.calculateRequiredCompoundMassForElementConcentration(
                            element,
                            salt,
                            concentration,
                            size,
                            concentrationUnit
                    );
                    data[i][0] = size + "";
                    data[i][1] = NumberFormatter.formatNumber(result / 1000);
                    data[i][2] = NumberFormatter.formatNumber(result);

                } catch (Exception e) {

                }

                i++;
            }

            String title = getResultTitle();
            String description = gson.toJson(data);

            try {
                boolean res = db.addHistory(title, CalculationRecord.ELEMENT_HISTORY_ITEM, description);
            } catch (Exception e) {

            }

            BottomSheetHelper.showExpandableBottomSheet(
                    MeasureSolid.this,
                    R.layout.sheet_layout,
                    title,
                    data,
                    () -> {
                        try {
                            db.addBookmark(title, CalculationRecord.ELEMENT_HISTORY_ITEM, description);
                        } catch (Exception e) {

                        }
                    }
            );
            resetInputs();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        String selectedElement = binding.element.getText().toString();

        if (!selectedElement.isEmpty() && util.getElementsMap().containsKey(selectedElement)) {
            binding.salt.setText("");

            Element element = util.getElementsMap().get(selectedElement);

            String[] salts = util.getFormattedDisplayName(element);
            setSpinnerItems(binding.salt, salts);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.measure_molarity_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.plusItem) {
            Intent intent = new Intent(MeasureSolid.this, AddCompound.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.listAdditions) {
            Intent intent = new Intent(MeasureSolid.this, AdditionalCompounds.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSpinnerItems(AutoCompleteTextView spinner, String[] options){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                options
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }
    public void setSpinnerItems(Spinner spinner, String[] options){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                options
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }
    public String getResultTitle(){
        StringBuilder builder =  new StringBuilder();
        String cValue = binding.concentration.getText().toString();
        String cUnit = binding.concentrationUnit.getSelectedItem().toString();
        String element = binding.element.getText().toString();
        String salt = binding.salt.getText().toString();

        builder.append("To make " + cValue + cUnit + " of " + element + "\n");
        builder.append("using " + salt);
        return builder.toString();
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

    private boolean hasMatchingSuggestions(String currentText, String[] allOptions) {
        if (currentText == null || currentText.trim().isEmpty()) {
            return true;
        }

        String searchText = currentText.toLowerCase().trim();

        for (String option : allOptions) {
            if (option.toLowerCase().contains(searchText)) {
                return true;
            }
        }
        return false;
    }

    private void resetInputs(){
        binding.element.setText("");
        binding.salt.setText("");
        binding.concentration.setText("");
        binding.volume.setText("");
    }
}
