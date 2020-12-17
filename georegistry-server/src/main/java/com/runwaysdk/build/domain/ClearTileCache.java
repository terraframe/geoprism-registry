package com.runwaysdk.build.domain;

import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.TileCache;

public class ClearTileCache
{
  public static void main(String[] args)
  {
    new ClearTileCache().doIt();
  }

  @Transaction
  private void doIt()
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(TileCache.CLASS);
    mdBusiness.getBusinessDAO().deleteAllRecords();
  }

}
