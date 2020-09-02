package net.geoprism.registry.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.Command;

import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.service.WMSService;

public class GeoserverCreateWMSCommand implements Command
{
  private Logger            log = LoggerFactory.getLogger(GeoserverCreateWMSCommand.class);

  private MasterListVersion version;

  public GeoserverCreateWMSCommand(MasterListVersion version)
  {
    this.version = version;
  }

  /**
   * Executes the statement in this Command.
   */
  public void doIt()
  {
    log.info("Creating WMS for MasterListVersion [" + this.version.getOid() + "]");

    new WMSService().createGeoServerLayer(version, false);
  }

  /**
   * Executes the undo in this Command, and closes the connection.
   */
  public void undoIt()
  {
  }

  /**
   * Returns a human readable string describing what this command is trying to
   * do.
   * 
   * @return human readable string describing what this command is trying to do.
   */
  public String doItString()
  {
    return null;
  }

  public String undoItString()
  {
    return null;
  }

  /*
   * Indicates if this Command deletes something.
   * 
   * @return <code><b>true</b></code> if this Command deletes something.
   */
  public boolean isUndoable()
  {
    return false;
  }

  @Override
  public void doFinally()
  {
  }

}
