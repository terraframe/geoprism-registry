package net.geoprism.registry.curation;

public class ListCurationHistoryDTO extends ListCurationHistoryDTOBase
{
  private static final long serialVersionUID = -1222918387;
  
  public ListCurationHistoryDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListCurationHistoryDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
