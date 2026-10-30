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
import java.util.List;

import org.axonframework.eventsourcing.eventstore.jdbc.AbstractEventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;

/**
 * Jdbc table factory for Postgresql databases.
 *
 * @author Rene de Waele
 * @since 3.0
 */
public class RegistryPostgresEventTableFactory extends AbstractEventTableFactory
{

  /**
   * Singleton PostgresEventTableFactory instance
   */
  public static final RegistryPostgresEventTableFactory INSTANCE = new RegistryPostgresEventTableFactory();

  private RegistryPostgresEventTableFactory()
  {
  }

  @Override
  public PreparedStatement createDomainEventTable(Connection connection, EventSchema schema) throws SQLException
  {
    String sql = "CREATE TABLE IF NOT EXISTS " + schema.domainEventTable() + " (\n" + //
        schema.globalIndexColumn() + " " + idColumnType() + " NOT NULL,\n" + //
        schema.aggregateIdentifierColumn() + " VARCHAR(255) NOT NULL,\n" + //
        schema.sequenceNumberColumn() + " BIGINT NOT NULL,\n" + //
        schema.typeColumn() + " VARCHAR(255),\n" + //
        schema.eventIdentifierColumn() + " VARCHAR(255) NOT NULL,\n" + //
        schema.metaDataColumn() + " " + payloadType() + ",\n" + //
        schema.payloadColumn() + " " + payloadType() + " NOT NULL,\n" + //
        schema.payloadRevisionColumn() + " VARCHAR(255),\n" + //
        schema.payloadTypeColumn() + " VARCHAR(255) NOT NULL,\n" + //
        schema.timestampColumn() + " " + timestampType() + " ,\n" + //
        "base_object_id" + " " + " VARCHAR(255), \n" + //
        "phase" + " " + " VARCHAR(255), \n" + //
        "commit_id" + " " + " VARCHAR(255),\n" + //
        "PRIMARY KEY (" + schema.globalIndexColumn() + "),\n" + //
        "UNIQUE (" + schema.aggregateIdentifierColumn() + ", " + schema.sequenceNumberColumn() + "),\n" + //
        "UNIQUE (" + schema.eventIdentifierColumn() + ")\n" + //
        ")";
    return connection.prepareStatement(sql);
  }

  public PreparedStatement createBaseObjectIndex(Connection connection, EventSchema schema) throws SQLException
  {
    String sql = "CREATE INDEX IF NOT EXISTS event_base_object_id ON " + schema.domainEventTable() + " USING btree (\n" + //
        RegistryEventStore.BASE_OBJECT + " \n" + //
        ")";

    return connection.prepareStatement(sql);
  }

  public PreparedStatement createCommitIndex(Connection connection, EventSchema schema) throws SQLException
  {
    String sql = "CREATE INDEX IF NOT EXISTS event_commit ON " + schema.domainEventTable() + " USING btree (\n" + //
        "commit_id \n" + //
        ")";

    return connection.prepareStatement(sql);
  }

  public PreparedStatement createLookupIndex(Connection connection, EventSchema schema) throws SQLException
  {
    String sql = "CREATE INDEX IF NOT EXISTS event_lookup ON " + schema.domainEventTable() + " USING btree (\n" + //
        "commit_id, \n" + //
        RegistryEventStore.PHASE + ", \n" + //
        schema.globalIndexColumn() + ", \n" + //
        RegistryEventStore.BASE_OBJECT + " \n" + //
        ")";

    return connection.prepareStatement(sql);
  }

  @Override
  protected String idColumnType()
  {
    return "BIGSERIAL";
  }

  @Override
  protected String payloadType()
  {
    return "bytea";
  }

}
