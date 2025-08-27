package net.geoprism.registry.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class MockRemoteClientBuilderService implements RemoteClientBuilderServiceIF
{
  public static String SOURCE       = "DEFAULT";

  public static String STALE_SOURCE = "STALE_SOURCE";

  public static String DEPENDENCY   = "DEPENDENCY";

  public static String ERROR        = "ERROR";

  @Override
  public RemoteClientIF open(final String source)
  {
    if (source.equals(STALE_SOURCE))
    {
      return new MockStaleRemoteClient();
    }
    else if (source.equals(DEPENDENCY))
    {
      return new MockDependentRemoteClient();
    }
    else if (source.equals(ERROR))
    {
      return new MockErrorRemoteClient();
    }

    return new MockRemoteClient();
  }

}
