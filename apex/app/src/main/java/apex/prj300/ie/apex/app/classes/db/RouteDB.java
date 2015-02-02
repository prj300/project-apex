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
    private static final int DATABASE_VERSION = 30;
    // database name
    private static final String DATABASE_NAME = "routeDb";
    // route table name
    private static final String TABLE_ROUTE = "routeTbl";
    // route details table column names
    private static String KEY_USER_ID = "userId";
    private static String KEY_GRADE = "grade";
    private static String KEY_TERRAIN = "terrain";
    private static String KEY_DISTANCE = "distance";
    private static String KEY_LATS = "lats";
    private static String KEY_LONGS = "longs";
    private static String KEY_DATE_CREATED = "dateCreated";

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
                + KEY_LATS + " TEXT,"
                + KEY_LONGS + " TEXT,"
                + KEY_DISTANCE + " FLOAT,"
                + KEY_DATE_CREATED + " TEXT" + ")";
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

        Gson gson = new Gson();
        // Convert route array to parsable json string

        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, route.getUserID());
        values.put(KEY_GRADE, String.valueOf(route.getGrade()));
        values.put(KEY_TERRAIN, String.valueOf(route.getTerrain()));
        values.put(KEY_LATS, gson.toJson(route.getLatitudes()));
        values.put(KEY_LONGS, gson.toJson(route.getLongitudes()));
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
