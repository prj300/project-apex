package apex.prj300.ie.apex.app.classes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;

import apex.prj300.ie.apex.app.classes.models.Route;

/**
 * Created by Enda on 23/01/2015.
 */
public class RouteDB extends SQLiteOpenHelper {

    /**
     * Static variables
     */
    // database version
    private static final int DATABASE_VERSION = 11;
    // database name
    private static final String DATABASE_NAME = "apex";
    // route table name
    private static final String TABLE_ROUTE = "route";
    // route details table column names
    private static String KEY_USER_ID = "userid";
    private static String KEY_GRADE = "grade";
    private static String KEY_TERRAIN = "terrain";
    private static String KEY_TIME = "time";
    private static String KEY_DISTANCE = "distance";
    private static String KEY_ROUTE = "route";
    private static String KEY_DATE_CREATED = "datecreated";

    public RouteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create tables
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // building table parameters
        String CREATE_ROUTE_TABLE = "CREATE TABLE " + TABLE_ROUTE + "("
                + KEY_USER_ID + " INTEGER,"
                + KEY_GRADE + " TEXT,"
                + KEY_TERRAIN + " TEXT,"
                + KEY_TIME + " TEXT,"
                + KEY_ROUTE + " TEXT,"
                + KEY_DISTANCE + " FLOAT,"
                + KEY_DATE_CREATED + " DATE" + ")";
        // execute sql
        db.execSQL(CREATE_ROUTE_TABLE);

    }

    /**
     * Drop table if new version database version is detected
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        // re-create
        onCreate(db);
    }

    /**
     * Store route
     */
    public void addRoute(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, route.getUserID());
        values.put(KEY_GRADE, String.valueOf(route.getGrade()));
        values.put(KEY_TERRAIN, String.valueOf(route.getTerrain()));
        values.put(KEY_TIME, String.valueOf(route.getTime()));
        values.put(KEY_ROUTE, route.getRoute());
        values.put(KEY_DISTANCE, route.getDistance());
        values.put(KEY_DATE_CREATED, String.valueOf(route.getDateCreated()));

        db.insert(TABLE_ROUTE, null, values);
        db.close(); // close connection to database
    }

    /**
     * Delete any previous route in database
     */
    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete rows
        db.delete(TABLE_ROUTE, null, null);
        db.close();
    }

}
