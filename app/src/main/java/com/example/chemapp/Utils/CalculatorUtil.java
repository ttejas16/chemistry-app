package com.example.chemapp.Utils;

import android.content.Context;

import com.example.chemapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CalculatorUtil {
    private static CalculatorUtil instance;
    private static Map<String, Compound> compoundsMap;
    private static Map<String, Element> elementsMap;

    private CalculatorUtil() {}

    public static void init(Context context){
        if (instance == null) {

            CompoundLoader compoundLoader = new CompoundLoader();
            compoundsMap = compoundLoader.loadCompoundAsMap(context.getApplicationContext(), R.raw.compound_data);

            ElementMapLoader elementMapLoader = new ElementMapLoader();
            elementsMap = elementMapLoader.loadElementAsMap(context.getApplicationContext(), R.raw.element_data);

            instance = new CalculatorUtil();
        }
    }
    public static CalculatorUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CalculatorUtil class not initialized");
        }

        return instance;
    }

    public Map<String, Compound> getCompoundsMap(){
        return compoundsMap;
    }

    public Map<String, Element> getElementsMap(){
        return elementsMap;
    }

    public String[] getSaltsOfElement(Element e){
        ArrayList<String> salts = new ArrayList<>();

        for(String salt:compoundsMap.keySet()) {
            Compound compound = compoundsMap.get(salt);

            // stream api functional programming bro
            Set<String> uniqueElements  = Arrays.stream(compound.elements).map(String::toLowerCase).collect(Collectors.toSet());

            if (uniqueElements.contains(e.name.toLowerCase())) {
                salts.add(salt);
            }
        }

        return salts.toArray(new String[0]);
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


    public String[] getFormattedDisplayName(){
        String[] Keys  = compoundsMap.keySet().toArray(new String[0]);
        return formatCompoundNameFromKeys(Keys);
    }

    public String[] getFormattedDisplayName(Element e){
        String[] Keys = getSaltsOfElement(e);
        return formatCompoundNameFromKeys(Keys);
    }

    private String[] formatCompoundNameFromKeys(String[] Keys){
        if (Keys == null || compoundsMap == null) {
            return new String[0];
        }
        ArrayList<String> saltNames = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (String key : Keys){
            Compound compound = compoundsMap.get(key);
            if(compound == null) continue;
            builder.append(key).append(" ").append("(").append(compound.molecularFormula).append(")");
            saltNames.add(builder.toString());
            builder.setLength(0);
        }
        return saltNames.toArray(new String[0]);
    }
}
