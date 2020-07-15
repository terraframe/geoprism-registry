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
 * This test documents DHIS2 behavior with regards to changing localization for org units.
 * 
 * Behavior I have seen:
 * 
 * The metadata API is the best way to change localization of org units. The entity
 * API simply does not process the "translations" array and it does not do anything with it.
 * You can also change localization via the "entityTranslations" api in the DHIS2Bridge.
 * 
 * @author rrowlands
 */
public class TranslationsTest
{
  private DHIS2Bridge facade;
  
  @Before
  public void setUp()
  {
    HTTPConnector connector = new HTTPConnector();
    connector.setCredentials(Constants.USERNAME, Constants.PASSWORD);
    connector.setServerUrl(Constants.DHIS2_URL);
    
    facade = new DHIS2Bridge(connector, Constants.API_VERSION);
  }
  
  @Test
  public void testMetadataPost() throws Exception
  {
    // Change some localization of Sierra Leone using the meatadata api.
    final String payload = "{\n" + 
        "  \"organisationUnits\": [\n" + 
        "    {\n" + 
        "      \"id\": \"ImspTQPwCqd\",\n" + 
        "      \"name\": \"Sierra Leone\",\n" + 
        "      \"shortName\": \"Sierra Leone\",\n" + 
        "      \"openingDate\": \"1970-01-01T00:00:00.000\",\n" + 
        "      \"translations\": [\n" + 
        "        {\n" + 
        "          \"property\": \"SHORT_NAME\",\n" + 
        "          \"locale\": \"km\",\n" + 
        "          \"value\": \"Localization Test km\"\n" + 
        "        },\n" + 
        "        {\n" + 
        "          \"property\": \"NAME\",\n" + 
        "          \"locale\": \"km\",\n" + 
        "          \"value\": \"Localization Test km\"\n" + 
        "        }\n" + 
        "      ]\n" + 
        "    }\n" + 
        "  ]\n" + 
        "}";
    
    List<NameValuePair> params = new ArrayList<NameValuePair>();
//    params.add(new BasicNameValuePair("importMode", "VALIDATE"));
    
    MetadataImportResponse resp = facade.metadataPost(params, new StringEntity(payload, Charset.forName("UTF-8")));
    
    System.out.println(resp.getResponse());
  }
}
