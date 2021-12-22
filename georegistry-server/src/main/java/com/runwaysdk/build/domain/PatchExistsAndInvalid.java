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
package com.runwaysdk.build.domain;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeBooleanDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeEnumerationDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.MdGeoVertexDAOIF;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.MasterListVersionQuery;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.etl.PublishMasterListVersionJob;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class PatchExistsAndInvalid
{
  public static final String STATUS_ATTRIBUTE_NAME = "status";
  
  private static final Logger logger = LoggerFactory.getLogger(PatchExistsAndInvalid.class);
  
  private static final Date today = new Date();
  
  public static void main(String[] args)
  {
    new PatchExistsAndInvalid().doItInReq();
  }
  
  @Request
  private void doItInReq()
  {
    addAttributes();
  }

  @Transaction
  private void addAttributes()
  {
    List<Universal> unis = getUniversals();

    for (Universal uni : unis)
    {
      MdBusinessDAO bizDAO = (MdBusinessDAO) BusinessFacade.getEntityDAO(uni.getMdBusiness());
      MdAttributeConcreteDAO postgresStatusAttr = (MdAttributeConcreteDAO) bizDAO.definesAttribute(STATUS_ATTRIBUTE_NAME);
      if (postgresStatusAttr != null)
      {
        postgresStatusAttr.delete();
      }
      
      MdGeoVertexDAO mdVertex = GeoVertexType.getMdGeoVertex(uni.getUniversalId());
      
      MdAttributeEnumerationDAO statusMdAttr = (MdAttributeEnumerationDAO) mdVertex.definesAttribute(STATUS_ATTRIBUTE_NAME);
      if (statusMdAttr != null)
      {
        statusMdAttr.delete();
      }
    }
    
    for (Universal uni : unis)
    {
      ServerGeoObjectType type = new ServerGeoObjectTypeConverter().build(uni);
      
      MdGraphClassDAOIF mdClass = type.getMdVertex();
      
      MdAttributeDAOIF existing = mdClass.definesAttribute(DefaultAttribute.EXISTS.getName());
      
      if (existing == null)
      {
        logger.info("Adding new attributes to [" + mdClass.getKey() + "].");
        
        MdAttributeBooleanDAO existsMdAttr = MdAttributeBooleanDAO.newInstance();
        existsMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.EXISTS.getName());
        existsMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.EXISTS.getDefaultLocalizedName());
        existsMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.EXISTS.getDefaultDescription());
        existsMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
        existsMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.FALSE);
        existsMdAttr.setValue(MdAttributeConcreteInfo.DEFAULT_VALUE, MdAttributeBooleanInfo.FALSE);
        existsMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
        existsMdAttr.apply();
        
        MdAttributeBooleanDAO invalidMdAttr = MdAttributeBooleanDAO.newInstance();
        invalidMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.INVALID.getName());
        invalidMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.INVALID.getDefaultLocalizedName());
        invalidMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.INVALID.getDefaultDescription());
        invalidMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
        invalidMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.FALSE);
        invalidMdAttr.setValue(MdAttributeConcreteInfo.DEFAULT_VALUE, MdAttributeBooleanInfo.FALSE);
        invalidMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
        invalidMdAttr.apply();
      }
    }
    
    patchMasterlistVersions();
    
    // TODO : We can't actually set this field to required unfortunately because of many different graph bugs.
    // If we set it to required immediately when it's created, orientdb throws an error saying that objects
    // don't have a required field. If we try to update the field to set it as required after the instance
    // data has been patched, Runway tries to create the attribute twice because the MdAttribute in the cache
    // still has the 'isNew' flag set to true. We can't do this in a separate transaction because the patching
    // transaction is controlled at a higher level than we have access to here.
//    enforceInvalidRequired();
  }
  
  private void enforceInvalidRequired()
  {
    List<Universal> unis = PatchExistsAndInvalid.getUniversals();

    for (Universal uni : unis)
    {
      ServerGeoObjectType type = new ServerGeoObjectTypeConverter().build(uni);
      
      MdGraphClassDAOIF mdClass = type.getMdVertex();
      
      logger.info("Setting invalid mdAttr to required for class [" + mdClass.getKey() + "].");
      
      MdAttributeDAO invalidMdAttr = (MdAttributeDAO) mdClass.definesAttribute(DefaultAttribute.INVALID.getName());
      invalidMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
      invalidMdAttr.apply();
    }
  }
  
  private void patchMasterlistVersions()
  {
    final Collection<Locale> locales = LocalizationFacade.getInstalledLocales();
    
    MasterListVersionQuery query = new MasterListVersionQuery(new QueryFactory());
  
    try (OIterator<? extends MasterListVersion> it = query.getIterator())
    {
      for (MasterListVersion version : it)
      {
//        ServerGeoObjectType type = version.getMasterlist().getGeoObjectType();
        
        // We are accessing it in this weird way because MasterList was changed to have new localized attributes. If we run this patch
        // before masterlist is patched then simply instantiating a MasterList object will throw an error when it tries to load the structs.
        BusinessDAO versionDAO = (BusinessDAO) BusinessDAO.get(version.getOid());
        String masterListOid = versionDAO.getValue(MasterListVersion.MASTERLIST);
        BusinessDAO masterListDAO = (BusinessDAO) BusinessDAO.get(masterListOid);
        String universalOid = masterListDAO.getValue(MasterList.UNIVERSAL);
        ServerGeoObjectType type = ServerGeoObjectType.get(Universal.get(universalOid));
        
        // Patch metadata
        //AttributeType existsAttr = type.getAttribute(DefaultAttribute.EXISTS.getName()).get();
        //MasterListVersion.createMdAttributeFromAttributeType(version, type, existsAttr, locales);
        
        AttributeType invalidAttr = type.getAttribute(DefaultAttribute.INVALID.getName()).get();
        MasterListVersion.createMdAttributeFromAttributeType(version, type, invalidAttr, locales);
        
        // Instance data is too hard to patch. We're just going to republish all the lists
        logger.info("Master list version [" + version.getKey() + "] has been queued for republishing.");
        
        PublishMasterListVersionJob job = new PublishMasterListVersionJob();
        job.setMasterListVersion(version);
        job.setMasterListId(masterListOid);
        job.apply();
    
//        job.start();
//        JobHistory history = job.createNewHistory();
        
        JobHistory history = new JobHistory();
        history.setStartTime(new Date());
        history.addStatus(AllJobStatus.QUEUED);
        history.apply();
        
        JobHistoryRecord record = new JobHistoryRecord(job, history);
        record.apply();
      }
    }
  }
  
//  private void patchMasterlistVersions()
//  {
//    final Collection<Locale> locales = LocalizationFacade.getInstalledLocales();
//    
//    MasterListVersionQuery query = new MasterListVersionQuery(new QueryFactory());
//    
//    try (OIterator<? extends MasterListVersion> it = query.getIterator())
//    {
//      for (MasterListVersion version : it)
//      {
//        ServerGeoObjectType type = version.getMasterlist().getGeoObjectType();
//        
//        // Patch metadata
//        AttributeType existsAttr = type.getAttribute(DefaultAttribute.EXISTS.getName()).get();
//        MdAttribute existsMdAttr = MasterListVersion.createMdAttributeFromAttributeType(version, type, existsAttr, locales).getPairs().keySet().iterator().next();
//        
//        AttributeType invalidAttr = type.getAttribute(DefaultAttribute.INVALID.getName()).get();
//        MdAttribute invalidMdAttr = MasterListVersion.createMdAttributeFromAttributeType(version, type, invalidAttr, locales).getPairs().keySet().iterator().next();
//        
//        
//        
//        // Patch instance data
//        MdBusiness table = version.getMdBusiness();
//        
//        String statement = "UPDATE " + table.getTableName() + " SET " + existsMdAttr.getColumnName() + " = " + 
//        
//        Database.executeStatement(statement);
//      }
//    }
//  }
  
//  private void patchMasterlistVersions(ServerGeoObjectType type)
//  {
//    AttributeType existsAttr = type.getAttribute(DefaultAttribute.EXISTS.getName()).get();
//    MasterList.createMdAttribute(type, existsAttr);
//    
//    AttributeType invalidAttr = type.getAttribute(DefaultAttribute.INVALID.getName()).get();
//    MasterList.createMdAttribute(type, invalidAttr);
//  }
  
  public static List<Universal> getUniversals()
  {
    QueryFactory qf = new QueryFactory();
    UniversalQuery uq = new UniversalQuery(qf);
    
    @SuppressWarnings("unchecked")
    List<Universal> unis = (List<Universal>) uq.getIterator().getAll();
    
    Iterator<Universal> it = unis.iterator();
    
    while (it.hasNext())
    {
      Universal uni = it.next();
      
      if (uni.getKey().equals(Universal.ROOT_KEY))
      {
        it.remove();
        continue;
      }
      
      MdGeoVertexDAOIF superType = GeoVertexType.getMdGeoVertex(uni.getUniversalId()).getSuperClass();
      
      if (superType != null && !superType.definesType().equals(GeoVertex.CLASS))
      {
        it.remove();
        continue;
      }
    }
    
    return unis;
  }
}
