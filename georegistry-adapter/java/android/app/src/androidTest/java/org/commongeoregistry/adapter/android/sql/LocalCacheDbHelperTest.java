package org.commongeoregistry.adapter.android.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.commongeoregistry.adapter.HttpRegistryClient;
import org.commongeoregistry.adapter.MockIdService;
import org.commongeoregistry.adapter.android.MockHttpConnector;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.FrequencyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;


@RunWith(AndroidJUnit4.class)
public class LocalCacheDbHelperTest {

    @Test
    public void testWrite() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext();

        /*
         * Setup mock objects
         */
        MockHttpConnector connector = new MockHttpConnector();
        HttpRegistryClient client = new HttpRegistryClient(connector, new MockIdService());

        MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);

        GeoObject geoObject = client.newGeoObjectInstance("State");
        geoObject.setCode("Test");
        geoObject.setUid("blarg");

        /*
         * Create database
         */
        LocalCacheDbHelper mDbHelper = new LocalCacheDbHelper(context);
        try {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            Assert.assertNotNull(db);

            ContentValues values = new ContentValues();
            values.put(LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID, "2c72a8d0-4637-42e9-814d-70751bd2a6ea");
            values.put(LocalCacheContract.GeoObjectEntry.COLUMN_NAME_OBJECT, geoObject.toJSON().toString());

            long newRowId = db.insert(LocalCacheContract.GeoObjectEntry.TABLE_NAME, null, values);

            Assert.assertNotEquals(0, db);
        } finally {
            mDbHelper.close();
        }
    }

    @Test
    public void testWriteAndRead() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext();

        /*
         * Setup mock objects
         */
        MockHttpConnector connector = new MockHttpConnector();
        HttpRegistryClient client = new HttpRegistryClient(connector, new MockIdService());

        MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue(""), false, false, FrequencyType.DAILY, client);

        GeoObject geoObject = client.newGeoObjectInstance("State");
        geoObject.setCode("Test");
        geoObject.setUid("blarg");

        /*
         * Create database
         */
        LocalCacheDbHelper mDbHelper = new LocalCacheDbHelper(context);
        try {

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            Assert.assertNotNull(db);

            String uuid = "2c72a8d0-4637-42e9-814d-70751bd2a6ea";
            String json = geoObject.toJSON().toString();

            ContentValues values = new ContentValues();
            values.put(LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID, uuid);
            values.put(LocalCacheContract.GeoObjectEntry.COLUMN_NAME_OBJECT, json);

            long newRowId = db.insert(LocalCacheContract.GeoObjectEntry.TABLE_NAME, null, values);

            Assert.assertNotEquals(0, db);

            db = mDbHelper.getReadableDatabase();

            String[] projection = {
                    BaseColumns._ID,
                    LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID,
                    LocalCacheContract.GeoObjectEntry.COLUMN_NAME_OBJECT
            };

            // Filter results WHERE "title" = 'My Title'
            String selection = LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID + " = ?";
            String[] selectionArgs = {uuid};

            // How you want the results sorted in the resulting Cursor
            String sortOrder = LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID + " DESC";

            Cursor cursor = db.query(
                    LocalCacheContract.GeoObjectEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            Assert.assertTrue(cursor.moveToNext());

            Assert.assertEquals(cursor.getString(1), uuid);
            Assert.assertEquals(cursor.getString(2), json);
        } finally {
            mDbHelper.close();
        }

    }


}
