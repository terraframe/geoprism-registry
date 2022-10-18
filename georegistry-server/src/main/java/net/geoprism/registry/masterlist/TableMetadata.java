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
package net.geoprism.registry.masterlist;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.localization.LocalizedValueIF;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.registry.ListTypeAttribute;
import net.geoprism.registry.ListTypeGeoObjectTypeGroup;
import net.geoprism.registry.ListTypeGroup;
import net.geoprism.registry.ListTypeHierarchyGroup;
import net.geoprism.registry.ListTypeVersion;
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

    public Group(Group parent)
    {
      this.parent = parent;
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

    public <T extends Group> T addChild(T group)
    {
      this.children.add(group);

      return group;
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
      ListTypeHierarchyGroup group = ListTypeHierarchyGroup.create(version, this.hierarchy);

      this.getChildren().forEach(child -> {
        child.create(version, group);
      });
    }

    public TypeGroup addTypeGroup(ServerGeoObjectType type, int level)
    {
      return this.addChild(new TypeGroup(this, type, level));
    }
  }

  public static class TypeGroup extends Group
  {

    private ServerGeoObjectType type;

    private Integer             level;

    public TypeGroup(Group parent, ServerGeoObjectType type, Integer level)
    {
      super(parent);
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

  public static class Attribute extends Group
  {

    private SupportedLocaleIF locale;

    private MdAttributeDAO    mdAttribute;

    private LocalizedValueIF  label;

    public Attribute(Group parent, MdAttributeDAO mdAttribute, LocalizedValue label)
    {
      super(parent);

      this.mdAttribute = mdAttribute;
      this.label = new LocalizedValueContainer(label);
    }

    public Attribute(Group parent, MdAttributeDAO mdAttribute, LocalizedValueIF label)
    {
      super(parent);

      this.mdAttribute = mdAttribute;
      this.label = label;
    }

    public Attribute(Group parent, MdAttributeDAO mdAttribute, SupportedLocaleIF locale)
    {
      super(parent);

      this.locale = locale;
      this.mdAttribute = mdAttribute;
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

    public MdAttributeDAO getMdAttribute()
    {
      return mdAttribute;
    }

    public void setMdAttribute(MdAttributeDAO mdAttribute)
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

    @Override
    public void create(ListTypeVersion version, ListTypeGroup parent)
    {
      ListTypeAttribute.create(parent, this.mdAttribute, this.locale, this.label);
    }
  }

  private MdBusiness  mdBusiness;

  private List<Group> groups;

  public TableMetadata()
  {
    this.groups = new LinkedList<Group>();
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

  public HierarchyGroup addHierarchyGroup(ServerHierarchyType hierarchy)
  {
    HierarchyGroup group = new HierarchyGroup(hierarchy);

    this.groups.add(group);

    return group;
  }
}
