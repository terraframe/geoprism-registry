package org.commongeoregistry.adapter.android;


import android.util.Base64;

import org.commongeoregistry.adapter.http.HttpCredentialConnector;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

public class AndroidHttpCredentialConnector extends HttpCredentialConnector
{
    @Override
    public void configureHttpUrlConnectionPost(HttpURLConnection con)
    {
        if (con instanceof HttpsURLConnection)
        {
            String creds = this.username + ":" + this.password;

            con.setRequestProperty("Authorization", "basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP));
        }
    }
}
