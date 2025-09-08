package com.example.chemapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.databinding.AddCompoundBinding;
import com.example.chemapp.databinding.MeasureMolarityBinding;

public class AddCompound extends AppCompatActivity {

    AddCompoundBinding binding;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = AddCompoundBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        CalculatorUtil util = CalculatorUtil.getInstance();

        binding.add.setOnClickListener(v -> {
            String compoundName = binding.compoundName.getText().toString();
            String molecularFormula = binding.molecularFormula.getText().toString();
            String iupacName = binding.iupacName.getText().toString();

            String molecularWeightString = binding.molecularWeight.getText().toString();
            String equivalentWeightString = binding.equivalentWeight.getText().toString();

            if (compoundName.isEmpty() || molecularFormula.isEmpty() || molecularWeightString.isEmpty()) {
                return;
            }

            if (iupacName.isEmpty()) {
                iupacName = compoundName;
            }

            try {
                double mw = Double.parseDouble(molecularWeightString);
                double ew = equivalentWeightString.isEmpty() ? 1 : Double.parseDouble(equivalentWeightString);

                boolean isAdded = util.addNewUserCompound(
                        compoundName, mw, ew,
                        molecularFormula, iupacName, AddCompound.this
                );

                if (isAdded) {
                    Toast.makeText(getApplicationContext(), "Added new compound", Toast.LENGTH_SHORT).show();
                    resetInputs();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "invalid inputs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetInputs(){
        binding.compoundName.setText("");
        binding.molecularFormula.setText("");
        binding.iupacName.setText("");
        binding.molecularWeight.setText("");
        binding.equivalentWeight.setText("");
    }
}
