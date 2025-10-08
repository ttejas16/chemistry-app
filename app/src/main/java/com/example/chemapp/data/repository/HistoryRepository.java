package com.example.chemapp.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.chemapp.Utils.CalculationRecord;
import com.example.chemapp.Utils.DbHelper.TableHistory;
import com.example.chemapp.Utils.DbHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {
    private final String tag = "HistoryRepository";
    private static HistoryRepository repository;
    private final DbHelper dbHelper;

    private HistoryRepository(Context context) {
        this.dbHelper = DbHelper.getInstance(context.getApplicationContext());
    }

    public static HistoryRepository getInstance(Context context) {
        if (repository == null) {
            repository = new HistoryRepository(context);
        }

        return repository;
    }

    public boolean addHistory(String title, int type, String description) throws Exception {
        if (title == null || title.isEmpty() || description == null || description.isEmpty()) {
            throw new IllegalArgumentException("No Title or Description");
        }

        if (type < 1 || type > 4) {
            throw new IllegalArgumentException("Invalid Type");
        }

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        boolean success = false;

        ContentValues values = new ContentValues();
        values.put(TableHistory.COLUMN_TITLE, title);
        values.put(TableHistory.COLUMN_TYPE, type);
        values.put(TableHistory.COLUMN_DESCRIPTION, description);

        db.beginTransaction();
        try {
            long id = db.insert(TableHistory.TABLE_NAME, null, values);

            if (id == -1) return false;

            String trimQuery = "DELETE FROM " + TableHistory.TABLE_NAME + " WHERE " + TableHistory.COLUMN_HISTORY_ID +
                    " NOT IN  (" + "SELECT " + TableHistory.COLUMN_HISTORY_ID + " FROM " + TableHistory.TABLE_NAME + " ORDER BY " + TableHistory.COLUMN_HISTORY_ID + " DESC LIMIT 10)";
            db.execSQL(trimQuery);

            success = true;

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(tag, "Error while trying to add history to database", e);
        } finally {
            db.endTransaction();
        }

        return success;
    }

    public List<CalculationRecord> getHistoryCalculations() {
        Gson gson = new Gson();

        List<CalculationRecord> historyList = new ArrayList<>();
        Cursor cursor = null;

        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + TableHistory.TABLE_NAME + " ORDER BY " + TableHistory.COLUMN_HISTORY_ID + " DESC";

        try {
            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(TableHistory.COLUMN_HISTORY_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(TableHistory.COLUMN_TITLE));

                int type = cursor.getInt(cursor.getColumnIndexOrThrow(TableHistory.COLUMN_TYPE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(TableHistory.COLUMN_DESCRIPTION));

                String[][] tableData = gson.fromJson(description, String[][].class);
                historyList.add(new CalculationRecord(id, title, type, description, tableData));
            }
        } catch (Exception e) {
            Log.e(tag, "Error while trying to get history calculations from database", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return historyList;
    }

    public boolean clearHistory() {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        int deleteCount = db.delete(TableHistory.TABLE_NAME, "1", null);
        if (deleteCount > 0) {
            Log.d(tag, "History Deleted cnt " + deleteCount);
            return true;
        }

        Log.d(tag, "History Not Deleted");
        return false;
    }
}
