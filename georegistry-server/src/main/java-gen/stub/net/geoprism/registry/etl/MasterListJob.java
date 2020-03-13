package net.geoprism.registry.etl;

import org.json.JSONObject;

public abstract class MasterListJob extends MasterListJobBase
{
  private static final long serialVersionUID = 23840799;

  public MasterListJob()
  {
    super();
  }

  public abstract JSONObject toJSON();

}
