package com.example.chemapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.databinding.ActivityMainBinding;

import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MeasureMolarity.class);
                startActivity(intent);
            }
        });

        binding.btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MeasureMass.class);
                startActivity(intent);
            }
        });

        binding.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MeasureSolid.class);
                startActivity(intent);
            }
        });
        /// TEMP CODE TO CHECK LOADING OF JSON IN MAP
//        CompoundLoader compoundLoader = new CompoundLoader();
//        compoundsMap = compoundLoader.loadCompoundAsMap(this, R.raw.compound_data);
//        ElementMapLoader elementMapLoader = new ElementMapLoader();
//        elementsMap = elementMapLoader.loadElementAsMap(this,R.raw.element_data);
        /*
        for(Map.Entry<String,Compound> entry: compoundsMap.entrySet()){
            String compoundName = entry.getKey();
            Compound compoundData = entry.getValue();

            Log.d("Compound data ",compoundData.toString());
        }
        int count =0;
        for(Map.Entry<String,Element> entry: elementsMap.entrySet()){
            String elementName = entry.getKey();
            Element elementData = entry.getValue();
            Log.d("Element data  ","No: "+ ++count + " "+elementData.toString());
        }
        */
        //TESTING THE FUNCTIONALITY OF THE PP CALCULATOR AND ELEMENT WISE CALCULATOR FUNCTIONALITY
//        try{
//
//            Log.d("Result","HELLOO"+calculateRequiredCompoundMassForElementConcentration("Ca","calcium hydrogen phosphate",20,500,1));
//            Log.d("Result","HELLOO"+calculateRequiredCompoundMassForElementConcentration("Na","magnesium disodium edta",20,500,1));
//
//        }catch (Exception e){
//            Log.d("Exception", ""+e);
//        }


        CalculatorUtil.init(this);
    }
}