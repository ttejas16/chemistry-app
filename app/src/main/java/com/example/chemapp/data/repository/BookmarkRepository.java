package com.example.chemapp.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.chemapp.utils.CalculationRecord;
import com.example.chemapp.utils.DbHelper;
import com.example.chemapp.utils.DbHelper.TableBookmarks;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class BookmarkRepository {
    private final String tag = "BookmarkRepository";
    private static BookmarkRepository repository;
    private final DbHelper dbHelper;

    private BookmarkRepository(Context context) {
        this.dbHelper = DbHelper.getInstance(context.getApplicationContext());
    }

    public static BookmarkRepository getInstance(Context context) {
        if (repository == null) {
            repository = new BookmarkRepository(context);
        }

        return repository;
    }

    public boolean addBookmark(String title, int type, String description) throws Exception {
        if (title == null || title.isEmpty() || description == null || description.isEmpty()) {
            throw new IllegalArgumentException("No Title or Description");
        }

        if (type < 1 || type > 4) {
            throw new IllegalArgumentException("Invalid Type");
        }

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();

            values.put(TableBookmarks.COLUMN_TITLE, title);
            values.put(TableBookmarks.COLUMN_TYPE, type);
            values.put(TableBookmarks.COLUMN_DESCRIPTION, description);

            success = db.insert(TableBookmarks.TABLE_NAME, null, values) != -1;
        } catch (Exception e) {
            Log.d(tag, "Error while trying to add bookmark to database", e);
        }

        return success;
    }

    public List<CalculationRecord> getBookmarks() {
        Gson gson = new Gson();
        List<CalculationRecord> bookmarkList = new ArrayList<>();
        Cursor cursor = null;

        SQLiteDatabase db = this.dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + TableBookmarks.TABLE_NAME + " ORDER BY " + TableBookmarks.COLUMN_BOOKMARK_ID + " DESC";

        try {
            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(TableBookmarks.COLUMN_BOOKMARK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(TableBookmarks.COLUMN_TITLE));

                int type = cursor.getInt(cursor.getColumnIndexOrThrow(TableBookmarks.COLUMN_TYPE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(TableBookmarks.COLUMN_DESCRIPTION));

                String[][] tableData = gson.fromJson(description, String[][].class);

                bookmarkList.add(new CalculationRecord(id, title, type, description, tableData));
            }
        } catch (Exception e) {
            Log.e(tag, "Error while trying to get bookmarks from database", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return bookmarkList;
    }

    public boolean deleteBookmark(String id) {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        int deleteCount = db.delete(
                TableBookmarks.TABLE_NAME, TableBookmarks.COLUMN_BOOKMARK_ID + " = ?",
                new String[]{id}
        );

        if (deleteCount > 0) {
            Log.d(tag, "Bookmark Deleted cnt " + deleteCount);
            return true;
        }

        Log.d(tag, "Bookmark Not Deleted");
        return false;
    }
}
