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
package com.runwaysdk.build.domain;

import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.GraphOrganization;

public class PatchOrganization
{
  public static void main(String[] args)
  {
    new PatchOrganization().doIt();
  }

  @Request
  private void doIt()
  {
    this.execute();
  }

  @Transaction
  private void execute()
  {
    List<? extends Organization> organizations = Organization.getOrganizations();

    for (Organization organization : organizations)
    {
      GraphOrganization graphOrganization = GraphOrganization.get(organization);

      if (graphOrganization == null)
      {
        graphOrganization = new GraphOrganization();
        graphOrganization.setCode(organization.getCode());
        graphOrganization.setOrganization(organization);
        LocalizedValueConverter.populate(graphOrganization, GraphOrganization.DISPLAYLABEL, LocalizedValueConverter.convertNoAutoCoalesce(organization.getDisplayLabel()));
        LocalizedValueConverter.populate(graphOrganization, GraphOrganization.CONTACTINFO, LocalizedValueConverter.convertNoAutoCoalesce(organization.getContactInfo()));
        graphOrganization.apply();
      }
    }
  }

}
