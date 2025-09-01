package com.example.chemapp;

import java.util.Arrays;

public class Compound {
    private  String name;
    public final String cas;
    public final String iupacName;
    public final double molecularWeight;
    public final double equivalentWeight;
    public final String[] elements;


    public Compound(String name, String cas, String iupacName, double molecularWeight, double equivalentWeight, String[] elements){
    this.name = name;
    this.cas= cas;
    this.iupacName = iupacName;
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
                "Elements " + Arrays.toString(this.elements);
    }
}
