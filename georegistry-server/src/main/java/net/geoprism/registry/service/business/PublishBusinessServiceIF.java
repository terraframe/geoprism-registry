package net.geoprism.registry.service.business;

import java.util.Optional;

import org.springframework.stereotype.Component;

import net.geoprism.registry.Publish;
import net.geoprism.registry.view.PublishDTO;

@Component
public interface PublishBusinessServiceIF
{

  void delete(Publish publish);

  Publish get(String oid);

  Publish create(PublishDTO configuration);

  Optional<Publish> getByUid(String uid);

}
