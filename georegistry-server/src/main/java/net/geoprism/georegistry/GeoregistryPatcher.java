package net.geoprism.georegistry;

import java.io.File;

import com.runwaysdk.constants.DeployProperties;
import com.runwaysdk.session.Request;

import net.geoprism.GeoprismPatcher;

public class GeoregistryPatcher extends GeoprismPatcher
{
  public GeoregistryPatcher(File metadataDir)
  {
    super(metadataDir);
  }

  @Override
  protected String[] getModules()
  {
    return new String[] { "geoprism", "georegistry" };
  }

  public static void main(String[] args)
  {
    String metadataPath = null;

    if (args.length > 0)
    {
      metadataPath = args[0];
    }

    executeWithRequest(metadataPath);
  }

  @Request
  private static void executeWithRequest(String metadataPath)
  {
    File fMetadataPath = null;
    if (metadataPath == null)
    {
      metadataPath = DeployProperties.getDeployBin();
      fMetadataPath = new File(metadataPath, "metadata");
    }
    else
    {
      fMetadataPath = new File(metadataPath);
    }

    execute(fMetadataPath);
  }

  public static void execute(File metadataDir)
  {
    GeoprismPatcher listener = new GeoregistryPatcher(metadataDir);
    listener.initialize();
    listener.startup();
    listener.shutdown();
  }
}
