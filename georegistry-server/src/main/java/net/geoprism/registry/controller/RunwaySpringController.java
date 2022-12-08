package net.geoprism.registry.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.constants.ClientRequestIF;

public abstract class RunwaySpringController
{
  @Autowired
  private HttpServletRequest request;

  protected HttpServletRequest getRequest()
  {
    return request;
  }

  protected String getSessionId()
  {
    return getClientRequest().getSessionId();
  }

  public ClientRequestIF getClientRequest()
  {
    return (ClientRequestIF) request.getAttribute(ClientConstants.CLIENTREQUEST);
  }
}
