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

import org.commongeoregistry.adapter.http.AbstractHttpConnector;
import org.commongeoregistry.adapter.http.HttpResponse;

import java.util.Map;

public class MockHttpConnector extends AbstractHttpConnector {
    private String url;

    private Map<String, String> params;

    private String body;

    private HttpResponse response;

    public MockHttpConnector() {
        super();
    }

    public MockHttpConnector(HttpResponse response) {
        super();
        this.response = response;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getBody() {
        return body;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    private void clear() {
        this.url = null;
        this.params = null;
        this.body = null;
    }

    @Override
    public HttpResponse httpGet(String url, Map<String, String> params) {
        this.clear();

        this.url = url;
        this.params = params;

        return this.response;
    }

    @Override
    public HttpResponse httpPost(String url, String body) {
        this.clear();

        this.url = url;
        this.body = body;

        return this.response;
    }

}
