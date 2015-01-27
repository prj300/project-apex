package apex.prj300.ie.apex.app.classes.methods;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import apex.prj300.ie.apex.app.classes.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Enda on 04/01/2015.
 */
public class UserDB extends SQLiteOpenHelper {

    /**
     * Static variables
     */
    // database version
    private static final int DATABASE_VERSION = 8;
    // database name
    private static final String DATABASE_NAME = "apex";
    // login table name
    private static final String TABLE_LOGIN = "login";

    //
    // login table columns
    private String KEY_ID = "id";

    // constructor
    public UserDB(Context context) {
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
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, user.getId()); // ID

        // insert row
        db.insert(TABLE_LOGIN, null, values);
        db.close(); // close connection to database
    }

    /**
     * Get user data from database
     */
    public List<User> getUser() {
        List<User> currentUser = new ArrayList<User>();
        // select query
        String selectQuery = "SELECT id FROM " + TABLE_LOGIN + " LIMIT 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // loop through rows and add to list
        if(cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(Integer.parseInt(cursor.getString(0)));
                currentUser.add(user);
            } while (cursor.moveToNext());
        }

        // return user
        return currentUser;
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