package net.geoprism.registry.jobs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ImportHistory extends ImportHistoryBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -678843468;
  
  public ImportHistory()
  {
    super();
  }
  
  public boolean hasImportErrors()
  {
    ImportErrorQuery query = new ImportErrorQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));
    return query.getCount() > 0;
  }

  public void deleteAllImportErrors()
  {
    ImportErrorQuery query = new ImportErrorQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    OIterator<? extends ImportError> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  public ImportConfiguration getConfig()
  {
    return ImportConfiguration.build(this.getConfigJson());
  }

  public void deleteAllValidationProblems()
  {
    ValidationProblemQuery query = new ValidationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    OIterator<? extends ValidationProblem> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  @Override
  public void delete()
  {
    deleteAllImportErrors();

    deleteAllValidationProblems();

    super.delete();
  }

  public ServerGeoObjectType getServerGeoObjectType()
  {
    return ServerGeoObjectType.get(this.getGeoObjectTypeCode());
  }

  public void enforceExecutePermissions()
  {
    JsonObject jo = JsonParser.parseString(this.getConfigJson()).getAsJsonObject();
    if (jo.has(ImportConfiguration.OBJECT_TYPE) && !jo.get(ImportConfiguration.OBJECT_TYPE).getAsString().equals("LPG") && !jo.get(ImportConfiguration.OBJECT_TYPE).getAsString().contains("RDF"))
      getConfig().enforceExecutePermissions();
  }
}
