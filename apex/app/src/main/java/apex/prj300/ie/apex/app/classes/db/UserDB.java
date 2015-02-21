package apex.prj300.ie.apex.app.classes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;

import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.models.Result;
import apex.prj300.ie.apex.app.classes.models.User;

import java.sql.Time;
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
    // Needs to be incremented every time modifications are made
    private static final int DATABASE_VERSION = 35;
    // database name
    private static final String DATABASE_NAME = "userDb";
    // user table name
    private static final String TABLE_USER = "userTbl";
    // table columns
    private static String KEY_ID = "id";
    private static String KEY_EMAIL = "email";
    private static String KEY_GRADE = "grade";
    private static String KEY_TOTAL_DISTANCE = "totalDistance";
    private static String KEY_TOTAL_TIME = "totalTime";
    private static String KEY_MAX_SPEED = "maxSpeed";
    private static String KEY_AVG_SPEED = "avgSpeed";

    // constructor
    public UserDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create tables
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // login table
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_GRADE + " TEXT,"
                + KEY_TOTAL_DISTANCE + " FLOAT,"
                + KEY_TOTAL_TIME + " TEXT,"
                + KEY_MAX_SPEED + " FLOAT,"
                + KEY_AVG_SPEED + " FLOAT" + ")";

        db.execSQL(CREATE_LOGIN_TABLE);
    }

    // upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        // Create tables again
        onCreate(db);
    }

    /**
     * Check login status
     */
    public int rowCount() {
        String countQuery = "SELECT * FROM " + TABLE_USER;
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
        values.put(KEY_ID, user.getId()); // 0
        values.put(KEY_EMAIL, user.getEmail()); // 1
        values.put(KEY_GRADE, String.valueOf(user.getGrade())); // 2
        values.put(KEY_TOTAL_DISTANCE, user.getTotalDistance()); // 3
        values.put(KEY_TOTAL_TIME, String.valueOf(user.getTotalTime())); // 4
        values.put(KEY_MAX_SPEED, user.getMaxSpeed()); // 5
        values.put(KEY_AVG_SPEED, user.getAvgSpeed()); // 6

        // insert row
        db.insert(TABLE_USER, null, values);
        db.close(); // close connection to database
    }

    /**
     * Update user's statistics
     * Called after a new route
     * is created/cycled.
     */
    public void updateUserStats(int id, User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TOTAL_DISTANCE, user.getTotalDistance());
        values.put(KEY_TOTAL_TIME, String.valueOf(user.getTotalTime()));
        values.put(KEY_MAX_SPEED, user.getMaxSpeed());
        values.put(KEY_AVG_SPEED, user.getAvgSpeed());

        db.update(TABLE_USER, values, "id =" + id, null);
        db.close();
    }


    /**
     * Get user data from database
     */
    public User getUser() {
        User user = new User();
        SQLiteDatabase db = this.getReadableDatabase();
        // select query
        String selectQuery = "SELECT * FROM " + TABLE_USER + " LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor != null) {
            cursor.moveToFirst();

            user = new User(cursor.getInt(0),
                    cursor.getString(1),
                    Grade.valueOf(cursor.getString(2)),
                    cursor.getFloat(3),
                    Long.valueOf(cursor.getString(4)),
                    cursor.getFloat(5), cursor.getFloat(6));

            cursor.close();
        }

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
        db.delete(TABLE_USER, null, null);
        db.close();
    }
}