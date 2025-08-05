package net.geoprism.registry.axon.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.axonframework.eventsourcing.eventstore.jdbc.EventSchema;

import net.geoprism.registry.Commit;

@FunctionalInterface
public interface IndexOfCommitStatementBuilder {

    /**
     * Creates a statement to be used at {@link JdbcEventStorageEngine#lastIndexFor(String)}
     *
     * @param connection          The connection to the database.
     * @param schema              The EventSchema to be used
     * @param aggregateIdentifier The identifier of the aggregate.
     * @return the newly created {@link PreparedStatement}.
     * @throws SQLException when an exception occurs while creating the prepared statement.
     */
    PreparedStatement build(Connection connection, EventSchema schema, Commit commit) throws SQLException;
}
