package com.example.chemapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView solutionTypeTitle, weightTypeTitle;
    Spinner saltSpinner, solutionTypeSpinner;
    EditText molarity, weight, volume;
    Button calculate;
    Map<String,String> molecularMap, equivalenceMap;
    CheckBox standardSizes;

    double sizes[] = {25.0, 50.0, 100.0, 250.0, 500.0, 1000.0};

    public boolean setSpinnerItems(Spinner spinner, String[] options){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        return true;
    }

    public void updateWeights(String salt) {
        if (saltSpinner.getSelectedItemPosition() == 0) {
            weight.setText("");
            return;
        };

        int solutionType = solutionTypeSpinner.getSelectedItemPosition();
        if (solutionType == 0) {
            weight.setText(molecularMap.get(salt));
        }
        else  {
            weight.setText(equivalenceMap.get(salt));
        }
    }

    public double calculateResult(double w, double c, double volumeInMillilitres){
        double l = volumeInMillilitres / 1000;
        return w * c * l;
    }

    public void showResultAlert(String salt, String res){
        String volumeTitle = String.format("%-20s","Volume(ml)");
        String weightTitle = String.format("%-20s","Weight(g)");

        unfocusAllEditTexts();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("For " + salt + " solution");
        builder.setMessage(String.format("%s%s\n\n%s", volumeTitle, weightTitle, res));
        builder.setPositiveButton("Close", (dialog, which) -> {
            dialog.dismiss();
        });
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        solutionTypeTitle = findViewById(R.id.solutionTypeTitle);
        weightTypeTitle = findViewById(R.id.weightTypeTtile);

        saltSpinner = findViewById(R.id.salt);
        solutionTypeSpinner = findViewById(R.id.solutionType);

        molarity = findViewById(R.id.molarity);
        weight = findViewById(R.id.weight);
        volume = findViewById(R.id.volume);
        standardSizes = findViewById(R.id.standardSizes);
        calculate = findViewById(R.id.calculate);

        InputStream is = getResources().openRawResource(R.raw.salt_data);

        molecularMap = new HashMap<>();
        equivalenceMap = new HashMap<>();
        CSVUtils.getMaps(is, molecularMap, equivalenceMap);

        String[] salts = new String[molecularMap.size() + 1];
        salts[0] = "Select salt ...";

        int i = 1;
        for(String salt: molecularMap.keySet()) {
            salts[i++] = salt;
        }

        setSpinnerItems(saltSpinner, salts);
        saltSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateWeights(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] solutionOptions = {
                "Molar solution", "Normal solution"
        };
        setSpinnerItems(solutionTypeSpinner, solutionOptions);
        solutionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0){
                    solutionTypeTitle.setText("Molarity");
                    weightTypeTitle.setText("Molecular Weight(g/mol)");
                }
                else {
                    solutionTypeTitle.setText("Normality");
                    weightTypeTitle.setText("Equivalence Weight(g/eq)");
                }
                updateWeights(saltSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saltSpinner.getSelectedItemPosition() == 0) return;

                String salt = saltSpinner.getSelectedItem().toString();
                String weightString = weight.getText().toString();
                String concentrationString = molarity.getText().toString();
                String volumeString = volume.getText().toString();

                if (salt.isEmpty() || concentrationString.isEmpty() || weightString.isEmpty() || (
                        volumeString.isEmpty() && !standardSizes.isChecked())) {
                    return;
                }
                
                try {
                    StringBuilder buffer = new StringBuilder();
                    double concentration = Double.parseDouble(concentrationString);
                    double weight = Double.parseDouble(weightString);
                    
                    if (standardSizes.isChecked()) {
                        for(double size: sizes) {
                            double res = calculateResult(weight, concentration, size);
                            String formatted = String.format("%-20.6f%.6f", size, res);

                            buffer.append(formatted);
                            buffer.append("\n");
                        }
                        
                    } else  {
                        double volume = Double.parseDouble(volumeString);
                        double res = calculateResult(weight, concentration, volume);
                        String formatted = String.format("%-20.6f%.6f", volume, res);

                        buffer.append(formatted);
                    }
                    
                    showResultAlert(salt, buffer.toString());


                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });

    }
}