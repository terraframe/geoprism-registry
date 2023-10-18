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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTimeJsonAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphDDLCommandAction;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;

import net.geoprism.registry.Organization;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.registry.action.geoobject.SetParentAction;
import net.geoprism.registry.action.geoobject.UpdateAttributeAction;
import net.geoprism.registry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.registry.graph.GeoVertexType;

public class CRAttributePatch
{
  private Logger logger = LoggerFactory.getLogger(CRAttributePatch.class);

  public static void main(String[] args)
  {
    new CRAttributePatch().doItInReq();
  }
  
  @Request
  private void doItInReq()
  {
    executeGraphDDLCommand("CREATE PROPERTY ChangeOverTime.oid IF NOT EXISTS STRING");
    
    doItInTrans();
  }

  @Transaction
  private void doItInTrans()
  {
    patchAllGos();
    
    patchAllCRS();
  }
  
  private void patchAllCRS()
  {
    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());
    
    logger.info("Updating [" + crq.getCount() + "] ChangeRequest records and patching in permissions info.");
    
    OIterator<? extends ChangeRequest> it = crq.getIterator();
    
    Outer:
    for (ChangeRequest cr : it)
    {
      String orgCode = null;
      String gotCode = null;
      String gotTypeCode = null;
      
      cr.appLock();
      
      boolean hasInvalidAction = false;
      
      OIterator<? extends AbstractAction> actionIt = cr.getAllAction();
      
      SetParentAction setParent = null;
      CreateGeoObjectAction createAction = null;
      
      for (AbstractAction action : actionIt)
      {
        if (action instanceof SetParentAction)
        {
          SetParentAction spa = ((SetParentAction)action);
          
          gotCode = spa.getChildCode();
          gotTypeCode = spa.getChildTypeCode();
          orgCode = Organization.getRootOrganizationCode(Universal.getByKey(gotTypeCode).getOwnerOid());
          
          setParent = (SetParentAction) action;
        }
        else if (action instanceof CreateGeoObjectAction)
        {
          CreateGeoObjectAction create = ((CreateGeoObjectAction)action);
          
          gotCode = GeoObjectOverTimeJsonAdapters.GeoObjectDeserializer.getCode(create.getGeoObjectJson());
          gotTypeCode = GeoObjectOverTimeJsonAdapters.GeoObjectDeserializer.getTypeCode(create.getGeoObjectJson());
          orgCode = Organization.getRootOrganizationCode(Universal.getByKey(gotTypeCode).getOwnerOid());
          
          createAction = (CreateGeoObjectAction) action;
        }
        else if (action instanceof UpdateGeoObjectAction)
        {
          UpdateGeoObjectAction update = ((UpdateGeoObjectAction)action);
          
          gotCode = GeoObjectOverTimeJsonAdapters.GeoObjectDeserializer.getCode(update.getGeoObjectJson());
          gotTypeCode = GeoObjectOverTimeJsonAdapters.GeoObjectDeserializer.getTypeCode(update.getGeoObjectJson());
          orgCode = Organization.getRootOrganizationCode(Universal.getByKey(gotTypeCode).getOwnerOid());
          
          hasInvalidAction = true;
        }
        else if (action instanceof UpdateAttributeAction)
        {
          continue Outer;
        }
        else
        {
          throw new UnsupportedOperationException("Unexpected action type [" + action.getType() + "].");
        }
      }
      
      if (setParent != null && createAction != null)
      {
        createAction.appLock();
        createAction.setParentJson(setParent.getJson());
        createAction.apply();
        
        setParent.delete();
      }
      else if (setParent != null || createAction != null)
      {
        // A set parent without a create (or vice versa)? This isn't supposed to exist
        cr.clearApprovalStatus();
        cr.addApprovalStatus(AllGovernanceStatus.INVALID);
      }
      
      cr.setOrganizationCode(orgCode);
      cr.setGeoObjectCode(gotCode);
      cr.setGeoObjectTypeCode(gotTypeCode);
      
      if (cr.getGovernanceStatus().equals(AllGovernanceStatus.PENDING) && hasInvalidAction)
      {
        cr.clearApprovalStatus();
        cr.addApprovalStatus(AllGovernanceStatus.INVALID);
      }
      
      cr.apply();
    }
  }
  
  private void patchAllGos()
  {
    for (Universal uni : getUniversals())
    {
      MdGeoVertexDAO mdVertex = GeoVertexType.getMdGeoVertex(uni.getUniversalId());
      
      List<? extends MdAttributeDAOIF> attributes = mdVertex.getAllDefinedMdAttributes();
      
      String[] skipAttrs = new String[] {
          DefaultAttribute.UID.getName(), "uuid", DefaultAttribute.CODE.getName(), DefaultAttribute.CREATE_DATE.getName(), DefaultAttribute.LAST_UPDATE_DATE.getName(),
          DefaultAttribute.SEQUENCE.getName(), DefaultAttribute.TYPE.getName(), MdAttributeConcreteInfo.OID, MdAttributeConcreteInfo.SEQUENCE
      };
      
      
      StringBuilder statement = new StringBuilder();
      statement.append("SELECT FROM " + mdVertex.getDBClassName());

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
      
      List<VertexObject> results = query.getResults();
      
      logger.info("Updating [" + results.size() + "] objects on table [" + mdVertex.getDBClassName() + "].");
      
      for (VertexObject vo : query.getResults())
      {
        for (MdAttributeDAOIF attr : attributes)
        {
          if (!ArrayUtils.contains(skipAttrs, attr.definesAttribute()))
          {
            ValueOverTimeCollection col = vo.getValuesOverTime(attr.definesAttribute());
            
            for (ValueOverTime vot : col)
            {
              vot.setOid(UUID.randomUUID().toString());
            }
          }
        }
        
        vo.apply();
      }
    }
  }
  
  public void executeGraphDDLCommand(String sql)
  {
    GraphDBService service = GraphDBService.getInstance();
    GraphRequest dml = service.getGraphDBRequest();
    GraphRequest ddl = service.getDDLGraphDBRequest();
    
    GraphDDLCommandAction action = service.ddlCommand(dml, ddl, sql, new HashMap<String, Object>());
    action.execute();
  }
  
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
      
//      MdGeoVertexDAOIF superType = GeoVertexType.getMdGeoVertex(uni.getUniversalId()).getSuperClass();
//      
//      if (superType != null && !superType.definesType().equals(GeoVertex.CLASS))
//      {
//        it.remove();
//        continue;
//      }
    }
    
    return unis;
  }
}
