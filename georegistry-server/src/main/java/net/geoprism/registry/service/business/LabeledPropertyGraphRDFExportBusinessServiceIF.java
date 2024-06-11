package net.geoprism.registry.service.business;

import java.io.OutputStream;

import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

public interface LabeledPropertyGraphRDFExportBusinessServiceIF
{
  public void export(LabeledPropertyGraphTypeVersion version, boolean writeGeometries, OutputStream os);
  
  
}
