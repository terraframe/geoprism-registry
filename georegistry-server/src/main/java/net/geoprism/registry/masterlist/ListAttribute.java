package net.geoprism.registry.masterlist;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMomentDAOIF;
import com.runwaysdk.dataaccess.MdAttributeNumberDAOIF;
import com.runwaysdk.session.Session;

public class ListAttribute implements ListColumn
{
  public static String NAME  = "name";

  public static String LABEL = "label";

  public static String VALUE = "value";

  public static String TYPE  = "type";

  private String       id;

  private String       name;

  private String       label;

  private String       type;

  private JsonObject   value;

  public ListAttribute(MdAttributeConcreteDAOIF mdAttribute)
  {
    this.id = mdAttribute.getOid();
    this.name = mdAttribute.definesAttribute();
    this.label = mdAttribute.getDisplayLabel(Session.getCurrentLocale());
    this.type = "input";

    if (mdAttribute instanceof MdAttributeMomentDAOIF)
    {
      this.type = "date";
      this.value = new JsonObject();
    }
    else if (mdAttribute instanceof MdAttributeBooleanDAOIF)
    {
      this.type = "boolean";
    }
    else if (mdAttribute instanceof MdAttributeNumberDAOIF)
    {
      this.type = "number";
    }
  }

  public ListAttribute(MdAttributeConcreteDAOIF mdAttribute, String label)
  {
    this(mdAttribute);

    this.label = label;
  }

  public ListAttribute(String name, String label, String type)
  {
    this.name = name;
    this.label = label;
    this.type = type;
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject attribute = new JsonObject();
    attribute.addProperty(NAME, this.name);
    attribute.addProperty(LABEL, this.label);
    attribute.addProperty(TYPE, this.type);

    if (this.value != null)
    {
      attribute.add(VALUE, this.value);
    }

    return attribute;
  }

  @Override
  public int getNumberOfColumns()
  {
    return 1;
  }

  public String getName()
  {
    return name;
  }

  @Override
  public Set<String> getColumnsIds()
  {
    if (this.id != null)
    {
      return Collections.singleton(this.id);
    }

    return new TreeSet<String>();
  }

}
