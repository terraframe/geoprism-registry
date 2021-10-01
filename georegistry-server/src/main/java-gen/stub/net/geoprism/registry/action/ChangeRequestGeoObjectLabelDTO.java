package net.geoprism.registry.action;

public class ChangeRequestGeoObjectLabelDTO extends ChangeRequestGeoObjectLabelDTOBase
{
  private static final long serialVersionUID = 271034825;
  
  public ChangeRequestGeoObjectLabelDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given LocalStructDTO into a new DTO.
  * 
  * @param localStructDTO The LocalStructDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ChangeRequestGeoObjectLabelDTO(com.runwaysdk.business.LocalStructDTO localStructDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(localStructDTO, clientRequest);
  }
  
}
