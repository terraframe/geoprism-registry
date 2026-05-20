package net.geoprism.registry.service.request;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.business.GraphBusinessService;
import net.geoprism.registry.view.EdgeImportConfigurationView;

@Service
public class EdgeImportService
{
  @Autowired
  private GraphBusinessService bizService;

  @Request(RequestType.SESSION)
  public ObjectNode getJsonImportConfiguration(String sessionId, String fileName, InputStream fileStream, EdgeImportConfigurationView view)
  {
    return bizService.getJsonImportConfiguration(fileName, fileStream, view);
  }
}
