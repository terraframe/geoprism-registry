/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package org.commongeoregistry.adapter.android;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.ServerResponseException;
import org.junit.Test;

import java.io.IOException;

public class ExceptionIntegrationTest {

    @Test(expected = AuthenticationException.class)
    public void testBadCredentials() throws AuthenticationException, ServerResponseException, IOException {
        Context context = InstrumentationRegistry.getTargetContext();

        AndroidHttpCredentialConnector connector = new AndroidHttpCredentialConnector();
        connector.setCredentials(AndroidIntegrationTest.user, "BAD");
        connector.setServerUrl(AndroidIntegrationTest.serverUrl);
        connector.initialize();

        AndroidTestRegistryClient client = new AndroidTestRegistryClient(connector, context);
        client.getGeoObjectTypes(new String[]{});
    }

    @Test(expected = ServerResponseException.class)
    public void testBadGeoObjectType() throws AuthenticationException, ServerResponseException, IOException {
        Context context = InstrumentationRegistry.getTargetContext();

        AndroidHttpCredentialConnector connector = new AndroidHttpCredentialConnector();
        connector.setCredentials(AndroidIntegrationTest.user, AndroidIntegrationTest.pass);
        connector.setServerUrl(AndroidIntegrationTest.serverUrl);
        connector.initialize();

        AndroidTestRegistryClient client = new AndroidTestRegistryClient(connector, context);
        client.getGeoObjectTypes(new String[]{"BAD"});
    }


}
