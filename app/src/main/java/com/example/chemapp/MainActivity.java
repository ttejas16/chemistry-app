package com.example.chemapp;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chemapp.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    Map<String,String> molecularMap, equivalenceMap;
    private ActivityMainBinding binding;
    final String[] solutionOptions = { "Molar solution", "Normal solution" };
    final String[] molarityUnitOptions = { "Mole (M)", "milliMole (mM)", "microMole (μM)"};
    final double[] sizes = {25.0, 50.0, 100.0, 250.0, 500.0, 1000.0};
    private Map<String, Compound> compoundsMap;
    private Map<String, Element> elementsMap;

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
                Intent intent = new Intent(getApplicationContext(), MolarCalculator.class);
                startActivity(intent);
            }
        });

        /// TEMP CODE TO CHECK LOADING OF JSON IN MAP
        CompoundLoader compoundLoader = new CompoundLoader();
        compoundsMap = compoundLoader.loadCompoundAsMap(this, R.raw.compound_data);
        ElementMapLoader elementMapLoader = new ElementMapLoader();
        elementsMap = elementMapLoader.loadElementAsMap(this,R.raw.element_data);
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
        try{

            Log.d("Result","HELLOO"+calculateRequiredCompoundMassForElementConcentration("Ca","calcium hydrogen phosphate",20,500,1));
            Log.d("Result","HELLOO"+calculateRequiredCompoundMassForElementConcentration("Na","magnesium disodium edta",20,500,1));

        }catch (Exception e){
            Log.d("Exception", ""+e);
        }

    }


    public double calculateSoluteMassForPartsPerConcentration(double targetPartsPerValue, double solutionVolumeMl, int partsPerUnitSelector){
        /*concentration IS IN PPM
         volumeML IS the desired volume of a solution
        The O/P here will be the required Weight of A solute in a solutionIN MILLIGRAMS
         1000 MILLIGRAM = 1 GRAMS
        Selector value :  1 : ppm
                          2 : ppb
                          3 : ppt

         returns  required amount of Salt in MILLIGRAMS

         */
        if(targetPartsPerValue < 0 || solutionVolumeMl < 0 || partsPerUnitSelector < 0 || partsPerUnitSelector > 3){
           throw new IllegalArgumentException("Error in Arguments");
        }
        double unitConversionFactorToMg =  partsPerUnitSelector ==1 ? 1 : partsPerUnitSelector ==2 ? 1000 : 1000000;
        //CAN USE SWITCH FOR MORE READABILITY
        return (targetPartsPerValue * (solutionVolumeMl /1000)) / unitConversionFactorToMg;
    }

    public double calculateRequiredCompoundMassForElementConcentration(String targetElementName, String sourceCompoundName, double concentration, double volumeMl,int partsPerUnitSelectorForTarget) throws Exception{

        if( targetElementName == null || sourceCompoundName == null || concentration < 0 || volumeMl < 0){
            throw new IllegalArgumentException("Error msg");
        }
        Element e = elementsMap.get(targetElementName);
        Compound c = compoundsMap.get(sourceCompoundName);
        if(e == null || c == null){
            throw new Exception("Element or Compound not found");
        }
        if(!c.isElementPresent(targetElementName)){
            throw new Exception("Element not present in compound");
        }
        double percentage = 0;
        double result = 0;
        // eleCnt = eleCountInCompound(element,compound)  it will return no. of elements in compound;
        int elementCount = c.getElementCount(targetElementName);


        double elementMassInCompound = e.getMolecularWeight() * elementCount;
        double molarMassOfCompound = c.getMolecularWeight();
        percentage =  elementMassInCompound / molarMassOfCompound;
        result = calculateSoluteMassForPartsPerConcentration(concentration,volumeMl,partsPerUnitSelectorForTarget) / percentage;

        // Returns Required Compound Mass in MILLIGRAMS
        return result;
    }




}