package net.geoprism.registry;

public class BusinessEdgeTypeDescriptionDTO extends BusinessEdgeTypeDescriptionDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 306215699;
  
  public BusinessEdgeTypeDescriptionDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given LocalStructDTO into a new DTO.
  * 
  * @param localStructDTO The LocalStructDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected BusinessEdgeTypeDescriptionDTO(com.runwaysdk.business.LocalStructDTO localStructDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(localStructDTO, clientRequest);
  }
  
}
