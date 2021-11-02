package com.runwaysdk.build.domain;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.model.ServerGeoObjectType;

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
      LocalizedValue goLabel = cr.getGeoObjectDisplayLabel();
      ServerGeoObjectType type = cr.getGeoObjectType();
      
      cr.appLock();
      cr.getGeoObjectLabel().setLocaleMap(goLabel.getLocaleMap());
      cr.getGeoObjectTypeLabel().setLocaleMap(type.getLabel().getLocaleMap());
      cr.apply();
    }
  }
}
