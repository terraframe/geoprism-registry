package net.geoprism.registry;

public class ListTypeAttributeGroupDTO extends ListTypeAttributeGroupDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1140655916;
  
  public ListTypeAttributeGroupDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListTypeAttributeGroupDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
