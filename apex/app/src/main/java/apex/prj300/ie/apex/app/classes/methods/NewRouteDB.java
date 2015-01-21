package apex.prj300.ie.apex.app.classes.methods;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import apex.prj300.ie.apex.app.classes.models.Route;

/**
 * Created by Enda on 21/01/2015.
 */
public class NewRouteDB extends SQLiteOpenHelper {

    /**
     * Database Name
     */
    private static final String DATABASE_NAME = "new_route";

    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Table Name
     */
    private static final String TABLE_NEW_ROUTE = "route_table";
    /**
     * Column names
     */
    private static final String KEY_POSX = "posx";
    private static final String KEY_POSY = "posy";

    /**
     * Constructor
     */
    public NewRouteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creating tables
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_ROUTE_TABLE = "CREATE TABLE " + TABLE_NEW_ROUTE + "("
                + KEY_POSX + " TEXT,"
                + KEY_POSY + " TEXT" + ")";
        db.execSQL(CREATE_ROUTE_TABLE);
    }

    /**
     * Upgrading database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEW_ROUTE);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    //delete all rows in table
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NEW_ROUTE, null, null);
        db.close();
    }

    // Add new position
    public void addPosition(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_POSX, route.getLatitude());
        values.put(KEY_POSY, route.getLongitude());
        // insert row
        db.insert(TABLE_NEW_ROUTE, null, values);

        // close connection
        db.close();
    }
}
