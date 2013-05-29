package eu.ttbox.geoping.domain.geofence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import eu.ttbox.geoping.core.PhoneNumberUtils;
import eu.ttbox.geoping.domain.model.CircleGeofence;

public class GeoFenceDatabase {

	public static final String TABLE_GEOFENCE = "geofence";

	public static class CircleGeofenceColumns {
		public static final String COL_ID = BaseColumns._ID; 
		// Phone
		public static final String COL_REQUEST_ID = "REQUEST_ID"; 
		// Location
		public static final String COL_LATITUDE_E6 = "LAT_E6";
		public static final String COL_LONGITUDE_E6 = "LNG_E6";
		public static final String COL_RADIUS = "RADIUS";  
		public static final String COL_TRANSITION = "TRANSITION"; 
        public static final String COL_EXPIRATION = "EXPIRATION"; 

		public static final String[] ALL_COLS = new String[] { COL_ID,   COL_REQUEST_ID//
				,  COL_LATITUDE_E6, COL_LONGITUDE_E6, COL_RADIUS,   COL_TRANSITION, COL_EXPIRATION //
				 
		};

	}

	private static final String CRITERIA_BY_ENTITY_ID = String.format("%s = ?", CircleGeofenceColumns.COL_ID);
	private static final String CRITERIA_BY_USER_ID = String.format("%s = ?", CircleGeofenceColumns.COL_REQUEST_ID);

	private SQLiteDatabase bdd;

	private GeofenceOpenHelper mDatabaseOpenHelper;

	private static final HashMap<String, String> mCircleGeofenceColumnMap = buildCircleGeofenceColumnMap();

	public GeoFenceDatabase(Context context) {
		mDatabaseOpenHelper = new GeofenceOpenHelper(context);
	}

	private static HashMap<String, String> buildCircleGeofenceColumnMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		// Add Id
		for (String col : CircleGeofenceColumns.ALL_COLS) {
			map.put(col, col);
		}
		// Add Suggest Aliases
		map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, String.format("%s AS %s", CircleGeofenceColumns.COL_LATITUDE_E6, SearchManager.SUGGEST_COLUMN_TEXT_1));
		map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, String.format("%s AS %s", CircleGeofenceColumns.COL_LONGITUDE_E6, SearchManager.SUGGEST_COLUMN_TEXT_2));
		map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
		// Add Other Aliases
		return map;
	}

	public Cursor getEntityById(String rowId, String[] projection) {
		String[] selectionArgs = new String[] { rowId };
		return queryEntities(projection, CRITERIA_BY_ENTITY_ID, selectionArgs, null);
	}

	public Cursor queryEntities(String[] projection, String selection, String[] selectionArgs, String order) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TABLE_GEOFENCE);
		builder.setProjectionMap(mCircleGeofenceColumnMap);
		Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, order);
		return cursor;
	}

	 
	public long insertEntity(ContentValues values) throws SQLException {
		long result = -1; 
		SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		try {
			db.beginTransaction();
			try {
				result = db.insertOrThrow(TABLE_GEOFENCE, null, values);
				// commit
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		} finally {
			db.close();
		}
		return result;
	}

 
	public int updateEntity(ContentValues values, String selection, String[] selectionArgs) {
		int result = -1; 
		SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		try {
			db.beginTransaction();
			try {
				result = db.update(TABLE_GEOFENCE, values, selection, selectionArgs);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		} finally {
			db.close();
		}
		return result;
	}

	public int deleteEntity(String selection, String[] selectionArgs) {
		int result = -1;
		SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
		try {
			db.beginTransaction();
			try {
				result = db.delete(TABLE_GEOFENCE, selection, selectionArgs);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		} finally {
			db.close();
		}
		return result;
	}

	public List<CircleGeofence> getTrakPointForToday(String userId) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.HOUR);
		calendar.clear(Calendar.HOUR_OF_DAY);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		long pointDate = calendar.getTimeInMillis();
		String whereClause = CircleGeofenceColumns.COL_REQUEST_ID + " = ? and " + CircleGeofenceColumns.COL_EXPIRATION + '>' + pointDate;
		Cursor c = bdd.query(TABLE_GEOFENCE, CircleGeofenceColumns.ALL_COLS, whereClause, new String[] { userId }, null, null, CircleGeofenceColumns.COL_EXPIRATION);
		return cursorToLivre(c);
	}

	public List<CircleGeofence> getTrakPointWithTitre(String userId) {
		Cursor c = bdd.query(TABLE_GEOFENCE, CircleGeofenceColumns.ALL_COLS, CRITERIA_BY_USER_ID, new String[] { userId }, null, null, CircleGeofenceColumns.COL_EXPIRATION);
		return cursorToLivre(c);
	}

	private List<CircleGeofence> cursorToLivre(Cursor c) {
		List<CircleGeofence> points = new ArrayList<CircleGeofence>(c.getCount());
		if (c.getCount() == 0)
			return points;

		if (c.moveToFirst()) { 
			GeofenceHelper helper = new GeofenceHelper().initWrapper(c);
			while (c.moveToNext()) { 
				CircleGeofence point = helper.getEntity(c);
				points.add(point);
			}
		}

		// On ferme le cursor
		c.close();

		// On retourne le livre
		return points;
	}

}