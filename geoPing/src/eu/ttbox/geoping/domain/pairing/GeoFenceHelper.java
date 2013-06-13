package eu.ttbox.geoping.domain.pairing;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.os.Bundle;
import android.widget.TextView;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.core.wrapper.BundleWrapper;
import eu.ttbox.geoping.domain.core.wrapper.ContentValuesWrapper;
import eu.ttbox.geoping.domain.core.wrapper.HelperWrapper;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase.GeoFenceColumns;

public class GeoFenceHelper {

    boolean isNotInit = true;

    public int idIdx = -1;
    public int nameIdx = -1;
    public int geofenceIdIdx = -1;

    public int latitudeE6Idx = -1;
    public int longitudeE6Idx = -1;
    public int radiusIdx = -1;

    public int expirationIdx = -1;
    public int transitionIdx = -1;

    // public int titreIdx = -1;

    public GeoFenceHelper initWrapper(Cursor cursor) {
        idIdx = cursor.getColumnIndex(GeoFenceColumns.COL_ID);
        nameIdx = cursor.getColumnIndex(GeoFenceColumns.COL_NAME);
        geofenceIdIdx = cursor.getColumnIndex(GeoFenceColumns.COL_REQUEST_ID);

        latitudeE6Idx = cursor.getColumnIndex(GeoFenceColumns.COL_LATITUDE_E6);
        longitudeE6Idx = cursor.getColumnIndex(GeoFenceColumns.COL_LONGITUDE_E6);
        radiusIdx = cursor.getColumnIndex(GeoFenceColumns.COL_RADIUS);

        transitionIdx = cursor.getColumnIndex(GeoFenceColumns.COL_TRANSITION);
        expirationIdx = cursor.getColumnIndex(GeoFenceColumns.COL_EXPIRATION);

        isNotInit = false;
        return this;
    }

    public CircleGeofence getEntity(Cursor cursor) {
        if (isNotInit) {
            initWrapper(cursor);
        }
        CircleGeofence geoTrack = new CircleGeofence();
        // Reader
        String name = nameIdx > -1 ? cursor.getString(nameIdx) : null;
        String geofenceId = geofenceIdIdx > -1 ? cursor.getString(geofenceIdIdx) : null;
        int latitudeE6 = cursor.getInt(latitudeE6Idx);
        int longitudeE6 = cursor.getInt(longitudeE6Idx);
        int radius = radiusIdx > -1 ? cursor.getInt(radiusIdx) : -1;
        long expiration = expirationIdx > -1 ? cursor.getLong(expirationIdx) : -1;
        int transition = transitionIdx > -1 ? cursor.getInt(transitionIdx) : -1;
        geoTrack.setId(idIdx > -1 ? cursor.getLong(idIdx) : AppConstants.UNSET_ID);
        geoTrack.setName(name);
        geoTrack.setRequestId(geofenceId);
        geoTrack.setLatitudeE6(latitudeE6);
        geoTrack.setLongitudeE6(longitudeE6);
        geoTrack.setRadiusInMeters(radius);
        geoTrack.setExpirationDuration(expiration);
        geoTrack.setTransitionType(transition);
        return geoTrack;
    }

    private GeoFenceHelper setTextWithIdx(TextView view, Cursor cursor, int idx) {
        view.setText(cursor.getString(idx));
        return this;
    }

    public GeoFenceHelper setTextId(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, idIdx);
    }

    public String getIdAsString(Cursor cursor) {
        return cursor.getString(idIdx);
    }

    public long getId(Cursor cursor) {
        return cursor.getLong(idIdx);
    }

    public static ContentValues getContentValues(CircleGeofence user) {
        ContentValuesWrapper wrapper = (ContentValuesWrapper) getWrapperValues(user, new ContentValuesWrapper(GeoFenceColumns.ALL_COLS.length), false);
        ContentValues initialValues = wrapper.getWrappedValue();
        return initialValues;
    }

    public static Bundle getBundleValues(CircleGeofence geoFence) {
        BundleWrapper wrapper = (BundleWrapper) getWrapperValues(geoFence, new BundleWrapper(GeoFenceColumns.ALL_COLS.length), false);
        Bundle bundle = wrapper.getWrappedValue();
        return bundle;
    }

    private static HelperWrapper<?> getWrapperValues(CircleGeofence geoFence, HelperWrapper<?> initialValues, boolean noHasCheck) {
        if (geoFence.id > -1) {
            initialValues.putLong(GeoFenceColumns.COL_ID, Long.valueOf(geoFence.id));
        }
        if (noHasCheck || geoFence.requestId != null ) {
           initialValues.putString(GeoFenceColumns.COL_REQUEST_ID, geoFence.getRequestId());
        }
        if (noHasCheck || geoFence.name != null ) {
          initialValues.putString(GeoFenceColumns.COL_NAME, geoFence.getName());
        }
        // Location
        initialValues.putInt(GeoFenceColumns.COL_LATITUDE_E6, geoFence.getLatitudeE6());
        initialValues.putInt(GeoFenceColumns.COL_LONGITUDE_E6, geoFence.getLongitudeE6());
        initialValues.putFloat(GeoFenceColumns.COL_RADIUS, geoFence.getRadiusInMeters());
        initialValues.putInt(GeoFenceColumns.COL_TRANSITION, geoFence.getTransitionType());
        initialValues.putLong(GeoFenceColumns.COL_EXPIRATION, geoFence.getExpirationDuration());

        return initialValues;
    }



    public static CircleGeofence getEntityFromContentValue(ContentValues initialValues) {
        if (initialValues == null || initialValues.size()<1) {
            return null;
        }
        CircleGeofence geofence = new CircleGeofence();

        if (initialValues.containsKey(GeoFenceColumns.COL_ID)) {
            geofence.setId(initialValues.getAsLong(GeoFenceColumns.COL_ID));
        }
        if (initialValues.containsKey(GeoFenceColumns.COL_REQUEST_ID)) {
            geofence.setRequestId(initialValues.getAsString(GeoFenceColumns.COL_REQUEST_ID));
        }
        if (initialValues.containsKey(GeoFenceColumns.COL_NAME)) {
            geofence.setName(initialValues.getAsString(GeoFenceColumns.COL_NAME));
        }
        
      
        // Geo
//        if (initialValues.containsKey(Intents.EXTRA_GEO_E6)) {
//            int[] geoLatLng = initialValues.getAsIntArray(Intents.EXTRA_GEO_E6);
//            int geoLatLngSize = geoLatLng.length;
//            if (geoLatLngSize >= 2) {
//                geofence.setLatitudeE6(geoLatLng[0]);
//                geofence.setLongitudeE6(geoLatLng[1]);
//            }
//            if (geoLatLngSize >= 3) {
//                geofence.setRadiusInMeters(geoLatLng[3]);
//            }
//        }
        if (initialValues.containsKey(GeoFenceColumns.COL_LATITUDE_E6)) {
            geofence.setLatitudeE6(initialValues.getAsInteger(GeoFenceColumns.COL_LATITUDE_E6));
        }
        if (initialValues.containsKey(GeoFenceColumns.COL_LONGITUDE_E6)) {
            geofence.setLongitudeE6(initialValues.getAsInteger(GeoFenceColumns.COL_LONGITUDE_E6));
        }

        if (initialValues.containsKey(GeoFenceColumns.COL_RADIUS)) {
            geofence.setRadiusInMeters(initialValues.getAsInteger(GeoFenceColumns.COL_RADIUS));
        }
        if (initialValues.containsKey(GeoFenceColumns.COL_TRANSITION)) {
            geofence.setTransitionType(initialValues.getAsInteger(GeoFenceColumns.COL_TRANSITION));
        }
        if (initialValues.containsKey(GeoFenceColumns.COL_EXPIRATION)) {
            geofence.setExpirationDuration(initialValues.getAsLong(GeoFenceColumns.COL_EXPIRATION));
        }
        return geofence;
    }

}