/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either commit 3 of the License, or (at your option) any
 * later commit.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;

import net.geoprism.registry.Publish;
import net.geoprism.registry.PublishQuery;
import net.geoprism.registry.view.PublishDTO;

@Service
public class PublishBusinessService implements PublishBusinessServiceIF
{
  @Autowired
  private CommitBusinessServiceIF commitService;

  @Override
  @Transaction
  public void delete(Publish publish)
  {
    this.commitService.getCommits(publish).forEach(commit -> {
      if (Session.getCurrentSession() != null)
      {
        this.commitService.remove(commit);
      }
      else
      {
        this.commitService.delete(commit);
      }

    });

    publish.delete();
  }

  @Override
  @Transaction
  public Publish create(PublishDTO configuration)
  {
    JsonArray json = configuration.toJson();

    Publish publish = new Publish();
    publish.setUid(configuration.getUid());
    publish.setTypeCodes(json.toString());
    publish.setForDate(configuration.getDate());
    publish.setStartDate(configuration.getStartDate());
    publish.setEndDate(configuration.getEndDate());
    publish.apply();

    return publish;
  }

  @Override
  public List<Publish> getAll()
  {
    PublishQuery query = new PublishQuery(new QueryFactory());

    try (OIterator<? extends Publish> iterator = query.getIterator())
    {
      return new LinkedList<>(iterator.getAll());
    }
  }

  @Override
  public Publish get(String oid)
  {
    return Publish.get(oid);
  }

  @Override
  public Optional<Publish> getByUid(String uid)
  {
    PublishQuery query = new PublishQuery(new QueryFactory());
    query.WHERE(query.getUid().EQ(uid));

    try (OIterator<? extends Publish> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return Optional.of(iterator.next());
      }
    }

    return Optional.empty();
  }

  @Override
  public Publish getByUidOrThrow(String uid)
  {
    return this.getByUid(uid).orElseThrow(() -> {
      throw new ProgrammingErrorException("Unable to find a publish with the uid [" + uid + "]");
    });
  }

}
