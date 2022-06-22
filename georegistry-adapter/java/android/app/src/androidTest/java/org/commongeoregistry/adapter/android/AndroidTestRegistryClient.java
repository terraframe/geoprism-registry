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

import com.google.gson.JsonObject;

import org.commongeoregistry.adapter.http.AbstractHttpConnector;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.Connector;
import org.commongeoregistry.adapter.http.HttpResponse;
import org.commongeoregistry.adapter.http.ResponseProcessor;
import org.commongeoregistry.adapter.http.ServerResponseException;

import java.io.IOException;
import java.util.HashMap;

public class AndroidTestRegistryClient extends AndroidRegistryClient
{
    public AndroidTestRegistryClient(Connector connector, Context context)
    {
        super(connector, context);
    }

    public void testSetUp() throws AuthenticationException, ServerResponseException, IOException {
        HttpResponse resp = ((AbstractHttpConnector)this.getConnector()).httpGetRaw("integrationtest/testSetUp", new HashMap<String, String>());
        ResponseProcessor.validateStatusCode(resp);
    }

    public void testCleanUp() throws AuthenticationException, ServerResponseException, IOException {
        HttpResponse resp = ((AbstractHttpConnector)this.getConnector()).httpGetRaw("integrationtest/testCleanUp", new HashMap<String, String>());
        ResponseProcessor.validateStatusCode(resp);
    }
}
