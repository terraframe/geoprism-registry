package net.geoprism.registry.masterlist;

import net.geoprism.registry.ListTypeAttribute;

public interface ColumnFilter
{

  public boolean isValid(ListColumn column);

  public boolean isValid(ListTypeAttribute listTypeAttribute);

}
