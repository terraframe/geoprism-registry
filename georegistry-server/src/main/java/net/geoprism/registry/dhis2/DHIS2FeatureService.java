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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.lang3.ArrayUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.RunwayException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.Option;
import net.geoprism.dhis2.dhis2adapter.response.model.ValueType;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.DHIS2TermAttributeMapping;
import net.geoprism.registry.etl.export.ExportRemoteException;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.etl.export.LoginException;
import net.geoprism.registry.etl.export.UnexpectedRemoteResponse;
import net.geoprism.registry.etl.export.dhis2.DHIS2OptionCache;
import net.geoprism.registry.etl.export.dhis2.DHIS2OptionCache.IntegratedOptionSet;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.AttributeTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectType;

public class DHIS2FeatureService
{
  public static final String[] OAUTH_INCOMPATIBLE_VERSIONS   = new String[] { "2.35.0", "2.35.1" };

  public static final int      LAST_TESTED_DHIS2_API_VERSION = 36;

  private static final Logger  logger                        = LoggerFactory.getLogger(DHIS2FeatureService.class);

  public DHIS2FeatureService()
  {

  }

  public static class DHIS2SyncError extends RunwayException
  {
    private static final long     serialVersionUID = 8463740942015611693L;

    protected DHIS2ImportResponse response;

    protected String              submittedJson;

    protected Throwable           error;

    protected String              geoObjectCode;

    protected Long                rowIndex;

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

  private List<DHIS2AttributeMapping> getMappingStrategies(AttributeType type)
  {
    List<DHIS2AttributeMapping> strategies = new ArrayList<DHIS2AttributeMapping>();

    if (type.getType().equals(AttributeTermType.TYPE))
    {
      strategies.add(new DHIS2TermAttributeMapping());
    }
    else
    {
      strategies.add(new DHIS2AttributeMapping());
    }

    return strategies;
  }

  @Request(RequestType.SESSION)
  public JsonArray getDHIS2AttributeConfiguration(String sessionId, String dhis2SystemOid, String geoObjectTypeCode)
  {
    DHIS2ExternalSystem system = DHIS2ExternalSystem.get(dhis2SystemOid);

    JsonArray response = new JsonArray();

    ServerGeoObjectType got = ServerGeoObjectType.get(geoObjectTypeCode);

    Map<String, AttributeType> cgrAttrs = got.getAttributeMap();

    DHIS2TransportServiceIF dhis2;
    try
    {
      dhis2 = DHIS2ServiceFactory.buildDhis2TransportService(system);
    }
    catch (InvalidLoginException e)
    {
      LoginException cgrlogin = new LoginException(e);
      throw cgrlogin;
    }
    catch (HTTPException | UnexpectedResponseException | BadServerUriException | IllegalArgumentException e)
    {
      HttpError cgrhttp = new HttpError(e);
      throw cgrhttp;
    }

    final DHIS2OptionCache optionCache = new DHIS2OptionCache(dhis2);

    List<Attribute> dhis2Attrs = getDHIS2Attributes(dhis2);

    final String[] skipAttrs = new String[] { DefaultAttribute.GEOMETRY.getName(), DefaultAttribute.SEQUENCE.getName(), DefaultAttribute.TYPE.getName() };

    for (AttributeType cgrAttr : cgrAttrs.values())
    {
      if (!ArrayUtils.contains(skipAttrs, cgrAttr.getName()))
      {
        JsonObject joAttr = new JsonObject();

        JsonObject joCgrAttr = new JsonObject();
        joCgrAttr.addProperty("name", cgrAttr.getName());
        joCgrAttr.addProperty("label", cgrAttr.getLabel().getValue());
        joCgrAttr.addProperty("type", cgrAttr.getType());
        joCgrAttr.addProperty("typeLabel", AttributeTypeMetadata.get().getTypeEnumDisplayLabel(cgrAttr.getType()));
        joAttr.add("cgrAttr", joCgrAttr);

        JsonArray jaStrategies = new JsonArray();
        List<DHIS2AttributeMapping> strategies = this.getMappingStrategies(cgrAttr);
        for (DHIS2AttributeMapping strategy : strategies)
        {
          jaStrategies.add(strategy.getClass().getName());
        }
        joAttr.add("attributeMappingStrategies", jaStrategies);

        JsonArray jaDhis2Attrs = new JsonArray();
        for (Attribute dhis2Attr : dhis2Attrs)
        {
          if (!dhis2Attr.getOrganisationUnitAttribute())
          {
            continue;
          }

          boolean valid = false;

          JsonObject jo = new JsonObject();

          if (cgrAttr instanceof AttributeBooleanType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.BOOLEAN) || dhis2Attr.getValueType().equals(ValueType.TRUE_ONLY) ))
          {
            valid = true;
          }
          else if (cgrAttr instanceof AttributeIntegerType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.INTEGER) || dhis2Attr.getValueType().equals(ValueType.INTEGER_POSITIVE) || dhis2Attr.getValueType().equals(ValueType.INTEGER_NEGATIVE) || dhis2Attr.getValueType().equals(ValueType.INTEGER_ZERO_OR_POSITIVE) ))
          {
            valid = true;
          }
          else if (cgrAttr instanceof AttributeFloatType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.NUMBER) || dhis2Attr.getValueType().equals(ValueType.UNIT_INTERVAL) || dhis2Attr.getValueType().equals(ValueType.PERCENTAGE) ))
          {
            valid = true;
          }
          else if (cgrAttr instanceof AttributeDateType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.DATE) || dhis2Attr.getValueType().equals(ValueType.DATETIME) || dhis2Attr.getValueType().equals(ValueType.TIME) || dhis2Attr.getValueType().equals(ValueType.AGE) ))
          {
            valid = true;
          }
          else if (cgrAttr instanceof AttributeTermType && dhis2Attr.getOptionSetId() != null)
          {
            valid = true;

            JsonArray jaDhis2Options = new JsonArray();

            IntegratedOptionSet set = optionCache.getOptionSet(dhis2Attr.getOptionSetId());

            SortedSet<Option> options = set.getOptions();

            for (Option option : options)
            {
              JsonObject joDhis2Option = new JsonObject();
              joDhis2Option.addProperty("code", option.getCode());
              joDhis2Option.addProperty("name", option.getName());
              joDhis2Option.addProperty("id", option.getName());
              jaDhis2Options.add(joDhis2Option);
            }

            jo.add("options", jaDhis2Options);
          }
          else if ( ( cgrAttr instanceof AttributeCharacterType || cgrAttr instanceof AttributeLocalType ) && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.TEXT) || dhis2Attr.getValueType().equals(ValueType.LONG_TEXT) || dhis2Attr.getValueType().equals(ValueType.LETTER) || dhis2Attr.getValueType().equals(ValueType.PHONE_NUMBER) || dhis2Attr.getValueType().equals(ValueType.EMAIL) || dhis2Attr.getValueType().equals(ValueType.USERNAME) || dhis2Attr.getValueType().equals(ValueType.URL) ))
          {
            valid = true;
          }

          if (valid)
          {
            jo.addProperty("dhis2Id", dhis2Attr.getId());
            jo.addProperty("code", dhis2Attr.getCode());
            jo.addProperty("name", dhis2Attr.getName());
            jaDhis2Attrs.add(jo);
          }
        }

        joAttr.add("dhis2Attrs", jaDhis2Attrs);

        if (cgrAttr instanceof AttributeTermType)
        {
          JsonArray terms = new JsonArray();

          List<Term> children = ( (AttributeTermType) cgrAttr ).getTerms();

          for (Term child : children)
          {
            JsonObject joTerm = new JsonObject();
            joTerm.addProperty("label", child.getLabel().getValue());
            joTerm.addProperty("code", child.getCode());
            terms.add(joTerm);
          }

          joAttr.add("terms", terms);
        }

        response.add(joAttr);
      }
    }

    return response;
  }

  private List<Attribute> getDHIS2Attributes(DHIS2TransportServiceIF dhis2)
  {
    try
    {
      MetadataGetResponse<Attribute> resp = dhis2.<Attribute> metadataGet(Attribute.class);

      if (!resp.isSuccess())
      {
        // if (resp.hasMessage())
        // {
        // ExportRemoteException ere = new ExportRemoteException();
        // ere.setRemoteError(resp.getMessage());
        // throw ere;
        // }
        // else
        // {
        UnexpectedRemoteResponse re = new UnexpectedRemoteResponse();
        throw re;
        // }
      }

      List<Attribute> attrs = resp.getObjects();

      attrs.addAll(buildDefaultDhis2OrgUnitAttributes());

      return attrs;
    }
    catch (InvalidLoginException e)
    {
      LoginException cgrlogin = new LoginException(e);
      throw cgrlogin;
    }
    catch (HTTPException | BadServerUriException e)
    {
      HttpError cgrhttp = new HttpError(e);
      throw cgrhttp;
    }
  }

  public static List<Attribute> buildDefaultDhis2OrgUnitAttributes()
  {
    List<Attribute> attrs = new ArrayList<Attribute>();

    final String[] names = new String[] { "name", "shortName", "code", "description", "openingDate", "closedDate", "comment", "url", "contactPerson", "address", "email", "phoneNumber" };

    for (String name : names)
    {
      Attribute attr = new Attribute();
      attr.setId(name);
      attr.setName(name);
      attr.setCode(name);
      attr.setOrganisationUnitAttribute(true);

      if (ArrayUtils.contains(new String[] { "openingDate", "closedDate" }, name))
      {
        attr.setValueType(ValueType.DATE);
      }
      else
      {
        attr.setValueType(ValueType.TEXT);
      }

      attrs.add(attr);
    }

    return attrs;
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
    catch (BadServerUriException e)
    {
      net.geoprism.registry.etl.export.BadServerUriException cgrhttp = new net.geoprism.registry.etl.export.BadServerUriException(e);
      throw cgrhttp;
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
    else if (system instanceof FhirExternalSystem)
    {
      capabilities.addProperty("oauth", true);
    }
    else
    {
      capabilities.addProperty("oauth", false);
    }

    return capabilities;
  }
}
