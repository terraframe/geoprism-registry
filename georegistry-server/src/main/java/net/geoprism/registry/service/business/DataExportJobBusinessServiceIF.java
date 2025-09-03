package net.geoprism.registry.service.business;

import org.springframework.stereotype.Component;

import com.runwaysdk.system.scheduler.ExecutionContext;

import net.geoprism.registry.etl.export.DataExportJob;

@Component
public interface DataExportJobBusinessServiceIF
{

  void execute(DataExportJob job, ExecutionContext executionContext) throws Throwable;

}
