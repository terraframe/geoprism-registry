package net.geoprism.registry;

public class HierarchyTypeSnapshotDisplayLabelDTO extends HierarchyTypeSnapshotDisplayLabelDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -125218632;
  
  public HierarchyTypeSnapshotDisplayLabelDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given LocalStructDTO into a new DTO.
  * 
  * @param localStructDTO The LocalStructDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected HierarchyTypeSnapshotDisplayLabelDTO(com.runwaysdk.business.LocalStructDTO localStructDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(localStructDTO, clientRequest);
  }
  
}
