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
package net.geoprism.dhis2.dhis2adapter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.ObjectReportResponse;

/**
 * This test documents DHIS2 behavior with regards to moving org unit(s) across a hierarchy (changing parents).
 * 
 * @author rrowlands
 */
public class OrgUnitRelationshipTest
{
  private DHIS2Facade facade;
  
  @Before
  public void setUp()
  {
    HTTPConnector connector = new HTTPConnector();
    connector.setCredentials(Constants.USERNAME, Constants.PASSWORD);
    connector.setServerUrl(Constants.DHIS2_URL);
    
    facade = new DHIS2Facade(connector, Constants.API_VERSION);
  }
  
  @Test
  public void testMetadataPost() throws Exception
  {
    // This payload changes the parent of OU_559 (Ngelehun CHC) from Badjia (OU_539) to Baoma (OU_540)
    final String payload = "{\n" + 
        "  \"organisationUnits\": [\n" + 
        "    {\n" + 
        "      \"id\": \"DiszpKrYNg8\",\n" + 
        "      \"name\": \"Ngelehun CHC\",\n" + 
        "      \"shortName\": \"Ngelehun CHC\",\n" + 
        "      \"openingDate\": \"1970-01-01T00:00:00.000\",\n" + 
        "      \"parent\": {\n" + 
        "        \"id\": \"vWbkYPRmKyS\"\n" + 
        "      },\n" + 
        "      \"path\": \"/ImspTQPwCqd/vWbkYPRmKyS/MXXDvFpfZmP\",\n" + 
        "      \"level\": 3\n" + 
        "    }\n" + 
        "  ]\n" + 
        "}";
    
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("importMode", "VALIDATE"));
    
    MetadataImportResponse resp = facade.metadataPost(params, new StringEntity(payload, Charset.forName("UTF-8")));
    
//    System.out.println(resp.getResponse());
  }
  
  /*
   * Changing a parent on an entity patch throws a backend sql error. Not exactly what I would have expected.
   */
//  @Test
//  public void testEntityPatch() throws Exception
//  {
//    // This payload changes the parent of OU_559 (Ngelehun CHC) from Badjia (OU_539) to Baoma (OU_540)
//    final String payload = "{\n" + 
//        "  \"code\": \"OU_559\",\n" + 
//        "  \"id\": \"DiszpKrYNg8\",\n" + 
//        "  \"name\": \"Ngelehun CHC\",\n" + 
//        "  \"parent\": {\n" + 
//        "    \"id\": \"vWbkYPRmKyS\"\n" + 
//        "  },\n" + 
//        "  \"path\": \"/ImspTQPwCqd/vWbkYPRmKyS/MXXDvFpfZmP\",\n" + 
//        "  \"level\": 3\n" + 
//        "}";
//    
//    List<NameValuePair> params = new ArrayList<NameValuePair>();
//    params.add(new BasicNameValuePair("importMode", "VALIDATE"));
//    
//    ObjectReportResponse resp = facade.entityIdPatch("organisationUnits", "DiszpKrYNg8", params, new StringEntity(payload, Charset.forName("UTF-8")));
//    
////    System.out.println(resp.getResponse());
//    
//    Assert.assertEquals(204, resp.getStatusCode());
//  }
}
