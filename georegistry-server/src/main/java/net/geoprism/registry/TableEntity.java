package net.geoprism.registry;

import com.runwaysdk.system.metadata.MdBusiness;

public interface TableEntity
{

  public String getOid();

  public String getMdBusinessOid();

  public MdBusiness getMdBusiness();

}
