/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package com.runwaysdk.build.domain;

import java.util.Collection;
import java.util.List;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.Pair;
import com.runwaysdk.constants.BusinessInfo;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeFloatInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeFloatDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.LocalizedValueStore;
import com.runwaysdk.localization.LocalizedValueStoreStoreValue;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.localization.DefaultLocaleView;
import net.geoprism.registry.masterlist.TableMetadata;
import net.geoprism.registry.masterlist.TableMetadata.Attribute;
import net.geoprism.registry.masterlist.TableMetadata.AttributeGroup;
import net.geoprism.registry.masterlist.TableMetadata.Group;
import net.geoprism.registry.masterlist.TableMetadata.HierarchyGroup;
import net.geoprism.registry.masterlist.TableMetadata.HolderGroup;
import net.geoprism.registry.masterlist.TableMetadata.TypeGroup;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class PatchListGroupMetadata
{
  public static void main(String[] args)
  {
    new PatchListGroupMetadata().doIt();
  }

  @Request
  private void doIt()
  {
    transaction();
  }

  @Transaction
  private void transaction()
  {
    LocalizedValueStore lvs = LocalizedValueStore.getByKey(DefaultLocaleView.LABEL);
    LocalizedValueStoreStoreValue defaultLocaleLabel = lvs.getStoreValue();
    Collection<SupportedLocaleIF> locales = LocalizationFacade.getSupportedLocales();

    List<? extends ListTypeVersion> versions = ListTypeVersion.getAll();

    for (ListTypeVersion version : versions)
    {
      this.patch(version, defaultLocaleLabel, locales);
    }
  }

  public void patch(ListTypeVersion version, LocalizedValueStoreStoreValue defaultLocaleLabel, Collection<SupportedLocaleIF> locales)
  {
    ListType listType = version.getListType();
    ServerGeoObjectType type = listType.getGeoObjectType();
    Collection<AttributeType> attributes = type.getAttributeMap().values();

    TableMetadata metadata = new TableMetadata(version.getMdBusiness(), type);
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());

    for (AttributeType attribute : attributes)
    {
      String name = attribute.getName();

      if (this.isValid(attribute))
      {

        if (attribute instanceof AttributeTermType || attribute instanceof AttributeClassificationType)
        {
          AttributeGroup attributeGroup = metadata.getRoot().addChild(new AttributeGroup(attribute));

          MdAttributeConcreteDAOIF codeAttribute = mdBusiness.definesAttribute(name);

          attributeGroup.addChild(new Attribute(codeAttribute, LocalizationFacade.localizeAll("data.property.label.code")));

          MdAttributeConcreteDAOIF mdAttributeDefaultLocale = mdBusiness.definesAttribute(name + ListTypeVersion.DEFAULT_LOCALE);

          attributeGroup.addChild(new Attribute(mdAttributeDefaultLocale, defaultLocaleLabel));

          for (SupportedLocaleIF locale : locales)
          {
            MdAttributeConcreteDAOIF mdAttributeLocale = mdBusiness.definesAttribute(name + locale.toString());

            if (mdAttributeLocale != null)
            {
              attributeGroup.addChild(new Attribute(mdAttributeLocale, locale));
            }
          }
        }
        else if (attribute instanceof AttributeLocalType)
        {
          AttributeGroup attributeGroup = metadata.getRoot().addChild(new AttributeGroup(attribute));

          MdAttributeConcreteDAOIF mdAttributeDefaultLocale = mdBusiness.definesAttribute(name + ListTypeVersion.DEFAULT_LOCALE);

          attributeGroup.addChild(new Attribute(mdAttributeDefaultLocale, defaultLocaleLabel));

          for (SupportedLocaleIF locale : locales)
          {
            MdAttributeConcreteDAOIF mdAttributeLocale = mdBusiness.definesAttribute(name + locale.toString());

            if (mdAttributeLocale != null)
            {
              attributeGroup.addChild(new Attribute(mdAttributeLocale, locale));
            }
          }

        }
        else if (! ( attribute.getName().equals(DefaultAttribute.UID.getName()) || attribute.getName().equals(DefaultAttribute.INVALID.getName()) ))
        {
          MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(name);

          if (mdAttribute != null)
          {
            metadata.getHolder().addChild(new Attribute(mdAttribute));
          }
        }
      }
    }

    if ( ( type.getGeometryType().equals(GeometryType.MULTIPOINT) || type.getGeometryType().equals(GeometryType.POINT) ) && listType.getIncludeLatLong())
    {
      MdAttributeConcreteDAOIF mdAttributeLatitude = mdBusiness.definesAttribute("latitude");

      if (mdAttributeLatitude != null)
      {
        metadata.getHolder().addChild(new Attribute(mdAttributeLatitude));
      }

      MdAttributeConcreteDAOIF mdAttributeLongitude = mdBusiness.definesAttribute("longitude");

      if (mdAttributeLongitude != null)
      {
        metadata.getHolder().addChild(new Attribute(mdAttributeLongitude));
      }
    }

    JsonArray hierarchies = listType.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<Pair<String, Integer>> pCodes = listType.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();

        ServerHierarchyType hierarchyType = ServiceFactory.getMetadataCache().getHierachyType(hCode).get();

        HierarchyGroup hierarchyGroup = metadata.addRootHierarchyGroup(hierarchyType);

        int level = 0;

        for (Pair<String, Integer> pair : pCodes)
        {
          level++;

          String pCode = pair.getFirst();

          ServerGeoObjectType got = ServerGeoObjectType.get(pCode);

          TypeGroup typeGroup = hierarchyGroup.addTypeGroup(got, level);

          String attributeName = hCode.toLowerCase() + pCode.toLowerCase();

          MdAttributeConcreteDAOIF mdAttributeCode = mdBusiness.definesAttribute(attributeName);

          if (mdAttributeCode != null)
          {
            typeGroup.addChild(new Attribute(mdAttributeCode, LocalizationFacade.localizeAll("data.property.label.code")));

            MdAttributeConcreteDAOIF mdAttributeDefaultLocale = mdBusiness.definesAttribute(attributeName + ListTypeVersion.DEFAULT_LOCALE);

            typeGroup.addChild(new Attribute(mdAttributeDefaultLocale, defaultLocaleLabel));

            for (SupportedLocaleIF locale : locales)
            {
              MdAttributeConcreteDAOIF mdAttributeLocale = mdBusiness.definesAttribute(attributeName + locale.getLocale().toString());

              if (mdAttributeLocale != null)
              {
                typeGroup.addChild(new Attribute(mdAttributeLocale, locale));
              }

            }
          }
        }
      }
    }

    JsonArray subtypeHierarchies = listType.getSubtypeHierarchiesAsJson();

    for (int i = 0; i < subtypeHierarchies.size(); i++)
    {
      JsonObject hierarchy = subtypeHierarchies.get(i).getAsJsonObject();

      if (hierarchy.has("selected") && hierarchy.get("selected").getAsBoolean())
      {
        String hCode = hierarchy.get("code").getAsString();

        ServerHierarchyType hierarchyType = ServerHierarchyType.get(hCode);

        HierarchyGroup hierarchyGroup = metadata.addRootHierarchyGroup(hierarchyType);

        HolderGroup holderGroup = hierarchyGroup.addChild(new HolderGroup());

        String attributeName = hCode.toLowerCase();

        MdAttributeConcreteDAOIF mdAttributeCode = mdBusiness.definesAttribute(attributeName);

        if (mdAttributeCode != null)
        {
          holderGroup.addChild(new Attribute(mdAttributeCode, LocalizationFacade.localizeAll("data.property.label.code")));

          MdAttributeConcreteDAOIF mdAttributeDefaultLocale = mdBusiness.definesAttribute(attributeName + ListTypeVersion.DEFAULT_LOCALE);

          holderGroup.addChild(new Attribute(mdAttributeDefaultLocale, defaultLocaleLabel));

          for (SupportedLocaleIF locale : locales)
          {
            MdAttributeConcreteDAOIF mdAttributeLocale = mdBusiness.definesAttribute(attributeName + locale.getLocale().toString());

            if (mdAttributeLocale != null)
            {
              holderGroup.addChild(new Attribute(mdAttributeLocale, locale));
            }
          }
        }
      }
    }

    List<Group> groups = metadata.getGroups();

    for (Group group : groups)
    {
      group.create(version, null);
    }
  }

  private boolean isValid(AttributeType attributeType)
  {
    if (attributeType.getName().equals(DefaultAttribute.UID.getName()))
    {
      return true;
    }

    if (attributeType.getName().equals(DefaultAttribute.SEQUENCE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.LAST_UPDATE_DATE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.CREATE_DATE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.EXISTS.getName()))
    {
      return false;
    }

    return true;
  }

  public boolean isValid(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute.isSystem() || mdAttribute.definesAttribute().equals(DefaultAttribute.UID.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.SEQUENCE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.LAST_UPDATE_DATE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.CREATE_DATE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.EXISTS.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(ListTypeVersion.ORIGINAL_OID))
    {
      return false;
    }

    // if (mdAttribute.definesAttribute().endsWith("Oid"))
    // {
    // return false;
    // }

    if (mdAttribute.definesAttribute().equals(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.OWNER))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.KEY))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.DOMAIN))
    {
      return false;
    }

    return true;
  }

}
