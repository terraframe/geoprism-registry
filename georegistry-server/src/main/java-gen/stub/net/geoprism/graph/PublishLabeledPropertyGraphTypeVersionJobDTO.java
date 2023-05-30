package net.geoprism.graph;

public class PublishLabeledPropertyGraphTypeVersionJobDTO extends PublishLabeledPropertyGraphTypeVersionJobDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -610239400;
  
  public PublishLabeledPropertyGraphTypeVersionJobDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected PublishLabeledPropertyGraphTypeVersionJobDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
