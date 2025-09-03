package com.example.chemapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.databinding.MeasureSolidBinding;

public class MeasureSolid extends AppCompatActivity {
    private MeasureSolidBinding binding;
    private final String[] concentrationUnits = {"ppm", "ppb", "ppt"};

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

        binding.navigation.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        CalculatorUtil util = CalculatorUtil.getInstance();
        String[] elements = util.getElementsMap().keySet().toArray(new String[0]);
        setSpinnerItems(binding.element, elements);

        setSpinnerItems(binding.concentrationUnit, concentrationUnits);

        binding.element.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedElement = parent.getItemAtPosition(position).toString();
                binding.salt.setText("");

                Element element = util.getElementsMap().get(selectedElement);
                String[] elementSalts = util.getSaltsOfElement(element);
                setSpinnerItems(binding.salt, elementSalts);
            }
        });

        binding.calculate.setOnClickListener(v -> {
            String element = binding.element.getText().toString();
            String salt = binding.salt.getText().toString();
            if (element.isEmpty() || salt.isEmpty()) {
                return;
            }

            if (!util.getElementsMap().containsKey(element) ||
                !util.getCompoundsMap().containsKey(salt)
            ) {
                return;
            }

            double concentration,volume;

            try {
                concentration = Double.parseDouble(binding.concentration.getText().toString());
                volume = Double.parseDouble(binding.volume.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(MeasureSolid.this, "Invalid inputs", Toast.LENGTH_SHORT).show();
                return;
            }


            double[] sizes = new double[]{volume};


            int concentrationUnit = binding.concentrationUnit.getSelectedItemPosition() + 1;


            String[][] data = new String[2][2];
            data[0][0] = "For Volume (mL)";
            data[0][1] = "Salt required (mg)";

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
                    data[i][1] = result + "";

                } catch (Exception e) {

                }

                i++;
            }

            BottomSheetHelper.showExpandableBottomSheet(MeasureSolid.this, R.layout.sheet_layout,getResultTitle(),data);
        });

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
}
