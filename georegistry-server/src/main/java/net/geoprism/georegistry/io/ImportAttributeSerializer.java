package net.geoprism.georegistry.io;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonObject;

public class ImportAttributeSerializer implements CustomSerializer
{
  private Set<String> filter;

  private Set<String> required;

  private boolean     includeCoordinates;

  public ImportAttributeSerializer(boolean includeCoordinates)
  {
    this(includeCoordinates, false);
  }

  public ImportAttributeSerializer(boolean includeCoordinates, boolean includeUid)
  {
    this.includeCoordinates = includeCoordinates;

    this.filter = new TreeSet<String>();
    this.filter.add(DefaultAttribute.STATUS.getName());
    this.filter.add(DefaultAttribute.LAST_UPDATE_DATE.getName());
    this.filter.add(DefaultAttribute.CREATE_DATE.getName());
    this.filter.add(DefaultAttribute.SEQUENCE.getName());
    this.filter.add(DefaultAttribute.TYPE.getName());

    if (!includeUid)
    {
      this.filter.add(DefaultAttribute.UID.getName());
    }

    this.required = new TreeSet<String>();
    this.required.add(DefaultAttribute.CODE.getName());
    this.required.add(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName());
    this.required.add(GeoObjectConfiguration.LATITUDE);
    this.required.add(GeoObjectConfiguration.LONGITUDE);
  }

  public Set<String> getFilter()
  {
    return filter;
  }

  @Override
  public Collection<AttributeType> attributes(GeoObjectType type)
  {
    List<AttributeType> attributes = type.getAttributeMap().values().stream().filter(attributeType -> !this.filter.contains(attributeType.getName())).collect(Collectors.toList());

    if (this.includeCoordinates)
    {
      attributes.add(0, GeoObjectConfiguration.latitude());
      attributes.add(0, GeoObjectConfiguration.longitude());
    }

    return attributes;
  }

  @Override
  public void configure(AttributeType attributeType, JsonObject json)
  {
    json.addProperty("required", this.required.contains(attributeType.getName()));
  }

}
