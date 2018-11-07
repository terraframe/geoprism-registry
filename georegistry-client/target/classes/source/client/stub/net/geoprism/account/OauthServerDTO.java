/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.account;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.business.BusinessDTO;
import com.runwaysdk.constants.ClientRequestIF;


public class OauthServerDTO extends OauthServerDTOBase 
{
  private static final long serialVersionUID = -431820160;

  public OauthServerDTO(ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }

  /**
   * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
   * 
   * @param businessDTO
   *          The BusinessDTO to duplicate
   * @param clientRequest
   *          The clientRequest this DTO should use to communicate with the server.
   */
  protected OauthServerDTO(BusinessDTO businessDTO, ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }

  public String getUrl(HttpServletRequest req)
  {
    try
    {
      URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath());

      String redirect = url.toString() + "/session/ologin";

      JSONObject state = new JSONObject();
      state.put(OauthServerIF.SERVER_ID, this.getOid());

      AuthenticationRequestBuilder builder = OAuthClientRequest.authorizationLocation(this.getAuthorizationLocation());
      builder.setClientId(this.getClientId());
      builder.setRedirectURI(redirect);
      builder.setResponseType("code");
      builder.setState(state.toString());

      OAuthClientRequest request = builder.buildQueryMessage();

      return request.getLocationUri();
    }
    catch (OAuthSystemException | JSONException | MalformedURLException e )
    {
      e.printStackTrace();
    }

    return null;
  }
}
