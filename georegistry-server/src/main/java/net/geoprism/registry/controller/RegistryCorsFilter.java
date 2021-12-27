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
package net.geoprism.registry.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import net.geoprism.registry.GeoregistryProperties;

public class RegistryCorsFilter implements Filter
{
  /**
   * Special thanks to our friends at the University of Oslo (DHIS2)
   * 
   * https://github.com/dhis2/dhis2-core/blob/c8dd0f5f17e49a44b023a6802b9e5b123a6f02da/dhis-2/dhis-web-api/src/main/java/org/hisp/dhis/webapi/filter/CorsFilter.java
   */
  
  public static final String CORS_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

  public static final String CORS_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

  public static final String CORS_MAX_AGE = "Access-Control-Max-Age";

  public static final String CORS_ALLOW_HEADERS = "Access-Control-Allow-Headers";

  public static final String CORS_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

  public static final String CORS_REQUEST_HEADERS = "Access-Control-Request-Headers";

  public static final String CORS_ALLOW_METHODS = "Access-Control-Allow-Methods";

  public static final String CORS_REQUEST_METHOD = "Access-Control-Request-Method";

  public static final String CORS_ORIGIN = "Origin";

  private static final String EXPOSED_HEADERS = "ETag, Location";

  private static final Integer MAX_AGE = 60 * 60; // 1hr max-age
  
  private List<String> whitelist;
  
  public RegistryCorsFilter()
  {
    whitelist = GeoregistryProperties.getCorsWhitelist();
  }
  
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
  {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;
      
      final String origin = request.getHeader("Origin");
      
      // Origin header is required for CORS requests
      if ( StringUtils.isEmpty( origin ) )
      {
        chain.doFilter(req, res);
        return;
      }
      
      if ( !this.isOriginWhitelisted(request, origin) )
      {
        chain.doFilter(req, res);
        return;
      }

      response.addHeader( CORS_ALLOW_CREDENTIALS, "true" );
      response.addHeader( CORS_ALLOW_ORIGIN, origin );
      response.addHeader( "Vary", CORS_ORIGIN );
      
      if ( isPreflight( request ) )
      {
          String requestHeaders = request.getHeader( CORS_REQUEST_HEADERS );
          String requestMethod = request.getHeader( CORS_REQUEST_METHOD );

          response.addHeader( CORS_ALLOW_METHODS, requestMethod );
          response.addHeader( CORS_ALLOW_HEADERS, requestHeaders );
          response.addHeader( CORS_MAX_AGE, String.valueOf( MAX_AGE ) );

          response.setStatus( HttpServletResponse.SC_NO_CONTENT );

          // CORS preflight requires a 2xx status code, so short-circuit the
          // filter chain

          return;
      }
      else
      {
          response.addHeader( CORS_EXPOSE_HEADERS, EXPOSED_HEADERS );
      }

      chain.doFilter(req, res);
  }
  
  private boolean isPreflight( HttpServletRequest request )
  {
      return "OPTIONS".equals( request.getMethod() )
          && !StringUtils.isEmpty( request.getHeader( CORS_ORIGIN ) )
          && !StringUtils.isEmpty( request.getHeader( CORS_REQUEST_METHOD ) );
  }
  
//  private boolean isOriginWhitelisted( HttpServletRequest request, String origin )
//  {
//      HttpServletRequestEncodingWrapper encodingWrapper = new HttpServletRequestEncodingWrapper( request );
//      UriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromContextPath( encodingWrapper )
//          .replacePath( "" );
//
//      String forwardedProto = request.getHeader( "X-Forwarded-Proto" );
//
//      if ( !StringUtils.isEmpty( forwardedProto ) )
//      {
//          uriBuilder.scheme( forwardedProto );
//      }
//
//      String localUrl = uriBuilder.build().toString();
//
//      return !StringUtils.isEmpty( origin ) && (localUrl.equals( origin ) ||
//          configurationService.isCorsWhitelisted( origin ));
//  }
  
  private boolean isOriginWhitelisted(HttpServletRequest request, final String origin)
  {
    final boolean isWhitelistAll = whitelist.contains("*");
    
    if (isWhitelistAll)
    {
      return true;
    }
    else
    {
      return origin != null && whitelist.contains(origin);
    }
  }

  @Override
  public void init(FilterConfig filterConfig)
  {
    
  }

  @Override
  public void destroy()
  {

  }
  
  /**
   * Simple HttpServletRequestWrapper implementation that makes sure that the
   * query string is properly encoded.
   */
  class HttpServletRequestEncodingWrapper extends HttpServletRequestWrapper
  {
      public HttpServletRequestEncodingWrapper( HttpServletRequest request )
      {
          super( request );
      }

      @Override
      public String getQueryString()
      {
          String queryString = super.getQueryString();

          if ( !StringUtils.isEmpty( queryString ) )
          {
              try
              {
                  return URLEncoder.encode( queryString, "UTF-8" );
              }
              catch ( UnsupportedEncodingException ignored )
              {
              }
          }

          return queryString;
      }
  }
}
