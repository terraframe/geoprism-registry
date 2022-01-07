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
package net.geoprism.registry.curation;

import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.ListTypeVersion;

public class ListCurationHistory extends ListCurationHistoryBase
{
  private static final long serialVersionUID = -1090701307;

  public ListCurationHistory()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    deleteAllCurationProblems();

    super.delete();
  }

  public boolean hasCurationProblems()
  {
    CurationProblemQuery query = new CurationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));
    return query.getCount() > 0;
  }

  public void deleteAllCurationProblems()
  {
    CurationProblemQuery query = new CurationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    try (OIterator<? extends CurationProblem> it = query.getIterator();)
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
  }

  public List<? extends CurationProblem> getAllCurationProblems()
  {
    CurationProblemQuery query = new CurationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    try (OIterator<? extends CurationProblem> it = query.getIterator())
    {
      return it.getAll();
    }
  }

  public static void deleteAll(ListTypeVersion version)
  {
    ListCurationHistoryQuery query = new ListCurationHistoryQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    try (OIterator<? extends ListCurationHistory> it = query.getIterator();)
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
  }

}
