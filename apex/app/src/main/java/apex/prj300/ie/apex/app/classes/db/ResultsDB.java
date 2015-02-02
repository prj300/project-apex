package apex.prj300.ie.apex.app.classes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import apex.prj300.ie.apex.app.classes.models.Results;

/**
 * Created by Enda on 31/01/2015.
 */
public class ResultsDB extends SQLiteOpenHelper {

    /**
     * Static variables
     */
    // database version
    private static final int DATABASE_VERSION = 4;
    // database name
    private static final String DATABASE_NAME = "resultsDb";
    // table names
    private static final String TABLE_RESULTS = "resultsTbl";
    // columns
    private static String KEY_DISTANCE = "distance";
    private static String KEY_MAX_SPEED = "maxSpeed";
    private static String KEY_AVG_SPEED = "avgSpeed";
    private static String KEY_TIME = "time";
    private static String KEY_DATE_CREATED = "dateCreated";

    public ResultsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creating tables
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RESULTS_TABLE = "CREATE TABLE " + TABLE_RESULTS + "("
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
    public void addResult(Results result) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DISTANCE, result.getDistance());
        values.put(KEY_MAX_SPEED, result.getMaxSpeed());
        values.put(KEY_AVG_SPEED, result.getAvgSpeed());
        values.put(KEY_TIME, String.valueOf(result.getTime()));
        values.put(KEY_DATE_CREATED, String.valueOf(result.getDateCreated()));

        db.insert(TABLE_RESULTS, null, values);
        db.close();
    }
}
