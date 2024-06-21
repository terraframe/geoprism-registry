package net.geoprism.registry.service.business;

import java.io.OutputStream;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessService.GeometryExportType;

public interface LabeledPropertyGraphRDFExportBusinessServiceIF
{
  public void export(LabeledPropertyGraphTypeVersion version, GeometryExportType geomExportType, OutputStream os);
  
  
}
