package org.commongeoregistry.adapter.android;

import android.content.Context;
import com.google.gson.JsonObject;

import org.commongeoregistry.adapter.HttpRegistryClient;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.Connector;
import org.commongeoregistry.adapter.http.HttpResponse;
import org.commongeoregistry.adapter.http.ResponseProcessor;
import org.commongeoregistry.adapter.http.ServerResponseException;

import java.io.IOException;
import java.util.Date;
import java.util.List;


public class AndroidRegistryClient extends HttpRegistryClient
{
  /**
   * 
   */
  private static final long serialVersionUID = 2367836756416546643L;
  
  private LocalObjectCache localObjectCache;

  /**
   *
   * @param connector URL to the common geo-registry
   */
  public AndroidRegistryClient(Connector connector, Context context)
  {
    super(connector, new AndroidSQLiteIdService());

    this.localObjectCache = new LocalObjectCache(context, this);
  }

  /**
   *
   * @param connector URL to the common geo-registry
   * @param localObjectCache LocalObjectCache to use with the client
   */
  public AndroidRegistryClient(Connector connector, LocalObjectCache localObjectCache)
  {
    super(connector, new AndroidSQLiteIdService());

    this.localObjectCache = localObjectCache;
  }

  /**
   * Returns a reference to the object that is managing the local persisted
   * cache on the Android device.
   * 
   * @return a reference to the object that is managing the local persisted
   * cache on the Android device.
   */
  public LocalObjectCache getLocalCache()
  {
    return this.localObjectCache;
  }
  
  /**
   * All modified objects that have been persisted will be pushed to the
   * 
   * common geo-registry.
   */
  public void pushObjectsToRegistry() throws AuthenticationException, ServerResponseException, IOException {
    List<AbstractActionDTO> actions = this.localObjectCache.getUnpushedActionHistory();

    String sActions = AbstractActionDTO.serializeActions(actions).toString();

    JsonObject params = new JsonObject();
    params.addProperty(RegistryUrls.SUBMIT_CHANGE_REQUEST_PARAM_ACTIONS, sActions);

    HttpResponse resp = this.getConnector().httpPost(RegistryUrls.SUBMIT_CHANGE_REQUEST, params.toString());
    ResponseProcessor.validateStatusCode(resp);

    this.localObjectCache.saveLastPushDate(new Date().getTime());
  }
}
