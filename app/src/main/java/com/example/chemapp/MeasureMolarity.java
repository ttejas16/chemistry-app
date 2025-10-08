package com.example.chemapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.Utils.CalculationRecord;
import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.Utils.Compound;
import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.Utils.NumberFormatter;
import com.example.chemapp.data.repository.BookmarkRepository;
import com.example.chemapp.data.repository.CompoundRepository;
import com.example.chemapp.data.repository.HistoryRepository;
import com.example.chemapp.databinding.MeasureMolarityBinding;
import com.google.gson.Gson;

import java.util.Arrays;

public class MeasureMolarity extends AppCompatActivity {
    private MeasureMolarityBinding binding;
    final String[] solutionOptions = { "Molar solution", "Normal solution" };
    final String[] molarityUnitOptions = { "M", "mM", "μM"};
    final double[] sizes = {25.0, 50.0, 100.0, 250.0, 500.0, 1000.0};
    String [] salts;
    CompoundRepository compoundRepository;

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

        setSupportActionBar(binding.navigation);
        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Gson gson = new Gson();
        BookmarkRepository bookmarkRepository = BookmarkRepository.getInstance(getApplicationContext());
        HistoryRepository historyRepository = HistoryRepository.getInstance(getApplicationContext());
        compoundRepository = CompoundRepository.getInstance(getApplicationContext());

        salts = compoundRepository.getAllDisplayNames();

        Arrays.sort(salts);
        setSpinnerItems(binding.salt, salts);

        binding.salt.setOnItemClickListener((parent, view, position, id) -> updateWeights(parent.getItemAtPosition(position).toString()));
        binding.salt.setOnDismissListener(() -> {
            String saltWithFormula = binding.salt.getText().toString();
            if (saltWithFormula.isEmpty()) {
                binding.salt.setError("please enter or select salt");
                return;
            }

            if (!isValidSelection(saltWithFormula, salts)) {
                binding.salt.setError("invalid salt");
                return;
            }

            binding.salt.setError(null);
        });
        binding.salt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!hasMatchingSuggestions(s.toString(), salts)) {
                    binding.salt.setError("invalid salt");
                }
                else {
                    binding.salt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
            if (!isValidSelection(salt, salts)) {
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
                data[0][0] = "Volume (mL)";
                data[0][1] = "Req weight (g)";
                data[0][2] = "Req weight (mg)";

                int j = 1;
                for(double vlm: volumes) {
                    double resInGrams = calculateResult(weight, concentration, vlm);
                    double resInMilligrams = resInGrams * 1000;

                    data[j][0] = vlm + "";
                    data[j][1] = NumberFormatter.formatNumber(resInGrams);
                    data[j][2] = NumberFormatter.formatNumber(resInMilligrams);

                    j++;
                }

                String title = getResultTitle();
                String description = gson.toJson(data);

                try {
                    boolean res = historyRepository.addHistory(title, CalculationRecord.MOLARITY_HISTORY_ITEM, description);
                } catch (Exception e) {

                }

                BottomSheetHelper.showExpandableBottomSheet(
                        MeasureMolarity.this,
                        R.layout.sheet_layout,
                        getResultTitle(),
                        data,
                        () -> {
                            try {
                                bookmarkRepository.addBookmark(title, CalculationRecord.MOLARITY_HISTORY_ITEM, description);
                            } catch (Exception e) {

                            }
                        }
                );

            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Please enter valid strings ...", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        salts = compoundRepository.getAllDisplayNames();
        Arrays.sort(salts);

        setSpinnerItems(binding.salt, salts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.measure_molarity_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.plusItem) {
            Intent intent = new Intent(MeasureMolarity.this, AddCompound.class);
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.listAdditions) {
            Intent intent = new Intent(MeasureMolarity.this, AdditionalCompounds.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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

    private boolean isValidSelection(String input, String[] items) {
        if (input == null || input.trim().isEmpty()) return false;

        return Arrays.asList(items).contains(input);
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
        try {
            double[] weights = compoundRepository.getWeights(saltName);

            if (solutionType == 0) {
                binding.weight.setText(String.valueOf(weights[0]));
            }
            else  {
                binding.weight.setText(String.valueOf(weights[1]));
            }
        } catch (Exception e) {

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
