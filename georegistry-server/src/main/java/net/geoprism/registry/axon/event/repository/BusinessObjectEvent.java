package net.geoprism.registry.axon.event.repository;

public interface BusinessObjectEvent extends RepositoryEvent
{

  String getAggregate();
}
