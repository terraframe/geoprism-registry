package net.geoprism.registry.service.business;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.session.Session;

import net.geoprism.registry.DateFormatter;
import net.geoprism.registry.business.TransitionEventBusinessService;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.query.graph.GeoObjectTypeRestrictionUtil;
import net.geoprism.registry.service.GPRServiceFactory;
import net.geoprism.registry.service.ServiceFactory;

public class GPRTransitionEventBusinessService extends TransitionEventBusinessService
{
  @Override
  public boolean readOnly(TransitionEvent tran)
  {
    RolePermissionService rps = GPRServiceFactory.getRolePermissionService();
    ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(tran.getBeforeTypeCode()).get();

    final String orgCode = tran.getBeforeTypeOrgCode();

    return ! ( rps.isSRA() || rps.isRA(orgCode) || rps.isRM(orgCode, type) || rps.isRC(orgCode, type) );
  }
  
  @Override
  public void addPageWhereCriteria(StringBuilder statement, Map<String, Object> parameters, String attrConditions)
  {
    List<String> whereConditions = new ArrayList<String>();

    // Add permissions criteria
    if (Session.getCurrentSession() != null)
    {
        String beforeCondition = GeoObjectTypeRestrictionUtil.buildTypeWritePermissionsFilter(TransitionEvent.BEFORETYPEORGCODE, TransitionEvent.BEFORETYPECODE);
        if (beforeCondition.length() > 0)
        {
          whereConditions.add(beforeCondition);
        }
        
        String afterCondition = GeoObjectTypeRestrictionUtil.buildTypeReadPermissionsFilter(TransitionEvent.AFTERTYPEORGCODE, TransitionEvent.AFTERTYPECODE);
        if (afterCondition.length() > 0)
        {
          whereConditions.add(afterCondition);
        }
    }

    // Filter based on attributes
    if (attrConditions != null && attrConditions.length() > 0)
    {
      List<String> lAttrConditions = new ArrayList<String>();
      JsonArray jaAttrConditions = JsonParser.parseString(attrConditions).getAsJsonArray();

      for (int i = 0; i < jaAttrConditions.size(); ++i)
      {
        JsonObject attrCondition = jaAttrConditions.get(i).getAsJsonObject();

        String attr = attrCondition.get("attribute").getAsString();

        MdVertexDAO eventMd = (MdVertexDAO) MdVertexDAO.getMdVertexDAO(TransitionEvent.CLASS);
        MdAttributeDAOIF mdAttr = eventMd.definesAttribute(attr);

        if (attr.equals(TransitionEvent.EVENTDATE))
        {
          DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
          format.setTimeZone(DateFormatter.SYSTEM_TIMEZONE);

          List<String> dateConditions = new ArrayList<String>();

          try
          {
            if (attrCondition.has("startDate") && !attrCondition.get("startDate").isJsonNull() && attrCondition.get("startDate").getAsString().length() > 0)
            {
              Date startDate = format.parse(attrCondition.get("startDate").getAsString());
              dateConditions.add(mdAttr.getColumnName() + ">=:startDate" + i);
              parameters.put("startDate" + i, startDate);
            }
            if (attrCondition.has("endDate") && !attrCondition.get("endDate").isJsonNull() && attrCondition.get("endDate").getAsString().length() > 0)
            {
              Date endDate = format.parse(attrCondition.get("endDate").getAsString());
              dateConditions.add(mdAttr.getColumnName() + "<=:endDate" + i);
              parameters.put("endDate" + i, endDate);
            }
          }
          catch (ParseException e)
          {
            throw new ProgrammingErrorException(e);
          }

          if (dateConditions.size() > 0)
          {
            lAttrConditions.add("(" + StringUtils.join(dateConditions, " AND ") + ")");
          }
        }
        else if (attrCondition.has("value") && !attrCondition.get("value").isJsonNull() && attrCondition.get("value").getAsString().length() > 0)
        {
          String value = attrCondition.get("value").getAsString();

          lAttrConditions.add(mdAttr.getColumnName() + "=:val" + i);
          parameters.put("val" + i, value);
        }
      }

      if (lAttrConditions.size() > 0)
      {
        whereConditions.add(StringUtils.join(lAttrConditions, " AND "));
      }
    }

    if (whereConditions.size() > 0)
    {
      statement.append(" WHERE " + StringUtils.join(whereConditions, " AND "));
    }
  }
}
