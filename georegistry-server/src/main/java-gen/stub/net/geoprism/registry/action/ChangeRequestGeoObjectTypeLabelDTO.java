package net.geoprism.registry.action;

public class ChangeRequestGeoObjectTypeLabelDTO extends ChangeRequestGeoObjectTypeLabelDTOBase
{
  private static final long serialVersionUID = -1587931741;
  
  public ChangeRequestGeoObjectTypeLabelDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given LocalStructDTO into a new DTO.
  * 
  * @param localStructDTO The LocalStructDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ChangeRequestGeoObjectTypeLabelDTO(com.runwaysdk.business.LocalStructDTO localStructDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(localStructDTO, clientRequest);
  }
  
}
