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

import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

public class GeoprismApplication
{
  private String      oid;

  private String      src;

  private String      label;

  private String      url;

  private Set<String> roleNames;

  public GeoprismApplication()
  {
    this.roleNames = new TreeSet<String>();
  }

  public String getOid()
  {
    return oid;
  }

  public void setId(String oid)
  {
    this.oid = oid;
  }

  public String getSrc()
  {
    return src;
  }

  public void setSrc(String src)
  {
    this.src = src;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public void addRole(String roleName)
  {
    this.roleNames.add(roleName);
  }

  public Set<String> getRoleNames()
  {
    return roleNames;
  }

  public void setRoleNames(Set<String> roleNames)
  {
    this.roleNames = roleNames;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public boolean isValid(Set<String> roleNames)
  {
    TreeSet<String> contains = new TreeSet<String>(this.roleNames);
    contains.retainAll(roleNames);

    return ( this.roleNames.size() == 0 || !contains.isEmpty() );
  }

  public JSONObject toJSON() throws JSONException
  {
    JSONObject object = new JSONObject();
    object.put("oid", this.oid);
    object.put("src", this.src);
    object.put("label", this.label);
    object.put("url", this.url);

    return object;
  }
}
