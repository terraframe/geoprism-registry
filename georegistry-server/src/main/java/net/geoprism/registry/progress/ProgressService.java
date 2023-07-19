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
package net.geoprism.registry.progress;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.geoprism.registry.ws.NotificationFacade;
import net.geoprism.registry.ws.ProgressMessage;

public class ProgressService
{
  private static Map<String, Progress> PROGRESS_MAP = Collections.synchronizedMap(new HashMap<String, Progress>());

  public static void put(String key, Progress progress)
  {
    PROGRESS_MAP.put(key, progress);

    if (progress.getCurrent() % 50 == 0 || progress.getCurrent().equals(progress.getTotal()))
    {
      NotificationFacade.queue(new ProgressMessage(key, progress));
    }
  }

  public static void remove(String key)
  {
    NotificationFacade.queue(new ProgressMessage(key, new Progress()));

    PROGRESS_MAP.remove(key);
  }

  public static Progress get(String key)
  {
    return PROGRESS_MAP.get(key);
  }

  public static Progress progress(String key)
  {
    Progress progress = PROGRESS_MAP.get(key);

    if (progress != null)
    {
      return progress;
    }

    return new Progress();
  }

}
