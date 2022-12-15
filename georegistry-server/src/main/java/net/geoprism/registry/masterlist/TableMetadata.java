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
package net.geoprism.registry.masterlist;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.localization.LocalizedValueIF;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.system.metadata.MdAttribute;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.registry.ListTypeAttribute;
import net.geoprism.registry.ListTypeGeoObjectTypeGroup;
import net.geoprism.registry.ListTypeGroup;
import net.geoprism.registry.ListTypeHierarchyGroup;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.LocalizedValueContainer;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class TableMetadata
{
  public static abstract class Group
  {
    private Group       parent;

    private List<Group> children;

    public Group()
    {
      this.parent = null;
      this.children = new LinkedList<Group>();
    }

    public Group getParent()
    {
      return parent;
    }

    public void setParent(Group parent)
    {
      this.parent = parent;
    }

    public List<Group> getChildren()
    {
      return children;
    }

    public <T extends Group> T addChild(T child)
    {
      child.setParent(this);

      this.children.add(child);

      return child;
    }

    public abstract void create(ListTypeVersion version, ListTypeGroup parent);
  }

  public static class HierarchyGroup extends Group
  {

    private ServerHierarchyType hierarchy;

    public HierarchyGroup(ServerHierarchyType hierarchy)
    {
      super();
      this.hierarchy = hierarchy;
    }

    public ServerHierarchyType getHierarchy()
    {
      return hierarchy;
    }

    public void setHierarchy(ServerHierarchyType hierarchy)
    {
      this.hierarchy = hierarchy;
    }

    @Override
    public void create(ListTypeVersion version, ListTypeGroup parent)
    {
      ListTypeHierarchyGroup group = ListTypeHierarchyGroup.create(version, parent, this.hierarchy);

      this.getChildren().forEach(child -> {
        child.create(version, group);
      });
    }

    public TypeGroup addTypeGroup(ServerGeoObjectType type, int level)
    {
      return this.addChild(new TypeGroup(type, level));
    }
  }

  public static class TypeGroup extends Group
  {

    private ServerGeoObjectType type;

    private Integer             level;

    public TypeGroup(ServerGeoObjectType type)
    {
      this.type = type;
      this.level = null;
    }

    public TypeGroup(ServerGeoObjectType type, Integer level)
    {
      this.type = type;
      this.level = level;
    }

    public ServerGeoObjectType getType()
    {
      return type;
    }

    public void setType(ServerGeoObjectType type)
    {
      this.type = type;
    }

    public Integer getLevel()
    {
      return level;
    }

    public void setLevel(Integer level)
    {
      this.level = level;
    }

    @Override
    public void create(ListTypeVersion version, ListTypeGroup parent)
    {
      ListTypeGeoObjectTypeGroup group = ListTypeGeoObjectTypeGroup.create(version, parent, this.type, this.level);

      this.getChildren().forEach(child -> {
        child.create(version, group);
      });
    }
  }

  public static class AttributeGroup extends Group
  {

    private AttributeType attributeType;

    public AttributeGroup(AttributeType attributeType)
    {
      this.attributeType = attributeType;
    }

    @Override
    public void create(ListTypeVersion version, ListTypeGroup parent)
    {
      LocalizedValue label = this.attributeType.getLabel();

      ListTypeGroup group = ListTypeGroup.create(version, parent, new LocalizedValueContainer(label));

      this.getChildren().forEach(child -> {
        child.create(version, group);
      });
    }
  }

  public static class HolderGroup extends Group
  {
    public HolderGroup()
    {

    }

    @Override
    public void create(ListTypeVersion version, ListTypeGroup parent)
    {
      ListTypeGroup group = ListTypeGroup.create(version, parent, null);

      this.getChildren().forEach(child -> {
        child.create(version, group);
      });
    }
  }

  public static class Attribute extends Group
  {

    private SupportedLocaleIF locale;

    private MdAttributeDAOIF  mdAttribute;

    private LocalizedValueIF  label;

    private int               rowspan = 1;

    public Attribute(MdAttribute mdAttribute)
    {
      this.mdAttribute = (MdAttributeDAOIF) BusinessFacade.getEntityDAO(mdAttribute);
      this.label = mdAttribute.getDisplayLabel();
    }

    public Attribute(MdAttribute mdAttribute, int rowspan)
    {
      this.rowspan = rowspan;
      this.mdAttribute = (MdAttributeDAOIF) BusinessFacade.getEntityDAO(mdAttribute);
      this.label = mdAttribute.getDisplayLabel();
    }

    public Attribute(MdAttributeDAOIF mdAttribute)
    {
      this.mdAttribute = mdAttribute;
      this.label = new LocalizedValueContainer(LocalizedValueConverter.convert(mdAttribute.getDisplayLabels()));
    }

    public Attribute(MdAttributeDAOIF mdAttribute, int rowspan)
    {
      this.mdAttribute = mdAttribute;
      this.rowspan = rowspan;
      this.label = new LocalizedValueContainer(LocalizedValueConverter.convert(mdAttribute.getDisplayLabels()));
    }

    public Attribute(MdAttributeDAOIF mdAttribute, LocalizedValue label)
    {

      this.mdAttribute = mdAttribute;
      this.label = new LocalizedValueContainer(label);
    }

    public Attribute(MdAttributeDAOIF mdAttribute, LocalizedValueIF label)
    {

      this.mdAttribute = mdAttribute;
      this.label = label;
    }

    public Attribute(MdAttribute mdAttribute, LocalizedValueIF label)
    {
      this.mdAttribute = (MdAttributeDAOIF) BusinessFacade.getEntityDAO(mdAttribute);
      this.label = label;
    }

    public Attribute(MdAttributeDAOIF mdAttribute, SupportedLocaleIF locale)
    {
      this.mdAttribute = mdAttribute;
      this.locale = locale;
      this.label = locale.getDisplayLabel();
    }

    public Attribute(MdAttribute mdAttribute, SupportedLocaleIF locale)
    {
      this.mdAttribute = (MdAttributeDAOIF) BusinessFacade.getEntityDAO(mdAttribute);
      this.locale = locale;
      this.label = locale.getDisplayLabel();
    }

    public SupportedLocaleIF getLocale()
    {
      return locale;
    }

    public void setLocale(SupportedLocaleIF locale)
    {
      this.locale = locale;
    }

    public MdAttributeDAOIF getMdAttribute()
    {
      return mdAttribute;
    }

    public void setMdAttribute(MdAttributeDAOIF mdAttribute)
    {
      this.mdAttribute = mdAttribute;
    }

    public LocalizedValueIF getLabel()
    {
      return label;
    }

    public void setLabel(LocalizedValueIF label)
    {
      this.label = label;
    }

    public int getRowspan()
    {
      return rowspan;
    }

    public void setRowspan(int rowspan)
    {
      this.rowspan = rowspan;
    }

    @Override
    public void create(ListTypeVersion version, ListTypeGroup parent)
    {
      ListTypeAttribute.create(parent, this.mdAttribute, this.locale, this.label, this.rowspan);
    }
  }

  private MdBusiness  mdBusiness;

  private List<Group> groups;

  private TypeGroup   root;

  public TableMetadata()
  {

  }

  public TableMetadata(MdBusiness mdBusiness, ServerGeoObjectType type)
  {
    this.mdBusiness = mdBusiness;
    this.groups = new LinkedList<Group>();
    this.root = new TypeGroup(type);

    this.groups.add(this.root);
  }

  public MdBusiness getMdBusiness()
  {
    return mdBusiness;
  }

  public void setMdBusiness(MdBusiness mdBusiness)
  {
    this.mdBusiness = mdBusiness;
  }

  public List<Group> getGroups()
  {
    return groups;
  }

  public TypeGroup getRoot()
  {
    return root;
  }

  public HierarchyGroup addRootHierarchyGroup(ServerHierarchyType hierarchy)
  {
    HierarchyGroup group = new HierarchyGroup(hierarchy);

    this.groups.add(group);

    return group;
  }
}
