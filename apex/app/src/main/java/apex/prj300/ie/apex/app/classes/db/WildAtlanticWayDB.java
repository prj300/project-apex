package apex.prj300.ie.apex.app.classes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import apex.prj300.ie.apex.app.classes.models.WayPoint;

/**
 * Created by Enda on 17/02/2015.
 */
public class WildAtlanticWayDB extends SQLiteOpenHelper {

    // Database version
    private static final int DATABASE_VERSION = 2;
    // Database name
    private static final String DATABASE_NAME = "wildAtlanticWayDb";

    // Table name
    private static final String TABLE_WAW = "wildAtlanticWayTbl";
    // Columns
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";

    // constructor
    public WildAtlanticWayDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table
        String CREATE_WILD_ATLANTIC_WAY_TABLE = "CREATE TABLE "
                + TABLE_WAW + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_LATITUDE + " DOUBLE,"
                + KEY_LONGITUDE + " DOUBLE" + ")";
        db.execSQL(CREATE_WILD_ATLANTIC_WAY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAW);
        // Create tables again
        onCreate(db);
    }

    /**
     * Add route to table
     */
    public void addRoute(ArrayList<WayPoint> wayPoints) {
        SQLiteDatabase db = this.getWritableDatabase();
        WayPoint wayPoint;

        ContentValues values = new ContentValues();
        for (int i = 0; i < wayPoints.size(); i++) {
            wayPoint = wayPoints.get(i);
            values.put(KEY_ID, wayPoint.getId());
            values.put(KEY_LATITUDE, wayPoint.getLatitude());
            values.put(KEY_LONGITUDE, wayPoint.getLongitude());
        }
        db.insert(TABLE_WAW, null, values);
    }

}
