package net.geoprism.registry.axon.command;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class CreateGeoObjectCommand extends ApplyGeoObjectCommand
{

  public CreateGeoObjectCommand()
  {
    super();
  }

  public CreateGeoObjectCommand(String uid, Boolean isImport, GeoObjectOverTime object, ServerParentTreeNodeOverTime parents)
  {
    super(uid, true, isImport, object, parents);
  }

  public CreateGeoObjectCommand(String uid, Boolean isImport, String object, String parents)
  {
    super(uid, true, isImport, object, parents);
  }

}
