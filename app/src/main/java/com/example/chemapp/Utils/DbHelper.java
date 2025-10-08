package com.example.chemapp.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.nfc.TagLostException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.database.sqlite.SQLiteDatabaseKt;

import com.example.chemapp.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * BookMarks
 * Id : PrimaryKey
 * Title : String
 * type : String (Like of which Operation Type Like getCompoundMassForElement or PPm/PPb  or Molarity/Normality  or Dilution )
 *
 * For Types I can Declare A Enum For easy Operations
 *
 * Description : String Will Contain The remaining Details of Calculation
 *
 * History
 * Make It of Only 10 entries restricted
 *
 * Id : PrimaryKey
 *
 * Title : String
 * type : String (Like of which Operation Type Like getCompoundMassForElement or PPm/PPb  or Molarity/Normality  or Dilution )
 * For Types I can Declare A Enum For easy Operations
 *
 * Description : String Will Contain The remaining Details of Calculation
 *
 *
 *
 *
 * */
public class DbHelper extends SQLiteOpenHelper {
    private static DbHelper instance;
    public static final String DATABASE_NAME = "ChemApp.db";
    private static final int DATABASE_VERSION = 1;

    public static abstract class TableBookmarks {
        public static final String TABLE_NAME = "Bookmarks";
        public static final String COLUMN_BOOKMARK_ID = "BookmarkId";
        public static final String COLUMN_TITLE = "Title";
        public static final String COLUMN_TYPE = "Type";
        public static final String COLUMN_DESCRIPTION = "Description";
    }

    public static abstract class TableHistory {
        public static final String TABLE_NAME = "History";
        public static final String COLUMN_HISTORY_ID = "HistoryId";
        public static final String COLUMN_TITLE = "Title";
        public static final String COLUMN_TYPE = "Type";
        public static final String COLUMN_DESCRIPTION = "Description";
    }

    public static abstract class TableCompounds {
        public static final String TABLE_NAME = "Compounds";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_USERADDED = "userAdded";
        public static final String COLUMN_CAS = "cas";
        public static final String COLUMN_IUPAC_NAME = "iupacName";
        public static final String COLUMN_MOLECULAR_FORMULA = "molecularFormula";
        public static final String COLUMN_MOLECULAR_WEIGHT = "molecularWeight";
        public static final String COLUMN_EQUIVALENT_WEIGHT = "equivalentWeight";
        public static final String COLUMN_DISPLAY_NAME = "displayName";
        public static final String COLUMN_ELEMENTS_JSON = "elementsJson";
    }

    public static abstract class TableElements {
        public static final String TABLE_NAME = "Elements";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_MOLECULAR_WEIGHT = "molecularWeight";
        public static final String COLUMN_MOLECULAR_FORMULA = "molecularFormula";
    }

    private static final Gson gson = new Gson();
    private final Context context;


    public static DbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        }

        return instance;
    }

    private DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DbHelper", "IN ON CREATE");
        String bookmarksCreateQ = "CREATE TABLE " + TableBookmarks.TABLE_NAME + "("
                + TableBookmarks.COLUMN_BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TableBookmarks.COLUMN_TITLE + " TEXT, "
                + TableBookmarks.COLUMN_TYPE + " int, "
                + TableBookmarks.COLUMN_DESCRIPTION + " TEXT)";

        String historyCreateQ = "CREATE TABLE " + TableHistory.TABLE_NAME + "("
                + TableHistory.COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TableHistory.COLUMN_TITLE + " TEXT, "
                + TableHistory.COLUMN_TYPE + " int, "
                + TableHistory.COLUMN_DESCRIPTION + " TEXT)";

        String CompoundsCreateQ = "CREATE TABLE " + TableCompounds.TABLE_NAME + "("
                + TableCompounds.COLUMN_NAME + " TEXT PRIMARY KEY," // Compound name is the primary key
                + TableCompounds.COLUMN_CAS + " TEXT,"
                + TableCompounds.COLUMN_IUPAC_NAME + " TEXT,"
                + TableCompounds.COLUMN_MOLECULAR_FORMULA + " TEXT,"
                + TableCompounds.COLUMN_MOLECULAR_WEIGHT + " REAL,"
                + TableCompounds.COLUMN_EQUIVALENT_WEIGHT + " REAL,"
                + TableCompounds.COLUMN_USERADDED + " INTEGER DEFAULT 0 CHECK(" + TableCompounds.COLUMN_USERADDED + " IN  (0,1)),"
                + TableCompounds.COLUMN_DISPLAY_NAME + " TEXT,"
                + TableCompounds.COLUMN_ELEMENTS_JSON + " TEXT" + ")";

        String ElementsCreateQ = "CREATE TABLE " + TableElements.TABLE_NAME + "("
                + TableElements.COLUMN_NAME + " TEXT PRIMARY KEY," // Compound name is the primary key
                + TableElements.COLUMN_MOLECULAR_FORMULA + " TEXT,"
                + TableElements.COLUMN_MOLECULAR_WEIGHT + " REAL )";

        db.execSQL(CompoundsCreateQ);
        db.execSQL(bookmarksCreateQ);
        db.execSQL(historyCreateQ);
        db.execSQL(ElementsCreateQ);
        populateDb(db);

    }

    public void populateDb(SQLiteDatabase db) {
        CompoundLoader compoundLoader = new CompoundLoader();
        Map<String, Compound> compoundsMap = compoundLoader.loadCompoundAsMap(context.getApplicationContext(), R.raw.compound_data);
        ElementMapLoader elementMapLoader = new ElementMapLoader();
        Map<String, Element> elementsMap = elementMapLoader.loadElementAsMap(context.getApplicationContext(), R.raw.element_data);
        loadCompoundsMapToDb(db, compoundsMap);
        loadElementsMapToDb(db, elementsMap);
    }

    public boolean addUserCompound(Compound compound) {
        if (compound == null || compound.getName() == null || compound.getName().isEmpty()) {
            Log.e("DbHelper", "Cannot add null compound or compound with no name.");
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
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
            Log.d("DbHelper", "User compound added/updated: " + compound.getName());
            return true;
        } else {
            Log.d("DbHelper", "Error adding/updating user compound: " + compound.getName());
            return false;
        }
    }

    public List<Compound> getAllUserCompounds() {
        List<Compound> userCompoundsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Gson gson = new Gson();
        Type elementsListType = new TypeToken<String[]>() {
        }.getType();


        try {
            cursor = db.rawQuery("Select * from " + TableCompounds.TABLE_NAME + " where " + TableCompounds.COLUMN_USERADDED + " = 1", new String[]{});

            if (cursor != null && cursor.moveToFirst()) {
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
            Log.e("DbHelper", "Error getting all user compounds", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        }
        Log.d("DbHelper", "Retrieved " + userCompoundsList.size() + " user compounds.");
        return userCompoundsList;
    }

    public boolean deleteUserCompound(String compoundName) {
        if (compoundName == null || compoundName.isEmpty()) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TableCompounds.TABLE_NAME, TableCompounds.COLUMN_NAME + " = ?", new String[]{compoundName});
        Log.d("DbHelper", "Deleted " + rowsDeleted + " user compound(s) with name: " + compoundName);
        return rowsDeleted > 0;
    }

    private void loadCompoundsMapToDb(SQLiteDatabase db, Map<String, Compound> compoundMap) {
        Gson gson = new Gson();

        for (String key : compoundMap.keySet()) {
            StringBuilder builder = new StringBuilder();

            Compound compound = compoundMap.get(key);

            String formattedFormula = CalculatorUtil.formatChemicalFormula(compound.molecularFormula);
            builder.append(key).append(" ").append("(").append(formattedFormula).append(")");

            String displayString = builder.toString();

            ContentValues values = new ContentValues();

            values.put(TableCompounds.COLUMN_NAME, compound.getName());
            values.put(TableCompounds.COLUMN_CAS, compound.cas);
            values.put(TableCompounds.COLUMN_MOLECULAR_FORMULA, formattedFormula);
            values.put(TableCompounds.COLUMN_DISPLAY_NAME, displayString);
            values.put(TableCompounds.COLUMN_EQUIVALENT_WEIGHT, compound.equivalentWeight);
            values.put(TableCompounds.COLUMN_MOLECULAR_WEIGHT, compound.molecularWeight);
            values.put(TableCompounds.COLUMN_IUPAC_NAME, compound.iupacName);

            String elementsJson = gson.toJson(compound.elements);
            values.put(TableCompounds.COLUMN_ELEMENTS_JSON, elementsJson);

            long rowId = db.insert(TableCompounds.TABLE_NAME, null, values);
            if (rowId == -1) {
                Log.e("DbHelper", "ERROR IN INSERTING Data  : " + key);
            }
        }
    }

    private void loadElementsMapToDb(SQLiteDatabase db, Map<String, Element> elementMap) {

        for (String key : elementMap.keySet()) {
            Element element = elementMap.get(key);

            ContentValues values = new ContentValues();

            values.put(TableElements.COLUMN_NAME, element.name);
            values.put(TableElements.COLUMN_MOLECULAR_FORMULA, element.molecularFormula);
            values.put(TableElements.COLUMN_MOLECULAR_WEIGHT, element.getMolecularWeight());

            long rowId = db.insert(TableElements.TABLE_NAME, null, values);
            if (rowId == -1) {
                Log.d("DbHelper", "ERROR IN INSERTING Data  : " + key);
            }
        }
    }

    /*
        Done  Function for Inserting Data into DB
        Done Function for retriving Wieghts from Display name
        //////////Remaining////
     */

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TableCompounds.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TableElements.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TableBookmarks.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TableHistory.TABLE_NAME);

        onCreate(db);
    }
}
