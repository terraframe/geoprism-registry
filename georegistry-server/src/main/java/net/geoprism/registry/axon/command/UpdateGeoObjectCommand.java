package net.geoprism.registry.axon.command;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class UpdateGeoObjectCommand extends ApplyGeoObjectCommand
{

  public UpdateGeoObjectCommand()
  {
    super();
  }

  public UpdateGeoObjectCommand(String uid, Boolean isImport, GeoObjectOverTime object, ServerParentTreeNodeOverTime parents)
  {
    super(uid, false, isImport, object, parents);
  }

  public UpdateGeoObjectCommand(String uid, Boolean isImport, String object, String parents)
  {
    super(uid, false, isImport, object, parents);
  }

}
