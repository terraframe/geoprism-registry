package net.geoprism.registry.service.business;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import net.geoprism.registry.Publish;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.graph.ExternalSystem;

@Component
public interface SynchronizationConfigBusinessServiceIF
{
  public List<SynchronizationConfig> getSynchronizations(Publish publish);

  public void delete(SynchronizationConfig synchronization);

  List<SynchronizationConfig> getAll(ExternalSystem system);

  SynchronizationConfig deserialize(JsonObject json, boolean lock);

  SynchronizationConfig deserialize(JsonObject json);

  List<SynchronizationConfig> getSynchronizationConfigsForOrg(Integer pageNumber, Integer pageSize);

  long getCount();

  List<? extends DataExportJob> getJobs(SynchronizationConfig synchronization);

  void apply(SynchronizationConfig config);

}
