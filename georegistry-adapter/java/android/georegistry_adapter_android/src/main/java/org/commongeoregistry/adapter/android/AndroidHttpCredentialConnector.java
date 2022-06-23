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
