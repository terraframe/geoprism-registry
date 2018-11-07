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
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class EncodingFilter implements Filter
{

  @Override
  public void init(FilterConfig config) throws ServletException
  {
    // Do nothing
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException
  {
    try
    {
      request.setCharacterEncoding(EncodingConstants.ENCODING);
      response.setCharacterEncoding(EncodingConstants.ENCODING);

      chain.doFilter(request, response);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new ServletException(e);
    }
    catch (IOException e)
    {
      throw new ServletException(e);
    }
  }

  @Override
  public void destroy()
  {
    // Do nothing
  }
}
