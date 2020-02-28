package net.geoprism.registry.etl;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;

public class ImportHistory extends ImportHistoryBase
{
  private static final long serialVersionUID = 752640606;
  
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
  
}
