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
