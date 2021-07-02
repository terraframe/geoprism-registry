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
package com.runwaysdk.build.domain;

import java.util.HashMap;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.metadata.MdAttributeBooleanDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;

import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.model.ServerGeoObjectType;

public class InvalidPatch
{
  private static final Logger logger = LoggerFactory.getLogger(InvalidPatch.class);
  
  public static void main(String[] args)
  {
    new InvalidPatch().doItInReq();
  }
  
  @Request
  private void doItInReq()
  {
    doItInTrans();
  }

  @Transaction
  private void doItInTrans()
  {
    QueryFactory qf = new QueryFactory();
    UniversalQuery uq = new UniversalQuery(qf);
    OIterator<? extends Universal> it = uq.getIterator();

    try
    {
      while (it.hasNext())
      {
        Universal uni = it.next();

        if (uni.getKey().equals(Universal.ROOT_KEY))
        {
          continue;
        }

        ServerGeoObjectType type = new ServerGeoObjectTypeConverter().build(uni);
        
        MdGraphClassDAOIF mdClass = type.getMdVertex();
        
        MdAttributeDAOIF existing = mdClass.definesAttribute(DefaultAttribute.INVALID.getName());
        
        if (existing == null)
        {
          logger.info("Adding invalid attribute to [" + mdClass.getKey() + "].");
          
          MdAttributeBooleanDAO invalidMdAttr = MdAttributeBooleanDAO.newInstance();
          invalidMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.INVALID.getName());
          invalidMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.INVALID.getDefaultLocalizedName());
          invalidMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.INVALID.getDefaultDescription());
          invalidMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
          invalidMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
          invalidMdAttr.setValue(MdAttributeConcreteInfo.DEFAULT_VALUE, MdAttributeBooleanInfo.FALSE);
          invalidMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
          invalidMdAttr.apply();
          
          StringBuilder statement = new StringBuilder();
          statement.append("UPDATE " + mdClass.getDBClassName() + " SET invalid=false");

          GraphDBService service = GraphDBService.getInstance();
          GraphRequest request = service.getGraphDBRequest();

          service.command(request, statement.toString(), new HashMap<>());
        }
      }
    }
    finally
    {
      it.close();
    }
  }
}
