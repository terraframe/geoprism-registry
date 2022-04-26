package org.commongeoregistry.adapter.android.sql;

import android.provider.BaseColumns;

public final class LocalCacheContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private LocalCacheContract() {
    }

    /* Inner class that defines the table contents */
    public static class GeoObjectEntry implements BaseColumns {
        public static final String TABLE_NAME = "geo_object";
        public static final String COLUMN_NAME_OBJECT = "object"; // The serialized json
        public static final String COLUMN_NAME_UID = "uid";
    }

    /* Inner class that defines the table contents */
    public static class TreeNodeEntry implements BaseColumns {
        public static final String TABLE_NAME = "tree_node";
        public static final String COLUMN_NAME_PARENT = "parent";
        public static final String COLUMN_NAME_CHILD = "child";
        public static final String COLUMN_NAME_HIERARCHY = "hierarchy";
    }

    public static class ActionEntry implements BaseColumns {
        public static final String TABLE_NAME = "action_history";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_JSON = "json";
        public static final String COLUMN_NAME_CREATE_ACTION_DATE = "create_action_date";
    }

    public static class ActionPushHistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "action_push_history";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_LAST_PUSH_DATE = "last_push_date";
    }

    public static class RegistryIdEntry implements BaseColumns {
        public static final String COLUMN_NAME_ID = "id";
        public static final String TABLE_NAME = "registry_ids";
        public static final String COLUMN_NAME_REGISTRY_ID = "registryid";
    }
}
