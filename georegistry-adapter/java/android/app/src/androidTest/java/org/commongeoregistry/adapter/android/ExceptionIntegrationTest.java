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
