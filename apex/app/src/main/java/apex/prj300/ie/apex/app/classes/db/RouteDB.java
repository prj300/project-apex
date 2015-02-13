package apex.prj300.ie.apex.app.classes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import apex.prj300.ie.apex.app.classes.models.Route;

/**
 * Created by Enda on 23/01/2015.
 */
public class RouteDB extends SQLiteOpenHelper {

    /**
     * Static variables
     */
    // database version
    private static final int DATABASE_VERSION = 38;
    // database name
    private static final String DATABASE_NAME = "routeDb";

    // uniquely identifies routes in both table
    private static String KEY_ROUTE_ID = "routeId";
    // route table name
    private static final String TABLE_ROUTE = "routeTbl";
    // route details table column names
    private static String KEY_USER_ID = "userId";
    private static String KEY_GRADE = "grade";
    private static String KEY_TERRAIN = "terrain";
    private static String KEY_DISTANCE = "distance";
    private static String KEY_DATE_CREATED = "dateCreated";

    // lats longs table name
    private static final String TABLE_LATS_LONGS = "latsLongsTbl";
    private static String KEY_LAT = "lats";
    private static String KEY_LONG = "longs";

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
                + KEY_ROUTE_ID + " INTEGER PRIMARY KEY,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_GRADE + " TEXT,"
                + KEY_TERRAIN + " TEXT,"
                + KEY_DISTANCE + " FLOAT,"
                + KEY_DATE_CREATED + " TEXT" + ")";
        // execute sql
        db.execSQL(CREATE_ROUTE_TABLE);

        String CREATE_LATS_LONGS_TABLE = "CREATE TABLE " + TABLE_LATS_LONGS + "("
                + KEY_ROUTE_ID + " INTEGER,"
                + KEY_LAT + " DOUBLE,"
                + KEY_LONG + " DOUBLE" + ")";
        // create table
        db.execSQL(CREATE_LATS_LONGS_TABLE);

    }

    /**
     * Drop table if new version database version is detected
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LATS_LONGS);
        // re-create
        onCreate(db);
    }

    /**
     * Store route
     */
    public void addRoute(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE_ID, route.getRouteID());
        values.put(KEY_USER_ID, route.getUserID());
        values.put(KEY_GRADE, String.valueOf(route.getGrade()));
        values.put(KEY_TERRAIN, String.valueOf(route.getTerrain()));
        values.put(KEY_DISTANCE, route.getDistance());
        values.put(KEY_DATE_CREATED, String.valueOf(route.getDateCreated()));

        db.insert(TABLE_ROUTE, null, values);
        db.close(); // close connection to database

        // call method to add lat long points into separate table
        // addLatsLong(route.getRouteID(), route.getLatitudes(), route.getLongitudes());
    }

    /**
     * Store lat and long points for routes
     */
    public void addLatsLong(int routeID, List<Double> latitudes, List<Double> longitudes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // Loop through list of latitudes and insert into database
        for(int i=0; i < latitudes.size() && i < longitudes.size(); i++) {
            values.put(KEY_ROUTE_ID, routeID);
            values.put(KEY_LAT, latitudes.get(i));
            values.put(KEY_LONG, longitudes.get(i));
        }
        db.insert(TABLE_ROUTE, null, values);
        db.close();

    }

    /**
     * Count number of rows
     */
    public int rowCount(int id) {
        // select all rows where user id is = id
        String count = "SELECT * FROM " +
                TABLE_ROUTE + " WHERE userId = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(count, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();
        // return count
        return rowCount;
    }

    /**
     * Delete any previous route in database
     */
    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete rows
        db.delete(TABLE_ROUTE, null, null);
        db.delete(TABLE_LATS_LONGS, null, null);
        db.close();
    }

}
