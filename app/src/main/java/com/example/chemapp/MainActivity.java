package com.example.chemapp;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.databinding.ActivityMainBinding;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    Map<String,String> molecularMap, equivalenceMap;
    private ActivityMainBinding binding;
    final String[] solutionOptions = { "Molar solution", "Normal solution" };
    final String[] molarityUnitOptions = { "Mole (M)", "milliMole (mM)", "microMole (μM)"};
    final double[] sizes = {25.0, 50.0, 100.0, 250.0, 500.0, 1000.0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        setSpinnerItems(binding.salt, salts);
        binding.salt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateWeights(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        setSpinnerItems(binding.solutionType, solutionOptions);
        binding.solutionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0){
                    binding.solutionTypeTitle.setText("Molarity (M)");
                    binding.weightTypeTtile.setText("Molecular Weight (g/mol)");
                    binding.molarityUnit.setEnabled(true);
                    binding.molarityUnit.setAlpha(1.0f);
                }
                else {
                    binding.solutionTypeTitle.setText("Normality");
                    binding.weightTypeTtile.setText("Equivalence Weight (g/eq)");
                    binding.molarityUnit.setSelection(0);
                    binding.molarityUnit.setEnabled(false);
                    binding.molarityUnit.setAlpha(0.5f);
                }
                updateWeights(binding.salt.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        setSpinnerItems(binding.molarityUnit, molarityUnitOptions);
        binding.molarityUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        binding.solutionTypeTitle.setText("Molarity (M)");
                        break;
                    case 1:
                        binding.solutionTypeTitle.setText("Molarity (mM)");
                        break;
                    case 2:
                        binding.solutionTypeTitle.setText("Molarity (μM)");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        binding.calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.salt.getSelectedItemPosition() == 0) return;

                String salt = binding.salt.getSelectedItem().toString();
                String weightString = binding.weight.getText().toString();
                String concentrationString = binding.molarity.getText().toString();
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
                        String formatted = String.format("%-20.2f %.2f%n", volume, res);

                        buffer.append(formatted);
                    }
                    
                    showResultAlert(salt, buffer.toString());


                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "Please enter valid strings ...", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public double getConcentration(){
        if (binding.molarity.getText().toString().isEmpty()) return 0;

        double res = Double.parseDouble(binding.molarity.getText().toString());

        if (binding.solutionType.getSelectedItemPosition() == 0) {
            switch (binding.molarityUnit.getSelectedItemPosition()) {
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

    public void updateWeights(String salt) {
        if (binding.salt.getSelectedItemPosition() == 0) {
            binding.weight.setText("");
            return;
        };

        int solutionType = binding.solutionType.getSelectedItemPosition();
        if (solutionType == 0) {
            binding.weight.setText(molecularMap.get(salt));
        }
        else  {
            binding.weight.setText(equivalenceMap.get(salt));
        }
    }
    public double calculateConcentrationByParts(double concentration,double volumeML){
        //concentration IS IN PPM
        // volumeML IS the desired volume of a solution
        //The O/P here will be the required Weight of A solute in a solutionIN MILLIGRAMS
        // 1000 MILLIGRAM = 1 GRAMS
        if(concentration < 0 || volumeML < 0){
           return 0;
        }

        return concentration * (volumeML/1000);
    }

    public double calculateCompoundWeightForElement(String element, String compound, double concentration, double volumeMl){
        //NOT SURE WHAT WILL BE THE INPUT TYPE FOR ELEMENT AND COMPOUND

        // eleCnt = eleCountInCompound(element,compound)  it will return no. of elements in compund;
        // eleWeight = eleCnt * getEleWeight(element);
        // percentage = eleWeight / compoundWeight;
        //result  =  calculateConcentrationByParts(concentration,volumeMl) / percentage;
        //return result; in milligrams
        return 0;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("For " + salt + " solution");
        builder.setMessage(String.format("%s%s%s\n\n%s", volumeTitle, weightTitle, weightTitleMg, res));
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
}