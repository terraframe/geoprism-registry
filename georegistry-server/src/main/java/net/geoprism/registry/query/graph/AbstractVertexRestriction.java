/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.query.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.runwaysdk.constants.MdAttributeLocalEmbeddedInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdGraphClassDAO;
import com.runwaysdk.session.Session;

public class AbstractVertexRestriction
{
  public String localize(String prefix)
  {
    final MdGraphClassDAOIF mdLocalStruct = MdGraphClassDAO.getMdGraphClassDAO(MdAttributeLocalEmbeddedInfo.EMBEDDED_LOCAL_VALUE);
    Locale locale = Session.getCurrentLocale();

    List<String> list = new ArrayList<String>();

    String localeString = locale.toString();

    for (int i = localeString.length(); i > 0; i = localeString.lastIndexOf('_', i - 1))
    {
      String subLocale = localeString.substring(0, i);

      for (MdAttributeConcreteDAOIF a : mdLocalStruct.definesAttributes())
      {
        if (a.definesAttribute().equalsIgnoreCase(subLocale))
        {
          list.add(subLocale);
        }
      }
    }

    list.add(MdAttributeLocalInfo.DEFAULT_LOCALE);

    StringBuilder builder = new StringBuilder();
    builder.append("COALESCE(");

    for (int i = 0; i < list.size(); i++)
    {
      if (i != 0)
      {
        builder.append(", ");
      }

      builder.append(prefix + "." + list.get(i));
    }

    builder.append(")");

    return builder.toString();
  }

}
