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
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.HttpResponse;
import org.commongeoregistry.adapter.http.ServerResponseException;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataCache;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpRegistryClientTest
{

  @Test
  public void testRefreshMetadataCache() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Setup mock objects
     */
    RegistryAdapterServer registry = new RegistryAdapterServer(new MockIdService());

    GeoObjectType state = MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, registry);

    JsonArray req1Array = new JsonArray();
    req1Array.add(state.toJSON());

    String organizationCode = "";

    HierarchyType locatedIn = MetadataFactory.newHierarchyType("LocatedIn", new LocalizedValue("LOCATED_IN_LABEL"), new LocalizedValue("LOCATED_IN_DESCRIPTION"), organizationCode, registry);

    JsonArray req2Array = new JsonArray();
    req2Array.add(locatedIn.toJSON());

    MockHttpRequest[] requests = new MockHttpRequest[] { new MockHttpRequest(new HttpResponse(req1Array.toString(), 200)), new MockHttpRequest(new HttpResponse(req2Array.toString(), 200)) };
    MockHttpConnector connector = new MockHttpConnector(requests);

    /*
     * Invoke method
     */
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.refreshMetadataCache();

    MockHttpRequest req1 = connector.getRequests().get(0);
    MockHttpRequest req2 = connector.getRequests().get(1);

    /*
     * Validate request
     */
    Assert.assertEquals(RegistryUrls.GEO_OBJECT_TYPE_GET_ALL, req1.getUrl());
    Assert.assertEquals(2, req1.getParams().size());

    /*
     * Validate response
     */
    MetadataCache cache = client.getMetadataCache();
    Optional<GeoObjectType> optional = cache.getGeoObjectType(state.getCode());

    Assert.assertTrue(optional.isPresent());

    GeoObjectType test = optional.get();

    Assert.assertEquals(state.getCode(), test.getCode());
    Assert.assertEquals(state.getDescription().getValue(), test.getDescription().getValue());
    Assert.assertEquals(state.getLabel().getValue(), test.getLabel().getValue());

    /*
     * Validate the hierarchy type request
     */
    Assert.assertEquals(RegistryUrls.HIERARCHY_TYPE_GET_ALL, req2.getUrl());
    Assert.assertEquals(1, req2.getParams().size());

    Optional<HierarchyType> htOpt = cache.getHierachyType(locatedIn.getCode());

    Assert.assertTrue(optional.isPresent());

    HierarchyType htCache = htOpt.get();

    Assert.assertEquals(locatedIn.getCode(), htCache.getCode());
    Assert.assertEquals(locatedIn.getDescription().getValue(), htCache.getDescription().getValue());
    Assert.assertEquals(locatedIn.getLabel().getValue(), htCache.getLabel().getValue());
  }

  @Test(expected = ServerResponseException.class)
  public void testRefreshMetadataCacheBadStatus() throws AuthenticationException, ServerResponseException, IOException
  {
    HttpResponse response = new HttpResponse(new JsonArray().toString(), 400);
    MockHttpConnector connector = new MockHttpConnector(new MockHttpRequest[] { new MockHttpRequest(response) });

    /*
     * Invoke method
     */
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.refreshMetadataCache();
  }

  @Test
  public void testGetGeoObject() throws AuthenticationException, ServerResponseException, IOException, InvocationTargetException
  {
    /*
     * Setup mock objects
     */
    MockHttpConnector connector = new MockHttpConnector(new MockHttpRequest[] { MockIdService.getIdRequest(500) });
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getIdService().populate(500);

    MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, client);

    GeoObject geoObject = client.newGeoObjectInstance("State");
    geoObject.setCode("Test");
    geoObject.setUid("blarg");

    connector.setNextRequest(new MockHttpRequest(new HttpResponse(geoObject.toJSON().toString(), 200)));

    /*
     * Invoke method
     */
    GeoObject test = client.getGeoObject(geoObject.getUid(), geoObject.getType().getCode());

    /*
     * Validate response
     */
    Assert.assertEquals(geoObject.getCode(), test.getCode());
    Assert.assertEquals(geoObject.getUid(), test.getUid());

    /*
     * Validate request
     */
    Assert.assertEquals(RegistryUrls.GEO_OBJECT_GET, connector.getUrl());

    Map<String, String> params = connector.getParams();

    Assert.assertNotNull(params);
    Assert.assertEquals(2, params.size());

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_PARAM_ID));
    Assert.assertEquals(geoObject.getUid(), params.get(RegistryUrls.GEO_OBJECT_GET_PARAM_ID));

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_PARAM_TYPE_CODE));
    Assert.assertEquals(geoObject.getType().getCode(), params.get(RegistryUrls.GEO_OBJECT_GET_PARAM_TYPE_CODE));
  }

  @Test(expected = RequiredParameterException.class)
  public void testGetGeoObjectMissingUID() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Invoke method
     */
    MockHttpConnector connector = new MockHttpConnector();
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getGeoObject(null, null);
  }

  @Test(expected = ServerResponseException.class)
  public void testGetGeoObjectBadStatus() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Invoke method
     */
    MockHttpConnector connector = new MockHttpConnector(new MockHttpRequest[] { new MockHttpRequest(new HttpResponse("", 400)) });
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getGeoObject("23", "foo");
  }

  @Test
  public void testCreateGeoObject() throws AuthenticationException, ServerResponseException, IOException, InvocationTargetException
  {
    /*
     * Setup mock objects
     */
    MockHttpConnector connector = new MockHttpConnector(new MockHttpRequest[] { MockIdService.getIdRequest(500), new MockHttpRequest(new HttpResponse("", 201)) });
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getIdService().populate(500);

    MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, client);

    GeoObject geoObject = client.newGeoObjectInstance("State");
    geoObject.setCode("Test");
    geoObject.setUid("blarg");

    /*
     * Invoke method
     */
    connector.setNextRequest(new MockHttpRequest(new HttpResponse(geoObject.toJSON().toString(), 201)));
    client.createGeoObject(geoObject);

    /*
     * Validate request
     */
    Assert.assertEquals(RegistryUrls.GEO_OBJECT_CREATE, connector.getUrl());

    String body = connector.getBody();

    Assert.assertNotNull(body);

    JsonObject params = new JsonParser().parse(body).getAsJsonObject();
    GeoObject test = GeoObject.fromJSON(client, params.get(RegistryUrls.GEO_OBJECT_CREATE_PARAM_GEOOBJECT).toString());

    Assert.assertEquals(geoObject.getUid(), test.getUid());
  }

  @Test(expected = RequiredParameterException.class)
  public void testCreateGeoObjectMissingUID() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Invoke method
     */
    MockHttpConnector connector = new MockHttpConnector();
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.createGeoObject(null);
  }

  @Test
  public void testUpdateGeoObject() throws AuthenticationException, ServerResponseException, IOException, InvocationTargetException
  {
    /*
     * Setup mock objects
     */
    MockHttpConnector connector = new MockHttpConnector(new MockHttpRequest[] { MockIdService.getIdRequest(500), new MockHttpRequest(new HttpResponse("", 201)) });
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getIdService().populate(500);

    MetadataFactory.newGeoObjectType("State", GeometryType.POLYGON, new LocalizedValue("State"), new LocalizedValue("State"), true, null, client);

    GeoObject geoObject = client.newGeoObjectInstance("State");
    geoObject.setCode("Test");
    geoObject.setUid("blarg");

    /*
     * Invoke method
     */
    connector.setNextRequest(new MockHttpRequest(new HttpResponse(geoObject.toJSON().toString(), 201)));
    client.updateGeoObject(geoObject);

    /*
     * Validate request
     */
    Assert.assertEquals(RegistryUrls.GEO_OBJECT_UPDATE, connector.getUrl());

    String body = connector.getBody();

    Assert.assertNotNull(body);

    JsonObject params = new JsonParser().parse(body).getAsJsonObject();

    GeoObject test = GeoObject.fromJSON(client, params.get(RegistryUrls.GEO_OBJECT_UPDATE_PARAM_GEOOBJECT).toString());

    Assert.assertEquals(geoObject.getUid(), test.getUid());
  }

  @Test(expected = RequiredParameterException.class)
  public void testUpdateGeoObjectMissingUID() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Invoke method
     */
    MockHttpConnector connector = new MockHttpConnector();
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.updateGeoObject(null);
  }

  @Test
  public void testGetChildGeoObjects() throws AuthenticationException, ServerResponseException, IOException, InvocationTargetException
  {
    MockHttpConnector connector = new MockHttpConnector(new MockHttpRequest[] { MockIdService.getIdRequest(500) });
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getIdService().populate(500);

    /*
     * Setup mock objects
     */
    TestFixture.defineExampleHierarchies(client);
    HierarchyType geoPolitical = client.getMetadataCache().getHierachyType(TestFixture.GEOPOLITICAL).get();

    GeoObject pOne = client.newGeoObjectInstance(TestFixture.PROVINCE);
    pOne.setCode("pOne");
    pOne.setUid("pOne");
    ChildTreeNode ptOne = new ChildTreeNode(pOne, geoPolitical);

    GeoObject dOne = client.newGeoObjectInstance(TestFixture.DISTRICT);
    dOne.setCode("dOne");
    dOne.setUid("dOne");
    ChildTreeNode dtOne = new ChildTreeNode(dOne, geoPolitical);
    ptOne.addChild(dtOne);

    GeoObject cOne = client.newGeoObjectInstance(TestFixture.COMMUNE);
    cOne.setCode("cOne");
    cOne.setUid("cOne");
    ChildTreeNode ctOne = new ChildTreeNode(cOne, geoPolitical);
    dtOne.addChild(ctOne);

    GeoObject dTwo = client.newGeoObjectInstance(TestFixture.DISTRICT);
    dTwo.setCode("dTwo");
    dTwo.setUid("dTwo");
    ChildTreeNode dtTwo = new ChildTreeNode(dTwo, geoPolitical);
    ptOne.addChild(dtTwo);

    connector.setNextRequest(new MockHttpRequest(new HttpResponse(dtOne.toJSON().toString(), 200)));

    /*
     * Invoke method
     */
    ChildTreeNode node = client.getChildGeoObjects(pOne.getUid(), pOne.getType().getCode(), new String[] { TestFixture.DISTRICT }, false, new Date());

    /*
     * Validate response
     */
    Assert.assertEquals(node.getGeoObject().getType().getCode(), dOne.getType().getCode());

    /*
     * Validate request
     */
    Assert.assertEquals(RegistryUrls.GEO_OBJECT_GET_CHILDREN, connector.getUrl());

    Map<String, String> params = connector.getParams();

    Assert.assertNotNull(params);
    Assert.assertEquals(5, params.size());

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENTCODE));
    Assert.assertEquals(pOne.getUid(), params.get(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENTCODE));

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENT_TYPE_CODE));
    Assert.assertEquals(pOne.getType().getCode(), params.get(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENT_TYPE_CODE));

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_CHILDREN_TYPES));
    Assert.assertEquals("[\"" + TestFixture.DISTRICT + "\"]", params.get(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_CHILDREN_TYPES));

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_RECURSIVE));
    Assert.assertEquals(Boolean.FALSE.toString(), params.get(RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_RECURSIVE));
  }

  @Test(expected = RequiredParameterException.class)
  public void testGetChildGeoObjectsMissingParentUID() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Invoke method
     */
    MockHttpConnector connector = new MockHttpConnector();
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getChildGeoObjects(null, "foo", new String[] { "Test" }, true, new Date());
  }

  @Test(expected = RequiredParameterException.class)
  public void testGetChildGeoObjectsMissingChildCode() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Invoke method
     */
    MockHttpConnector connector = new MockHttpConnector();
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getChildGeoObjects("Abc", null, new String[] { "Test" }, true, new Date());
  }

  @Test
  public void testGetParentGeoObjects() throws AuthenticationException, ServerResponseException, IOException, InvocationTargetException
  {
    MockHttpConnector connector = new MockHttpConnector(new MockHttpRequest[] { MockIdService.getIdRequest(500) });
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getIdService().populate(500);

    /*
     * Setup mock objects
     */
    TestFixture.defineExampleHierarchies(client);
    HierarchyType geoPolitical = client.getMetadataCache().getHierachyType(TestFixture.GEOPOLITICAL).get();

    GeoObject pOne = client.newGeoObjectInstance(TestFixture.PROVINCE);
    pOne.setCode("pOne");
    pOne.setUid("pOne");
    ChildTreeNode ptOne = new ChildTreeNode(pOne, geoPolitical);

    GeoObject dOne = client.newGeoObjectInstance(TestFixture.DISTRICT);
    dOne.setCode("dOne");
    dOne.setUid("dOne");
    ChildTreeNode dtOne = new ChildTreeNode(dOne, geoPolitical);
    ptOne.addChild(dtOne);

    GeoObject cOne = client.newGeoObjectInstance(TestFixture.COMMUNE);
    cOne.setCode("cOne");
    cOne.setUid("cOne");
    ChildTreeNode ctOne = new ChildTreeNode(cOne, geoPolitical);
    dtOne.addChild(ctOne);

    GeoObject dTwo = client.newGeoObjectInstance(TestFixture.DISTRICT);
    dTwo.setCode("dTwo");
    dTwo.setUid("dTwo");
    ChildTreeNode dtTwo = new ChildTreeNode(dTwo, geoPolitical);
    ptOne.addChild(dtTwo);

    connector.setNextRequest(new MockHttpRequest(new HttpResponse(dtOne.toJSON().toString(), 200)));

    /*
     * Invoke method
     */
    ParentTreeNode node = client.getParentGeoObjects(cOne.getUid(), cOne.getType().getCode(), new String[] { TestFixture.DISTRICT }, false, new Date());

    /*
     * Validate response
     */
    Assert.assertEquals(node.getGeoObject().getType().getCode(), dOne.getType().getCode());

    /*
     * Validate request
     */
    Assert.assertEquals(RegistryUrls.GEO_OBJECT_GET_PARENTS, connector.getUrl());

    Map<String, String> params = connector.getParams();

    Assert.assertNotNull(params);
    Assert.assertEquals(5, params.size());

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILDCODE));
    Assert.assertEquals(cOne.getUid(), params.get(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILDCODE));

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILD_TYPE_CODE));
    Assert.assertEquals(cOne.getType().getCode(), params.get(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILD_TYPE_CODE));

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_PARENT_TYPES));
    Assert.assertEquals("[\"" + TestFixture.DISTRICT + "\"]", params.get(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_PARENT_TYPES));

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_RECURSIVE));
    Assert.assertEquals(Boolean.FALSE.toString(), params.get(RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_RECURSIVE));
  }

  @Test(expected = RequiredParameterException.class)
  public void testGetParentGeoObjectsMissingParentUID() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Invoke method
     */
    MockHttpConnector connector = new MockHttpConnector();
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getParentGeoObjects(null, "foo", new String[] { "Test" }, true, new Date());
  }

  @Test
  public void testGetGeoObjectUids() throws AuthenticationException, ServerResponseException, IOException, InvocationTargetException
  {
    MockHttpConnector connector = new MockHttpConnector(new MockHttpRequest[] { MockIdService.getIdRequest(500) });
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getIdService().populate(500);

    /*
     * Setup mock objects
     */
    JsonArray values = new JsonArray();
    values.add("uid1");
    values.add("uid2");
    values.add("uid3");

    connector.setNextRequest(new MockHttpRequest(new HttpResponse(values.toString(), 200)));

    /*
     * Invoke method
     */
    Set<String> list = client.getGeoObjectUids(values.size());

    /*
     * Validate response
     */
    Assert.assertEquals(values.size(), list.size());

    /*
     * Validate request
     */
    Assert.assertEquals(RegistryUrls.GEO_OBJECT_GET_UIDS, connector.getUrl());

    Map<String, String> params = connector.getParams();

    Assert.assertNotNull(params);
    Assert.assertEquals(1, params.size());

    Assert.assertTrue(params.containsKey(RegistryUrls.GEO_OBJECT_GET_UIDS_PARAM_AMOUNT));
    Assert.assertEquals(Integer.toString(values.size()), params.get(RegistryUrls.GEO_OBJECT_GET_UIDS_PARAM_AMOUNT));
  }

  @Test(expected = RequiredParameterException.class)
  public void testGetGeoObjectUidsMissingParentUID() throws AuthenticationException, ServerResponseException, IOException
  {
    /*
     * Invoke method
     */
    MockHttpConnector connector = new MockHttpConnector();
    HttpRegistryClient client = new HttpRegistryClient(connector);
    client.getGeoObjectUids(null);
  }
}
