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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Serves the Angular index.html with its {@code <base href>} set to the
 * servlet context path of the current request (e.g. "/" or "/gpr/").
 *
 * The UI is built with a placeholder base href. Because every relative URL in
 * the UI (scripts, styles, assets, "./api/..." calls) resolves against the base
 * href, rewriting it at request time lets a single build run under any context
 * path without rebuilding.
 *
 * The status already set on the response is preserved: SessionFilter sets a
 * 401 on unauthenticated requests (including API calls) and then forwards to
 * the index page. The UI relies on that 401 to redirect to the login page, so
 * it must not be overwritten with a 200.
 */
@Component
public class IndexHtmlProvider
{
  private static final String  INDEX    = "static/index.html";

  private static final Pattern BASE_TAG = Pattern.compile("<base\\s+href\\s*=\\s*\"[^\"]*\"\\s*/?>", Pattern.CASE_INSENSITIVE);

  private static final Pattern HEAD_TAG = Pattern.compile("<head(\\s[^>]*)?>", Pattern.CASE_INSENSITIVE);

  public ResponseEntity<String> render(HttpServletRequest request, HttpServletResponse response)
  {
    final String baseTag = "<base href=\"" + HtmlUtils.htmlEscape(getBaseHref(request)) + "\">";

    String html = this.readIndex();

    Matcher matcher = BASE_TAG.matcher(html);

    if (matcher.find())
    {
      html = matcher.replaceFirst(Matcher.quoteReplacement(baseTag));
    }
    else
    {
      // No base tag in the build output, insert one at the top of <head>
      Matcher head = HEAD_TAG.matcher(html);

      if (head.find())
      {
        html = html.substring(0, head.end()) + baseTag + html.substring(head.end());
      }
    }

    return ResponseEntity.status(response.getStatus()) //
        .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)) //
        .cacheControl(CacheControl.noCache()) //
        .body(html);
  }

  public static String getBaseHref(HttpServletRequest request)
  {
    String contextPath = request.getContextPath();

    if (contextPath == null || contextPath.isEmpty() || contextPath.equals("/"))
    {
      return "/";
    }

    return contextPath.endsWith("/") ? contextPath : contextPath + "/";
  }

  /**
   * Read on every request (the file is small) so that a rebuilt UI is picked up
   * without having to restart the server.
   */
  private String readIndex()
  {
    try (InputStream stream = new ClassPathResource(INDEX).getInputStream())
    {
      return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
    }
    catch (IOException e)
    {
      throw new UncheckedIOException("Unable to read " + INDEX, e);
    }
  }
}
