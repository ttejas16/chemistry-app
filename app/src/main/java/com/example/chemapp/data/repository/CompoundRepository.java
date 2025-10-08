package com.example.chemapp.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.Utils.DbHelper.TableCompounds;

import java.util.ArrayList;

public class CompoundRepository {
    private final String tag = "CompoundRepository";
    private static CompoundRepository repository;
    private final DbHelper dbHelper;
    private CompoundRepository(Context context) {
        this.dbHelper = DbHelper.getInstance(context.getApplicationContext());
    }

    public static CompoundRepository getInstance(Context context) {
        if (repository == null) {
            repository = new CompoundRepository(context);
        }

        return repository;
    }

    public String[] getAllDisplayNames(){
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        ArrayList<String> displayNames = new ArrayList<>();

        Cursor cursor = null;

        String query = "Select "+ TableCompounds.COLUMN_DISPLAY_NAME + " from " + TableCompounds.TABLE_NAME +";";

        try{
            cursor = db.rawQuery(query,new String[]{});

            if(cursor.moveToFirst()){
                do{
                    displayNames.add(cursor.getString(cursor.getColumnIndexOrThrow(TableCompounds.COLUMN_DISPLAY_NAME)));
                }while (cursor.moveToNext());
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
}
