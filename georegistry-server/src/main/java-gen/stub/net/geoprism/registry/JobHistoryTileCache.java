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
package net.geoprism.registry;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdAttributeBlobDAO;
import com.runwaysdk.dataaccess.transaction.ThreadTransactionState;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.dataaccess.transaction.TransactionType;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.jobs.GPRJobHistory;
import net.geoprism.registry.tile.GeometryTableVectorTileBuilder;

public class JobHistoryTileCache extends JobHistoryTileCacheBase
{
  private static final long           serialVersionUID = 1L;

  public static final ExecutorService executor         = Executors.newFixedThreadPool(5);

  private static class CacheCallable implements Callable<byte[]>
  {
    private ThreadTransactionState state;

    private String                 historyId;

    private int                    x;

    private int                    y;

    private int                    zoom;

    public CacheCallable(ThreadTransactionState state, String historyId, int x, int y, int zoom)
    {
      super();
      this.state = state;
      this.historyId = historyId;
      this.x = x;
      this.y = y;
      this.zoom = zoom;
    }

    @Override
    public byte[] call() throws Exception
    {
      return this.call(this.state);
    }

    @Request(RequestType.THREAD)
    public byte[] call(ThreadTransactionState state)
    {
      try
      {
        /*
         * Re-check
         */
        byte[] cached = getCachedTile(historyId, x, y, zoom);

        if (cached != null)
        {
          return cached;
        }

        GPRJobHistory history = GPRJobHistory.get(this.historyId);

        GeometryTableVectorTileBuilder builder = new GeometryTableVectorTileBuilder(history);
        byte[] tile = builder.write(zoom, x, y);

        if (tile.length < MdAttributeBlobDAO.getMaxLength())
        {
          this.populateTile(state, tile);
        }

        return tile;
      }
      catch (Exception e)
      {
        e.printStackTrace();

        return null;
      }
    }

    @Transaction(TransactionType.THREAD)
    private void populateTile(ThreadTransactionState state, byte[] tile)
    {
      JobHistoryTileCache cache = new JobHistoryTileCache();
      cache.setHistoryId(this.historyId);
      cache.setX(this.x);
      cache.setY(this.y);
      cache.setZ(this.zoom);
      cache.setTile(tile);
      cache.apply();
    }
  }

  private static final long serialhistoryUID = -1074424078;

  public JobHistoryTileCache()
  {
    super();
  }

  @Transaction
  public static byte[] getTile(String historyId, int x, int y, int zoom)
  {
    byte[] cached = getCachedTile(historyId, x, y, zoom);

    if (cached != null)
    {
      return cached;
    }
    else
    {
      /*
       * Store the tile into the cache for future reads
       */
      SessionIF session = Session.getCurrentSession();

      if (session != null)
      {
        ThreadTransactionState state = ThreadTransactionState.getCurrentThreadTransactionState();

        try
        {
          byte[] result = executor.submit(new CacheCallable(state, historyId, x, y, zoom)).get();

          return result;
        }
        catch (InterruptedException | ExecutionException e)
        {
          throw new ProgrammingErrorException(e);
        }
      }

      return null;
    }
  }

  public static byte[] getCachedTile(String historyId, int x, int y, int zoom)
  {
    JobHistoryTileCacheQuery query = new JobHistoryTileCacheQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(historyId));
    query.AND(query.getX().EQ(x));
    query.AND(query.getY().EQ(y));
    query.AND(query.getZ().EQ(zoom));

    OIterator<? extends JobHistoryTileCache> it = query.getIterator();

    try
    {
      if (it.hasNext())
      {
        JobHistoryTileCache tile = it.next();
        return tile.getTile();
      }

      return null;
    }
    finally
    {
      it.close();
    }
  }

  public static void deleteTiles(GPRJobHistory history)
  {
    JobHistoryTileCacheQuery query = new JobHistoryTileCacheQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(history));

    try (OIterator<? extends JobHistoryTileCache> it = query.getIterator())
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
  }

  public static void deleteTilesBefore(Date date)
  {
    JobHistoryTileCacheQuery query = new JobHistoryTileCacheQuery(new QueryFactory());
    query.WHERE(query.getCreateDate().LE(date));

    try (OIterator<? extends JobHistoryTileCache> it = query.getIterator())
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
  }
}
