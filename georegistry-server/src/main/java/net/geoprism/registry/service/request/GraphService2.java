package net.geoprism.registry.service.request;

import java.io.InputStream;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.service.business.GraphBusinessService;

@Service
public class GraphService2
{
  @Autowired GraphBusinessService bizService;
  
  @Request(RequestType.SESSION)
  public ObjectNode getJsonImportConfiguration(String sessionId, String graphTypeClass, String graphTypeCode, Date startDate, Date endDate, String source, String fileName, InputStream fileStream)
  {
    return bizService.getJsonImportConfiguration(graphTypeClass, graphTypeCode, startDate, endDate, source, fileName, fileStream);
  }
}
