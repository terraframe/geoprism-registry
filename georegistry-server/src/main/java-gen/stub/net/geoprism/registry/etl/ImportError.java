package net.geoprism.registry.etl;

import com.runwaysdk.query.QueryFactory;

public class ImportError extends ImportErrorBase
{
  private static final long serialVersionUID = 1410792643;
  
  public static enum Resolution
  {
    IGNORE,
    APPLY_GEO_OBJECT,
    UNRESOLVED
  }
  
  public ImportError()
  {
    super();
  }
  
  public static ImportErrorQuery queryResolutionStatus(String historyId, String resolutionStatus)
  {
    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    
    ieq.WHERE(ieq.getResolution().EQ(resolutionStatus));
    
    return ieq;
  }
  
  @Override
  public void apply()
  {
    if (this.getResolution().equals(""))
    {
      this.setResolution(Resolution.UNRESOLVED.name());
    }
    
    super.apply();
  }
  
}
