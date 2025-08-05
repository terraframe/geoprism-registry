package net.geoprism.registry.axon.config;

import org.axonframework.eventhandling.GenericDomainEventEntry;

public class IndexGenericDomainEventEntry<T> extends GenericDomainEventEntry<T> implements IndexDomainEventData<T>
{
  private Long globalIndex;

  public IndexGenericDomainEventEntry(Long globalIndex, String type, String aggregateIdentifier, long sequenceNumber, String eventIdentifier, Object timestamp, String payloadType, String payloadRevision, T payload, T metaData)
  {
    super(type, aggregateIdentifier, sequenceNumber, eventIdentifier, timestamp, payloadType, payloadRevision, payload, metaData);

    this.globalIndex = globalIndex;
  }

  @Override
  public Long getGlobalIndex()
  {
    return this.globalIndex;
  }

}
