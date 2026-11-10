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
package net.geoprism.registry.axon.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;

/**
 * Contract which defines how to build a PreparedStatement for use on {@link JdbcEventStorageEngine#fetchDomainEvents(String,
 * long, int)}
 *
 * @author Lucas Campos
 * @since 4.3
 */
@FunctionalInterface
public interface ReadEventDataForBaseObjectStatementBuilder {

    /**
     * Creates a statement to read domain event entries for an aggregate with given identifier starting with the first
     * entry having a sequence number that is equal or larger than the given {@code firstSequenceNumber}.
     *
     * @param connection          The connection to the database.
     * @param schema              The EventSchema to be used
     * @param firstSequenceNumber The expected sequence number of the first returned entry.
     * @param batchSize           The number of items to include in the batch.
     * @param identifier          The identifier of the aggregate.
     * @return The newly created {@link PreparedStatement}.
     * @throws SQLException when an exception occurs while creating the prepared statement.
     */
    PreparedStatement build(Connection connection, EventSchema schema, String baseObjectId, long firstIndex, Long lastIndex) throws SQLException;
}
