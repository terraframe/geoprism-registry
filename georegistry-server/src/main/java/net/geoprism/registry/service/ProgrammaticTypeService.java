/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.ProgrammaticType;

public class ProgrammaticTypeService
{

  /**
   * Creates a {@link ProgrammaticType} from the given JSON.
   * 
   * @param sessionId
   * @param ptJSON
   *          JSON of the {@link ProgrammaticType} to be created.
   * @return newly created {@link ProgrammaticType}
   */
  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String ptJSON)
  {
    ProgrammaticType type = ProgrammaticType.apply(JsonParser.parseString(ptJSON).getAsJsonObject());

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return type.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonArray listByOrg(String sessionId)
  {
    return ProgrammaticType.listByOrg();
  }

  @Request(RequestType.SESSION)
  public JsonArray getAll(String sessionId)
  {
    return ProgrammaticType.getAll();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    ProgrammaticType type = ProgrammaticType.get(oid);
    type.delete();

    ( (Session) Session.getCurrentSession() ).reloadPermissions();
  }

  @Request(RequestType.SESSION)
  public JsonObject edit(String sessionId, String oid)
  {
    ProgrammaticType type = ProgrammaticType.get(oid);
    type.lock();

    return type.toJSON(true);
  }

  @Request(RequestType.SESSION)
  public void unlock(String sessionId, String oid)
  {
    ProgrammaticType type = ProgrammaticType.get(oid);
    type.unlock();
  }

  /**
   * Adds an attribute to the given {@link ProgrammaticType}.
   * 
   * @pre given {@link ProgrammaticType} must already exist.
   * 
   * @param sessionId
   *
   * @param programmaticTypeCode
   *          string of the {@link ProgrammaticType} to be updated.
   * @param attributeTypeJSON
   *          AttributeType to be added to the ProgrammaticType
   * @return updated {@link ProgrammaticType}
   */
  @Request(RequestType.SESSION)
  public AttributeType createAttributeType(String sessionId, String programmaticTypeCode, String attributeTypeJSON)
  {
    ProgrammaticType got = ProgrammaticType.getByCode(programmaticTypeCode);

    // ServiceFactory.getProgrammaticTypePermissionService().enforceCanWrite(got.getOrganization().getCode(),
    // got, got.getIsPrivate());

    AttributeType attrType = got.createAttributeType(attributeTypeJSON);

    return attrType;
  }

  /**
   * Updates an attribute in the given {@link ProgrammaticType}.
   * 
   * @pre given {@link ProgrammaticType} must already exist.
   * 
   * @param sessionId
   * @param programmaticTypeCode
   *          string of the {@link ProgrammaticType} to be updated.
   * @param attributeTypeJSON
   *          AttributeType to be added to the ProgrammaticType
   * @return updated {@link AttributeType}
   */
  @Request(RequestType.SESSION)
  public AttributeType updateAttributeType(String sessionId, String programmaticTypeCode, String attributeTypeJSON)
  {
    ProgrammaticType got = ProgrammaticType.getByCode(programmaticTypeCode);

    // ServiceFactory.getProgrammaticTypePermissionService().enforceCanWrite(got.getOrganization().getCode(),
    // got, got.getIsPrivate());

    AttributeType attrType = got.updateAttributeType(attributeTypeJSON);

    return attrType;
  }

  /**
   * Deletes an attribute from the given {@link ProgrammaticType}.
   * 
   * @pre given {@link ProgrammaticType} must already exist.
   * @pre given {@link ProgrammaticType} must already exist.
   * 
   * @param sessionId
   * @param code
   *          string of the {@link ProgrammaticType} to be updated.
   * @param attributeName
   *          Name of the attribute to be removed from the ProgrammaticType
   * @return updated {@link ProgrammaticType}
   */
  @Request(RequestType.SESSION)
  public void removeAttributeType(String sessionId, String programmaticTypeCode, String attributeName)
  {
    ProgrammaticType got = ProgrammaticType.getByCode(programmaticTypeCode);

    // ServiceFactory.getProgrammaticTypePermissionService().enforceCanWrite(got.getOrganization().getCode(),
    // got, got.getIsPrivate());

    got.removeAttribute(attributeName);
  }

}
