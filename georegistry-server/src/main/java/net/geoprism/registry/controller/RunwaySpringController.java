package net.geoprism.registry.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.runwaysdk.ClientRequest;
import com.runwaysdk.constants.ClientConstants;

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
    return ( (ClientRequest) request.getAttribute(ClientConstants.CLIENTREQUEST) ).getSessionId();
  }
}
