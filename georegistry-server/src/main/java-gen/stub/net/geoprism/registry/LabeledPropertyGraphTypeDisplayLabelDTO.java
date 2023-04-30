package net.geoprism.registry;

public class LabeledPropertyGraphTypeDisplayLabelDTO extends LabeledPropertyGraphTypeDisplayLabelDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -43158113;
  
  public LabeledPropertyGraphTypeDisplayLabelDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given LocalStructDTO into a new DTO.
  * 
  * @param localStructDTO The LocalStructDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected LabeledPropertyGraphTypeDisplayLabelDTO(com.runwaysdk.business.LocalStructDTO localStructDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(localStructDTO, clientRequest);
  }
  
}
