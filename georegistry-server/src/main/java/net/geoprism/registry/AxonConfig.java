package net.geoprism.registry;

import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jdbc.UnitOfWorkAwareConnectionProviderWrapper;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jdbc.EventTableFactory;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcSQLErrorCodesResolver;
import org.axonframework.eventsourcing.eventstore.jdbc.PostgresEventTableFactory;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jdbc.JdbcSagaStore;
import org.axonframework.queryhandling.DefaultQueryBusSpanFactory;
import org.axonframework.queryhandling.DefaultQueryGateway;
import org.axonframework.queryhandling.DefaultQueryUpdateEmitterSpanFactory;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryBusSpanFactory;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.queryhandling.QueryUpdateEmitterSpanFactory;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.axonframework.queryhandling.SimpleQueryUpdateEmitter;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.tracing.LoggingSpanFactory;
import org.axonframework.tracing.SpanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

import net.geoprism.registry.axon.RunwayRequestConnectionProvider;

@Configuration
public class AxonConfig
{

  @Bean
  public ConnectionProvider connectionProvider()
  {
    return new UnitOfWorkAwareConnectionProviderWrapper(new RunwayRequestConnectionProvider());
  }

  @Bean
  public TransactionManager transactionManager()
  {
    return NoTransactionManager.INSTANCE;
  }

  @Bean
  public Serializer serializer()
  {
    return JacksonSerializer.defaultSerializer();
  }

  @Bean
  public TokenStore tokenStore(Serializer serializer, ConnectionProvider connectionProvider)
  {
    return JdbcTokenStore.builder() //
        .connectionProvider(connectionProvider) //        
        .serializer(serializer) //
        .build();
  }

  @Lazy
  @Bean
  public SagaStore<Object> sagaStore(Serializer serializer, ConnectionProvider connectionProvider)
  {
    return JdbcSagaStore.builder() //
        .connectionProvider(connectionProvider) //
        .serializer(serializer) //
        .build();
  }

  @Bean
  public PersistenceExceptionResolver persistenceExceptionResolver()
  {
    return new JdbcSQLErrorCodesResolver();
  }

  @Bean
  public EventTableFactory tableFactory()
  {
    return PostgresEventTableFactory.INSTANCE;
  }

  @Bean
  public EventStorageEngine eventStorageEngine(Serializer serializer, PersistenceExceptionResolver persistenceExceptionResolver, TransactionManager transactionManager, EventTableFactory tableFactory, ConnectionProvider connectionProvider)
  {

    JdbcEventStorageEngine storageEngine = JdbcEventStorageEngine.builder()//
        .connectionProvider(connectionProvider) //
        .snapshotSerializer(serializer) //
        .persistenceExceptionResolver(persistenceExceptionResolver) //
        .eventSerializer(serializer) //
        .transactionManager(transactionManager) //
        .build();
    // If the schema has not been constructed yet, the createSchema method can
    // be used:
    storageEngine.createSchema(tableFactory);

    return storageEngine;
  }

  @Bean
  public EventStore eventStore(EventStorageEngine storageEngine)
  {
    return EmbeddedEventStore.builder() //
        .storageEngine(storageEngine) //
        .build();
  }

  @Bean
  public SpanFactory spanFactory()
  {
    return LoggingSpanFactory.INSTANCE;
  }

  @Bean
  public QueryUpdateEmitterSpanFactory queryUpdateEmitterSpanFactory(SpanFactory spanFactory)
  {
    return DefaultQueryUpdateEmitterSpanFactory.builder() //
        .spanFactory(spanFactory) //
        .build();
  }

  @Bean
  @Primary
  public QueryUpdateEmitter updateEmitter(QueryUpdateEmitterSpanFactory queryUpdateEmitterSpanFactory)
  {
    return SimpleQueryUpdateEmitter.builder() //
        .spanFactory(queryUpdateEmitterSpanFactory) //
        .build();
  }

  @Bean
  public QueryBusSpanFactory queryBusSpanFactory(SpanFactory spanFactory)
  {
    return DefaultQueryBusSpanFactory.builder() //
        .spanFactory(spanFactory).build();
  }

  @Bean
  public QueryBus queryBus(QueryBusSpanFactory queryBusSpanFactory, TransactionManager transactionManager, QueryUpdateEmitter updateEmitter)
  {
    return SimpleQueryBus.builder() //
        .transactionManager(transactionManager) //
        .queryUpdateEmitter(updateEmitter) //
        .spanFactory(queryBusSpanFactory).build();
  }

  @Bean
  public QueryGateway queryGateway(QueryBus queryBus)
  {
    return DefaultQueryGateway.builder() //
        .queryBus(queryBus) //
        .build();
  }

}
