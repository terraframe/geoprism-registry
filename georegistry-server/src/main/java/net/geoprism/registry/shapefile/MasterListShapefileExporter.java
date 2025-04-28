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
package net.geoprism.registry.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDoubleDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTextDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributeLineStringDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributeMultiLineStringDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributeMultiPointDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributeMultiPolygonDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributePointDAOIF;
import com.runwaysdk.gis.dataaccess.MdAttributePolygonDAOIF;
import com.runwaysdk.query.OIterator;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.excel.MasterListExcelExporter;
import net.geoprism.registry.excel.MasterListExcelExporter.MasterListExcelExporterSheet;
import net.geoprism.registry.util.SessionPredicate;

public class MasterListShapefileExporter
{
  private static Logger                            logger = LoggerFactory.getLogger(MasterListShapefileExporter.class);

  public static final String                       GEOM   = "the_geom";

  private Map<String, String>                      columnNames;

  private MasterList                               list;

  private MasterListVersion                        version;

  private MdBusinessDAOIF                          mdBusiness;

  private List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  private String                                   filterJson;

  public MasterListShapefileExporter(MasterListVersion version, MdBusinessDAOIF mdBusiness, List<? extends MdAttributeConcreteDAOIF> mdAttributes, String filterJson)
  {
    this.version = version;
    this.mdBusiness = mdBusiness;
    this.mdAttributes = mdAttributes;
    this.filterJson = filterJson;
    this.columnNames = new HashMap<String, String>();

    this.list = version.getMasterlist();
  }

  public Map<String, String> getColumnNames()
  {
    return columnNames;
  }

  public MasterList getList()
  {
    return list;
  }

  public void setList(MasterList list)
  {
    this.list = list;
  }

  public File writeToFile() throws IOException
  {
    SimpleFeatureType featureType = createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = features(featureType);

    String name = SessionPredicate.generateId();

    File root = new File(new File(VaultProperties.getPath("vault.default"), "files"), name);
    root.mkdirs();

    File directory = new File(root, this.getList().getCode());
    directory.mkdirs();

    File file = new File(directory, this.getList().getCode() + ".shp");

    /*
     * Get an output file name and create the new shapefile
     */
    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<String, Serializable>();
    params.put("url", file.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);
    params.put("charset", "UTF-8");
    
    ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
    dataStore.setCharset(Charset.forName("UTF-8"));
    dataStore.createSchema(featureType);

    /*
     * Write the features to the shapefile
     */
    try (Transaction transaction = new DefaultTransaction())
    {
      String typeName = dataStore.getTypeNames()[0];
      SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

      if (featureSource instanceof SimpleFeatureStore)
      {
        SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

        featureStore.setTransaction(transaction);
        try
        {
          featureStore.addFeatures(collection);
          transaction.commit();

        }
        catch (Exception problem)
        {
          transaction.rollback();

          throw new ProgrammingErrorException(problem);
        }
      }
      else
      {
        throw new ProgrammingErrorException(typeName + " does not support read/write access");
      }
    }

    dataStore.dispose();
    
    this.writeEncodingFile(directory);
    
    this.writeDictionaryFile(directory);

    return directory;
  }
  
  /**
   * Writes an additional "data dictionary" / metadata excel spreadsheet to the directory, which is intended to be
   * part of the final Shapfile. This is useful for downstream developers trying to make sense of the GIS data.
   * 
   * See also:
   * - https://github.com/terraframe/geoprism-registry/issues/628
   */
  private void writeDictionaryFile(File directory)
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.version.getMdBusinessOid());

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> this.version.isValid(mdAttribute)).collect(Collectors.toList());

    mdAttributes = mdAttributes.stream().filter(mdAttribute -> !mdAttribute.definesAttribute().equals("invalid")).collect(Collectors.toList());
    
    String excelFilter;
    if (filterJson == null)
    {
      excelFilter = "[{attribute:invalid,value:false}]";
    }
    else
    {
      excelFilter = new String(filterJson);
    }
    
    try
    {
      File file = new File(directory, "metadata.xlsx");
      FileOutputStream fos = new FileOutputStream(file);
      
      MasterListExcelExporter exporter = new MasterListExcelExporter(this.version, mdBusiness, mdAttributes, excelFilter, this.columnNames, new MasterListExcelExporterSheet[] {MasterListExcelExporterSheet.DICTIONARY, MasterListExcelExporterSheet.METADATA});

      Workbook wb = exporter.createWorkbook();
      
      wb.write(fos);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  /**
   * Writes an additional .cpg file in the provided directory. This file contains the content "UTF-8". This file
   * is not part of the official Shapefile spec, however it has become an unofficial addendum for specifying an
   * encoding.
   * 
   * See also:
   * - https://github.com/terraframe/geoprism-registry/issues/671
   * - https://gis.stackexchange.com/questions/3529/which-character-encoding-is-used-by-the-dbf-file-in-shapefiles
   */
  private void writeEncodingFile(File directory)
  {
    File cpg = new File(directory, this.getList().getCode() + ".cpg");
    
    try
    {
      FileUtils.write(cpg, "UTF-8", Charset.forName("UTF-8"));
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public InputStream export() throws IOException
  {
    // Zip up the entire contents of the file
    final File directory = this.writeToFile();
    final PipedOutputStream pos = new PipedOutputStream();
    final PipedInputStream pis = new PipedInputStream(pos);

    Thread t = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          try (ZipOutputStream zipFile = new ZipOutputStream(pos))
          {
            File[] files = directory.listFiles();

            for (File file : files)
            {
              ZipEntry entry = new ZipEntry(file.getName());
              zipFile.putNextEntry(entry);

              try (FileInputStream in = new FileInputStream(file))
              {
                IOUtils.copy(in, zipFile);
              }
            }
          }
          finally
          {
            pos.close();
          }

          FileUtils.deleteQuietly(directory);
        }
        catch (IOException e)
        {
          logger.error("Error while writing the workbook", e);
        }
      }
    });
    t.setDaemon(true);
    t.start();

    return pis;

  }

  public FeatureCollection<SimpleFeatureType, SimpleFeature> features(SimpleFeatureType featureType)
  {
    List<SimpleFeature> features = new ArrayList<SimpleFeature>();
    SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);

    BusinessQuery query = this.version.buildQuery(this.filterJson);
    query.ORDER_BY_DESC(query.aCharacter(DefaultAttribute.CODE.getName()));

    OIterator<Business> objects = query.getIterator();

    try
    {

      while (objects.hasNext())
      {
        Business row = objects.next();

        builder.set(GEOM, row.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

        for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
        {
          String attributeName = mdAttribute.definesAttribute();
          Object value = row.getObjectValue(attributeName);

          if (value != null)
          {
            builder.set(this.getColumnName(attributeName), value);
          }
        }

        SimpleFeature feature = builder.buildFeature(row.getValue(DefaultAttribute.CODE.getName()));
        features.add(feature);
      }
    }
    finally
    {
      objects.close();
    }

    return new ListFeatureCollection(featureType, features);
  }

  public SimpleFeatureType createFeatureType()
  {
    MdAttributeGeometryDAOIF geometryAttribute = (MdAttributeGeometryDAOIF) this.mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setName(this.list.getDisplayLabel().getValue());
    builder.setCRS(DefaultGeographicCRS.WGS84);
    builder.add(GEOM, this.getShapefileType(geometryAttribute), 4326);

    this.mdAttributes.forEach(attribute -> {
      builder.add(generateColumnName(attribute.definesAttribute()), this.getShapefileType(attribute));
    });

    return builder.buildFeatureType();
  }

  public String getColumnName(String name)
  {
    if (this.columnNames.containsKey(name))
    {
      return this.columnNames.get(name);
    }

    throw new ProgrammingErrorException("Unable to find column name with key [" + name + "]");
  }

  public String generateColumnName(String name)
  {
    if (!this.columnNames.containsKey(name))
    {
      String format = this.format(name);

      int count = 1;

      String value = new String(format);

      while (this.columnNames.containsValue(value))
      {
        if (count == 1)
        {
          format = format.substring(0, format.length() - 1);
        }

        if (count == 10)
        {
          format = format.substring(0, format.length() - 1);
        }

        value = format + ( count++ );
      }

      this.columnNames.put(name, value);
    }

    return this.columnNames.get(name);
  }

  private String format(String name)
  {
    if (name.equals(GeoObject.DISPLAY_LABEL))
    {
      return "label";
    }

    return name.substring(0, Math.min(10, name.length()));
  }

  private Class<?> getShapefileType(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute instanceof MdAttributeBooleanDAOIF)
    {
      return Boolean.class;
    }
    else if (mdAttribute instanceof MdAttributeCharacterDAOIF)
    {
      return String.class;
    }
    else if (mdAttribute instanceof MdAttributeDateDAOIF)
    {
      return Date.class;
    }
    else if (mdAttribute instanceof MdAttributeDateTimeDAOIF)
    {
      return Date.class;
    }
    else if (mdAttribute instanceof MdAttributeDoubleDAOIF)
    {
      return Double.class;
    }
    else if (mdAttribute instanceof MdAttributeLongDAOIF)
    {
      return Long.class;
    }
    else if (mdAttribute instanceof MdAttributeTextDAOIF)
    {
      return String.class;
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + mdAttribute.getClass().getSimpleName() + "]");
  }

  private Class<?> getShapefileType(MdAttributeGeometryDAOIF mdAttribute)
  {
    if (mdAttribute instanceof MdAttributePointDAOIF)
    {
      return Point.class;
    }
    else if (mdAttribute instanceof MdAttributeMultiPointDAOIF)
    {
      return MultiPoint.class;
    }
    else if (mdAttribute instanceof MdAttributeLineStringDAOIF)
    {
      return LineString.class;
    }
    else if (mdAttribute instanceof MdAttributeMultiLineStringDAOIF)
    {
      return MultiLineString.class;
    }
    else if (mdAttribute instanceof MdAttributePolygonDAOIF)
    {
      return Polygon.class;
    }
    else if (mdAttribute instanceof MdAttributeMultiPolygonDAOIF)
    {
      return MultiPolygon.class;
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + mdAttribute.getClass().getSimpleName() + "]");
  }

}
