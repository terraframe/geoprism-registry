package net.geoprism.registry.progress;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProgressService
{
  private static Map<String, Progress> PROGRESS_MAP = Collections.synchronizedMap(new HashMap<String, Progress>());

  public static void put(String key, Progress progress)
  {
    PROGRESS_MAP.put(key, progress);
  }

  public static void remove(String key)
  {
    PROGRESS_MAP.remove(key);
  }

  public static Progress progress(String key)
  {
    Progress progress = PROGRESS_MAP.get(key);

    if (progress != null)
    {
      return progress;
    }

    return new Progress(1l, 1l, "");
  }

}
