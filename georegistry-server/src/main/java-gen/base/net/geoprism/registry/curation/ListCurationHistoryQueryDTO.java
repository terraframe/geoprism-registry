package net.geoprism.registry.curation;

@com.runwaysdk.business.ClassSignature(hash = 2084334727)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ListCurationHistory.java
 *
 * @author Autogenerated by RunwaySDK
 */
public class ListCurationHistoryQueryDTO extends com.runwaysdk.system.scheduler.JobHistoryQueryDTO
{
private static final long serialVersionUID = 2084334727;

  protected ListCurationHistoryQueryDTO(String type)
  {
    super(type);
  }

@SuppressWarnings("unchecked")
public java.util.List<? extends net.geoprism.registry.curation.ListCurationHistoryDTO> getResultSet()
{
  return (java.util.List<? extends net.geoprism.registry.curation.ListCurationHistoryDTO>)super.getResultSet();
}
}
