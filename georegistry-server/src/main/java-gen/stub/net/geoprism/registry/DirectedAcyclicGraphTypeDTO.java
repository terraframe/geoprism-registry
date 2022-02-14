package net.geoprism.registry;

public class DirectedAcyclicGraphTypeDTO extends DirectedAcyclicGraphTypeDTOBase
{
  private static final long serialVersionUID = -1378607407;
  
  public DirectedAcyclicGraphTypeDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected DirectedAcyclicGraphTypeDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
