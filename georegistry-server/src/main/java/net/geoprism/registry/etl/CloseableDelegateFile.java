package net.geoprism.registry.etl;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.runwaysdk.resource.CloseableFile;

public class CloseableDelegateFile extends CloseableFile
{
  private static final long serialVersionUID = 6284756151941329839L;
  
  private File delegate = null;

  public CloseableDelegateFile(File file, File delegate)
  {
    super(file.toURI(), true);
    this.delegate = delegate;
  }
  
  public void close()
  {
    if (this.isTemporary())
    {
      FileUtils.deleteQuietly(this.delegate);
    }
  }
}
