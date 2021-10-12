/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.etl.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import net.geoprism.registry.graph.FhirExternalSystem;

public class BasicFhirConnection implements FhirConnection
{
  private FhirExternalSystem externalSystem;

  private FhirContext        ctx;

  private IGenericClient     client;

  public BasicFhirConnection(FhirExternalSystem externalSystem)
  {
    super();
    this.externalSystem = externalSystem;
    this.ctx = FhirContext.forR4();

    this.open();
  }

  @Override
  public IGenericClient getClient()
  {
    return this.client;
  }

  @Override
  public FhirContext getFhirContext()
  {
    return this.ctx;
  }

  @Override
  public FhirExternalSystem getExternalSystem()
  {
    return externalSystem;
  }

  @Override
  public String getSystem()
  {
    return this.externalSystem.getSystem();
  }

  @Override
  public void open()
  {
    IRestfulClientFactory factory = ctx.getRestfulClientFactory();
    factory.setSocketTimeout(-1);

    this.client = factory.newGenericClient(this.externalSystem.getUrl());
  }

  @Override
  public void close() throws Exception
  {
    // this.connection.close();
  }
}
