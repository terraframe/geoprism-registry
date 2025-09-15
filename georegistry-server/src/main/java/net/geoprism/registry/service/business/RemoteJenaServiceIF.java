package net.geoprism.registry.service.business;

import java.util.List;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.springframework.stereotype.Component;

import net.geoprism.registry.etl.JenaExportConfig;

@Component
public interface RemoteJenaServiceIF
{
  void load(Model model, JenaExportConfig config);

  void update(List<String> statements, JenaExportConfig config);

  void clear(JenaExportConfig config);

  Optional<String> query(String statement, JenaExportConfig config);
}
