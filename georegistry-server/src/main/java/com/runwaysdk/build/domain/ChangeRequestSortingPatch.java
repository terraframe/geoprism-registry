package com.runwaysdk.build.domain;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class ChangeRequestSortingPatch
{
  public static void main(String[] args)
  {
    new ChangeRequestSortingPatch().doIt();
  }
  
  @Transaction
  private void doIt()
  {
    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());
    
    OIterator<? extends ChangeRequest> it = crq.getIterator();
    
    for (ChangeRequest cr : it)
    {
      VertexServerGeoObject vsGo = cr.getGeoObject();
      ServerGeoObjectType type = vsGo.getType();
      
      cr.appLock();
      cr.getGeoObjectLabel().setLocaleMap(vsGo.getDisplayLabel().getLocaleMap());
      cr.getGeoObjectTypeLabel().setLocaleMap(type.getLabel().getLocaleMap());
      cr.apply();
    }
  }
}
