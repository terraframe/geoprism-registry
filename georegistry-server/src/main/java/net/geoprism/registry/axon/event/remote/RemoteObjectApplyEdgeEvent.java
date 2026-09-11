package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import net.geoprism.registry.view.TypeInfo;

public class RemoteObjectApplyEdgeEvent extends RemoteObjectEdgeEvent implements RemoteEvent
{

  public RemoteObjectApplyEdgeEvent()
  {
    super();
  }

  public RemoteObjectApplyEdgeEvent(String commitId, String key, String sourceCode, TypeInfo sourceType, String edgeUid, TypeInfo edgeType, String targetCode, TypeInfo targetType, Date startDate, Date endDate, String dataSource)
  {
    super(commitId, key, sourceCode, sourceType, edgeUid, edgeType, targetCode, targetType, startDate, endDate, dataSource);
  }

  public RemoteObjectApplyEdgeEvent(String commitId, String sourceCode, TypeInfo sourceType, String edgeUid, TypeInfo edgeType, String targetCode, TypeInfo targetType, Date startDate, Date endDate, String dataSource)
  {
    super(commitId, sourceCode, sourceType, edgeUid, edgeType, targetCode, targetType, startDate, endDate, dataSource);
  }

}
