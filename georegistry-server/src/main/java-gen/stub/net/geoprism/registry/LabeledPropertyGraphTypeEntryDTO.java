package net.geoprism.registry;

public class LabeledPropertyGraphTypeEntryDTO extends LabeledPropertyGraphTypeEntryDTOBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 896711826;
  
  public LabeledPropertyGraphTypeEntryDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected LabeledPropertyGraphTypeEntryDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
}
