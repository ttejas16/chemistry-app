package com.example.chemapp;

import com.opencsv.CSVReaderHeaderAware;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CSVUtils {
    public static void getMaps(
            InputStream inputStream,
            Map<String,String> molecularMap,
            Map<String,String> equivalenceMap
    ){
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new InputStreamReader(inputStream))) {
            Map<String, String> row;
            while ((row = reader.readMap()) != null) {
                String saltName = row.get("Salt Name");
                String molecularWeight = row.get("Molecular Weight (g/mol)");
                String equivalenceWeight = row.get("Equivalent Weight (g/eq)");

                molecularMap.put(saltName, molecularWeight);
                equivalenceMap.put(saltName, equivalenceWeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
