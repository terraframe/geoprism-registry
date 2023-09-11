/**
 *
 */
package org.commongeoregistry.adapter.http;

import java.io.IOException;
import java.util.Map;

public interface Connector
{

  public HttpResponse httpGet(String url, Map<String, String> params) throws AuthenticationException, IOException;

  public HttpResponse httpPost(String url, String body) throws AuthenticationException, IOException;

}