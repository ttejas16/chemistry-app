package com.example.chemapp.utils;

import android.content.Context;
import android.util.Log;

import com.example.chemapp.data.repository.CompoundRepository;
import com.example.chemapp.data.repository.ElementRepository;

public class CalculatorUtil {
    private static CalculatorUtil instance;
    private final ElementRepository elementRepository;
    private final CompoundRepository compoundRepository;

    private CalculatorUtil(Context context) {
        this.elementRepository = ElementRepository.getInstance(context.getApplicationContext());
        this.compoundRepository = CompoundRepository.getInstance(context.getApplicationContext());
    }

    public static CalculatorUtil getInstance(Context context) {
        if (instance == null) {
            instance = new CalculatorUtil(context);
        }

        return instance;
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

        Element e = elementRepository.getElement(targetElementName);
        Compound c = compoundRepository.getCompound(sourceCompoundName);
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
        Log.d("CalculatorUtil","Percentage of Element in compound" + percentage);
        result = calculateSoluteMassForPartsPerConcentration(concentration,volumeMl,partsPerUnitSelectorForTarget) / percentage;

        // Returns Required Compound Mass in MILLIGRAMS
        Log.d("CalculatorUtil",sourceCompoundName +" mass for Element "+ targetElementName
                +"  with concentration " + concentration +
                (partsPerUnitSelectorForTarget ==1 ? "ppm" : partsPerUnitSelectorForTarget ==2 ? "ppb" : "ppt")+" is " + result);
        return result;
    }

    public double getDilutionResultFrom(
            double stockConcentration, int stockConcentrationUnit,
            double reqConcentration, int reqConcentrationUnit,
            double volumeInMillilitres
    ) {
        // stockC * stockV = reqC * reqV;
        // stockV = (reqC * reqV) / stockC ;

        // concentration will be normalized so
        // always convert concentration values to ppm or M

        double normalizedStockConcentration = getNormalizedConcentration(stockConcentration, stockConcentrationUnit);
        double normalizedReqConcentration = getNormalizedConcentration(reqConcentration, reqConcentrationUnit);

        return (normalizedReqConcentration * volumeInMillilitres) / normalizedStockConcentration;
    }

    /**
     *
     * @param c
     * @param unit
     * 1 for PPM and M,
     * 2 for PPB and mM,
     * 3 for PPT and uM
     */
    private double getNormalizedConcentration(double c, int unit){
        switch (unit) {
            case 1:
                return c;

            case 2:
                return c / 1000;

            case 3:
                return c / 1000000;

            default:
                return c;
        }
    }
}
