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
import java.util.List;

public class JavaMenuProvider
{

  public List<MenuItem> getMenu()
  {
    ArrayList<MenuItem> items = new ArrayList<MenuItem>();

    String jspDir = "net/geoprism/jsp/";

    MenuItem accManage = new MenuItem("Account Management", null, null);
    accManage.addChild(new MenuItem("User Accounts", jspDir + "useraccounts.jsp", null));
    accManage.addChild(new MenuItem("Roles", jspDir + "roles.jsp", null));
    items.add(accManage);

    MenuItem dataType = new MenuItem("DataType Management", null, null);
    dataType.addChild(new MenuItem("Term Ontology Administration", jspDir + "termAdmin.jsp", null));
    dataType.addChild(new MenuItem("Data Browser", jspDir + "dataBrowser.jsp", null));
    items.add(dataType);

    MenuItem importer = new MenuItem("Sales Force Import Manager", jspDir + "importer.jsp", null);
    items.add(importer);

    return items;
  }

}
