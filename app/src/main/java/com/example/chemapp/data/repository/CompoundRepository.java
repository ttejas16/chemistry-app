package com.example.chemapp.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.chemapp.utils.CalculatorUtil;
import com.example.chemapp.utils.Compound;
import com.example.chemapp.utils.DbHelper;
import com.example.chemapp.utils.DbHelper.TableCompounds;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompoundRepository {
    private final String tag = "CompoundRepository";
    private static CompoundRepository repository;
    private final DbHelper dbHelper;

    private final Gson gson = new Gson();

    private CompoundRepository(Context context) {
        this.dbHelper = DbHelper.getInstance(context.getApplicationContext());
    }

    public static CompoundRepository getInstance(Context context) {
        if (repository == null) {
            repository = new CompoundRepository(context);
        }

        return repository;
    }

    public String[] getAllDisplayNames() {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        ArrayList<String> displayNames = new ArrayList<>();

        Cursor cursor = null;

        String query = "Select " + TableCompounds.COLUMN_DISPLAY_NAME + " from " + TableCompounds.TABLE_NAME + ";";

        try {
            cursor = db.rawQuery(query, new String[]{});

            if (cursor.moveToFirst()) {
                do {
                    displayNames.add(cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_DISPLAY_NAME)));
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e(tag, "Error getting all  compounds DisplayName", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return displayNames.toArray(new String[0]);
    }

    public String[] getSaltsOfElement(String element) {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        ArrayList<String> result = new ArrayList<>();

        Cursor cursor = null;

        String query = "SELECT "
                + TableCompounds.COLUMN_DISPLAY_NAME + ","
                + TableCompounds.COLUMN_ELEMENTS_JSON + " FROM "
                + TableCompounds.TABLE_NAME;

        try {
            cursor = db.rawQuery(query, new String[]{});

            if (cursor.moveToFirst()) {
                do {
                    String displayName = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_DISPLAY_NAME));
                    String elementJson = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_ELEMENTS_JSON));

                    String[] elements = gson.fromJson(elementJson, String[].class);
                    Set<String> elementSet = new HashSet<>(Arrays.asList(elements));

                    if (elementSet.contains(element)) {
                        result.add(displayName);
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(tag, "error while fetching salts of:" + element);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return result.toArray(new String[0]);
    }

    public double[] getWeights(String name) throws Exception {
        Cursor cursor = null;
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Display Name cannot be null or empty");
        }
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();

        String query = "SELECT " + TableCompounds.COLUMN_MOLECULAR_WEIGHT + ", " + TableCompounds.COLUMN_EQUIVALENT_WEIGHT +
                " FROM " + TableCompounds.TABLE_NAME +
                " WHERE " + TableCompounds.COLUMN_DISPLAY_NAME + " = ? OR " + TableCompounds.COLUMN_NAME + " = ? LIMIT 1;";

        try {
            cursor = db.rawQuery(query, new String[]{name, name});

            if (cursor.moveToFirst()) {
                double mWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_MOLECULAR_WEIGHT));
                double eWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_EQUIVALENT_WEIGHT));
                return new double[]{mWeight, eWeight};
            } else {
                // No record found matching the name
                return null;
            }

        } catch (Exception e) {
            Log.e(tag, "Error getting weights for name: " + name, e);
            throw new Exception("Failed to retrieve weights for: " + name, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public Compound getCompound(String compoundName) throws Exception {
        if (compoundName == null || compoundName.isEmpty()) {
            throw new IllegalArgumentException();
        }

        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String query = "Select * from " + TableCompounds.TABLE_NAME +
                " where " + TableCompounds.COLUMN_NAME + " = ? LIMIT 1 ;";

        try (Cursor cursor = db.rawQuery(query, new String[]{compoundName})) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_NAME));
                String cas = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_CAS));
                String iupacName = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_IUPAC_NAME));
                String molecularFormula = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_MOLECULAR_FORMULA));

                double molecularWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_MOLECULAR_WEIGHT));
                double equivalentWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_EQUIVALENT_WEIGHT));

                String elementsJson = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_ELEMENTS_JSON));
                String[] elements = gson.fromJson(elementsJson, String[].class);

                return new Compound(name, cas, iupacName, molecularFormula, molecularWeight, equivalentWeight, elements);
            }
        } catch (Exception e) {
            Log.e(tag, "Error in fetching Compound");
        }

        return null;
    }

    public boolean addUserCompound(
            String compoundName,
            String molecularFormula,
            String iupacName,
            double molecularWeight,
            double equivalentWeight
    ) {
        if (compoundName.isEmpty() || molecularFormula.isEmpty() || iupacName.isEmpty()) {
            throw new IllegalArgumentException("invalid arguments");
        }

        String newCompoundName = compoundName + " (userdefined)";

        if (isCompoundPresent(newCompoundName)) {
            return false;
        }

        String[] elements = Compound.getElementsFromMolecularFormula(molecularFormula);
        Compound compound = new Compound(newCompoundName, "None", iupacName, molecularFormula, molecularWeight, equivalentWeight, elements);

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        StringBuilder builder = new StringBuilder();

        String formattedFormula = CalculatorUtil.formatChemicalFormula(compound.molecularFormula);

        builder.append(compound.getName()).append(" ").append("(").append(formattedFormula).append(")");
        String displayString = builder.toString();

        values.put(TableCompounds.COLUMN_NAME, compound.getName());
        values.put(TableCompounds.COLUMN_CAS, compound.cas);
        values.put(TableCompounds.COLUMN_IUPAC_NAME, compound.iupacName);
        values.put(TableCompounds.COLUMN_MOLECULAR_FORMULA, formattedFormula);
        values.put(TableCompounds.COLUMN_MOLECULAR_WEIGHT, compound.molecularWeight);
        values.put(TableCompounds.COLUMN_EQUIVALENT_WEIGHT, compound.equivalentWeight);
        values.put(TableCompounds.COLUMN_USERADDED, 1);
        values.put(TableCompounds.COLUMN_DISPLAY_NAME, displayString);

        Gson gson = new Gson();
        String elementsJson = gson.toJson(compound.elements);

        values.put(TableCompounds.COLUMN_ELEMENTS_JSON, elementsJson);

        // Use insertWithOnConflict to handle cases where compound name might already exist.
        long result = db.insertWithOnConflict(TableCompounds.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);

        if (result != -1) {
            Log.d(tag, "User compound added/updated: " + compound.getName());
            return true;
        }

        Log.d(tag, "Error adding/updating user compound: " + compound.getName());
        return false;
    }

    public boolean isCompoundPresent(String compoundName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = null;
        boolean isPresent = false;

        String selection = TableCompounds.COLUMN_NAME + " = ?";
        String[] selectionArgs = new String[]{compoundName};

        try {
            cursor = db.query(
                    TableCompounds.TABLE_NAME,
                    new String[]{TableCompounds.COLUMN_NAME},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null,
                    "1" // Limit to 1 result for efficiency
            );

            if (cursor.moveToFirst()) {
                // If moveToFirst returns true, a row was found, so it exists
                isPresent = true;
            }
        } catch (Exception e) {
            Log.e(tag, "compound already exists: " + compoundName, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return isPresent;
    }

    public List<Compound> getAllUserCompounds() {
        List<Compound> userCompoundsList = new ArrayList<>();
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();

        Cursor cursor = null;
        Gson gson = new Gson();

        Type elementsListType = new TypeToken<String[]>() {
        }.getType();

        try {
            cursor = db.rawQuery("SELECT * FROM " + TableCompounds.TABLE_NAME + " WHERE " + TableCompounds.COLUMN_USERADDED + " = 1", new String[]{});

            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_NAME));
                    String cas = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_CAS));
                    String iupacName = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_IUPAC_NAME));
                    String molecularFormula = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_MOLECULAR_FORMULA));

                    double molecularWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_MOLECULAR_WEIGHT));
                    double equivalentWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_EQUIVALENT_WEIGHT));

                    String elementsJson = cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_ELEMENTS_JSON));
                    String[] elements = gson.fromJson(elementsJson, elementsListType);

                    Compound compound = new Compound(name, cas, iupacName, molecularFormula, molecularWeight, equivalentWeight, elements);
                    userCompoundsList.add(compound);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(tag, "Error getting all user compounds", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        Log.d(tag, "Retrieved " + userCompoundsList.size() + " user compounds.");
        return userCompoundsList;
    }

    public boolean deleteUserCompound(String compoundName) {
        if (compoundName == null || compoundName.isEmpty()) {
            return false;
        }

        if (!compoundName.contains("(userdefined)")) {
            Log.d(tag, "Trying to delete non-user-defined compound: " + compoundName);
            return false;
        }

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        int rowsDeleted = db.delete(TableCompounds.TABLE_NAME, TableCompounds.COLUMN_NAME + " = ?", new String[]{compoundName});

        Log.d(tag, "Deleted " + rowsDeleted + " user compound(s) with name: " + compoundName);
        return rowsDeleted > 0;
    }
}
