package bg.o.sim.taggerizmus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

/**
 * Provides and interface for SQLite db queries involving adding, removing or modifying locations.
 * Also maintains a caching collection of Markers by their unique Db id.
 */
public class DbAdapter {

    private static DbAdapter instance;

    /*Holds all markers for easier(and faster) access and modification, stored as <db id - Marker instance>*/
    private final HashMap<Long, Marker> markerCache;

    private DbHelper h;
    private Context c;

    private DbAdapter(Context context) {
        if (context == null) throw new IllegalArgumentException("The Context must be non-null !!!");

        this.c = context;
        this.h = DbHelper.getInstance(c);
        this.markerCache = new HashMap<>();
    }

    public static DbAdapter getInstance(Context c) {
        if (instance == null) instance = new DbAdapter(c);
        return instance;
    }

    @Nullable
    /**
     * Returns a Marker from the cache, based on its Db id.
     * <b>Use the id supplied by the {@link MarkerDetail} tag of the Marker instance, not the {@link Marker#getId()} !</b>
     * Returns <code>null</code> if the cache doesn't contain a mapping to that id.
     */
    public Marker getMarker(long id) {
        return id < 1 ? null : markerCache.get(id);
    }

    /**
     * Stores the marker passed in the appropriate DB table and then adds it to the cache collection.
     *
     * @param m {@link Marker} instance which to store.
     */
    public void addMarker(Marker m, List<Address> addresses) {

        double lat = m.getPosition().latitude;
        double lng = m.getPosition().longitude;

        String address = "N/A";
        String country = c.getString(R.string.not_in_country);

        if (addresses.size() > 0) {
            country = addresses.get(0).getCountryName();

            String street = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String postalCode = addresses.get(0).getPostalCode();

            StringBuilder sb = new StringBuilder();
            if (street != null) sb.append(street + ", ");
            if (city != null) sb.append(city + ", ");
            if (postalCode != null) sb.append(postalCode + ", ");
            if (state != null) sb.append(state + ", ");
            address = sb.toString();
        }


        //TODO - ReverseGeoWhatever
        if (m == null) return;
        ContentValues values = new ContentValues(4);
        values.put(h.LOCATION_COL_ADDRESS, address);
        values.put(h.LOCATION_COL_COUNTRY, country);
        values.put(h.LOCATION_COL_LONGITUDE, m.getPosition().longitude);
        values.put(h.LOCATION_COL_LATITUDE, m.getPosition().latitude);

        long id = -1;

        try {
            id = h.getWritableDatabase().insertWithOnConflict(h.TABLE_LOCATION, null, values, SQLiteDatabase.CONFLICT_ROLLBACK);
        } catch (SQLiteException e) {
            Log.e("LOADER: ", "SQLite failure: " + e.getCause());
        }

        if (id < 0) return;

        m.setTag(new MarkerDetail(id, address, country, new LatLng(m.getPosition().latitude, m.getPosition().longitude)));
        markerCache.put(id, m);
        Log.i("LOADER: ", "INSERTED LOC: " + m.getPosition().toString());
    }

    //TODO - cache Markers;

    //TODO - removeMarker;

    //TODO - modifyMarker; single method taking all params or a separate method for each param?

    public void loadMarkers(final GoogleMap map) {
        //TODO - validate map param!
        new AsyncTask<Void, MarkerDetail, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Cursor c = h.getReadableDatabase().query(h.TABLE_LOCATION, h.LOCATION_COLUMNS, null, null, null, null, null);

                int indxId = c.getColumnIndex(h.LOCATION_COL_ID);
                int indxAdr = c.getColumnIndex(h.LOCATION_COL_ADDRESS);
                int indxCntry = c.getColumnIndex(h.LOCATION_COL_COUNTRY);
                int indxLat = c.getColumnIndex(h.LOCATION_COL_LATITUDE);
                int indxLng = c.getColumnIndex(h.LOCATION_COL_LONGITUDE);

                Log.e("LOADER: ", "STARTED. COUNT: " + c.getCount());

                while (c.moveToNext()) {

                    long id = c.getLong(indxId);

                    String address = c.getString(indxAdr);
                    String country = c.getString(indxCntry);

                    double latitude = c.getDouble(indxLat);
                    double longitude = c.getDouble(indxLng);
                    LatLng latLng = new LatLng(latitude, longitude);

                    publishProgress(new MarkerDetail(id, address, country, latLng));

                }
                c.close();
                Log.e("LOADER: ", "FINISHED");
                return null;
            }


            @Override
            protected void onProgressUpdate(MarkerDetail... values) {
                if (values.length == 0 || values[0] == null) return;

                //Add a Marker, corresponding the the loaded from Db MarkerDetail, to the map.
                Marker m = map.addMarker(new MarkerOptions().position(values[0].getLatLng()));
                //Store the details as the Marker's Tag.
                m.setTag(values[0]);
                //Cache the Marker itself.
                markerCache.put(values[0].getId(), m);
            }

        }.execute();
    }

    /**
     * Singleton {@link SQLiteOpenHelper} implementation class.
     */
    private static class DbHelper extends SQLiteOpenHelper {

        //DataBase version const:
        private static final int DB_VERSION = 1;

        //DateBase name const
        private static final String DB_NAME = "taggerizmus.db";

        //Table name const
        private static final String TABLE_LOCATION = "locations";

        //Specific table columns consts
        private static final String LOCATION_COL_ID = "_id";
        private static final String LOCATION_COL_ADDRESS = "Address";
        private static final String LOCATION_COL_COUNTRY = "Country";
        private static final String LOCATION_COL_LATITUDE = "Lat";
        private static final String LOCATION_COL_LONGITUDE = "Long";
        private static final String[] LOCATION_COLUMNS = {
                LOCATION_COL_ID,
                LOCATION_COL_ADDRESS,
                LOCATION_COL_COUNTRY,
                LOCATION_COL_LATITUDE,
                LOCATION_COL_LONGITUDE
        };

        private static final String CREATE_TRANSACTION = "" +
                "CREATE TABLE " + TABLE_LOCATION +
                " ( " +
                LOCATION_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOCATION_COL_ADDRESS + " VARCHAR(512), " +
                LOCATION_COL_COUNTRY + " VARCHAR(64), " + //As of May 2017, the longest country name in English is the 'UK of GB and NI' at 48 char. 64 symbols should suffice for country names;
                LOCATION_COL_LATITUDE + " REAL, " +
                LOCATION_COL_LONGITUDE + " REAL " +
                " );";


        private static DbHelper instance;

        private DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * Singleton instance getter.
         *
         * @param context Context instance required for SQLiteOpenHelper constructor.
         * @return Singleton instance of the DbHelper.
         */
        private static DbHelper getInstance(@NonNull Context context) {
            if (context == null)
                throw new IllegalArgumentException("Context MUST ne non-null!!!");
            if (instance == null)
                instance = new DbHelper(context);
            return instance;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //TODO
            db.execSQL(CREATE_TRANSACTION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //TODO keep data from old table.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION + " ;");
            onCreate(db);
        }

    }
}
