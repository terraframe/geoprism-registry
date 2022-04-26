package org.commongeoregistry.adapter.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.tree.AddChildActionDTO;
import org.commongeoregistry.adapter.action.geoobject.CreateGeoObjectActionDTO;
import org.commongeoregistry.adapter.action.geoobject.UpdateGeoObjectActionDTO;
import org.commongeoregistry.adapter.android.sql.LocalCacheContract;
import org.commongeoregistry.adapter.android.sql.LocalCacheContract.GeoObjectEntry;
import org.commongeoregistry.adapter.android.sql.LocalCacheContract.TreeNodeEntry;
import org.commongeoregistry.adapter.android.sql.LocalCacheDbHelper;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.dataaccess.TreeNode;
import org.commongeoregistry.adapter.id.EmptyIdCacheException;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * This is a local persisted cache of {@link GeoObject}s and how they relate to each other through relationships
 * for offline use.
 *
 * @author nathan
 * @author rrowlands
 * @author jsmethie
 */
public class LocalObjectCache implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4759560897184243298L;

    /**
     * Index of parent object in the database cursor
     */
    private static final int PARENT_INDEX = 0;

    /**
     * Index of child object in the database cursor
     */
    private static final int CHILD_INDEX = 1;

    /**
     * Index of hierarchy column in the database cursor
     */
    private static final int HIERARCHY_INDEX = 2;

    /*
     * Database helper for managing the local cache sqlite database
     */
    private LocalCacheDbHelper mDbHelper;

    /*
     * Adapter used to deserialize cached object
     */
    private RegistryAdapter adapter;

    public LocalObjectCache(Context context, RegistryAdapter adapter) {
        this.mDbHelper = new LocalCacheDbHelper(context);
        this.adapter = adapter;
    }

    void saveLastPushDate(long lastPush)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(LocalCacheContract.ActionPushHistoryEntry.COLUMN_NAME_ID, 0);
        values.put(LocalCacheContract.ActionPushHistoryEntry.COLUMN_NAME_LAST_PUSH_DATE, lastPush);

        db.insertWithOnConflict(LocalCacheContract.ActionPushHistoryEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private long getLastPushDate(SQLiteDatabase db)
    {
        String[] select_columns = {
            LocalCacheContract.ActionPushHistoryEntry.COLUMN_NAME_LAST_PUSH_DATE
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = LocalCacheContract.ActionPushHistoryEntry.COLUMN_NAME_ID + " = ?";
        String[] selectionArgs = {"0"};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = LocalCacheContract.ActionPushHistoryEntry.COLUMN_NAME_ID + " DESC";

        Cursor cursor = null;
        try {
            cursor = db.query(
                    LocalCacheContract.ActionPushHistoryEntry.TABLE_NAME,   // The table to query
                    select_columns,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            if (!cursor.moveToNext()) {
                return -1;
            }

            long longDate = cursor.getLong(0);

            return longDate;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Returns the number of ids saved in our database.
     */
    public int countNumberRegistryIds()
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.query(
                LocalCacheContract.RegistryIdEntry.TABLE_NAME,
                new String[]{LocalCacheContract.RegistryIdEntry.COLUMN_NAME_ID},
                null,
                null,
                null,
                null,
                null
            );

            return cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    void addRegistryIds(Collection<String> newIds)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        for (String id : newIds) {
            ContentValues values = new ContentValues();
            values.put(LocalCacheContract.RegistryIdEntry.COLUMN_NAME_REGISTRY_ID, id);

            db.insertWithOnConflict(LocalCacheContract.RegistryIdEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
        }
    }

    String nextRegistryId()
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                LocalCacheContract.RegistryIdEntry.COLUMN_NAME_REGISTRY_ID,
                LocalCacheContract.RegistryIdEntry.COLUMN_NAME_ID
        };

        String registryId;
        String id;

        // Fetch an id from the database
        Cursor cursor = null;
        try {
            cursor = db.query(
                    LocalCacheContract.RegistryIdEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null               // The sort order
            );

            if (!cursor.moveToNext())
            {
                throw new EmptyIdCacheException();
            }

            registryId = cursor.getString(0);
            id = cursor.getString(1);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        // Remove the id from the database
        db.delete(
            LocalCacheContract.RegistryIdEntry.TABLE_NAME,
            LocalCacheContract.RegistryIdEntry.COLUMN_NAME_ID + " = ?",
            new String[]{id}
        );


        return registryId;
    }

    /**
     * Returns all action history that has been recorded since the last time we have pushed to the server
     *
     * @return
     */
    public List<AbstractActionDTO> getUnpushedActionHistory()
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        long lastPush = getLastPushDate(db);

        String[] projection = {
            LocalCacheContract.ActionEntry.COLUMN_NAME_ID,
            LocalCacheContract.ActionEntry.COLUMN_NAME_CREATE_ACTION_DATE,
            LocalCacheContract.ActionEntry.COLUMN_NAME_JSON
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = LocalCacheContract.ActionEntry.COLUMN_NAME_CREATE_ACTION_DATE + " >= ?";
        String[] selectionArgs = {String.valueOf(lastPush)};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = LocalCacheContract.ActionEntry.COLUMN_NAME_ID + " ASC";

        ArrayList<AbstractActionDTO> history = new ArrayList<AbstractActionDTO>();

        Cursor cursor = null;
        try {
            cursor = db.query(
                    LocalCacheContract.ActionEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            int i = 0;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                long createActionDate = cursor.getLong(1);
                String json = cursor.getString(2);

                AbstractActionDTO action = AbstractActionDTO.parseAction(json);
                history.add(action);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return history;
    }

    /**
     * Returns the history of all actions performed on the cache. This is used when
     * pushing changes made in offline mode to the server.
     */
    public List<AbstractActionDTO> getAllActionHistory()
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                LocalCacheContract.ActionEntry.COLUMN_NAME_CREATE_ACTION_DATE,
                LocalCacheContract.ActionEntry.COLUMN_NAME_JSON
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = LocalCacheContract.ActionEntry.COLUMN_NAME_ID + " ASC";

        Cursor cursor = null;
        try {
            cursor = db.query(
                    LocalCacheContract.ActionEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            ArrayList<AbstractActionDTO> history = new ArrayList<AbstractActionDTO>();

            int i = 0;
            while (cursor.moveToNext()) {
                long createActionDate = cursor.getLong(0);
                String json = cursor.getString(1);

                AbstractActionDTO action = AbstractActionDTO.parseAction(json);
                history.add(action);
            }

            return history;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Adds the given {@link TreeNode} object to the local cache.
     *
     * @param treeNode
     */
    public void cache(TreeNode treeNode) {
        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            this.cache(treeNode, db);

            // your sql stuff
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to cache tree node. A database error has occurred.", e);
        } finally {
            db.endTransaction();
        }

    }

    private void cache(TreeNode treeNode, SQLiteDatabase db) {
        /*
         * Ensure the object is serialized into the object table
         */
        this.insertGeoObject(treeNode.getGeoObject(), db);

        if (treeNode instanceof ParentTreeNode) {
            ParentTreeNode pNode = (ParentTreeNode) treeNode;

            List<ParentTreeNode> parents = pNode.getParents();

            for (ParentTreeNode parent : parents) {
                /*
                 * Add a record into the relationship table
                 */
                this.insertTreeNode(parent, treeNode, parent.getHierachyType(), db);

                /*
                 * Recursively update all the parents
                 */
                this.cache(parent, db);
            }
        } else if (treeNode instanceof ChildTreeNode) {
            ChildTreeNode cNode = (ChildTreeNode) treeNode;

            List<ChildTreeNode> children = cNode.getChildren();

            for (ChildTreeNode child : children) {
                /*
                 * Add a record into the relationship table
                 */
                this.insertTreeNode(treeNode, child, child.getHierachyType(), db);

                /*
                 * Recursively update all the children
                 */
                this.cache(child, db);
            }
        }
    }

    /**
     * Records the update to the action cache and saves the new relationship to the object cache.
     * When online synchronization happens at a later point this update will be processed.
     *
     * @param childGeoObject
     * @param parentGeoObject
     * @param hierarchyType
     */
//    public void addChild(GeoObject childGeoObject, GeoObject parentGeoObject, HierarchyType hierarchyType) {
//        ChildTreeNode treeNode = new ChildTreeNode(parentGeoObject, hierarchyType);
//        treeNode.addChild(new ChildTreeNode(childGeoObject, hierarchyType));
//
//        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
//        try {
//            db.beginTransaction();
//
//            this.cache(treeNode, db);
//
//            AddChildActionDTO action = new AddChildActionDTO();
//            action.setChildId(childGeoObject.getUid());
//            action.setChildTypeCode(childGeoObject.getType().getCode());
//            action.setParentId(parentGeoObject.getUid());
//            action.setParentTypeCode(parentGeoObject.getType().getCode());
//            action.setHierarchyCode(hierarchyType.getCode());
//            this.insertAction(action, db);
//
//            db.setTransactionSuccessful();
//        } catch (SQLException e) {
//            throw new RuntimeException("Unable to add child. A database error has occurred.", e);
//        } finally {
//            db.endTransaction();
//        }
//    }

    /**
     * Add the given {@link GeoObject} to the local cache.
     *
     * @param geoObject
     */
    public void cache(GeoObject geoObject) {
        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            this.insertGeoObject(geoObject, db);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to cache geo object. A database error has occurred.", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Add the given {@link GeoObjectOverTime} to the local cache.
     *
     * @param geoObject
     */
    public void cache(GeoObjectOverTime geoObject) {
        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            this.insertGeoObjectOverTime(geoObject, db);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to cache geo object. A database error has occurred.", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Records the update to the action cache and saves the updated {@link GeoObject} to the
     * object cache. When online synchronization happens at a later point this update will be
     * processed.
     *
     * @param geoObject
     */
//    public void updateGeoObject(GeoObject geoObject) {
//        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
//        try {
//            db.beginTransaction();
//
//            this.insertGeoObject(geoObject, db);
//
//            UpdateGeoObjectActionDTO action = new UpdateGeoObjectActionDTO();
//            action.setGeoObject(geoObject.toJSON());
//            this.insertAction(action, db);
//
//            db.setTransactionSuccessful();
//        } catch (SQLException e) {
//            throw new RuntimeException("Unable to update geo object. A database error has occurred.", e);
//        } finally {
//            db.endTransaction();
//        }
//    }

    /**
     * Records the update to the action cache and saves the updated {@link GeoObjectOverTime} to the
     * object cache. When online synchronization happens at a later point this update will be
     * processed.
     *
     * @param geoObject
     */
    public void updateGeoObjectOverTime(GeoObjectOverTime geoObject) {
        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            this.insertGeoObjectOverTime(geoObject, db);

            UpdateGeoObjectActionDTO action = new UpdateGeoObjectActionDTO();
            action.setGeoObject(geoObject.toJSON());
            this.insertAction(action, db);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to update geo object. A database error has occurred.", e);
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Records the create to the action cache and saves the new {@link GeoObject} to the
     * object cache. When online synchronization happens at a later point this create will be
     * processed.
     *
     * @param geoObject
     */
//    public void createGeoObject(GeoObject geoObject) {
//        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
//        try {
//            db.beginTransaction();
//
//            this.insertGeoObject(geoObject, db);
//
//            CreateGeoObjectActionDTO action = new CreateGeoObjectActionDTO();
//            action.setGeoObject(geoObject.toJSON());
//            this.insertAction(action, db);
//
//            db.setTransactionSuccessful();
//        } catch (SQLException e) {
//            throw new RuntimeException("Unable to create geo object. A database error has occurred.", e);
//        } finally {
//            db.endTransaction();
//        }
//    }

    /**
     * Records the create to the action cache and saves the new {@link GeoObject} to the
     * object cache. When online synchronization happens at a later point this create will be
     * processed.
     *
     * @param geoObject
     */
    public void createGeoObjectOverTime(GeoObjectOverTime geoObject) {
        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
        try {
            db.beginTransaction();

            this.insertGeoObjectOverTime(geoObject, db);

            CreateGeoObjectActionDTO action = new CreateGeoObjectActionDTO();
            action.setGeoObject(geoObject.toJSON());
            this.insertAction(action, db);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to create geo object. A database error has occurred.", e);
        } finally {
            db.endTransaction();
        }
    }

    private void insertAction(AbstractActionDTO action, SQLiteDatabase db)
    {
        ContentValues values = new ContentValues();
        values.put(LocalCacheContract.ActionEntry.COLUMN_NAME_JSON, action.toJSON().toString());
        values.put(LocalCacheContract.ActionEntry.COLUMN_NAME_CREATE_ACTION_DATE, action.getCreateActionDate().getTime());

        db.insertWithOnConflict(LocalCacheContract.ActionEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
    }

    private void insertGeoObject(GeoObject geoObject, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(GeoObjectEntry.COLUMN_NAME_UID, geoObject.getUid());
        values.put(GeoObjectEntry.COLUMN_NAME_OBJECT, geoObject.toJSON().toString());

        db.insertWithOnConflict(GeoObjectEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void insertGeoObjectOverTime(GeoObjectOverTime geoObject, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(GeoObjectEntry.COLUMN_NAME_UID, geoObject.getUid());
        values.put(GeoObjectEntry.COLUMN_NAME_OBJECT, geoObject.toJSON().toString());

        db.insertWithOnConflict(GeoObjectEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void insertTreeNode(TreeNode parent, TreeNode child, HierarchyType hierachyType, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(TreeNodeEntry.COLUMN_NAME_PARENT, parent.getGeoObject().getUid());
        values.put(TreeNodeEntry.COLUMN_NAME_CHILD, child.getGeoObject().getUid());
        values.put(TreeNodeEntry.COLUMN_NAME_HIERARCHY, hierachyType.getCode());

        db.insertWithOnConflict(TreeNodeEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Returns the GeoObject with the given UID. The object must have been previously cached before you may
     * call this method.
     *
     * @param _uid UID of the GeoObject.
     * @return GeoObject with the given UID.
     */
    public GeoObject getGeoObject(String _uid) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                GeoObjectEntry.COLUMN_NAME_UID,
                GeoObjectEntry.COLUMN_NAME_OBJECT
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID + " = ?";
        String[] selectionArgs = {_uid};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID + " DESC";

        Cursor cursor = null;
        try {


            cursor = db.query(
                    LocalCacheContract.GeoObjectEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            if (!cursor.moveToNext()) {
                throw new RuntimeException("GeoObject with uid [" + _uid + "] does not exist in the local cache");
            }

            return GeoObject.fromJSON(this.adapter, cursor.getString(2));

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Returns the GeoObjectOverTime with the given UID. The object must have been previously cached before you may
     * call this method.
     *
     * @param _uid UID of the GeoObjectOverTime.
     * @return GeoObjectOverTime with the given UID.
     */
    public GeoObjectOverTime getGeoObjectOverTime(String _uid) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                GeoObjectEntry.COLUMN_NAME_UID,
                GeoObjectEntry.COLUMN_NAME_OBJECT
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID + " = ?";
        String[] selectionArgs = {_uid};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = LocalCacheContract.GeoObjectEntry.COLUMN_NAME_UID + " DESC";

        Cursor cursor = null;
        try {


            cursor = db.query(
                    LocalCacheContract.GeoObjectEntry.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            if (!cursor.moveToNext()) {
                throw new RuntimeException("GeoObject with uid [" + _uid + "] does not exist in the local cache");
            }

            return GeoObjectOverTime.fromJSON(this.adapter, cursor.getString(2));

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * Returns the {@link GeoObject} with the given UID and its children of the given types.
     * <p>
     * Shall we include the hierarchy types as a parameter as well?
     *
     * @param parentUid     UID of the parent {@link GeoObject}
     * @param childrenTypes an array of object types.
     * @param recursive     true if all recursive children should be fetched, or false if only immediate
     *                      children should be fetched.
     * @return {@link ChildTreeNode} containing the {@link GeoObject} with the given UID and its
     * children of the given types.
     */
    public ChildTreeNode getChildGeoObjects(String parentUid, String[] childrenTypes, Boolean recursive) {
        GeoObject parent = this.getGeoObject(parentUid);

        ChildTreeNode node = new ChildTreeNode(parent, null);
        this.addChildren(node, childrenTypes, recursive);

        return node;
    }

    /**
     * Adds all children from the local cache to the provided ChildTreeNode.
     *
     * @param parent The tree node to add the children to.
     * @param childrenTypes The type code of the children to fetch
     * @param recursive If set to true we will include the full descendent graph (all children of children)
     */
    public void addChildren(ChildTreeNode parent, String[] childrenTypes, Boolean recursive) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        List<ChildTreeNode> children = new LinkedList<ChildTreeNode>();

        Cursor cursor = null;

        try {
            cursor = this.getChildrenCursor(parent.getGeoObject().getUid(), childrenTypes, db);

            while (cursor.moveToNext()) {
                children.add(this.getChildTreeNode(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (ChildTreeNode child : children) {
            if (recursive) {
                this.addChildren(child, childrenTypes, recursive);
            }

            parent.addChild(child);
        }
    }

    @NonNull
    private ChildTreeNode getChildTreeNode(Cursor cursor) {
        GeoObject child = GeoObject.fromJSON(this.adapter, cursor.getString(CHILD_INDEX));
        String code = cursor.getString(HIERARCHY_INDEX);

        Optional<HierarchyType> oHierarchyType = this.adapter.getMetadataCache().getHierachyType(code);

        if (!oHierarchyType.isPresent()) {
            throw new RuntimeException("Unknown hierarchy type for code [" + code + "]");
        }

        HierarchyType hierarchyType = oHierarchyType.get();

        return new ChildTreeNode(child, hierarchyType);
    }

    private Cursor getChildrenCursor(String parentId, String[] childrenTypes, SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p." + GeoObjectEntry.COLUMN_NAME_OBJECT);
        sql.append(", c." + GeoObjectEntry.COLUMN_NAME_OBJECT);
        sql.append(", n." + TreeNodeEntry.COLUMN_NAME_HIERARCHY);
        sql.append(" FROM " + TreeNodeEntry.TABLE_NAME + " AS n");
        sql.append(" INNER JOIN " + GeoObjectEntry.TABLE_NAME + " AS p ON p." + GeoObjectEntry.COLUMN_NAME_UID + " = n." + TreeNodeEntry.COLUMN_NAME_PARENT);
        sql.append(" INNER JOIN " + GeoObjectEntry.TABLE_NAME + " AS c ON c." + GeoObjectEntry.COLUMN_NAME_UID + " = n." + TreeNodeEntry.COLUMN_NAME_CHILD);
        sql.append(" WHERE n." + TreeNodeEntry.COLUMN_NAME_PARENT + " = ?");
        sql.append(" AND n." + TreeNodeEntry.COLUMN_NAME_HIERARCHY + " IN (" + TextUtils.join(",", Collections.nCopies(childrenTypes.length, "?")) + ")");

        List<String> params = new LinkedList<String>();
        params.add(parentId);
        params.addAll(Arrays.asList(childrenTypes));

        return db.rawQuery(sql.toString(), params.toArray(new String[params.size()]));
    }


    /**
     * Returns the {@link GeoObject} with the given UID and its parent of the given types.
     * <p>
     * Shall we include the hierarchy types as a parameter as well?
     *
     * @param childUid    UID of the child {@link GeoObject}
     * @param parentTypes an array of object types.
     * @param recursive   true if all recursive parents should be fetched, or false if only immediate
     *                    recursive should be fetched.
     * @return {@link ParentTreeNode} containing the {@link GeoObject} with the given UID and its
     * children of the given types.
     */
    public ParentTreeNode getParentGeoObjects(String childUid, String[] parentTypes, Boolean recursive) {
        GeoObject child = this.getGeoObject(childUid);

        ParentTreeNode node = new ParentTreeNode(child, null);
        this.addParents(node, parentTypes, recursive);

        return node;
    }

    /**
     * Adds all parents from the local cache to the provided ParentTreeNode.
     *
     * @param child The tree node to add the children to.
     * @param parentTypes The type code of the parents to fetch.
     * @param recursive If set to true we will include the full ancestor graph (all parents of parents)
     */
    public void addParents(ParentTreeNode child, String[] parentTypes, Boolean recursive) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        List<ParentTreeNode> parents = new LinkedList<ParentTreeNode>();

        Cursor cursor = null;

        try {
            cursor = this.getParentCursor(child.getGeoObject().getUid(), parentTypes, db);

            while (cursor.moveToNext()) {
                parents.add(this.getParentTreeNode(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (ParentTreeNode parent : parents) {
            if (recursive) {
                this.addParents(parent, parentTypes, recursive);
            }

            child.addParent(parent);
        }
    }

    @NonNull
    private ParentTreeNode getParentTreeNode(Cursor cursor) {
        GeoObject parent = GeoObject.fromJSON(this.adapter, cursor.getString(PARENT_INDEX));
        String code = cursor.getString(HIERARCHY_INDEX);

        Optional<HierarchyType> oHierarchyType = this.adapter.getMetadataCache().getHierachyType(code);

        if (!oHierarchyType.isPresent()) {
            throw new RuntimeException("Unknown hierarchy type for code [" + code + "]");
        }

        HierarchyType hierarchyType = oHierarchyType.get();

        return new ParentTreeNode(parent, hierarchyType);
    }

    private Cursor getParentCursor(String childId, String[] parentTypes, SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p." + GeoObjectEntry.COLUMN_NAME_OBJECT);
        sql.append(", c." + GeoObjectEntry.COLUMN_NAME_OBJECT);
        sql.append(", n." + TreeNodeEntry.COLUMN_NAME_HIERARCHY);
        sql.append(" FROM " + TreeNodeEntry.TABLE_NAME + " AS n");
        sql.append(" INNER JOIN " + GeoObjectEntry.TABLE_NAME + " AS p ON p." + GeoObjectEntry.COLUMN_NAME_UID + " = n." + TreeNodeEntry.COLUMN_NAME_PARENT);
        sql.append(" INNER JOIN " + GeoObjectEntry.TABLE_NAME + " AS c ON c." + GeoObjectEntry.COLUMN_NAME_UID + " = n." + TreeNodeEntry.COLUMN_NAME_CHILD);
        sql.append(" WHERE n." + TreeNodeEntry.COLUMN_NAME_CHILD + " = ?");
        sql.append(" AND n." + TreeNodeEntry.COLUMN_NAME_HIERARCHY + " IN (" + TextUtils.join(",", Collections.nCopies(parentTypes.length, "?")) + ")");

        List<String> params = new LinkedList<String>();
        params.add(childId);
        params.addAll(Arrays.asList(parentTypes));

        return db.rawQuery(sql.toString(), params.toArray(new String[params.size()]));
    }

    /**
     * Drops all data from the database. Use carefully.
     */
    public void clear() {
        this.mDbHelper.recreate(this.mDbHelper.getWritableDatabase());
    }


    void close() {
        this.mDbHelper.close();
    }
}
