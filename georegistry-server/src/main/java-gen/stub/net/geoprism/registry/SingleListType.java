package net.geoprism.registry;

import com.google.gson.JsonObject;

public class SingleListType extends SingleListTypeBase
{
  private static final long serialVersionUID = 1505919949;

  public SingleListType()
  {
    super();
  }
  
  @Override
  public JsonObject toJSON(boolean includeEntries)
  {
    JsonObject object = super.toJSON(includeEntries);
    object.addProperty(LIST_TYPE, SINGLE);
    object.addProperty(VALIDON, GeoRegistryUtil.formatDate(this.getValidOn(), false));

    return object;
  }

  @Override
  protected void parse(JsonObject object)
  {
    super.parse(object);

    this.setValidOn(GeoRegistryUtil.parseDate(object.get(SingleListType.VALIDON).getAsString()));
  }

  @Override
  public void createEntries()
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    this.getOrCreateEntry(this.getValidOn());
  }

  @Override
  protected String formatVersionLabel(LabeledVersion version)
  {
    return GeoRegistryUtil.formatDate(this.getValidOn(), false);
  }

}
