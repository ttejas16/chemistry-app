package com.example.chemapp.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
    private static DbHelper instance ;
    public static final String DATABASE_NAME = "ChemApp.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BOOKMARKS = "Bookmarks";
    public static final String COLUMN_BOOKMARK_ID = "BookmarkId";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String TABLE_HISTORY = "History";
    public static final String COLUMN_HISTORY_ID = "HistoryId";

    public static final String TABLE_COMPOUNDS = "Compounds";
    public static final String COLUMN_C_NAME = "name";
    public static  final  String COLUMN_C_USERADDED = "userAdded";
    public static final String COLUMN_C_CAS = "cas";
    public static final String COLUMN_C_IUPAC_NAME = "iupacName";
    public static final String COLUMN_C_MOLECULAR_FORMULA = "molecularFormula";
    public static final String COLUMN_C_MOLECULAR_WEIGHT = "molecularWeight";
    public static final String COLUMN_C_EQUIVALENT_WEIGHT = "equivalentWeight";
    public static final String COLUMN_C_DISPLAY_NAME = "displayName";
    public static final String COLUMN_C_ELEMENTS_JSON = "elementsJson";

    public static final String TABLE_ELEMENTS = "Elements";
    public static final String COLUMN_E_NAME = "name";
    public static final String COLUMN_E_MOLECULAR_WEIGHT = "molecularWeight";
    public static final String COLUMN_E_MOLECULAR_FORMULA = "molecularFormula";
    private static final Gson gson = new Gson();



    public static DbHelper getInstance(Context context){
        if(instance == null){
            instance = new DbHelper(context.getApplicationContext(),DATABASE_NAME,null,DATABASE_VERSION);
        }
        return instance;
    }


    private DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String bookmarksCreateQ = "CREATE TABLE " + TABLE_BOOKMARKS + "(" + COLUMN_BOOKMARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TITLE + " TEXT, " + COLUMN_TYPE + " int, " + COLUMN_DESCRIPTION + " TEXT)";
        String historyCreateQ = "CREATE TABLE " + TABLE_HISTORY + "(" + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TITLE + " TEXT, " + COLUMN_TYPE + " int, " + COLUMN_DESCRIPTION + " TEXT)";
        String CompoundsCreateQ = "CREATE TABLE " + TABLE_COMPOUNDS + "("
                + COLUMN_C_NAME + " TEXT PRIMARY KEY," // Compound name is the primary key
                + COLUMN_C_CAS + " TEXT,"
                + COLUMN_C_IUPAC_NAME + " TEXT,"
                + COLUMN_C_MOLECULAR_FORMULA + " TEXT,"
                + COLUMN_C_MOLECULAR_WEIGHT + " REAL,"
                + COLUMN_C_EQUIVALENT_WEIGHT + " REAL,"
                + COLUMN_C_USERADDED + " INTEGER DEFAULT 0 CHECK(" + COLUMN_C_USERADDED +" IN  (0,1)),"
                + COLUMN_C_DISPLAY_NAME + " TEXT,"
                + COLUMN_C_ELEMENTS_JSON + " TEXT" + ")";

        String ElementsCreateQ = "CREATE TABLE " + TABLE_ELEMENTS + "("
                + COLUMN_E_NAME + " TEXT PRIMARY KEY," // Compound name is the primary key
                + COLUMN_E_MOLECULAR_FORMULA + " TEXT,"
                + COLUMN_E_MOLECULAR_WEIGHT + " REAL )";
        db.execSQL(CompoundsCreateQ);
        db.execSQL(bookmarksCreateQ);
        db.execSQL(historyCreateQ);
        db.execSQL(ElementsCreateQ);

        CalculatorUtil util = CalculatorUtil.getInstance();

        Map<String,Compound> compoundsMap = util.getCompoundsMap();
        loadCompoundsMapToDb(compoundsMap);
        Map<String,Element> elementMap = util.getElementsMap();
        loadElementsMapToDb(elementMap);
    }
    public boolean addHistory(String title, int type, String description) throws  Exception {
        if(title == null || title.isEmpty() || description == null || description.isEmpty()  ){
            throw new IllegalArgumentException("No Title or Description");
        }

        if(type < 1 || type > 4){
            throw new IllegalArgumentException("Invalid Type");
        }

        SQLiteDatabase db  = this.getWritableDatabase();
        boolean success = false;

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE,title);
        values.put(COLUMN_TYPE,type);
        values.put(COLUMN_DESCRIPTION,description);

        db.beginTransaction();

        try{
            long id = db.insert(TABLE_HISTORY, null, values);
//            Log.d("inserted id", id + "");

            if (id == -1) return false;

            String trimQuery = "DELETE FROM " + TABLE_HISTORY + " WHERE " + COLUMN_HISTORY_ID +
                    " NOT IN  (SELECT "+ COLUMN_HISTORY_ID + " FROM " + TABLE_HISTORY + " ORDER BY " + COLUMN_HISTORY_ID + " DESC LIMIT 10)";
            db.execSQL(trimQuery);
            success = true;
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.d("DbHelper", "Error while trying to add history to database", e);
        }finally {
            db.endTransaction();
        }

        return success;
    }

    public List<CalculationRecord> getHistoryCalculations(){
        List<CalculationRecord> historyList = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + COLUMN_HISTORY_ID + " DESC";
       try{
           cursor = db.rawQuery(query,null);
            while(cursor.moveToNext()){
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));

                int type = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String[][] tableData = gson.fromJson(description, String[][].class);

                historyList.add(new CalculationRecord(id, title, type, description, tableData));
            }
       } catch (Exception e) {
           Log.e("DbHelper", "Error while trying to get history calculations from database", e);
       }finally {
           if (cursor != null) cursor.close();

       }

//       Log.d("history list", historyList.toString());
       return historyList;
    }
    public boolean clearHistory(){
        SQLiteDatabase db = this.getWritableDatabase();

        int delCnt = db.delete(TABLE_HISTORY,"1",null);
        if(delCnt > 0) {
            Log.d("Database Operation", "History Deleted cnt " + delCnt);
            return true;
        }
        Log.d("Database Operation", "History Not Deleted");
        return false;
    }
    public boolean addBookmark (String title, int type, String description)throws Exception {
        if(title == null || title.isEmpty() || description == null || description.isEmpty()  ){
            throw new IllegalArgumentException("No Title or Description");
        }
        if(type < 1 || type > 4){
            throw new IllegalArgumentException("Invalid Type");
        }
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try{
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_TYPE, type);
            values.put(COLUMN_DESCRIPTION, description);
            success = db.insert(TABLE_BOOKMARKS, null, values) != -1;
        } catch (Exception e) {
            Log.d("DbHelper", "Error while trying to add bookmark to database", e);
        }
        return success;

    }

    public List<CalculationRecord> getBookmarks(){
        List<CalculationRecord> bookmarkList = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_BOOKMARKS + " ORDER BY " + COLUMN_BOOKMARK_ID + " DESC";
        try{
            cursor = db.rawQuery(query,null);
            while(cursor.moveToNext()){
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOKMARK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));

                String[][] tableData = gson.fromJson(description, String[][].class);

                bookmarkList.add(new CalculationRecord(id, title, type, description, tableData));
            }
        } catch (Exception e){
            Log.e("DbHelper", "Error while trying to get bookmarks from database", e);
        }finally {
            if (cursor != null) cursor.close();
        }
        return bookmarkList;
    }

    public boolean deleteBookmark(String id){
        SQLiteDatabase db = this.getWritableDatabase();

        int delCnt =  db.delete(TABLE_BOOKMARKS, COLUMN_BOOKMARK_ID + " = ?", new String[]{id});
        if(delCnt > 0) {
            Log.d("Database Operation", "Bookmark Deleted cnt " + delCnt);
            return true;
        }
        Log.d("Database Operation", "Bookmark Not Deleted");
        return false;
    }

    public boolean addUserCompound(Compound compound) {
        if (compound == null || compound.getName() == null || compound.getName().isEmpty()) {
            Log.e("DbHelper", "Cannot add null compound or compound with no name.");
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_C_NAME, compound.getName());
        values.put(COLUMN_C_CAS, compound.cas);
        values.put(COLUMN_C_IUPAC_NAME, compound.iupacName);
        values.put(COLUMN_C_MOLECULAR_FORMULA, compound.molecularFormula);
        values.put(COLUMN_C_MOLECULAR_WEIGHT, compound.molecularWeight);
        values.put(COLUMN_C_EQUIVALENT_WEIGHT, compound.equivalentWeight);
        values.put(COLUMN_C_USERADDED,1);

        Gson gson = new Gson();
        String elementsJson = gson.toJson(compound.elements);
        values.put(COLUMN_C_ELEMENTS_JSON, elementsJson);

        // Use insertWithOnConflict to handle cases where compound name might already exist.
        long result = db.insertWithOnConflict(TABLE_USER_COMPOUNDS, null, values, SQLiteDatabase.CONFLICT_ABORT);

        if (result != -1) {
            Log.e("DbHelper", "User compound added/updated: " + compound.getName());
            return true;
        } else {
            Log.e("DbHelper", "Error adding/updating user compound: " + compound.getName());
            return false;
        }
    }

    public List<Compound> getAllUserCompounds() {
        List<Compound> userCompoundsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Gson gson = new Gson();
        Type elementsListType = new TypeToken<String[]>(){}.getType();

        String[] projection = {
                COLUMN_C_NAME,
                COLUMN_C_CAS,
                COLUMN_C_IUPAC_NAME,
                COLUMN_C_MOLECULAR_FORMULA,
                COLUMN_C_MOLECULAR_WEIGHT,
                COLUMN_C_EQUIVALENT_WEIGHT,
                COLUMN_C_ELEMENTS_JSON
        };

        try {
            cursor = db.rawQuery("Select * from "+ TABLE_COMPOUNDS + " where "+ COLUMN_C_USERADDED + " = 1",new String[]{});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_C_NAME));
                    String cas = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_C_CAS));
                    String iupacName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_C_IUPAC_NAME));
                    String molecularFormula = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_C_MOLECULAR_FORMULA));
                    double molecularWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_C_MOLECULAR_WEIGHT));
                    double equivalentWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_C_EQUIVALENT_WEIGHT));
                    String elementsJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_C_ELEMENTS_JSON));

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
        int rowsDeleted = db.delete(TABLE_COMPOUNDS, COLUMN_C_NAME + " = ?", new String[]{compoundName});
        Log.d("DbHelper", "Deleted " + rowsDeleted + " user compound(s) with name: " + compoundName);
        return rowsDeleted > 0;
    }

   private void loadCompoundsMapToDb(Map<String, Compound> compoundMap){
       SQLiteDatabase db  =  this.getWritableDatabase();
       for(String key : compoundMap.keySet()) {

            StringBuilder builder = new StringBuilder();
            Compound compound = compoundMap.get(key);
            String formattedFormula = CalculatorUtil.formatChemicalFormula(compound.molecularFormula);
            builder.append(key).append(" ").append("(").append(formattedFormula).append(")");
            String displayString = builder.toString();
            ContentValues values = new ContentValues();
            values.put(COLUMN_C_NAME,compound.getName());
            values.put(COLUMN_C_CAS,compound.cas);
            values.put(COLUMN_C_MOLECULAR_FORMULA,formattedFormula);
            values.put(COLUMN_C_DISPLAY_NAME,displayString);
            values.put(COLUMN_C_EQUIVALENT_WEIGHT,compound.equivalentWeight);
            values.put(COLUMN_C_MOLECULAR_WEIGHT,compound.molecularFormula);
            values.put(COLUMN_C_IUPAC_NAME,compound.iupacName);
            Gson gson = new Gson();
            String elementsJson = gson.toJson(compound.elements);
            values.put(COLUMN_C_ELEMENTS_JSON, elementsJson);
            long rowId = db.insert(TABLE_COMPOUNDS,null,values);
            if(rowId == -1){
                Log.e("DbHelper", "ERROR IN INSERTING Data  : " + key );
            }
       }
   }

   private void loadElementsMapToDb(Map<String,Element> elementMap){
        SQLiteDatabase db = this.getWritableDatabase();
        for(String key : elementMap.keySet()) {

           StringBuilder builder = new StringBuilder();
           Element element = elementMap.get(key);

           ContentValues values = new ContentValues();
           values.put(COLUMN_E_NAME,element.name);
           values.put(COLUMN_E_MOLECULAR_FORMULA,element.molecularFormula);
           values.put(COLUMN_E_MOLECULAR_WEIGHT,element.getMolecularWeight());

           long rowId = db.insert(TABLE_ELEMENTS,null,values);
           if(rowId == -1){
               Log.e("DbHelper", "ERROR IN INSERTING Data  : " + key );
           }
        }
   }
   public String[] getDisplayName(){
       SQLiteDatabase db = getReadableDatabase();
       ArrayList <String> displayNames = new ArrayList<>();
       Cursor cursor = null;
       String query = "Select "+ COLUMN_C_DISPLAY_NAME + " from " + TABLE_COMPOUNDS +" ;";
       try{
           cursor = db.rawQuery(query,new String[]{});

           if(cursor != null && cursor.moveToFirst()){
               do{
                   displayNames.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_C_DISPLAY_NAME)));
               }while (cursor.moveToNext());
           }

       } catch (Exception e) {
           Log.e("DbHelper", "Error getting all  compounds DisplayName", e);
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
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_C_MOLECULAR_WEIGHT + ", " + COLUMN_C_EQUIVALENT_WEIGHT +
                " FROM " + TABLE_COMPOUNDS +
                " WHERE " + COLUMN_C_DISPLAY_NAME + " = ? OR " + COLUMN_C_NAME + " = ? LIMIT 1;";

        try {
            cursor = db.rawQuery(query, new String[]{name, name});

            if (cursor != null && cursor.moveToFirst()) {


                double mWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_C_MOLECULAR_WEIGHT));
                double eWeight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_C_EQUIVALENT_WEIGHT));
                return new double[]{mWeight, eWeight};
            } else {
                // No record found matching the name
                return null;
            }

        } catch (Exception e) {
            Log.e("DbHelper", "Error getting weights for name: " + name, e);
            throw new Exception("Failed to retrieve weights for: " + name, e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
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

    }
}
