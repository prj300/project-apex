package apex.prj300.ie.apex.app.classes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import apex.prj300.ie.apex.app.classes.models.WayPoint;

/**
 * Created by Enda on 17/02/2015.
 */
public class WildAtlanticWayDB extends SQLiteOpenHelper {

    // Database version
    private static final int DATABASE_VERSION = 6;
    // Database name
    private static final String DATABASE_NAME = "wildAtlanticWayDb";
    // Table names
    private static final String TABLE_LATS_LONGS = "latsLongsTbl";
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
                + TABLE_LATS_LONGS + "("
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LATS_LONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);
        // Create tables again
        onCreate(db);
    }

    /**
     * Add route lat longs to table
     */
    public void addLatLongs(ArrayList<LatLng> latLongs) {
        SQLiteDatabase db = this.getWritableDatabase();
        LatLng latLng;

        ContentValues values = new ContentValues();
        for (int i = 0; i < latLongs.size(); i++) {
            latLng = latLongs.get(i);
            values.put(KEY_ID, 1);
            values.put(KEY_LATITUDE, latLng.latitude);
            values.put(KEY_LONGITUDE, latLng.longitude);
        }
        db.insert(TABLE_LATS_LONGS, null, values);
        db.close();
    }

    /**
     * Add attributes of route to separate table
     */
    public void addRoute(float distance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
    }

    /**
     * Delete any previous route in database
     */
    public void resetTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete rows
        db.delete(TABLE_LATS_LONGS, null, null);
        db.delete(TABLE_ROUTE, null, null);
        db.close();
    }

    /**
     * Retrieve lat long points from the table
     */
    public ArrayList<LatLng> getLatLngs() {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        String select = "SELECT latitude, longitude FROM "
                + TABLE_LATS_LONGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(select, null);

        if(cursor.moveToFirst()) {
            do {
                latLngs.add(new LatLng
                        (cursor.getDouble(0), cursor.getDouble(1)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return latLngs;
    }

}
