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
package net.geoprism.registry.dhis2;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.RunwayException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.registry.etl.export.ExportRemoteException;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.etl.export.LoginException;
import net.geoprism.registry.etl.export.UnexpectedRemoteResponse;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;

public class DHIS2FeatureService
{
  public static final String[] OAUTH_INCOMPATIBLE_VERSIONS = new String[] {"2.35.0", "2.35.1"};
  
  public static final int LAST_TESTED_DHIS2_API_VERSION = 35;
  
  private static final Logger logger = LoggerFactory.getLogger(DHIS2FeatureService.class);
  
  public DHIS2FeatureService()
  {
    
  }
  
  public static class DHIS2SyncError extends RunwayException
  {
    private static final long serialVersionUID = 8463740942015611693L;

    protected DHIS2ImportResponse    response;
    
    protected String          submittedJson;

    protected Throwable       error;
    
    protected String          geoObjectCode;
    
    protected Long            rowIndex;

    public DHIS2SyncError(Long rowIndex, DHIS2ImportResponse response, String submittedJson, Throwable t, String geoObjectCode)
    {
      super("");
      this.response = response;
      this.submittedJson = submittedJson;
      this.error = t;
      this.geoObjectCode = geoObjectCode;
      this.rowIndex = rowIndex;
    }
  }
  
  public DHIS2TransportServiceIF getTransportService(DHIS2ExternalSystem es)
  {
    DHIS2TransportServiceIF dhis2;
    
    try
    {
      dhis2 = DHIS2ServiceFactory.buildDhis2TransportService(es);
    }
    catch (InvalidLoginException e)
    {
      LoginException cgrlogin = new LoginException(e);
      throw cgrlogin;
    }
    catch (HTTPException | UnexpectedResponseException e)
    {
      HttpError cgrhttp = new HttpError(e);
      throw cgrhttp;
    }
    
    return dhis2;
  }
  
  public void setExternalSystemDhis2Version(DHIS2ExternalSystem es)
  {
    this.setExternalSystemDhis2Version(getTransportService(es), es);
  }
  
  public void setExternalSystemDhis2Version(DHIS2TransportServiceIF dhis2, DHIS2ExternalSystem es)
  {
    es.setVersion(dhis2.getVersionRemoteServer());
    es.apply();
  }
  
  public void validateDhis2Response(DHIS2Response resp)
  {
    if (!resp.isSuccess())
    {
      if (resp.hasMessage())
      {
        ExportRemoteException ere = new ExportRemoteException();
        ere.setRemoteError(resp.getMessage());
        throw ere;
      }
      else
      {
        UnexpectedRemoteResponse re = new UnexpectedRemoteResponse();
        throw re;
      }
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject getSystemCapabilities(String sessionId, String systemJSON)
  {
    JsonObject capabilities = new JsonObject();
    
    JsonObject jo = JsonParser.parseString(systemJSON).getAsJsonObject();

    ExternalSystem system = ExternalSystem.desieralize(jo);
    
    if (system instanceof DHIS2ExternalSystem)
    {
      DHIS2ExternalSystem dhis2System = (DHIS2ExternalSystem) system;
      
      DHIS2TransportServiceIF dhis2 = getTransportService(dhis2System);
      
      String version = dhis2.getVersionRemoteServer();
      
      if (ArrayUtils.contains(DHIS2FeatureService.OAUTH_INCOMPATIBLE_VERSIONS, version))
      {
        capabilities.addProperty("oauth", false);
      }
      else
      {
        capabilities.addProperty("oauth", true);
      }
    }
    else
    {
      capabilities.addProperty("oauth", false);
    }
    
    return capabilities;
  }
}
