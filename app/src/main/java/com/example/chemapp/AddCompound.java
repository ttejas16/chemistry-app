package com.example.chemapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.Utils.CalculatorUtil;
import com.example.chemapp.Utils.Compound;
import com.example.chemapp.data.repository.CompoundRepository;
import com.example.chemapp.data.repository.ElementRepository;
import com.example.chemapp.databinding.AddCompoundBinding;

import org.apache.commons.lang3.builder.ToStringBuilder;

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
        CompoundRepository compoundRepository = CompoundRepository.getInstance(getApplicationContext());
        ElementRepository elementRepository = ElementRepository.getInstance(getApplicationContext());
        binding.molecularFormula.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String inputText = s.toString();

                if(inputText.isEmpty()){
                    return;
                }

                String[] result = Compound.getElementsFromMolecularFormula1(inputText);

                if(result.length > 0 && result[0].startsWith("Error")){
                    binding.molecularFormula.setError(result[0]);
                    return ;
                }
                try{
                    Log.d("Molecular Weight", ""+elementRepository.getMolecularWeight(result));

                }catch (Exception e){
                    Toast.makeText(AddCompound.this, " "+e.toString(), Toast.LENGTH_SHORT).show();

                }


                binding.molecularFormula.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

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

                boolean isAdded = compoundRepository.addUserCompound(
                        compoundName, molecularFormula,
                        iupacName, mw, ew
                );

                if (isAdded) {
                    Toast.makeText(AddCompound.this, "Added new compound", Toast.LENGTH_SHORT).show();
                    resetInputs();
                } else {
                    Toast.makeText(AddCompound.this, "Compound already exists", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(AddCompound.this, "invalid inputs", Toast.LENGTH_SHORT).show();
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
