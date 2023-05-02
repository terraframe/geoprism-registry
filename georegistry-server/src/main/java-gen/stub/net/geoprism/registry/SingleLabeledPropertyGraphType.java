package net.geoprism.registry;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;

public class SingleLabeledPropertyGraphType extends SingleLabeledPropertyGraphTypeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1579048184;

  public SingleLabeledPropertyGraphType()
  {
    super();
  }

  @Override
  public JsonObject toJSON(boolean includeEntries)
  {
    JsonObject object = super.toJSON(includeEntries);
    object.addProperty(GRAPH_TYPE, SINGLE);
    object.addProperty(VALIDON, GeoRegistryUtil.formatDate(this.getValidOn(), false));

    return object;
  }

  @Override
  protected void parse(JsonObject object)
  {
    super.parse(object);

    this.setValidOn(GeoRegistryUtil.parseDate(object.get(SingleLabeledPropertyGraphType.VALIDON).getAsString()));
  }

  @Override
  @Transaction
  public void createEntries(JsonObject metadata)
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    this.getOrCreateEntry(this.getValidOn(), metadata);
  }

  @Override
  protected JsonObject formatVersionLabel(LabeledVersion version)
  {
    JsonObject object = new JsonObject();
    object.addProperty("type", "date");
    object.addProperty("value", GeoRegistryUtil.formatDate(this.getValidOn(), false));

    return object;
  }

}
