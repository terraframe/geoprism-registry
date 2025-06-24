package net.geoprism.registry.axon.command;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class GeoObjectCreateCommand extends GeoObjectApplyCommand
{

  public GeoObjectCreateCommand()
  {
    super();
  }

  public GeoObjectCreateCommand(String uid, Boolean isImport, GeoObjectOverTime object, ServerParentTreeNodeOverTime parents, Boolean refreshWorking)
  {
    super(uid, true, isImport, object, parents, refreshWorking);
  }

  public GeoObjectCreateCommand(String uid, Boolean isImport, String object, String parents, Boolean refreshWorking)
  {
    super(uid, true, isImport, object, parents, refreshWorking);
  }

}
