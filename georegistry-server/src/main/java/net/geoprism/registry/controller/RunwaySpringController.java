package net.geoprism.registry.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;

public abstract class RunwaySpringController
{
  @Autowired
  private HttpServletRequest request;

  private ClientRequestIF    clientRequest;

  protected HttpServletRequest getRequest()
  {
    return request;
  }

  protected String getSessionId()
  {
    return getClientRequest().getSessionId();
  }
  
  public void setClientRequest(ClientRequestIF clientRequest)
  {
    this.clientRequest = clientRequest;
  }

  public ClientRequestIF getClientRequest()
  {
    if (clientRequest == null)
    {
      this.clientRequest = (ClientRequestIF) request.getAttribute(ClientConstants.CLIENTREQUEST);
    }

    return this.clientRequest;
  }
}
