/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
