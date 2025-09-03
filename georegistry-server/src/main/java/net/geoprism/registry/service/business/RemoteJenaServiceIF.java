package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Component;

import net.geoprism.registry.etl.JenaExportConfig;

@Component
public interface RemoteJenaServiceIF
{
  void load(String graphName, Model model, JenaExportConfig config);

  void update(List<String> statements, JenaExportConfig config);

  void clear(String graphName, JenaExportConfig config);

  Optional<String> query(String statement, JenaExportConfig config);
}
