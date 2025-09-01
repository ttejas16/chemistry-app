package com.example.chemapp;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.databinding.MeasureMolarityBinding;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MeasureMolarity extends AppCompatActivity {
    Map<String,String> molecularMap, equivalenceMap;
    private MeasureMolarityBinding binding;
    final String[] solutionOptions = { "Molar solution", "Normal solution" };
    final String[] molarityUnitOptions = { "M", "mM", "μM"};
    final double[] sizes = {25.0, 50.0, 100.0, 250.0, 500.0, 1000.0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = MeasureMolarityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        InputStream is = getResources().openRawResource(R.raw.salt_data);

        molecularMap = new HashMap<>();
        equivalenceMap = new HashMap<>();
        CSVUtils.getMaps(is, molecularMap, equivalenceMap);

        String[] salts = new String[molecularMap.size()];

        int i = 0;
        for(String salt: molecularMap.keySet()) {
            salts[i++] = salt;
        }

        Arrays.sort(salts);
        setSpinnerItems(binding.salt, salts);

        binding.salt.setOnItemClickListener((parent, view, position, id) -> updateWeights(parent.getItemAtPosition(position).toString()));


        setSpinnerItems(binding.solutionType, solutionOptions);
        binding.solutionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0){
                    binding.concentrationTitle.setText(R.string.concentration_label_molarity);
                    binding.weightTitle.setText(R.string.weight_label_molarity);
                    binding.concentrationUnit.setEnabled(true);
                    binding.concentrationUnit.setAlpha(1.0f);
                }
                else {
                    binding.concentrationTitle.setText(R.string.concentration_label_normality);
                    binding.weightTitle.setText(R.string.weight_label_normality);
                    binding.concentrationUnit.setSelection(0);
                    binding.concentrationUnit.setEnabled(false);
                    binding.concentrationUnit.setAlpha(0.5f);
                }
                updateWeights(binding.salt.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        setSpinnerItems(binding.concentrationUnit, molarityUnitOptions);

        binding.calculate.setOnClickListener(v -> {
            if (binding.salt.getText().toString().isEmpty()) return;

            String salt = binding.salt.getText().toString();
            String weightString = binding.weight.getText().toString();
            String concentrationString = binding.concentration.getText().toString();
            String volumeString = binding.volume.getText().toString();

            if (salt.isEmpty() || concentrationString.isEmpty() || weightString.isEmpty() || (
                    volumeString.isEmpty() && !binding.standardSizes.isChecked())) {
                return;
            }

            try {
                StringBuilder buffer = new StringBuilder();
                double concentration = getConcentration();
                double weight = Double.parseDouble(weightString);

                if (binding.standardSizes.isChecked()) {
                    for(double size: sizes) {
                        double res = calculateResult(weight, concentration, size);
                        String formatted = String.format("%-25.2f %-25.2f %-20.2f%n", size, res, res * 1000);
                        buffer.append(formatted);
                    }

                } else  {
                    double volume = Double.parseDouble(volumeString);
                    double res = calculateResult(weight, concentration, volume);
                    String formatted = String.format("%-25.2f %-25.2f %-20.2f%n", volume, res, res * 1000);

                    buffer.append(formatted);
                }

                showResultAlert(salt, buffer.toString());


            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Please enter valid strings ...", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public double getConcentration(){
        if (binding.concentration.getText().toString().isEmpty()) return 0;

        double res = Double.parseDouble(binding.concentration.getText().toString());

        if (binding.solutionType.getSelectedItemPosition() == 0) {
            switch (binding.concentrationUnit.getSelectedItemPosition()) {
                case 1:
                    res = res / 1000;
                    break;
                case 2:
                    res = res / 1000000;
                    break;
            }
        }

        return res;
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
    public void setSpinnerItems(AutoCompleteTextView spinner, String[] options){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                options
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }

    public void updateWeights(String salt) {
        if (binding.salt.getText().length() == 0) {
            binding.weight.setText("");
            return;
        }

        int solutionType = binding.solutionType.getSelectedItemPosition();
        if (solutionType == 0) {
            binding.weight.setText(molecularMap.get(salt));
        }
        else  {
            binding.weight.setText(equivalenceMap.get(salt));
        }
    }

    public double calculateResult(double w, double c, double volumeInMillilitres){
        double l = volumeInMillilitres / 1000;
        return w * c * l;
    }

    public void showResultAlert(String salt, String res){
        String volumeTitle = String.format("%-20s","Volume(ml)");
        String weightTitle = String.format("%-20s","Weight(g)");
        String weightTitleMg = String.format("%-20s","Weight(mg)");

        unfocusAllEditTexts();

        AlertDialog.Builder builder = new AlertDialog.Builder(MeasureMolarity.this);
        builder.setTitle("For " + salt + " solution");
        builder.setMessage(String.format("%s%s%s\n\n%s", volumeTitle, weightTitle, weightTitleMg, res));
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void unfocusAllEditTexts() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();

            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }
}
