/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism;

import java.io.IOException;
import java.io.InputStream;

public class ContentStream extends InputStream
{
  private final InputStream in;

  private final String      contentType;

  public ContentStream(InputStream in, String contentType)
  {
    this.in = in;
    this.contentType = contentType;
  }

  public int read() throws IOException
  {
    return in.read();
  }

  public int read(byte b[]) throws IOException
  {
    return in.read(b);
  }

  public int read(byte b[], int off, int len) throws IOException
  {
    return in.read(b, off, len);
  }

  public long skip(long n) throws IOException
  {
    return in.skip(n);
  }

  public int available() throws IOException
  {
    return in.available();
  }

  public void close() throws IOException
  {
    in.close();
  }

  public void mark(int readlimit)
  {
    in.mark(readlimit);
  }

  public void reset() throws IOException
  {
    in.reset();
  }

  public boolean markSupported()
  {
    return in.markSupported();
  }

  public String getContentType()
  {
    return contentType;
  }
}
