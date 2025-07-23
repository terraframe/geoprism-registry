package net.geoprism.registry.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StaticResourceResolver implements Filter
{
  private static final String STATIC_PREFIX = "static/";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

      HttpServletRequest req = (HttpServletRequest) request;
      HttpServletResponse res = (HttpServletResponse) response;

      String path = req.getRequestURI();

      // Strip context path if present
      if (!req.getContextPath().isEmpty() && path.startsWith(req.getContextPath())) {
          path = path.substring(req.getContextPath().length());
      }

      // Remove leading slash
      if (path.startsWith("/")) {
          path = path.substring(1);
      }

      // Only serve requests that look like files (contain a dot)
      if (!path.contains(".")) {
          chain.doFilter(request, response);
          return;
      }

      Resource resource = new ClassPathResource(STATIC_PREFIX + path);
      if (!resource.exists() || !resource.isReadable()) {
          chain.doFilter(request, response); // Continue down filter chain
          return;
      }

      // Set content type
      String mimeType = req.getServletContext().getMimeType(path);
      if (mimeType == null) {
          mimeType = "application/octet-stream";
      }
      res.setContentType(mimeType);

      // Write resource to response
      try (InputStream in = resource.getInputStream()) {
        res.setContentLengthLong(resource.contentLength());
        StreamUtils.copy(in, res.getOutputStream());
    }
  }
}
