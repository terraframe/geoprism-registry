/**
 *
 */
package org.commongeoregistry.adapter;

public class GeoObjectTypeNotFoundException extends RuntimeException
{

  private static final long serialVersionUID = 2855165577383780849L;
  
  private String typeCode;
  
  public GeoObjectTypeNotFoundException(String typeCode)
  {
    this.typeCode = typeCode;
  }

  public String getTypeCode()
  {
    return typeCode;
  }

  public void setTypeCode(String typeCode)
  {
    this.typeCode = typeCode;
  }
  
}
