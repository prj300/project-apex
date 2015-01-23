package apex.prj300.ie.apex.app.classes.db;

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
    private static final int DATABASE_VERSION = 10;
    // database name
    private static final String DATABASE_NAME = "apex";
    // user table name
    private static final String TABLE_USER = "user";
    // table columns
    private static String KEY_ID = "id";
    private static String KEY_EMAIL = "email";
    private static String KEY_GRADE = "grade";
    private static String KEY_EXPERIENCE = "experience";
    private static String KEY_TOTAL_DISTANCE = "totaldistance";
    private static String KEY_TOTAL_TIME = "totaltime";
    private static String KEY_TOTAL_CALORIES = "totalcalories";
    private static String KEY_MAX_SPEED = "maxspeed";
    private static String KEY_AVG_SPEED = "avgspeed";

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
                + KEY_EXPERIENCE + " INTEGER,"
                + KEY_TOTAL_DISTANCE + " FLOAT,"
                + KEY_TOTAL_TIME + " DATE,"
                + KEY_TOTAL_CALORIES + " INTEGER,"
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
        values.put(KEY_ID, user.getId()); // ID
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_GRADE, String.valueOf(user.getGrade()));
        values.put(KEY_EXPERIENCE, user.getExperience());
        values.put(KEY_TOTAL_DISTANCE, user.getTotalDistance());
        values.put(KEY_TOTAL_TIME, String.valueOf(user.getTotalTime()));
        values.put(KEY_TOTAL_CALORIES, user.getTotalCalories());
        values.put(KEY_MAX_SPEED, user.getMaxSpeed());
        values.put(KEY_AVG_SPEED, user.getAvgSpeed());

        // insert row
        db.insert(TABLE_USER, null, values);
        db.close(); // close connection to database
    }

    /**
     * Get user data from database
     */
    public List<User> getUser() {
        List<User> currentUser = new ArrayList<User>();
        // select query
        String selectQuery = "SELECT * FROM " + TABLE_USER + " LIMIT 1";

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
        db.delete(TABLE_USER, null, null);
        db.close();
    }
}