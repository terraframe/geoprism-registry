package net.geoprism.registry.service.business;

import java.util.List;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.axon.event.remote.RemoteEvent;

public class MockErrorRemoteClient extends MockRemoteClient
{
  @Override
  public List<RemoteEvent> getRemoteEvents(String uid, Integer chunk)
  {
    if (chunk != 0)
    {
      throw new ProgrammingErrorException("SOMETHING FAILED");
    }

    return super.getRemoteEvents(uid, chunk);
  }
}
