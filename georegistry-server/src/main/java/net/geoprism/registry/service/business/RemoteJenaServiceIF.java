package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Component;

@Component
public interface RemoteJenaServiceIF
{
  void load(String graphName, Model model);

  void update(List<String> statements);

  void clear(String graphName);

  Optional<String> query(String statement);
}
