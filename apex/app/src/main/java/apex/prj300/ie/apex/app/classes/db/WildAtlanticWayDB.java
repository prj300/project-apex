package apex.prj300.ie.apex.app.classes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import apex.prj300.ie.apex.app.classes.models.WayPoint;

/**
 * Created by Enda on 17/02/2015.
 */
public class WildAtlanticWayDB extends SQLiteOpenHelper {

    // Database version
    private static final int DATABASE_VERSION = 15;
    // Database name
    private static final String DATABASE_NAME = "wildAtlanticWayDb";
    // Table names
    private static final String TABLE_WAY_POINTS = "wayPointsTbl";
    private static final String TABLE_DISCOVERY_POINTS = "discoveryPointsTbl";
    // Columns
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LOCATION_ID = "locationId";
    private static final String KEY_NAME = "name";
    private static final String KEY_COUNTY = "county";

    // constructor
    public WildAtlanticWayDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        String CREATE_LATS_LONGS_TABLE = "CREATE TABLE "
                + TABLE_WAY_POINTS + "("
                + KEY_ID + " INTEGER,"
                + KEY_LATITUDE + " DOUBLE,"
                + KEY_LONGITUDE + " DOUBLE" + ")";

        String CREATE_DISCOVERY_POINTS_TABLE = "CREATE TABLE "
                + TABLE_DISCOVERY_POINTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_LOCATION_ID + " INTEGER,"
                + KEY_COUNTY + " TEXT, "
                + KEY_LATITUDE + " DOUBLE,"
                + KEY_LONGITUDE + " DOUBLE" + ")";

        db.execSQL(CREATE_LATS_LONGS_TABLE);
        db.execSQL(CREATE_DISCOVERY_POINTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAY_POINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISCOVERY_POINTS);
        // Create tables again
        onCreate(db);
    }

    /**
     * Add way points to table
     */
    public void addWaypoints(ArrayList<WayPoint> wayPoints) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        for (int i = 0; i < wayPoints.size(); i++) {
            values.put(KEY_ID, wayPoints.get(i).getId());
            values.put(KEY_LATITUDE, wayPoints.get(i).getLatitude());
            values.put(KEY_LONGITUDE, wayPoints.get(i).getLongitude());
            db.insert(TABLE_WAY_POINTS, null, values);
        }
        Log.d(DATABASE_NAME, TABLE_WAY_POINTS + " Rows: " + wayPoints.size());
        db.close();
    }

    /**
     * Store discovery points
     */
    public void addDiscoveryPoints(ArrayList<WayPoint> wayPoints) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for(int i=0;i < wayPoints.size();i++) {
            values.put(KEY_ID, wayPoints.get(i).getId());
            values.put(KEY_LOCATION_ID, wayPoints.get(i).getLocation_id());
            values.put(KEY_NAME, wayPoints.get(i).getName());
            values.put(KEY_COUNTY, wayPoints.get(i).getCounty());
            values.put(KEY_LATITUDE, wayPoints.get(i).getLatitude());
            values.put(KEY_LONGITUDE, wayPoints.get(i).getLongitude());
            db.insert(TABLE_DISCOVERY_POINTS, null, values);
        }
        Log.d(DATABASE_NAME, TABLE_DISCOVERY_POINTS + " Rows " + wayPoints.size() + " added");
        db.close();
    }

    /**
     * Delete any previous route in database
     */
    public void resetRouteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete rows
        db.delete(TABLE_WAY_POINTS, null, null);
        db.close();
    }

    /**
     * Delete any previous route in database
     */
    public void resetDiscoveries() {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete rows
        db.delete(TABLE_DISCOVERY_POINTS, null, null);
        db.close();
    }



    /**
     * Retrieve lat lng points from table
     */
    public ArrayList<LatLng> getLatLngs() {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        String select = "SELECT latitude, longitude FROM " + TABLE_WAY_POINTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(select, null);

        // while the cursor has next line to move to do this
        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                LatLng latLng = new LatLng(cursor
                        .getDouble(cursor.getColumnIndex("latitude")),
                        cursor.getDouble(cursor.getColumnIndex("longitude")));
                latLngs.add(latLng); // add new lat long to arraylist
                cursor.moveToNext();
            }
            cursor.close();
            Log.d(DATABASE_NAME, "Cursor Count: " + cursor.getCount());
        }
        db.close();
        return latLngs;
    }

    public ArrayList<WayPoint> getDiscoveryPoints() {
        ArrayList<WayPoint> wayPoints = new ArrayList<>();
        String select = "SELECT * FROM " + TABLE_DISCOVERY_POINTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(select, null);

        if(cursor.moveToFirst()) {
            while(!cursor.isAfterLast()) {
                wayPoints.add(new WayPoint(
                        cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getDouble(cursor.getColumnIndex("latitude")),
                        cursor.getDouble(cursor.getColumnIndex("longitude")),
                        cursor.getInt(cursor.getColumnIndex("locationId")),
                        cursor.getString(cursor.getColumnIndex("county")),
                        cursor.getString(cursor.getColumnIndex("name"))));
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return wayPoints;
    }

}
