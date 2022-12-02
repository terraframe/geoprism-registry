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
package net.geoprism.registry.etl;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.runwaysdk.resource.CloseableFile;

public class CloseableDelegateFile extends CloseableFile
{
  private static final long serialVersionUID = 6284756151941329839L;

  private File              delegate         = null;

  public CloseableDelegateFile(File file, File delegate)
  {
    super(file.toURI(), true);
    this.delegate = delegate;
  }

  public void close()
  {
    if (this.isDeleteOnClose())
    {
      FileUtils.deleteQuietly(this.delegate);
    }
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof CloseableDelegateFile)
    {
      return this.delegate.equals( ( (CloseableDelegateFile) obj ).delegate);
    }

    return false;
  }
}
