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
    private static final int DATABASE_VERSION = 10;
    // Database name
    private static final String DATABASE_NAME = "wildAtlanticWayDb";
    // Table names
    private static final String TABLE_WAYPOINTS = "wayPointsTbl";
    private static final String TABLE_ROUTE = "routeTbl";
    // Columns
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_DISTANCE = "distance";

    // constructor
    public WildAtlanticWayDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        String CREATE_LATS_LONGS_TABLE = "CREATE TABLE "
                + TABLE_WAYPOINTS + "("
                + KEY_ID + " INTEGER,"
                + KEY_LATITUDE + " DOUBLE,"
                + KEY_LONGITUDE + " DOUBLE" + ")";

        String CREATE_ROUTE_TABLE = "CREATE TABLE "
                + TABLE_ROUTE + "("
                + KEY_DISTANCE + " FLOAT" + ")";

        db.execSQL(CREATE_LATS_LONGS_TABLE);
        db.execSQL(CREATE_ROUTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAYPOINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
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
            db.insert(TABLE_WAYPOINTS, null, values);
        }
        Log.d(DATABASE_NAME, "Rows: " + wayPoints.size());
        db.close();
    }

    /**
     * Delete any previous route in database
     */
    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete rows
        db.delete(TABLE_WAYPOINTS, null, null);
        db.delete(TABLE_ROUTE, null, null);
        db.close();
    }

    /**
     * Retrieve lat lng points from table
     */
    public ArrayList<LatLng> getLatLngs() {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        String select = "SELECT latitude, longitude FROM " + TABLE_WAYPOINTS;
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

}
