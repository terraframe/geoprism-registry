package net.geoprism.registry;

public class ListTypeGeoObjectTypeGroupDTO extends ListTypeGeoObjectTypeGroupDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -440231779;
  
  public ListTypeGeoObjectTypeGroupDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListTypeGeoObjectTypeGroupDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
