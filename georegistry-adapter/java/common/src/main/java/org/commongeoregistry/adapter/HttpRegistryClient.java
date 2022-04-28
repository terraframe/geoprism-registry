/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.Connector;
import org.commongeoregistry.adapter.http.HttpResponse;
import org.commongeoregistry.adapter.http.ResponseProcessor;
import org.commongeoregistry.adapter.http.ServerResponseException;
import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.MemoryOnlyIdService;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * This class is used by remote systems wishing to interface with the Common
 * Geo-Registry. This will run on the remote system and will pull over the
 * metadata for each {@link GeoObjectType}.
 * 
 * @author nathan
 * @author rrowlands
 *
 */
public class HttpRegistryClient extends RegistryAdapter
{
  /**
   * 
   */
  private static final long serialVersionUID = -8311449977719450035L;

  private Connector         connector;

  /**
   * 
   * 
   * @param _cgrURL
   *          URL to the common geo-registry
   */
  public HttpRegistryClient(Connector connector)
  {
    super(new MemoryOnlyIdService());

    this.connector = connector;
    ( (MemoryOnlyIdService) this.getIdService() ).setClient(this);
  }

  public HttpRegistryClient(Connector connector, AdapterIdServiceIF idService)
  {
    super(idService);

    this.connector = connector;

    if (idService instanceof MemoryOnlyIdService)
    {
      ( (MemoryOnlyIdService) idService ).setClient(this);
    }
  }

  /**
   * Returns the HTTP connector used for making custom requests to the geo
   * registry server.
   * 
   */
  public Connector getConnector()
  {
    return this.connector;
  }

  /**
   * Clears the metadata cache and populates it with the metadata from the
   * common geo-registry.
   * 
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   * 
   */
  public void refreshMetadataCache() throws AuthenticationException, ServerResponseException, IOException
  {
    this.getMetadataCache().rebuild();

    GeoObjectType[] gots = this.getGeoObjectTypes(new String[] {}, new String[] {});

    for (GeoObjectType got : gots)
    {
      this.getMetadataCache().addGeoObjectType(got);
    }

    HierarchyType[] hts = this.getHierarchyTypes(new String[] {});

    for (HierarchyType ht : hts)
    {
      this.getMetadataCache().addHierarchyType(ht);
    }
  }

  /**
   * Returns the {@link GeoObject} with the given UID.
   * 
   * @param _uid
   *          UID of the {@link GeoObject}.
   * 
   * @return GeoObject with the given UID.
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public GeoObject getGeoObject(String id, String typeCode) throws AuthenticationException, ServerResponseException, IOException
  {
    if (id == null || id.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET, RegistryUrls.GEO_OBJECT_GET_PARAM_ID);
    }
    if (typeCode == null || typeCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET, RegistryUrls.GEO_OBJECT_GET_PARAM_TYPE_CODE);
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put(RegistryUrls.GEO_OBJECT_GET_PARAM_ID, id);
    params.put(RegistryUrls.GEO_OBJECT_GET_PARAM_TYPE_CODE, typeCode);

    HttpResponse resp = this.connector.httpGet(RegistryUrls.GEO_OBJECT_GET, params);
    ResponseProcessor.validateStatusCode(resp);

    GeoObject geoObject = GeoObject.fromJSON(this, resp.getAsString());

    return geoObject;
  }

  /**
   * Returns the {@link GeoObject} with the given code.
   * 
   * @param code
   *          code of the {@link GeoObject}.
   * 
   * @return GeoObject with the given code.
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public GeoObject getGeoObjectByCode(String code, String typeCode) throws AuthenticationException, ServerResponseException, IOException
  {
    if (code == null || code.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_CODE, RegistryUrls.GEO_OBJECT_GET_CODE_PARAM_CODE);
    }
    if (typeCode == null || typeCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_CODE, RegistryUrls.GEO_OBJECT_GET_CODE_PARAM_TYPE_CODE);
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put(RegistryUrls.GEO_OBJECT_GET_CODE_PARAM_CODE, code);
    params.put(RegistryUrls.GEO_OBJECT_GET_CODE_PARAM_TYPE_CODE, typeCode);

    HttpResponse resp = this.connector.httpGet(RegistryUrls.GEO_OBJECT_GET_CODE, params);
    ResponseProcessor.validateStatusCode(resp);

    GeoObject geoObject = GeoObject.fromJSON(this, resp.getAsString());

    return geoObject;
  }

  /**
   * Returns the {@link GeoObjectOverTime} with the given UID.
   * 
   * @param _uid
   *          UID of the GeoObject.
   * 
   * @return GeoObject with the given UID.
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public GeoObjectOverTime getGeoObjectOverTime(String id, String typeCode) throws AuthenticationException, ServerResponseException, IOException
  {
    if (id == null || id.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_TIME_GET, RegistryUrls.GEO_OBJECT_TIME_GET_PARAM_ID);
    }
    if (typeCode == null || typeCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_TIME_GET, RegistryUrls.GEO_OBJECT_TIME_GET_PARAM_TYPE_CODE);
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put(RegistryUrls.GEO_OBJECT_TIME_GET_PARAM_ID, id);
    params.put(RegistryUrls.GEO_OBJECT_TIME_GET_PARAM_TYPE_CODE, typeCode);

    HttpResponse resp = this.connector.httpGet(RegistryUrls.GEO_OBJECT_TIME_GET, params);
    ResponseProcessor.validateStatusCode(resp);

    GeoObjectOverTime geoObject = GeoObjectOverTime.fromJSON(this, resp.getAsString());

    return geoObject;
  }

  /**
   * Returns the {@link GeoObjectOverTime} with the given code.
   * 
   * @param code
   *          code of the GeoObject.
   * 
   * @return GeoObject with the given code.
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public GeoObjectOverTime getGeoObjectOverTimeByCode(String code, String typeCode) throws AuthenticationException, ServerResponseException, IOException
  {
    if (code == null || code.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_TIME_GET_CODE, RegistryUrls.GEO_OBJECT_TIME_GET_CODE_PARAM_CODE);
    }
    if (typeCode == null || typeCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_TIME_GET_CODE, RegistryUrls.GEO_OBJECT_TIME_GET_CODE_PARAM_TYPE_CODE);
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put(RegistryUrls.GEO_OBJECT_TIME_GET_CODE_PARAM_CODE, code);
    params.put(RegistryUrls.GEO_OBJECT_TIME_GET_CODE_PARAM_TYPE_CODE, typeCode);

    HttpResponse resp = this.connector.httpGet(RegistryUrls.GEO_OBJECT_TIME_GET_CODE, params);
    ResponseProcessor.validateStatusCode(resp);

    GeoObjectOverTime geoObject = GeoObjectOverTime.fromJSON(this, resp.getAsString());

    return geoObject;
  }

  /**
   * Sends the given {@link GeoObject} to the common geo-registry to be created.
   * 
   * @pre the status on the {@link GeoObject} is in the new state.
   * 
   * @param _geoObject
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public GeoObject createGeoObject(GeoObject _geoObject) throws AuthenticationException, ServerResponseException, IOException
  {
    if (_geoObject == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_CREATE, RegistryUrls.GEO_OBJECT_CREATE_PARAM_GEOOBJECT);
    }

    JsonObject jsonObject = _geoObject.toJSON();

    JsonObject params = new JsonObject();
    params.add(RegistryUrls.GEO_OBJECT_CREATE_PARAM_GEOOBJECT, jsonObject);

    HttpResponse resp = this.connector.httpPost(RegistryUrls.GEO_OBJECT_CREATE, params.toString());
    ResponseProcessor.validateStatusCode(resp);

    GeoObject retGeo = GeoObject.fromJSON(this, resp.getAsString());
    return retGeo;
  }

  /**
   * Sends the given {@link GeoObjectOverTime} to the common geo-registry to be
   * created.
   * 
   * @pre the status on the {@link GeoObjectOverTime} is in the new state.
   * 
   * @param _geoObject
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public GeoObjectOverTime createGeoObjectOverTime(GeoObjectOverTime _geoObject) throws AuthenticationException, ServerResponseException, IOException
  {
    if (_geoObject == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_TIME_CREATE, RegistryUrls.GEO_OBJECT_TIME_CREATE_PARAM_GEOOBJECT);
    }

    JsonObject jsonObject = _geoObject.toJSON();

    JsonObject params = new JsonObject();
    params.add(RegistryUrls.GEO_OBJECT_TIME_CREATE_PARAM_GEOOBJECT, jsonObject);

    HttpResponse resp = this.connector.httpPost(RegistryUrls.GEO_OBJECT_TIME_CREATE, params.toString());
    ResponseProcessor.validateStatusCode(resp);

    GeoObjectOverTime retGeo = GeoObjectOverTime.fromJSON(this, resp.getAsString());
    return retGeo;
  }

  /**
   * Creates a relationship between @parentUid and @childUid.
   * @param startDate TODO
   * @param endDate TODO
   * 
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   *
   * @pre Both the parent and child have already been persisted / applied
   * @post A relationship will exist between @parent and @child
   *
   * @returns ParentTreeNode The new node which was created with the provided
   *          parent.
   */
  public ParentTreeNode addChild(String parentCode, String parentTypeCode, String childCode, String childTypeCode, String hierarchyCode, Date startDate, Date endDate) throws AuthenticationException, ServerResponseException, IOException
  {
    if (childCode == null || childCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_ADD_CHILD, RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILDCODE);
    }
    if (childTypeCode == null || childTypeCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_ADD_CHILD, RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILD_TYPE_CODE);
    }
    if (parentCode == null || parentCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_ADD_CHILD, RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENTCODE);
    }
    if (parentTypeCode == null || parentTypeCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_ADD_CHILD, RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENT_TYPE_CODE);
    }
    
    if (hierarchyCode == null || hierarchyCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_ADD_CHILD, RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_HIERARCHY_CODE);
    }

    if (startDate == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_ADD_CHILD, RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_START_DATE);
    }
    
    if (endDate == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_ADD_CHILD, RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_END_DATE);
    }
    
    JsonObject params = new JsonObject();
    params.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILDCODE, childCode);
    params.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILD_TYPE_CODE, childTypeCode);
    params.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENTCODE, parentCode);
    params.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENT_TYPE_CODE, parentTypeCode);
    params.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_HIERARCHY_CODE, hierarchyCode);
    params.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_START_DATE, JsonDateUtil.format(startDate));
    params.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_END_DATE, JsonDateUtil.format(endDate));

    HttpResponse resp = this.connector.httpPost(RegistryUrls.GEO_OBJECT_ADD_CHILD, params.toString());
    ResponseProcessor.validateStatusCode(resp);

    ParentTreeNode ret = ParentTreeNode.fromJSON(resp.getAsString(), this);
    return ret;
  }

  /**
   * Sends the given {@link GeoObject} to the common geo-registry to be updated.
   * 
   * @pre the status on the {@link GeoObject} is NOT in the new state.
   * 
   * @param _geoObject
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public GeoObject updateGeoObject(GeoObject _geoObject) throws AuthenticationException, ServerResponseException, IOException
  {
    if (_geoObject == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_UPDATE, RegistryUrls.GEO_OBJECT_UPDATE_PARAM_GEOOBJECT);
    }

    JsonObject jsonObject = _geoObject.toJSON();

    JsonObject params = new JsonObject();
    params.add(RegistryUrls.GEO_OBJECT_UPDATE_PARAM_GEOOBJECT, jsonObject);

    HttpResponse resp = this.connector.httpPost(RegistryUrls.GEO_OBJECT_UPDATE, params.toString());
    ResponseProcessor.validateStatusCode(resp);

    GeoObject retGeo = GeoObject.fromJSON(this, resp.getAsString());
    return retGeo;
  }

  /**
   * Sends the given {@link GeoObjectOverTime} to the common geo-registry to be
   * updated.
   * 
   * @pre the status on the {@link GeoObjectOverTime} is NOT in the new state.
   * 
   * @param _geoObject
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public GeoObjectOverTime updateGeoObjectOverTime(GeoObjectOverTime _geoObject) throws AuthenticationException, ServerResponseException, IOException
  {
    if (_geoObject == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_TIME_UPDATE, RegistryUrls.GEO_OBJECT_TIME_UPDATE_PARAM_GEOOBJECT);
    }

    JsonObject jsonObject = _geoObject.toJSON();

    JsonObject params = new JsonObject();
    params.add(RegistryUrls.GEO_OBJECT_TIME_UPDATE_PARAM_GEOOBJECT, jsonObject);

    HttpResponse resp = this.connector.httpPost(RegistryUrls.GEO_OBJECT_TIME_UPDATE, params.toString());
    ResponseProcessor.validateStatusCode(resp);

    GeoObjectOverTime retGeo = GeoObjectOverTime.fromJSON(this, resp.getAsString());
    return retGeo;
  }

  /**
   * Returns the {@link GeoObject} with the given UID and its children of the
   * given types.
   * 
   * @param childrenTypes
   *          An array of type codes of the children to be fetched. If null or
   *          empty string is provided, all non-leaf types will be fetched.
   * @param recursive
   *          true if all recursive children should be fetched, or false if only
   *          immediate children should be fetched.
   * @param date
   *          TODO
   * @param parentUid
   *          UID of the parent {@link GeoObject}
   * 
   * @return {@link ChildTreeNode} containing the {@link GeoObject} with the
   *         given UID and its children of the given types.
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public ChildTreeNode getChildGeoObjects(String parentCode, String parentTypeCode, String[] childrenTypes, Boolean recursive, Date date) throws AuthenticationException, ServerResponseException, IOException
  {
    if (parentCode == null || parentCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_CHILDREN, RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENTCODE);
    }

    if (parentTypeCode == null || parentTypeCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_CHILDREN, RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENT_TYPE_CODE);
    }

    if (recursive == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_CHILDREN, RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_RECURSIVE);
    }

    JsonArray serialized = new JsonArray();

    if (childrenTypes != null)
    {
      for (String childType : childrenTypes)
      {
        serialized.add(childType);
      }
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENTCODE, parentCode);
    params.put(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENT_TYPE_CODE, parentTypeCode);
    params.put(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_CHILDREN_TYPES, serialized.toString());
    params.put(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_RECURSIVE, recursive.toString());

    if (date != null)
    {
      params.put(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_DATE, JsonDateUtil.format(date));
    }

    HttpResponse resp = this.connector.httpGet(RegistryUrls.GEO_OBJECT_GET_CHILDREN, params);
    ResponseProcessor.validateStatusCode(resp);

    ChildTreeNode tn = ChildTreeNode.fromJSON(resp.getAsString(), this);

    return tn;
  }

  /**
   * Returns the {@link GeoObject} with the given UID and its parent of the
   * given types.
   * 
   * Shall we include the hierarchy types as a parameter as well?
   * 
   * @param childId
   *          UID of the child {@link GeoObject}
   * @param childTypeCode
   *          The code of the child {@link GeoObjectType}
   * @param parentTypes
   *          An array of type codes of the parents to be fetched.
   * @param recursive
   *          true if all recursive parents should be fetched, or false if only
   *          immediate recursive should be fetched.
   * @param date
   *          TODO
   * @return {@link ParentTreeNode} containing the {@link GeoObject} with the
   *         given UID and its children of the given types.
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public ParentTreeNode getParentGeoObjects(String childCode, String childTypeCode, String[] parentTypes, Boolean recursive, Date date) throws AuthenticationException, ServerResponseException, IOException
  {
    if (childCode == null || childCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_PARENTS, RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILDCODE);
    }

    if (childTypeCode == null || childTypeCode.length() == 0)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_PARENTS, RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILD_TYPE_CODE);
    }

    if (recursive == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_PARENTS, RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_RECURSIVE);
    }

    JsonArray serialized = new JsonArray();

    if (parentTypes != null)
    {
      for (String parentType : parentTypes)
      {
        serialized.add(parentType);
      }
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILDCODE, childCode);
    params.put(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILD_TYPE_CODE, childTypeCode);
    params.put(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_PARENT_TYPES, serialized.toString());
    params.put(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_RECURSIVE, recursive.toString());

    if (date != null)
    {
      params.put(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_DATE, JsonDateUtil.format(date));
    }

    HttpResponse resp = this.connector.httpGet(RegistryUrls.GEO_OBJECT_GET_PARENTS, params);
    ResponseProcessor.validateStatusCode(resp);

    ParentTreeNode tn = ParentTreeNode.fromJSON(resp.getAsString(), this);

    return tn;
  }

  /**
   * Get list of valid UIDs for use in creating new GeoObjects. The Common
   * Geo-Registry will only accept newly created GeoObjects with a UID that was
   * issued from the Common GeoRegistry.
   * 
   * @param numberOfUids
   * 
   * @return An array of UIDs.
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public Set<String> getGeoObjectUids(Integer numberOfUids) throws AuthenticationException, ServerResponseException, IOException
  {
    if (numberOfUids == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_GET_UIDS, RegistryUrls.GEO_OBJECT_GET_UIDS_PARAM_AMOUNT);
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put(RegistryUrls.GEO_OBJECT_GET_UIDS_PARAM_AMOUNT, numberOfUids.toString());

    HttpResponse resp = this.connector.httpGet(RegistryUrls.GEO_OBJECT_GET_UIDS, params);
    ResponseProcessor.validateStatusCode(resp);

    JsonArray values = resp.getAsJsonArray();

    Set<String> set = new HashSet<String>(values.size());

    for (int i = 0; i < values.size(); i++)
    {
      set.add(values.get(i).getAsString());
    }

    return set;
  }

  /**
   * Sends the given {@link GeoObjectType} to the common geo-registry to be
   * created.
   * 
   * @param geoObjectType
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  public void createGeoObjectType(GeoObjectType geoObjectType) throws AuthenticationException, ServerResponseException, IOException
  {
    if (geoObjectType == null)
    {
      throw new RequiredParameterException(RegistryUrls.GEO_OBJECT_TYPE_CREATE, "geoObjectType");
    }

    JsonObject jsonObject = geoObjectType.toJSON();

    JsonObject params = new JsonObject();
    params.add("gtJSON", jsonObject);

    HttpResponse resp = this.connector.httpPost(RegistryUrls.GEO_OBJECT_TYPE_CREATE, params.toString());
    ResponseProcessor.validateStatusCode(resp);
  }

  /**
   * @throws IOException
   * @throws ServerResponseException
   * @throws AuthenticationException
   *           Returns an array of {@link GeoOjectType} objects that define the
   *           given list of types.
   *
   * @pre
   * @post
   *
   * @param types
   *          An array of GeoObjectType codes. If blank then all GeoObjectType
   *          objects are returned.
   *
   * @returns
   * @throws
   **/
  public GeoObjectType[] getGeoObjectTypes(String[] codes, String[] hierarchies) throws AuthenticationException, ServerResponseException, IOException
  {
    if (codes == null)
    {
      codes = new String[] {};
    }

    JsonArray types = new JsonArray();
    for (String code : codes)
    {
      types.add(code);
    }

    if (hierarchies == null)
    {
      hierarchies = new String[] {};
    }

    JsonArray jaHierarchies = new JsonArray();
    for (String hierarchy : hierarchies)
    {
      jaHierarchies.add(hierarchy);
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put(RegistryUrls.GEO_OBJECT_TYPE_GET_ALL_PARAM_TYPES, types.toString());
    params.put(RegistryUrls.GEO_OBJECT_TYPE_GET_ALL_PARAM_HIERARCHIES, jaHierarchies.toString());

    HttpResponse resp = this.connector.httpGet(RegistryUrls.GEO_OBJECT_TYPE_GET_ALL, params);
    ResponseProcessor.validateStatusCode(resp);

    GeoObjectType[] gots = GeoObjectType.fromJSONArray(resp.getAsString(), this);

    return gots;
  }

  /**
   * Returns an array of {@link HierarchyType} that define the given list of
   * types. If no types are provided then all will be returned.
   * 
   * @param types
   *          An array of HierarchyType codes that will be retrieved.
   * @throws AuthenticationException
   * @throws ServerResponseException
   * @throws IOException
   */
  private HierarchyType[] getHierarchyTypes(String[] types) throws AuthenticationException, ServerResponseException, IOException
  {
    if (types == null)
    {
      types = new String[] {};
    }

    JsonArray jaTypes = new JsonArray();
    for (String type : types)
    {
      jaTypes.add(type);
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put("types", jaTypes.toString());

    HttpResponse resp = this.connector.httpGet(RegistryUrls.HIERARCHY_TYPE_GET_ALL, params);
    ResponseProcessor.validateStatusCode(resp);

    HierarchyType[] hts = HierarchyType.fromJSONArray(resp.getAsString(), this);

    return hts;
  }
}
