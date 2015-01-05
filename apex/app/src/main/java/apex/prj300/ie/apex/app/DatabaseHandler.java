package apex.prj300.ie.apex.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by Enda on 04/01/2015.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    /**
     * Static variables
     */
    // database version
    private static final int DATABASE_VERSION = 3;
    // database name
    private static final String DATABASE_NAME = "apex";
    // login table name
    private static final String TABLE_LOGIN = "login";

    //
    // login table columns
    private String KEY_ID = "id";

    // constructor
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // creating tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // login table
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    // upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        // Create tables again
        onCreate(db);
    }

    /**
     * Check login status
     */
    public int rowCount() {
        String countQuery = "SELECT * FROM " + TABLE_LOGIN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();
        // return row count
        return rowCount;
    }


    /**
     * Store user in database
     */
    public void addUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);

        // insert row
        db.insert(TABLE_LOGIN, null, values);
        db.close(); // close connection to database
    }

    /**
     * Get user data from database
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        // (there should be only one row anyway, we are just being explicit)
        cursor.moveToFirst();
        if(cursor.getCount() > 0) { // user exists
            user.put("id", cursor.getString(1));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }

    /**
     * Reset tables
     */
    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }
}