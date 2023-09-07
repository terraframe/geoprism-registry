/**
 *
 */
package org.commongeoregistry.adapter.constants;

public enum GeometryType 
{
  POINT(),
  
  LINE(),
  
  POLYGON(),
  
  MULTIPOINT(),
  
  MULTILINE(),
  
  MULTIPOLYGON(),
  
  MIXED();
  
  private GeometryType()
  {
  }
}
