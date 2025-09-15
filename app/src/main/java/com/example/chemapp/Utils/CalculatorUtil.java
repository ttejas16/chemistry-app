package com.example.chemapp.Utils;

import android.content.Context;
import android.util.Log;

import com.example.chemapp.R;

import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CalculatorUtil {
    private static CalculatorUtil instance;
    private static Map<String, Compound> compoundsMap;
    private static Map<String, Element> elementsMap;
    private static final String[] SUBSCRIPT_DIGITS = {
            "₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"
    };
    private CalculatorUtil() {}

    public static void init(Context context){
        if (instance == null) {

            CompoundLoader compoundLoader = new CompoundLoader();
            compoundsMap = compoundLoader.loadCompoundAsMap(context.getApplicationContext(), R.raw.compound_data);
            loadAndMergeUserCompounds(context);
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

            String formulaWithSubscripts = formatChemicalFormula(compound.molecularFormula);
            builder.append(key).append(" ").append("(").append(formulaWithSubscripts).append(")");
            saltNames.add(builder.toString());
            builder.setLength(0);
        }
        return saltNames.toArray(new String[0]);
    }

    public static String formatChemicalFormula(String formula) {
        if (formula == null || formula.isEmpty()) {
            return formula;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = formula.toCharArray();

        for (char ch : chars) {
            if (Character.isDigit(ch)) {
                int digit = Character.getNumericValue(ch);
                result.append(SUBSCRIPT_DIGITS[digit]);
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }


    public boolean addNewUserCompound(String compoundName, double molecularWeight,double equivalentWeight, String molecularFormula,String iupacName,Context context) throws IllegalArgumentException{

        if(molecularFormula.isEmpty() || compoundName.isEmpty() || iupacName.isEmpty()){
            throw new IllegalArgumentException("addNewCompound : IllegalArgument provided");

        }
        String newCompoundName = compoundName + " (userdefined)";
        String[] elements = Compound.getElementsFromMolecularFormula(molecularFormula);
        Compound compound = new Compound(newCompoundName,"None",iupacName,molecularFormula,molecularWeight,equivalentWeight,elements);
        if(compoundsMap.containsKey(compoundName) || compoundsMap.containsKey(newCompoundName)) {
            Log.d("addNewCompound", "Compound Already Exists");
            return  false;
        }
        compoundsMap.put(compound.getName(),compound);
        DbHelper db = DbHelper.getInstance(context);
        return db.addUserCompound(compound);

    }

    public static void loadAndMergeUserCompounds(Context context) {
        DbHelper dbHelper = DbHelper.getInstance(context);
        List<Compound> userCompounds = dbHelper.getAllUserCompounds();

        if (compoundsMap == null) {
            compoundsMap = new HashMap<>();
        }

        if (userCompounds != null && !userCompounds.isEmpty()) {
            Log.d("CalculatorUtil", "Merging " + userCompounds.size() + " user compounds.");
            for (Compound userCompound : userCompounds) {
                // User's compound overrides default if names collide
                compoundsMap.put(userCompound.getName(), userCompound);
            }
        }
    }

    // the Input for the Compound name must be the Element of Compound Class i.e Comppound.getname();
    public void removeUserCompound(Context context, String compoundName) {
        if (compoundName == null || compoundName.isEmpty()) return;

        if(!compoundName.contains("(userdefined)")){

            Log.d("CalculatorUtil", "Trying to delete non-user-defined compound: " + compoundName);
            return ;
        }
        DbHelper dbHelper = DbHelper.getInstance(context);
        boolean success = dbHelper.deleteUserCompound(compoundName);

        if (success) {
            if (compoundsMap != null) {
                compoundsMap.remove(compoundName); // Update in-memory map
            }
            Log.d("CalculatorUtil", "User compound removed from persistence and in-memory map: " + compoundName);
        } else {
            Log.e("CalculatorUtil", "Failed to remove user compound from persistence: " + compoundName);
        }
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

    private double getNormalizedConcentration(double c, int unit){
        /*
        * 1 for PPM
        * 2 for PPB
        * 3 for PPT
        * 4 for M
        * 5 for mM
        * 6 for uM
        */
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
