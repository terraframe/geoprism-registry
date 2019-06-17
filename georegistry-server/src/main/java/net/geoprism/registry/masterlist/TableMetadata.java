package net.geoprism.registry.masterlist;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.runwaysdk.system.metadata.MdAttribute;
import com.runwaysdk.system.metadata.MdBusiness;

public class TableMetadata
{
  private MdBusiness                    mdBusiness;

  private Map<MdAttribute, MdAttribute> pairs;

  public TableMetadata()
  {
    this.pairs = new HashedMap<MdAttribute, MdAttribute>();
  }

  public MdBusiness getMdBusiness()
  {
    return mdBusiness;
  }

  public void setMdBusiness(MdBusiness mdBusiness)
  {
    this.mdBusiness = mdBusiness;
  }

  public Map<MdAttribute, MdAttribute> getPairs()
  {
    return pairs;
  }

  public void addPair(MdAttribute target, MdAttribute source)
  {
    this.pairs.put(target, source);
  }
}
