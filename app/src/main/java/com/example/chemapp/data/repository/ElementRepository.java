package com.example.chemapp.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.chemapp.Utils.DbHelper;
import com.example.chemapp.Utils.DbHelper.TableElements;

import java.util.ArrayList;

public class ElementRepository {
    private final String tag = "ElementRepository";
    private static ElementRepository repository;
    private final DbHelper dbHelper;
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

        return result.toArray(new String[0]);
    }
}
