package net.geoprism.registry.etl;

public class PublishMasterListVersionJobDTO extends PublishMasterListVersionJobDTOBase
{
  private static final long serialVersionUID = -1197029792;
  
  public PublishMasterListVersionJobDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected PublishMasterListVersionJobDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
