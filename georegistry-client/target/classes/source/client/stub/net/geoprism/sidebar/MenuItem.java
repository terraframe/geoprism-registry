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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.geoprism.GeoprismUserDTO;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.URLConfigurationManager;
import com.runwaysdk.controller.URLConfigurationManager.UriForwardMapping;
import com.runwaysdk.controller.URLConfigurationManager.UriMapping;

public class MenuItem
{
  private String                         name;

  private String                         url;

  private String                         mappedUrl;

  private String                         roles;

  private Boolean                        synch;

  private String                         classes;

  private ArrayList<MenuItem>            children = new ArrayList<MenuItem>();

  private static URLConfigurationManager mapper   = new URLConfigurationManager();

  public MenuItem(String name, String URL, String roles)
  {
    this.name = name;
    this.url = URL;
    this.roles = roles;
    this.synch = false;
    this.classes = "";

    if (URL != null)
    {
      UriMapping mapping = mapper.getMapping(URL);
      if (mapping != null && mapping instanceof UriForwardMapping)
      {
        this.mappedUrl = ( (UriForwardMapping) mapping ).getUriEnd();
      }
    }
  }

  public void addChild(MenuItem child)
  {
    this.children.add(child);
  }

  public void addChildren(ArrayList<MenuItem> children)
  {
    this.children.addAll(children);
  }

  public boolean hasAccess(ClientRequestIF request)
  {
    if (this.getRoles() != null && this.getRoles().length() > 0)
    {
      return GeoprismUserDTO.isRoleMemeber(request, this.getRoles());
    }

    return true;
  }

  public boolean handlesUri(String uri, String context)
  {
    // If we have children loop over them.
    if (this.getURL() == null)
    {
      List<MenuItem> children = this.getChildren();
      for (MenuItem child : children)
      {
        if (child.handlesUri(uri, context))
        {
          return true;
        }
      }
      return false;
    }

    if (this.mappedUrl != null)
    {
      return uri.equals(context + this.mappedUrl);
    }
    else
    {
      return uri.equals(context + this.getURL());
    }
  }

  public String getName()
  {
    return this.name;
  }

  public boolean isSynch()
  {
    return this.synch;
  }

  public String getClasses()
  {
    return this.classes;
  }

  public void setSynch(String synch)
  {
    try
    {
      this.synch = new Boolean(synch);
    }
    catch (Throwable t)
    {
    }
  }

  public void setClasses(String classes)
  {
    this.classes = classes;
  }

  public String getURL()
  {
    return this.url;
  }

  public String getRoles()
  {
    return roles;
  }

  public boolean hasChildren()
  {
    return !this.children.isEmpty();
  }

  public List<MenuItem> getChildren()
  {
    return this.children;
  }

  @Override
  public String toString()
  {
    String out = "MenuItem: " + this.getName();
    if (this.getURL() != null)
    {
      out = out + this.getURL();
    }

    Iterator<MenuItem> it = this.getChildren().iterator();
    while (it.hasNext())
    {
      out = out + it.next().toString() + "\n";
    }

    return out;
  }
}
