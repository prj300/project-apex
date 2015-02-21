package apex.prj300.ie.apex.app.classes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import apex.prj300.ie.apex.app.classes.models.Result;

/**
 * Created by Enda on 31/01/2015.
 */
public class ResultsDB extends SQLiteOpenHelper {

    /**
     * Static variables
     */
    // database version
    private static final int DATABASE_VERSION = 14;
    // database name
    private static final String DATABASE_NAME = "resultsDb";
    // table names
    private static final String TABLE_RESULTS = "resultsTbl";
    // columns
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROUTE_ID = "routeId";
    private static final String KEY_DISTANCE = "distance";
    private static final String KEY_MAX_SPEED = "maxSpeed";
    private static final String KEY_AVG_SPEED = "avgSpeed";
    private static final String KEY_TIME = "time";
    private static final String KEY_DATE_CREATED = "dateCreated";

    public ResultsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creating tables
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RESULTS_TABLE = "CREATE TABLE " + TABLE_RESULTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_ROUTE_ID + " INTEGER,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_DISTANCE + " FLOAT,"
                + KEY_MAX_SPEED + " FLOAT,"
                + KEY_AVG_SPEED + " FLOAT,"
                + KEY_TIME + " TEXT,"
                + KEY_DATE_CREATED + " TEXT" + ")";
        db.execSQL(CREATE_RESULTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTS);
        onCreate(db);
    }

    /**
     * Store results
     */
    public void addResult(Result result) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, result.getRouteId());
        values.put(KEY_USER_ID, result.getUserId());
        values.put(KEY_ROUTE_ID, result.getRouteId());
        values.put(KEY_DISTANCE, result.getDistance());
        values.put(KEY_MAX_SPEED, result.getMaxSpeed());
        values.put(KEY_AVG_SPEED, result.getAvgSpeed());
        values.put(KEY_TIME, String.valueOf(result.getTime()));
        values.put(KEY_DATE_CREATED, String.valueOf(result.getDateCreated()));

        db.insert(TABLE_RESULTS, null, values);
        db.close();
    }

    public ArrayList<Result> getResults(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Result> results = new ArrayList<>();

        // select all results
        String select = "SELECT * FROM " + TABLE_RESULTS
                + " WHERE userId = " + userId;

        Cursor cursor = db.rawQuery(select, null);

        // if there are rows to add
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();

            // add row to array while the cursor
            // has another line to go to
            do {
                results.add(new Result(cursor.getInt(0),
                        cursor.getInt(1), cursor.getInt(2),
                        cursor.getFloat(3), cursor.getFloat(4),
                        cursor.getFloat(5), cursor.getLong(6),
                        Date.valueOf(cursor.getString(7))));
            } while (cursor.moveToNext());

            cursor.close();
        }
        db.close();

        return results;
    }


    /**
     * Get a single result
     */
    public Result getResult(int userId, int routeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Result result = new Result();

        // select result
        String selectQuery = "SELECT * FROM " + TABLE_RESULTS
                + "where routeId = " + routeId + " userId = " + userId;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor != null) {
            cursor.moveToFirst();

            do {
                // get requested results details
                result = new Result(cursor.getInt(0),
                        cursor.getInt(1), cursor.getInt(2),
                        cursor.getFloat(3), cursor.getFloat(4),
                        cursor.getFloat(5), cursor.getLong(6),
                        Date.valueOf(cursor.getString(7)));

            } while (cursor.moveToNext());

            cursor.close();
        }

        // close connection
        db.close();

        return result;
    }

    /**
     * Calculate the average overall speed from the set of results
     */
    public float getAverageSpeed() {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT sum(avgSpeed) from " + TABLE_RESULTS;

        Cursor cursor = db.rawQuery(select, null);
        float avg = Float.NaN;
        if(cursor.moveToFirst()) {
            avg = Float.valueOf(cursor.getString(0));
        }
        cursor.close();
        return avg;
    }

    /**
     * Get number of rows
     */
    public int rowCount() {
        String count = "SELECT * FROM " + TABLE_RESULTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(count, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();
        // return row count
        return rowCount;
    }

    /**
     * Reset tables
     */
    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete rows
        db.delete(TABLE_RESULTS, null, null);
        db.close();
    }

}
