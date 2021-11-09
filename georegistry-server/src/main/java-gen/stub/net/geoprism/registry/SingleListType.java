package net.geoprism.registry;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Session;

import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class SingleListType extends SingleListTypeBase
{
  private static final long serialVersionUID = 1505919949;

  public SingleListType()
  {
    super();
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
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
  public void publishVersions()
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));

    try
    {
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

      ListTypeEntry entry = this.getOrCreateEntry(this.getValidOn());

      ( (Session) Session.getCurrentSession() ).reloadPermissions();

      entry.publish();
    }
    finally
    {
      Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    }
  }

  @Override
  protected String formatVersionLabel(LabeledVersion version)
  {
    return GeoRegistryUtil.formatDate(this.getValidOn(), false);
  }

}
