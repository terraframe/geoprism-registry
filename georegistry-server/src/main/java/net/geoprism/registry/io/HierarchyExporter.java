package net.geoprism.registry.io;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.AttributeReferenceIF;
import com.runwaysdk.dataaccess.BusinessDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdTermRelationshipDAOIF;
import com.runwaysdk.dataaccess.RelationshipDAOIF;
import com.runwaysdk.dataaccess.io.dataDefinition.ExportMetadata;
import com.runwaysdk.dataaccess.io.dataDefinition.VersionExporter;
import com.runwaysdk.query.BusinessDAOQuery;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.RelationshipDAOQuery;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.RegistryConstants;


public class HierarchyExporter
{
  /**
   * Exports the metadata for the hierarchies. If a
   * file of the specified filename already exists then the file is overwritten.
   *
   * @param fileName
   *            The name of the xml file to create.
   * @param schemaLocation
   *            The location of the schema
   * @param _exportOnlyModifiedAttributes
   *             True if only modified attributes should be exported, false otherwise.
   */
  @Request
  public static void exportHierarchyDefinition(String fileName, String schemaLocation, boolean _exportOnlyModifiedAttributes)
  {
    ExportMetadata exportMetadata = new ExportMetadata();
    
    QueryFactory qf = new QueryFactory();
    
    // Export the MdBusinesses that define the hierarchy attributes
    BusinessDAOQuery uQ = qf.businessDAOQuery(Universal.CLASS);
    BusinessDAOQuery mdbQ = qf.businessDAOQuery(MdBusiness.CLASS);
    mdbQ.WHERE(mdbQ.aUUID(MdBusiness.OID).EQ(uQ.aReference(Universal.MDBUSINESS).aUUID(Universal.OID)));    
    OIterator<? extends BusinessDAOIF> mdbI = mdbQ.getIterator();
    try
    {
      while (mdbI.hasNext())
      {
        MdBusinessDAOIF mdBusiness = (MdBusinessDAOIF)mdbI.next();
System.out.println(mdBusiness.getType()+"  "+mdBusiness.getTypeName());
        exportMetadata.addCreateOrUpdate(mdBusiness);
      }
    }
    finally
    {
      mdbI.close();
    }
    
    // Export the Universals
    uQ = qf.businessDAOQuery(Universal.CLASS);
    OIterator<? extends BusinessDAOIF> uQI = uQ.getIterator();
    try
    {
      while (uQI.hasNext())
      {
        BusinessDAOIF businessDAOIF = (BusinessDAOIF)uQI.next();
System.out.println(businessDAOIF.getType()+"  "+businessDAOIF.getKey());
        exportMetadata.addCreateOrUpdate(businessDAOIF);
      }
    }
    finally
    {
      mdbI.close();
    }
    
    // Export the MdTermRelationships that involve universals
    List<MdTermRelationshipDAOIF> universalRelList = getUniversalRelationships();
    for (MdTermRelationshipDAOIF mdTermRelationshipDAOIF : universalRelList)
    {
System.out.println(mdTermRelationshipDAOIF.getType()+"  "+mdTermRelationshipDAOIF.getKey());
      exportMetadata.addCreateOrUpdate(mdTermRelationshipDAOIF);
    }
    
    // Export the instances of the relationships between the universals.
    for (MdTermRelationshipDAOIF mdTermRelationshipDAOIF : universalRelList)
    {
      RelationshipDAOQuery relQ = qf.relationshipDAOQuery(mdTermRelationshipDAOIF.definesType());
      OIterator<RelationshipDAOIF> relI = relQ.getIterator();
      
      try
      {
        while (relI.hasNext())
        {
          RelationshipDAOIF relationshipDAOIF = relI.next();
          exportMetadata.addCreateOrUpdate(relationshipDAOIF);
        }
      }
      finally
      {
        relI.close();
      }
    }
    
    
    List<MdTermRelationshipDAOIF> geoEntityRelList = getGeoEntityRelationships();
    for (MdTermRelationshipDAOIF mdTermRelationshipDAOIF : geoEntityRelList)
    {
System.out.println(mdTermRelationshipDAOIF.getType()+"  "+mdTermRelationshipDAOIF.getKey());
      exportMetadata.addCreateOrUpdate(mdTermRelationshipDAOIF);
    }

    VersionExporter.export(fileName, schemaLocation, exportMetadata);
    
 //   FileInstanceExporter.export(fileName, schemaLocation, queries, _exportOnlyModifiedAttributes);
  }
  

  
  
  
  /**
   * Exports all instances of Universals, including Leaf Types. If a
   * file of the specified filename already exists then the file is overwritten.
   *
   * @param fileName
   *            The name of the xml file to create.
   * @param schemaLocation
   *            The location of the schema
   * @param _exportOnlyModifiedAttributes
   *             True if only modified attributes should be exported, false otherwise.
   */
  @Request
  public static void exportHierarchyInstances(String fileName, String schemaLocation, boolean _exportOnlyModifiedAttributes)
  {
    ExportMetadata exportMetadata = new ExportMetadata();
    
    QueryFactory qf = new QueryFactory();
        
    List<Universal> universalList = new LinkedList<Universal>();
    
    UniversalQuery uQ = new UniversalQuery(qf);
    // All of the leaf node types will be last in the query
    uQ.ORDER_BY(uQ.getIsLeafType(), OrderBy.SortOrder.ASC);
    
    OIterator<? extends Universal> i = uQ.getIterator();
    try
    {
      while (i.hasNext())
      {
        universalList.add(i.next());
      }
    }
    finally
    {
      i.close();
    }

    for (Universal universal : universalList)
    {
      exportUniversalInstances(exportMetadata, universal);
    }
    
    
    List<MdTermRelationshipDAOIF> geoEntityRelList = getGeoEntityRelationships();
    
    for (MdTermRelationshipDAOIF mdTermRelationshipDAOIF : geoEntityRelList)
    {
      exportMdTermRelInstances(exportMetadata, mdTermRelationshipDAOIF);
    }
    
    VersionExporter.export(fileName, schemaLocation, exportMetadata);
  }
  
  /**
   * Export all instances of the given {@link Universal} type. 
   * 
   * @param universal
   */
  private static void exportUniversalInstances(ExportMetadata exportMetadata, Universal universal)
  {
    boolean isLeafType = universal.getIsLeafType();
    
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    QueryFactory qf = new QueryFactory();
    BusinessDAOQuery q = qf.businessDAOQuery(mdBusiness.definesType());
    
    System.out.println("\nExporting Universal ["+universal.getUniversalId()+"] instances:");
    System.out.println("-----------------------------------------------------------------------");
    
    int counter = 0;
    
    OIterator<BusinessDAOIF> i = q.getIterator();
    try
    {
      for (BusinessDAOIF businessDAOIF : i)
      {
        if (!isLeafType)
        {
          BusinessDAOIF geoEntity = ((AttributeReferenceIF)businessDAOIF.getAttributeIF(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME)).dereference();
          exportMetadata.addCreate(geoEntity);
        }
        exportMetadata.addCreate(businessDAOIF);
        
        System.out.print(".");
        
        if (counter % 100 == 0)
        {
          System.out.println();
        }
        
        counter++;
      }
    }
    finally
    {
      i.close();
    }
  }
  
  /**
   * 
   * @param mdTermRelationshipDAOIF
   */
  private static void exportMdTermRelInstances(ExportMetadata exportMetadata, MdTermRelationshipDAOIF mdTermRelationshipDAOIF)
  {
    QueryFactory qf = new QueryFactory();
    RelationshipDAOQuery q = qf.relationshipDAOQuery(mdTermRelationshipDAOIF.definesType());
    
    
    System.out.println("\nExporting Relationship ["+mdTermRelationshipDAOIF.getTypeName()+"] instances:");
    System.out.println("-----------------------------------------------------------------------");
    
    int counter = 0;
    
    OIterator<RelationshipDAOIF> i = q.getIterator();
    try
    {
      for (RelationshipDAOIF relationshipDAOIF : i)
      {
        exportMetadata.addCreate(relationshipDAOIF);
        
        System.out.print(".");
        
        if (counter % 100 == 0)
        {
          System.out.println();
        }
        
        counter++;
      }
    }
    finally
    {
      i.close();
    }
    
  }
  

  
  /**
   * Returns a list of {@link MdTermRelationshipDAOIF} that defines relationships between universals.
   * 
   * @return list of {@link MdTermRelationshipDAOIF} that defines relationships between universals.
   */
  private static List<MdTermRelationshipDAOIF> getUniversalRelationships()
  {
    List<MdTermRelationshipDAOIF> list = new LinkedList<MdTermRelationshipDAOIF>();
    
    QueryFactory qf = new QueryFactory();
    
    // Export the MdTermRelationships that involve universals
    MdBusiness univMdBusiness = MdBusiness.getMdBusiness(Universal.CLASS);
    BusinessDAOQuery trQ = qf.businessDAOQuery(MdTermRelationship.CLASS);
    trQ.WHERE(trQ.get(MdTermRelationship.PARENTMDBUSINESS).EQ(univMdBusiness.getOid()).
        AND(trQ.get(MdTermRelationship.CHILDMDBUSINESS).EQ(univMdBusiness.getOid())));
    
    OIterator<? extends BusinessDAOIF> mdtrI = trQ.getIterator();
    try
    {
      while (mdtrI.hasNext())
      {
        MdTermRelationshipDAOIF businessDAOIF = (MdTermRelationshipDAOIF)mdtrI.next();
        list.add(businessDAOIF);
      }
    }
    finally
    {
      mdtrI.close();
    }
    
    return list;
  }
  
  /**
   * Returns a list of {@link MdTermRelationshipDAOIF} that defines relationships between geoentities.
   * 
   * @return list of {@link MdTermRelationshipDAOIF} that defines relationships between geoentities.
   */
  private static List<MdTermRelationshipDAOIF> getGeoEntityRelationships()
  {
    List<MdTermRelationshipDAOIF> list = new LinkedList<MdTermRelationshipDAOIF>();
    
    QueryFactory qf = new QueryFactory();
    
    // Export the MdTermRelationships that involve universals
    MdBusiness geoEntityMdBusiness = MdBusiness.getMdBusiness(GeoEntity.CLASS);
    BusinessDAOQuery trQ = qf.businessDAOQuery(MdTermRelationship.CLASS);
    trQ.WHERE(trQ.get(MdTermRelationship.PARENTMDBUSINESS).EQ(geoEntityMdBusiness.getOid()).
        AND(trQ.get(MdTermRelationship.CHILDMDBUSINESS).EQ(geoEntityMdBusiness.getOid())));
    OIterator<? extends BusinessDAOIF> mdtrI = trQ.getIterator();
    try
    {
      while (mdtrI.hasNext())
      {
        MdTermRelationshipDAOIF businessDAOIF = (MdTermRelationshipDAOIF)mdtrI.next();
        list.add(businessDAOIF);
      }
    }
    finally
    {
      mdtrI.close();
    }
    
    return list;
  }
  
  
}
