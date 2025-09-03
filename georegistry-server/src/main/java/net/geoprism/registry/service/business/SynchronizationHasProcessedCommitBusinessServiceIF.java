package net.geoprism.registry.service.business;

import org.springframework.stereotype.Component;

import net.geoprism.registry.Commit;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.SynchronizationHasProcessedCommit;

@Component
public interface SynchronizationHasProcessedCommitBusinessServiceIF
{

  boolean hasBeenPublished(SynchronizationConfig export, Commit commit);

  SynchronizationHasProcessedCommit create(SynchronizationConfig export, Commit commit);

}
