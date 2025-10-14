package com.example.chemapp.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.Utils.DbHelper.TableElements;
import com.example.chemapp.Utils.Element;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ElementRepository {
    private final String tag = "ElementRepository";
    private static ElementRepository repository;
    private final DbHelper dbHelper;

    private String[] allElements;
    private ElementRepository(Context context) {
        this.dbHelper = DbHelper.getInstance(context.getApplicationContext());
    }

    public static ElementRepository getInstance(Context context) {
        if (repository == null) {
            repository = new ElementRepository(context);
        }

        return repository;
    }

    public String[] getAllElements(){
        if(allElements != null){
            Log.d("ElementRepository", "Returning cached elements");
            return allElements;
        }
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        ArrayList<String> result = new ArrayList<>();

        String query = "SELECT " + TableElements.COLUMN_NAME + " FROM " + TableElements.TABLE_NAME + ";";

        try (Cursor cursor = db.rawQuery(query, new String[]{})) {

            if (cursor.moveToFirst()) {
                do {
                    int columnIndex = cursor.getColumnIndexOrThrow(TableElements.COLUMN_NAME);
                    result.add(cursor.getString(columnIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(tag, "error in fetching all elements", e);
        }
        allElements = result.toArray(new String[0]);
        Log.d("ElementRepository", "Returning DB fetched elements");
        return allElements;
    }

    public double getMolecularWeightOfCompound(String[] elementsArray) throws Exception{
        SQLiteDatabase db  = this.dbHelper.getReadableDatabase();
        double result = 0;
        String query = "SELECT " + TableElements.COLUMN_MOLECULAR_WEIGHT + " FROM " + TableElements.TABLE_NAME +
                " WHERE " + TableElements.COLUMN_NAME + " = ?;";
        Map<String,Double> elementWeightMap = new HashMap<>();
        for(String element : elementsArray) {

            if(!element.isEmpty()  && elementWeightMap.containsKey(element)){
                result += elementWeightMap.get(element);

            }
            else{

                try (Cursor cursor = db.rawQuery(query, new String[]{element})) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(TableElements.COLUMN_MOLECULAR_WEIGHT);
                        elementWeightMap.put(element, cursor.getDouble(columnIndex));
                        result += cursor.getDouble(columnIndex);
                    }

                } catch (Exception e) {
                    Log.d(tag, "error in fetching molecular weight", e);
                }
            }
        }

        return result;

    }

    public Element getElement(String elementName) throws Exception{
        if(elementName.isEmpty() || elementName == null){
            throw new IllegalArgumentException();
        }
        SQLiteDatabase db  = this.dbHelper.getReadableDatabase();
        String query = "Select * from "+ TableElements.TABLE_NAME +
                " where "+ TableElements.COLUMN_NAME +" = ? LIMIT 1 ;";

        try(Cursor cursor = db.rawQuery(query, new String[]{elementName}))
        {
            if(cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndexOrThrow(TableElements.COLUMN_NAME);
                int molecularWeightIndex = cursor.getColumnIndexOrThrow(TableElements.COLUMN_MOLECULAR_WEIGHT);
                int molecularFormulaIndex = cursor.getColumnIndexOrThrow(TableElements.COLUMN_MOLECULAR_FORMULA);
                String name  = cursor.getString(nameIndex);
                String molecularFormula = cursor.getString(molecularFormulaIndex);
                Double molecularWeight = cursor.getDouble(molecularWeightIndex);
                return new Element(name,molecularFormula,molecularWeight);
            }
        }catch (Exception e){
            Log.d(tag, "error in fetching element", e);
        }
        return null;
    }

}
