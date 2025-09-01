package com.example.chemapp;

import androidx.annotation.NonNull;

public class Element {
    public final String name;
    public final String molecularFormula;
    public final Double molecularWeight;

    public Element( String name, String molecularFormula, Double molecularWeight){
        this.molecularFormula = molecularFormula;
        this.name = name;
        this.molecularWeight = molecularWeight;
    }

    public Double getMolecularWeight() {
        return molecularWeight;
    }

    @NonNull
    public String toString(){
        return "Name: " + name + "\nMolecular Formula: " + molecularFormula + "\nMolecular Weight: " + molecularWeight;
    }
}
