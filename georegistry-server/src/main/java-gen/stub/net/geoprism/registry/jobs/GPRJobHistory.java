/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.jobs;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.scheduler.AllJobStatus;

import net.geoprism.registry.JobHistoryTileCache;
import net.geoprism.registry.RollbackCheckpoint;
import net.geoprism.registry.view.TypeInfo;

public class GPRJobHistory extends GPRJobHistoryBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 2845054;

  public GPRJobHistory()
  {
    super();
  }

  public List<TypeInfo> getTypesAsList()
  {
    if (!StringUtils.isBlank(this.getTypes()))
    {
      ObjectMapper mapper = new ObjectMapper();

      try
      {
        return mapper.readerForListOf(TypeInfo.class).readValue(this.getTypes());
      }
      catch (JsonMappingException e)
      {
        e.printStackTrace();
      }
      catch (JsonProcessingException e)
      {
        e.printStackTrace();
      }

    }

    return new LinkedList<>();
  }

  public void setTypesFromList(Collection<TypeInfo> types)
  {

    try
    {
      this.setTypes(new ObjectMapper().writeValueAsString(types));
    }
    catch (JsonMappingException e)
    {
      e.printStackTrace();
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  @Transaction
  public void delete()
  {
    RollbackCheckpoint.getAll(this).forEach(RollbackCheckpoint::delete);

    JobHistoryTileCache.deleteTiles(this);

    super.delete();
  }

  public static long getPendingCount()
  {
    GPRJobHistoryQuery q = new GPRJobHistoryQuery(new QueryFactory());
    q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED, AllJobStatus.RUNNING));

    return q.getCount();
  }

}
