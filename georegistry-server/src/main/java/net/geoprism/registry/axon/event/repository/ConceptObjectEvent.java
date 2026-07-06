package net.geoprism.registry.axon.event.repository;

public interface ConceptObjectEvent extends RepositoryEvent
{

  String getBaseObjectId();
}
