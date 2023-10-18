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

import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.DeleteContext;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;

public class PatchCodeMetadata
{
  public static void main(String[] args)
  {
    new PatchCodeMetadata().doIt();
  }

  @Request
  private void doIt()
  {
    this.transaction();
  }

  @Transaction
  private void transaction()
  {
    MdAttributeDAOIF mdAttributeGeoId = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS).definesAttribute("geoId");

    if (mdAttributeGeoId != null)
    {
      mdAttributeGeoId.getBusinessDAO().delete();
    }

    List<ServerGeoObjectType> types = ServiceFactory.getMetadataCache().getAllGeoObjectTypes();
    types = types.stream().filter(t -> t.getIsAbstract()).collect(Collectors.toList());

    for (ServerGeoObjectType type : types)
    {
      MdVertexDAOIF mdVertex = type.getMdVertex();

      if (mdVertex.definesAttribute(DefaultAttribute.CODE.getName()) != null)
      {
        // Remove the code attribute
        delete(type);

        create(type);
      }
    }
  }

  // @Transaction
  public void create(ServerGeoObjectType type)
  {
    List<ServerGeoObjectType> subtypes = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class).getSubtypes(type);

    for (ServerGeoObjectType subtype : subtypes)
    {
      // Add the code attribute to the subtypes
      MdClassDAOIF[] mdClasses = new MdClassDAOIF[] { subtype.getMdVertex(), subtype.getMdBusinessDAO() };

      for (MdClassDAOIF mdClass : mdClasses)
      {
        MdAttributeCharacterDAO codeMdAttr = MdAttributeCharacterDAO.newInstance();
        codeMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.CODE.getName());
        codeMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.CODE.getDefaultLocalizedName());
        codeMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.CODE.getDefaultDescription());
        codeMdAttr.setValue(MdAttributeCharacterInfo.SIZE, MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
        codeMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
        codeMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
        codeMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.UNIQUE_INDEX.getOid());
        codeMdAttr.apply();
      }

    }
  }

  // @Transaction
  public void delete(ServerGeoObjectType type)
  {
    DeleteContext context = new DeleteContext();
    context.setRemoveValues(false);
    context.setExecuteImmediately(true);

    type.getMdVertex().definesAttribute(DefaultAttribute.CODE.getName()).getBusinessDAO().delete(context);
    type.getMdBusinessDAO().definesAttribute(DefaultAttribute.CODE.getName()).getBusinessDAO().delete(context);
  }
}
