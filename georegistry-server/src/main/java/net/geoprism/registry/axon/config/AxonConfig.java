package net.geoprism.registry.axon.config;

import java.sql.SQLException;

import org.axonframework.common.jdbc.ConnectionProvider;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jdbc.UnitOfWorkAwareConnectionProviderWrapper;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBusSpanFactory;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.JdbcTokenStore;
import org.axonframework.eventhandling.tokenstore.jdbc.PostgresTokenTableFactory;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcEventStorageEngine.Builder;
import org.axonframework.eventsourcing.eventstore.jdbc.JdbcSQLErrorCodesResolver;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jdbc.JdbcSagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
public class AxonConfig
{
  @Autowired
  public void configureProcessingGroupErrorHandling(EventProcessingConfigurer processingConfigurer)
  {
    processingConfigurer.usingSubscribingEventProcessors();
    processingConfigurer.registerDefaultErrorHandler(conf -> PropagatingErrorHandler.instance());
  }

  @Bean
  public ListenerInvocationErrorHandler listenerInvocationErrorHandler()
  {
    return PropagatingErrorHandler.INSTANCE;
  }

  @Bean
  public ConnectionProvider connectionProvider()
  {
    // return new RunwayRequestConnectionProvider();
    return new UnitOfWorkAwareConnectionProviderWrapper(new RunwayRequestConnectionProvider());
  }

  @Bean
  public TransactionManager transactionManager()
  {
    // return new RunwayTransactionManager();
    return NoTransactionManager.INSTANCE;
  }

  // @Bean
  // public SpanFactory spanFactory()
  // {
  // return LoggingSpanFactory.INSTANCE;
  // }
  //
  // @Bean
  // public RevisionResolver revisionResolver()
  // {
  // return new AnnotationRevisionResolver();
  // }
  //
  // @Bean
  // public Serializer serializer(RevisionResolver revisionResolver)
  // {
  // ChainingConverter converter = new ChainingConverter(beanClassLoader);
  //
  // return JacksonSerializer.builder() //
  // .revisionResolver(revisionResolver) //
  // .converter(converter) //
  // .objectMapper(new ObjectMapper()).build();
  // }
  //
  @Bean
  public TokenStore tokenStore(Serializer serializer, ConnectionProvider connectionProvider)
  {
    JdbcTokenStore store = JdbcTokenStore.builder() //
        .connectionProvider(connectionProvider) //
        .serializer(serializer) //
        .build();

    store.createSchema(PostgresTokenTableFactory.INSTANCE);

    return store;
  }

  @Lazy
  @Bean
  public SagaStore<Object> sagaStore(Serializer serializer, ConnectionProvider connectionProvider) throws SQLException
  {
    JdbcSagaStore store = JdbcSagaStore.builder() //
        .connectionProvider(connectionProvider) //
        .serializer(serializer) //
        .build();

    store.createSchema();

    return store;
  }

  @Bean
  @Primary
  public Serializer defaultSerializer()
  {
    return JacksonSerializer.builder().lenientDeserialization().build();
  }

  @Bean
  public PersistenceExceptionResolver persistenceExceptionResolver()
  {
    return new JdbcSQLErrorCodesResolver();
  }

  @Bean
  public EventStorageEngine eventStorageEngine(Serializer serializer, //
      PersistenceExceptionResolver persistenceExceptionResolver, //
      TransactionManager transactionManager, //
      ConnectionProvider connectionProvider)
  {

    Builder builder = JdbcEventStorageEngine.builder()//
        .connectionProvider(connectionProvider) //
        .snapshotSerializer(serializer) //
        .persistenceExceptionResolver(persistenceExceptionResolver) //
        .eventSerializer(serializer) //
        .transactionManager(transactionManager) //
        .appendEvents(CustomJdbcEventStorageEngineStatements::appendEvents);

    RegistryEventStorageEngine storageEngine = new RegistryEventStorageEngine(builder, transactionManager);

    // If the schema has not been constructed yet, the createSchema method can
    // be used:
    storageEngine.createSchema(CustomPostgresEventTableFactory.INSTANCE);

    return storageEngine;
  }

  @Qualifier("eventStore")
  @Bean(name = "eventBus")
  public RegistryEventStore eventStore(EventStorageEngine storageEngine, EventBusSpanFactory eventBusSpanFactory)
  {
    org.axonframework.eventsourcing.eventstore.EmbeddedEventStore.Builder builder = EmbeddedEventStore.builder() //
        .storageEngine(storageEngine) //
        // .messageMonitor(configuration.messageMonitor(EventStore.class,
        // "eventStore")) //
        .spanFactory(eventBusSpanFactory);

    return new RegistryEventStore(builder);
  }

  // @Bean
  // public EventBusSpanFactory eventBusSpanFactory(SpanFactory spanFactory)
  // {
  // return DefaultEventBusSpanFactory.builder() //
  // .spanFactory(spanFactory) //
  // .build();
  // }
  //
  // @Qualifier("eventStore")
  // @Bean(name = "eventBus")
  // public EmbeddedEventStore eventStore(EventStorageEngine storageEngine,
  // EventBusSpanFactory eventBusSpanFactory)
  // {
  // return EmbeddedEventStore.builder() //
  // .storageEngine(storageEngine) //
  // // .messageMonitor(configuration.messageMonitor(EventStore.class,
  // // "eventStore")) //
  // .spanFactory(eventBusSpanFactory) //
  // .build();
  // }
  //
  // @Bean
  // public EventGateway eventGateway(EventBus eventBus)
  // {
  // return DefaultEventGateway.builder().eventBus(eventBus).build();
  // }
  //
  // @Bean
  // public DuplicateCommandHandlerResolver duplicateCommandHandlerResolver()
  // {
  // return LoggingDuplicateCommandHandlerResolver.instance();
  // }
  //
  // @Bean
  // public QueryUpdateEmitterSpanFactory
  // queryUpdateEmitterSpanFactory(SpanFactory spanFactory)
  // {
  // return DefaultQueryUpdateEmitterSpanFactory.builder() //
  // .spanFactory(spanFactory) //
  // .build();
  // }
  //
  // @Bean
  // @Primary
  // public QueryUpdateEmitter updateEmitter(QueryUpdateEmitterSpanFactory
  // queryUpdateEmitterSpanFactory)
  // {
  // return SimpleQueryUpdateEmitter.builder() //
  // .spanFactory(queryUpdateEmitterSpanFactory) //
  // .build();
  // }
  //
  // @Bean
  // public QueryBusSpanFactory queryBusSpanFactory(SpanFactory spanFactory)
  // {
  // return DefaultQueryBusSpanFactory.builder() //
  // .spanFactory(spanFactory) //
  // .build();
  // }
  //
  // @Bean
  // public SimpleQueryBus queryBus(QueryBusSpanFactory queryBusSpanFactory,
  // QueryUpdateEmitter updateEmitter, TransactionManager transactionManager)
  // {
  // return SimpleQueryBus.builder() //
  // // .messageMonitor(axonConfiguration.messageMonitor(QueryBus.class,
  // // "queryBus")) //
  // .transactionManager(transactionManager) //
  // .errorHandler(LoggingQueryInvocationErrorHandler.builder().build()) //
  // .queryUpdateEmitter(updateEmitter) //
  // .spanFactory(queryBusSpanFactory) //
  // .build();
  // }
  //
  // @Bean
  // public QueryGateway queryGateway(QueryBus queryBus)
  // {
  // return DefaultQueryGateway.builder() //
  // .queryBus(queryBus) //
  // .build();
  // }
  //
  // @Bean
  // public CommandBusSpanFactory commandBusSpanFactory(SpanFactory spanFactory)
  // {
  // return DefaultCommandBusSpanFactory.builder() //
  // .spanFactory(spanFactory) //
  // .build();
  // }
  //
  // @Bean
  // public SimpleCommandBus commandBus(TransactionManager txManager,
  // CommandBusSpanFactory commandBusSpanFactory,
  // DuplicateCommandHandlerResolver duplicateCommandHandlerResolver)
  // {
  // SimpleCommandBus commandBus = SimpleCommandBus.builder() //
  // .transactionManager(txManager) //
  // .duplicateCommandHandlerResolver(duplicateCommandHandlerResolver) //
  // .spanFactory(commandBusSpanFactory) // .messageMonitor() //
  // .build();
  //
  // // commandBus.registerHandlerInterceptor(new
  // //
  // CorrelationDataInterceptor<>(axonConfiguration.correlationDataProviders()));
  //
  // return commandBus;
  // }
  //
  // @Bean
  // public CommandGateway commandGateway(CommandBus commandBus)
  // {
  // return DefaultCommandGateway.builder().commandBus(commandBus).build();
  // }
  //
  // @Override
  // public void setBeanClassLoader(@Nonnull ClassLoader classLoader)
  // {
  // this.beanClassLoader = classLoader;
  // }
}
