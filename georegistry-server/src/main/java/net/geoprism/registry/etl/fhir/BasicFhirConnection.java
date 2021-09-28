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
