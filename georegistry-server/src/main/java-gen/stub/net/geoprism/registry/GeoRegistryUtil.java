/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.excel.MasterListExcelExporter;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.shapefile.MasterListShapefileExporter;
import net.geoprism.registry.xml.XMLImporter;

public class GeoRegistryUtil extends GeoRegistryUtilBase
{
  private static final long    serialVersionUID = 2034796376;

  public static final TimeZone SYSTEM_TIMEZONE  = TimeZone.getTimeZone("UTC");

  public GeoRegistryUtil()
  {
    super();
  }

  public static Date parseDate(String sDate)
  {
    Date date = null;

    if (sDate != null && sDate.length() > 0)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      try
      {
        date = format.parse(sDate);
      }
      catch (ParseException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
    
    return date;
  }
  
  public static String formatIso8601(Date date, boolean includeTime)
  {
    if (date == null)
    {
      return "null";
    }
    
    if (!includeTime)
    {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      formatter.setTimeZone(SYSTEM_TIMEZONE);
      return formatter.format(date);
    }
    else
    {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      formatter.setTimeZone(SYSTEM_TIMEZONE);
      return formatter.format(date);
    }
  }

  public static Date parseIso8601(String date)
  {
    String s = "2020-02-13T18:51:09.840Z";
    TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(s);
    Instant i = Instant.from(ta);
    Date d = Date.from(i);
    return d;
  }

  @Authenticate
  public static String createHierarchyType(String htJSON)
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();

    HierarchyType hierarchyType = HierarchyType.fromJSON(htJSON, adapter);

    ServiceFactory.getHierarchyPermissionService().enforceCanCreate(hierarchyType.getOrganizationCode());

    ServerHierarchyType sType = new ServerHierarchyTypeBuilder().createHierarchyType(hierarchyType);

    // The transaction did not error out, so it is safe to put into the cache.
    ServiceFactory.getMetadataCache().addHierarchyType(sType);

    return hierarchyType.getCode();
  }

  @Transaction
  public static InputStream exportMasterListShapefile(String oid, String filterJson)
  {
    MasterListVersion version = MasterListVersion.get(oid);
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> version.isValid(mdAttribute)).collect(Collectors.toList());
    
    if (filterJson.contains("invalid")) {
      mdAttributes = mdAttributes.stream().filter(mdAttribute -> !mdAttribute.definesAttribute().equals("invalid")).collect(Collectors.toList());
    }

    try
    {
      MasterListShapefileExporter exporter = new MasterListShapefileExporter(version, mdBusiness, mdAttributes, filterJson);

      return exporter.export();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Transaction
  public static InputStream exportMasterListExcel(String oid, String filterJson)
  {
    MasterListVersion version = MasterListVersion.get(oid);
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> version.isValid(mdAttribute)).collect(Collectors.toList());

    if (filterJson.contains("invalid")) {
      mdAttributes = mdAttributes.stream().filter(mdAttribute -> !mdAttribute.definesAttribute().equals("invalid")).collect(Collectors.toList());
    }
    
    try
    {
      MasterListExcelExporter exporter = new MasterListExcelExporter(version, mdBusiness, mdAttributes, filterJson, null);

      return exporter.export();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Authenticate
  public static void importTypes(String orgCode, InputStream istream)
  {
    Organization organization = Organization.getByCode(orgCode);

    XMLImporter xmlImporter = new XMLImporter();
    xmlImporter.importXMLDefinitions(organization, istream);
  }

}
