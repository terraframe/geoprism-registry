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
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;

import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitQuery;
import net.geoprism.registry.Publish;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.EventPublishingConfiguration;

@Service
public class PublishBusinessService implements PublishBusinessServiceIF
{
  @Autowired
  private CommitBusinessServiceIF        commitService;

  @Autowired
  private HierarchyTypeBusinessServiceIF hTypeService;

  @Override
  @Transaction
  public void delete(Publish publish)
  {
    this.getCommits(publish).forEach(commit -> {
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
  public List<Commit> getCommits(Publish publish)
  {
    CommitQuery query = new CommitQuery(new QueryFactory());
    query.WHERE(query.getPublish().EQ(publish));
    query.ORDER_BY_DESC(query.getVersionNumber());

    try (OIterator<? extends Commit> it = query.getIterator())
    {
      return new LinkedList<Commit>(it.getAll());
    }
  }

  @Override
  public Commit getMostRecentCommit(Publish publish)
  {
    CommitQuery query = new CommitQuery(new QueryFactory());
    query.WHERE(query.getPublish().EQ(publish));
    query.ORDER_BY_DESC(query.getVersionNumber());

    try (OIterator<? extends Commit> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        return iterator.next();
      }
    }

    return null;
  }

  @Override
  @Transaction
  public Publish create(EventPublishingConfiguration configuration)
  {
    Publish publish = new Publish();
    publish.setUid(UUID.randomUUID().toString());
    publish.setTypeCodes(configuration.toJson().toString());
    publish.setForDate(configuration.getDate());
    publish.setStartDate(configuration.getStartDate());
    publish.setEndDate(configuration.getEndDate());
    publish.apply();

    return publish;
  }

  @Override
  public Publish get(String oid)
  {
    return Publish.get(oid);
  }

  public EventPublishingConfiguration toConfiguration(Publish publish)
  {
    EventPublishingConfiguration configuration = new EventPublishingConfiguration(publish.getForDate(), publish.getStartDate(), publish.getEndDate());

    JsonArray array = JsonParser.parseString(publish.getTypeCodes()).getAsJsonArray();

    array.forEach(element -> {
      JsonObject object = element.getAsJsonObject();

      String type = object.get("type").getAsString();
      String code = object.get("code").getAsString();

      if (type.equals("GeoObjectType"))
      {
        configuration.addGeoObjectType(ServerGeoObjectType.get(code));
      }
      else if (type.equals("HierarchyType"))
      {
        configuration.addHierarchyType(this.hTypeService.get(code));
      }

    });

    return configuration;
  }

}
