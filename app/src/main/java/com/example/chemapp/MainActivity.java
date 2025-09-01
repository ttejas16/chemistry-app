package com.example.chemapp;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
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




}