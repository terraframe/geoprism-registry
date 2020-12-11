package net.geoprism.registry.model;

public class GeoObjectTypeMetadataDTO extends GeoObjectTypeMetadataDTOBase
{
  private static final long serialVersionUID = -1926980777;
  
  public GeoObjectTypeMetadataDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected GeoObjectTypeMetadataDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
