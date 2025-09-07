package com.example.chemapp.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Compound {
    private  String name;
    public final String cas;
    public final String iupacName;
    public final String molecularFormula;
    public final double molecularWeight;
    public final double equivalentWeight;
    public final String[] elements;


    public Compound(String name, String cas, String iupacName, String molecularFormula, double molecularWeight, double equivalentWeight, String[] elements){
    this.name = name;
    this.cas= cas;
    this.iupacName = iupacName;
    this.molecularFormula = molecularFormula;
    this.molecularWeight = molecularWeight;
    this.equivalentWeight = equivalentWeight;
    this.elements = elements;
    }
   public String getName(){
        return this.name;
   }

   public void setName(String name){
        this.name = name;
   }

    public boolean isElementPresent(String element){
        for(String e: elements){
            if(e.equalsIgnoreCase(element)){
                return true;
            }
        }
        return false;
    }
   public int getElementCount(String element){
        int count = 0;
        for(String e: elements){
            if(e.equalsIgnoreCase(element)){
                count++;
            }
        }
        return count;
   }
   public double getMolecularWeight(){
        return this.molecularWeight;
   }
    public String toString(){
        return "Name: " + this.name + "\n" +
                "CAS: " + this.cas + "\n" +
                "IUPAC Name: " + this.iupacName + "\n" +
                "Molecular Weight: " + this.molecularWeight + "\n" +
                "Equivalence Weight: " + this.equivalentWeight + "\n" +
                "Molecular Formula: "  + this.molecularFormula + "\n" +
                "Elements " + Arrays.toString(this.elements);
    }

    public static String[] getElementsFromMolecularFormula(String molecularFormula){
        if(molecularFormula == null || molecularFormula.isEmpty()){
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        Stack<Map<String, Integer>> stack = new Stack<>();
        Map<String,Integer> resultMap ;
        stack.push(new HashMap<>());
        int i=0;
        int n = molecularFormula.length();
        while (i < n) {
            char c = molecularFormula.charAt(i);

            if (Character.isUpperCase(c)) {
                int start = i++;
                while (i < n && Character.isLowerCase(molecularFormula.charAt(i))) {
                    i++;
                }
                String element = molecularFormula.substring(start, i);

                start = i;
                while (i < n && Character.isDigit(molecularFormula.charAt(i))) {
                    i++;
                }
                int count = start < i ? Integer.parseInt(molecularFormula.substring(start, i)) : 1;

                // Add the element and its count to the current map (top of the stack).
                Map<String, Integer> currentMap = stack.peek();
                currentMap.put(element, currentMap.getOrDefault(element, 0) + count);

            } else if (c == '(' || c == '[') {

                stack.push(new HashMap<>());
                i++;

            } else if (c == ')' || c == ']') {

                Map<String, Integer> completedGroup = stack.pop();
                i++;

                int start = i;
                while (i < n && Character.isDigit(molecularFormula.charAt(i))) {
                    i++;
                }
                int multiplier = start < i ? Integer.parseInt(molecularFormula.substring(start, i)) : 1;


                Map<String, Integer> parentMap = stack.peek();
                for (Map.Entry<String, Integer> entry : completedGroup.entrySet()) {
                    String element = entry.getKey();
                    int count = entry.getValue() * multiplier;
                    parentMap.put(element, parentMap.getOrDefault(element, 0) + count);
                }
            }
        }
        resultMap = stack.pop();

        for(Map.Entry<String,Integer> entry: resultMap.entrySet() ){
            String element = entry.getKey();
            int count  = entry.getValue();
            while(count > 0){
                result.add(element);
                count --;
            }
        }

        return result.toArray(new String[0]);

    }
}
