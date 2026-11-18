/**
 *
 */
package net.geoprism.registry;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import net.geoprism.registry.service.business.CommitBusinessServiceIF;
import net.geoprism.registry.service.business.PublishBusinessServiceIF;

public abstract class PublishDatasetTest extends EventDatasetTest implements InstanceTestClassListener
{
  @Autowired
  protected PublishBusinessServiceIF publishService;

  @Autowired
  protected CommitBusinessServiceIF  commitService;

  public void delete(Publish publish)
  {
    // Delete all of the publishes that were generated because of
    // dependencies
    List<Publish> dependencies = this.commitService.getCommits(publish) //
        .stream() //
        .flatMap(c -> this.commitService.getDependencies(c).stream()) //
        .map(d -> d.getPublish()) //
        .distinct() //
        .filter(p -> !p.getOid().equals(publish.getOid())).toList();

    try
    {
      publishService.delete(publish);
    }
    finally
    {
      dependencies.forEach(publishService::delete);
    }
  }

}
