package bg.o.sim.taggerizmus;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

/**
 * Provides and interface for SQLite db queries involving adding, removing or modifing locations.
 */
public class DbAdapter {


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

        private static final String CREATE_TRANSACTION = "" +
                "CREATE TABLE " + TABLE_LOCATION +
                " ( " +
                LOCATION_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LOCATION_COL_ADDRESS + " VARCHAR(512), " +
                LOCATION_COL_COUNTRY + " VARCHAR(64), " + //As of May 2017, the longest country name in English is the 'UK of GB and NI' at 48 char. 64 symbols should suffice for country names;
                LOCATION_COL_LATITUDE + " REAL, " +
                LOCATION_COL_LONGITUDE + " REAL, " +
                ");";


        private static DbHelper instance;
        private final Context context;

        private DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.context = context;
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
            db.execSQL("");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //TODO keep data from old table.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION + " ;");
            onCreate(db);
        }
    }
}
