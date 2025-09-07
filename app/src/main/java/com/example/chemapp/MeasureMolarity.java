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

import com.example.chemapp.Utils.CalculationRecord;
import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.Utils.Compound;
import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.databinding.MeasureMolarityBinding;

import java.util.Arrays;
import java.util.Map;

public class MeasureMolarity extends AppCompatActivity {
    private MeasureMolarityBinding binding;
    final String[] solutionOptions = { "Molar solution", "Normal solution" };
    final String[] molarityUnitOptions = { "M", "mM", "μM"};
    final double[] sizes = {25.0, 50.0, 100.0, 250.0, 500.0, 1000.0};
    CalculatorUtil util = CalculatorUtil.getInstance();
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

        DbHelper db = DbHelper.getInstance(MeasureMolarity.this);

        String[] salts = util.getFormattedDisplayName();
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
            if (!Arrays.asList(salts).contains(salt)) {
                return;
            }

            String weightString = binding.weight.getText().toString();
            String concentrationString = binding.concentration.getText().toString();
            String volumeString = binding.volume.getText().toString();

            if (salt.isEmpty() || concentrationString.isEmpty() || weightString.isEmpty() || (
                    volumeString.isEmpty() && !binding.standardSizes.isChecked())) {
                return;
            }

            try {
                double concentration = getConcentration();
                double weight = Double.parseDouble(weightString);

                double[] volumes;

                if (binding.standardSizes.isChecked()) {
                    volumes = sizes;
                } else  {
                    double volume = Double.parseDouble(volumeString);
                    volumes = new double[]{volume};
                }

                String[][] data = new String[volumes.length + 1][3];
                data[0][0] = "For volume (mL)";
                data[0][1] = "Req weight (g)";
                data[0][2] = "Req weight (mg)";

                int j = 1;
                for(double vlm: volumes) {
                    double resInGrams = calculateResult(weight, concentration, vlm);
                    double resInMilligrams = resInGrams * 1000;
                    data[j][0] = vlm + "";
                    data[j][1] = resInGrams + "";
                    data[j][2] = resInMilligrams + "";

                    j++;
                }

                String title = getResultTitle();
                String description = getDescription(data);

                try {
                    boolean res = db.addHistory(title, CalculationRecord.MOLARITY_HISTORY_ITEM, description);
                } catch (Exception e) {

                }

                BottomSheetHelper.showExpandableBottomSheet(
                        MeasureMolarity.this,
                        R.layout.sheet_layout,
                        getResultTitle(),
                        data,
                        () -> {
                            try {
                                db.addBookmark(title, CalculationRecord.MOLARITY_HISTORY_ITEM, description);
                            } catch (Exception e) {

                            }
                        }
                );

            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Please enter valid strings ...", Toast.LENGTH_SHORT).show();

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

    public String getResultTitle(){
        String salt = binding.salt.getText().toString();
        String concentration = binding.concentration.getText().toString();
        String cUnit = binding.concentrationUnit.getSelectedItem().toString();
        int solutionType = binding.solutionType.getSelectedItemPosition();

        StringBuilder builder = new StringBuilder();
        builder.append("For " + salt + " solution of ");
        builder.append(concentration + " ");

        if (solutionType == 0) {
            builder.append(cUnit);
        }
        else {
            builder.append("N");
        }

        return builder.toString();
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

    public void updateWeights(String formattedSaltName) {
        if (binding.salt.getText().length() == 0) {
            binding.weight.setText("");
            return;
        }

        if(formattedSaltName == null || formattedSaltName.isEmpty()){
            binding.weight.setText("");
            return;
        }

        String saltName = formattedSaltName;
        int splitIndex = formattedSaltName.lastIndexOf(" (");

        if(splitIndex != -1){
            saltName = formattedSaltName.substring(0,splitIndex).trim();
        }

        int solutionType = binding.solutionType.getSelectedItemPosition();
        Compound  compound = util.getCompoundsMap().get(saltName);

        if (compound == null){
            return;
        }

        if (solutionType == 0) {
            binding.weight.setText(String.valueOf(compound.molecularWeight));
        }
        else  {
            binding.weight.setText(String.valueOf(compound.equivalentWeight));
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
