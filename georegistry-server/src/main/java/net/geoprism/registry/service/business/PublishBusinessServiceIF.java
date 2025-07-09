package net.geoprism.registry.service.business;

import java.util.List;

import org.springframework.stereotype.Component;

import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.view.EventPublishingConfiguration;

@Component
public interface PublishBusinessServiceIF
{

  void delete(Publish publish);

  List<Commit> getCommits(Publish publish);

  Publish get(String oid);

  Publish create(EventPublishingConfiguration configuration);

  Commit getMostRecentCommit(Publish publish);

}
