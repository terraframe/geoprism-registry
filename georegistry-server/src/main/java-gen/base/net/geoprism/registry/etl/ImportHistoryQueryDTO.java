package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = 866276390)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ImportHistory.java
 *
 * @author Autogenerated by RunwaySDK
 */
public class ImportHistoryQueryDTO extends com.runwaysdk.system.scheduler.JobHistoryQueryDTO
{
private static final long serialVersionUID = 866276390;

  protected ImportHistoryQueryDTO(String type)
  {
    super(type);
  }

@SuppressWarnings("unchecked")
public java.util.List<? extends net.geoprism.registry.etl.ImportHistoryDTO> getResultSet()
{
  return (java.util.List<? extends net.geoprism.registry.etl.ImportHistoryDTO>)super.getResultSet();
}
}
