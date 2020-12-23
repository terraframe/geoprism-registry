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
package net.geoprism.registry.conversion;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;

public class SupportedLocaleCache
{
  private static List<Locale> locales = null;

  public static synchronized List<Locale> getLocales()
  {
    if (locales == null)
    {
      locales = SupportedLocaleDAO.getSupportedLocales();
    }

    return locales;
  }

  public static synchronized List<String> getLocaleNames()
  {
    if (locales == null)
    {
      locales = SupportedLocaleDAO.getSupportedLocales();
    }

    List<String> list = new LinkedList<String>();
    list.add(MdAttributeLocalInfo.DEFAULT_LOCALE);

    for (Locale locale : locales)
    {
      list.add(locale.toString());
    }

    return list;
  }

  public static synchronized void clear()
  {
    locales = null;
  }

}
