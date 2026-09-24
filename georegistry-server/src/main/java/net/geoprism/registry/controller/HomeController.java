package net.geoprism.registry.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController
{
  @Autowired
  private IndexHtmlProvider indexHtml;

  @GetMapping({ "/", "/index.html" })
  public ResponseEntity<String> home(HttpServletRequest request, HttpServletResponse response)
  {
    return this.indexHtml.render(request, response);
  }
}
