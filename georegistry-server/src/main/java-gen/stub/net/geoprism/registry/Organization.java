package net.geoprism.registry;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.QueryFactory;

public class Organization extends OrganizationBase
{
  private static final long serialVersionUID = -640706555;

  public Organization()
  {
    super();
  }

  /**
   * Builds the this object's key name.
   */
  @Override
  public String buildKey()
  {
    return this.getCode();
  }

  public static List<? extends Organization> getOrganizations()
  {
    OrganizationQuery query = new OrganizationQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel().localize());

    return query.getIterator().getAll();
  }
  
}
