package net.geoprism.registry.axon.command;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class CreateGeoObjectCommand extends ApplyGeoObjectCommand
{

  public CreateGeoObjectCommand()
  {
    super();
  }

  public CreateGeoObjectCommand(String uid, Boolean isImport, GeoObjectOverTime object, ServerParentTreeNodeOverTime parents, Boolean refreshWorking)
  {
    super(uid, true, isImport, object, parents, refreshWorking);
  }

  public CreateGeoObjectCommand(String uid, Boolean isImport, String object, String parents, Boolean refreshWorking)
  {
    super(uid, true, isImport, object, parents, refreshWorking);
  }

}
