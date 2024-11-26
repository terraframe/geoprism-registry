/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.StreamResource;

import net.geoprism.registry.excel.ListTypeExcelExporter;
import net.geoprism.registry.excel.ListTypeExcelExporter.ListMetadataSource;
import net.geoprism.registry.excel.MasterListExcelExporter;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.permission.GPROrganizationPermissionService;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.shapefile.ListTypeShapefileExporter;
import net.geoprism.registry.shapefile.MasterListShapefileExporter;
import net.geoprism.registry.xml.XMLImporter;

public class GeoRegistryUtil extends GeoRegistryUtilBase
{
  private static final long               serialVersionUID  = 2034796376;

  public static final TimeZone            SYSTEM_TIMEZONE   = TimeZone.getTimeZone("UTC");

  public static final String              LOCAL_DATE_FORMAT = "yyyy-MM-dd";

  // Work around for authenticated methods with complex attributes
  private static List<ServerOrganization> organizations;

  public GeoRegistryUtil()
  {
    super();
  }

  public static String formatDateForPresentation(Date date, boolean includeTime)
  {
    if (date == null)
    {
      return LocalizationFacade.localize("changeovertime.present");
    }

    return formatIso8601(date, includeTime);
  }

  public static String formatIso8601(Date date, boolean includeTime)
  {
    if (date == null)
    {
      return "null";
    }

    if (!includeTime)
    {
      SimpleDateFormat formatter = new SimpleDateFormat(LOCAL_DATE_FORMAT);
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

  public static String formatDate(Date date, boolean includeTime)
  {
    if (date != null)
    {

      if (!includeTime)
      {
        SimpleDateFormat formatter = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
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

    return null;
  }

  public static Date getCurrentDate()
  {
    Calendar calendar = Calendar.getInstance(SYSTEM_TIMEZONE);
    String dateString = calendar.get(Calendar.YEAR) + "-" + ( calendar.get(Calendar.MONTH) + 1 ) + "-" + calendar.get(Calendar.DAY_OF_MONTH);

    return parseDate(dateString);
  }

  public static Date parseDate(String date)
  {
    return parseDate(date, false);
  }

  public static Date parseDate(String date, boolean throwClientException)
  {
    if (date != null && date.length() > 0)
    {

      try
      {
        SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
        format.setTimeZone(SYSTEM_TIMEZONE);

        return format.parse(date);
      }
      catch (ParseException e)
      {
        if (throwClientException)
        {
          throw new RuntimeException("Unable to parse the date [" + date + "]. The date format must be [" + GeoObjectImportConfiguration.DATE_FORMAT + "]");
        }
        else
        {
          throw new ProgrammingErrorException(e);
        }
      }
    }

    return null;
  }

  public static boolean isBetweenInclusive(Date versionDate, Date startDate, Date endDate)
  {
    return ( versionDate.after(startDate) || versionDate.equals(startDate) ) && ( versionDate.before(endDate) || versionDate.equals(endDate) );
  }

  @Authenticate
  public static String createHierarchyType(String htJSON)
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();

    HierarchyType hierarchyType = HierarchyType.fromJSON(htJSON, adapter);

    ServiceFactory.getHierarchyPermissionService().enforceCanCreate(hierarchyType.getOrganizationCode());

    ServerHierarchyType sType = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class).createHierarchyType(hierarchyType);

    // The transaction did not error out, so it is safe to put into the cache.
    ServiceFactory.getMetadataCache().addHierarchyType(sType);

    return hierarchyType.getCode();
  }

  @Authenticate
  public static String applyClassificationType(String json)
  {
    ClassificationTypeBusinessServiceIF service = ServiceFactory.getBean(ClassificationTypeBusinessServiceIF.class);
    JsonObject object = JsonParser.parseString(json).getAsJsonObject();

    ClassificationType type = service.apply(object);

    return type.toJSON().toString();
  }

  @Transaction
  public static InputStream exportMasterListShapefile(String oid, String filterJson)
  {
    MasterListVersion version = MasterListVersion.get(oid);
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> version.isValid(mdAttribute)).collect(Collectors.toList());

    if (filterJson.contains("invalid"))
    {
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

    if (filterJson.contains("invalid"))
    {
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

  @Transaction
  public static InputStream exportListTypeShapefile(String oid, String json, String actualGeometryType)
  {
    ListTypeVersion version = ListTypeVersion.get(oid);
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());
    JsonObject criteria = ( json != null ) ? JsonParser.parseString(json).getAsJsonObject() : new JsonObject();

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> version.isValid(mdAttribute)).collect(Collectors.toList());

    if (json != null && json.contains("invalid"))
    {
      mdAttributes = mdAttributes.stream().filter(mdAttribute -> !mdAttribute.definesAttribute().equals("invalid")).collect(Collectors.toList());
    }

    // If the list isn't public and the user isn't a member of the organization
    // the remove all non code and display label attributes
    GPROrganizationPermissionService permissions = ServiceFactory.getBean(GPROrganizationPermissionService.class);

    if (version.getListVisibility().equals(ListType.PRIVATE) && !permissions.isMemberOrSRA(version.getListType().getOrganization()))
    {
      mdAttributes = mdAttributes.stream().filter(mdAttribute -> {
        String attributeName = mdAttribute.definesAttribute();

        return attributeName.equals("code") || attributeName.contains("displayLabel");
      }).collect(Collectors.toList());
    }

    try
    {
      ListTypeShapefileExporter exporter = new ListTypeShapefileExporter(version, mdBusiness, mdAttributes, criteria, actualGeometryType);

      return exporter.export();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Transaction
  public static InputStream exportListTypeExcel(String oid, String json)
  {
    ListTypeVersion version = ListTypeVersion.get(oid);
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());
    JsonObject criteria = ( json != null ) ? JsonParser.parseString(json).getAsJsonObject() : new JsonObject();

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> version.isValid(mdAttribute)).collect(Collectors.toList());

    if (json != null && json.contains("invalid"))
    {
      mdAttributes = mdAttributes.stream().filter(mdAttribute -> !mdAttribute.definesAttribute().equals("invalid")).collect(Collectors.toList());
    }

    // If the list isn't public and the user isn't a member of the organization
    // the remove all non code and display label attributes
    GPROrganizationPermissionService permissions = ServiceFactory.getBean(GPROrganizationPermissionService.class);

    if (version.getListVisibility().equals(ListType.PRIVATE) && !permissions.isMemberOrSRA(version.getListType().getOrganization()))
    {
      mdAttributes = mdAttributes.stream().filter(mdAttribute -> {
        String attributeName = mdAttribute.definesAttribute();

        return attributeName.equals("code") || attributeName.contains("displayLabel");
      }).collect(Collectors.toList());
    }

    try
    {
      ListTypeExcelExporter exporter = new ListTypeExcelExporter(version, mdAttributes, null, criteria, ListMetadataSource.LIST);

      return exporter.export();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Authenticate
  public static void importTypes(String json, InputStream istream)
  {
    JsonArray orgCodes = JsonParser.parseString(json).getAsJsonArray();
    List<ServerOrganization> organizations = new LinkedList<>();

    for (int i = 0; i < orgCodes.size(); i++)
    {
      String orgCode = orgCodes.get(i).getAsString();

      organizations.add(ServerOrganization.getByCode(orgCode));
    }

    XMLImporter xmlImporter = new XMLImporter(false);
    xmlImporter.importXMLDefinitions(new StreamResource(istream, "domain.xml"), organizations);
  }

  public static void importTypes(String orgCode, ApplicationResource resource)
  {
    ServerOrganization organization = ServerOrganization.getByCode(orgCode);

    XMLImporter xmlImporter = new XMLImporter();
    xmlImporter.importXMLDefinitions(resource, organization);
  }

  public static void zipDirectory(File sourceFolder, File targetZip)
  {
    // Creating a ZipOutputStream by wrapping a FileOutputStream
    try (FileOutputStream fos = new FileOutputStream(targetZip); ZipOutputStream zos = new ZipOutputStream(fos))
    {
      Path sourcePath = sourceFolder.toPath();
      // Walk the tree structure using WalkFileTree method
      Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>()
      {
        @Override
        // Before visiting the directory create the directory in zip archive
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
        {
          // Don't create dir for root folder as it is already created with .zip
          // name
          if (!sourcePath.equals(dir))
          {
            zos.putNextEntry(new ZipEntry(sourcePath.relativize(dir).toString() + "/"));
            zos.closeEntry();
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        // For each visited file add it to zip entry
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
        {
          zos.putNextEntry(new ZipEntry(sourcePath.relativize(file).toString()));
          Files.copy(file, zos);
          zos.closeEntry();
          return FileVisitResult.CONTINUE;
        }
      });
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

}
