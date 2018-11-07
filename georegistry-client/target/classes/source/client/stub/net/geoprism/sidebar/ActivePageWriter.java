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
package net.geoprism.sidebar;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import com.runwaysdk.controller.URLConfigurationManager;

public class ActivePageWriter
{
  String                  uri;

  JspWriter               out;

  String                  context;

  // If we've already redirected to a jsp, then we need to
  URLConfigurationManager mapper;

  public ActivePageWriter(HttpServletRequest request, JspWriter out)
  {
    this.uri = request.getRequestURI();
    this.out = out;
    this.context = request.getContextPath();
  }

  public String getActiveClass(MenuItem item)
  {
    if (item.handlesUri(this.uri, this.context))
    {
      return "class=\"blueactive\"";
    }
    else
    {
      return "";
    }
  }

  public void writeLiA(MenuItem item, String classes, boolean isSync) throws IOException
  {
    String href = "";
    String url = item.getURL();
    String title = item.getName();

    if (url.equals("#"))
    {
      href = "#";
    }
    else
    {
      if (isSync)
      {
        href = context + "/" + url;
      }
      else
      {
        href = context + "/app#" + url;
      }
    }

    String clazz = "";
    if (classes != null && classes.length() > 0)
    {
      clazz = "class=\"" + classes + "\"";
    }

    String html = "<li><a " + clazz + " " + this.getActiveClass(item) + " href=\"" + href + "\">";

    html = html + title;

    html = html + "</a></li>";

    out.print(html);
  }

  public void writeLiA(String title, String url, boolean isSync) throws IOException
  {
    this.writeLiA(new MenuItem(title, url, null), null, isSync);
  }

  public void writeLiA(String title, String url, String classes, boolean isSync) throws IOException
  {
    this.writeLiA(new MenuItem(title, url, null), classes, isSync);
  }
}
