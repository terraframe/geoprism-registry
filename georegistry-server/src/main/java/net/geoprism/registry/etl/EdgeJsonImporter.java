package net.geoprism.registry.etl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.runwaysdk.resource.ApplicationResource;

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;

public class EdgeJsonImporter
{
  private static final Logger logger = LoggerFactory.getLogger(EdgeJsonImporter.class);

  private ApplicationResource resource;

  private GraphType           graphType;

  private Date                startDate;

  private Date                endDate;

  public EdgeJsonImporter(ApplicationResource resource, GraphType graphType, Date startDate, Date endDate)
  {
    this.resource = resource;
    this.graphType = graphType;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public void importData() throws JsonSyntaxException, IOException
  {
    ServerGeoObjectService service = new ServerGeoObjectService();

    try (InputStream stream = resource.openNewStream())
    {
      JsonObject data = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();
  
      JsonArray edges = data.get("edges").getAsJsonArray();
  
      logger.info("About to import [" + edges.size() + "] edges as MdEdge [" + this.graphType.getCode() + "].");
  
      for (int i = 0; i < edges.size(); ++i)
      {
        JsonObject joEdge = edges.get(i).getAsJsonObject();
  
        String sourceCode = joEdge.get("source").getAsString();
        String sourceTypeCode = joEdge.get("sourceType").getAsString();
        String targetCode = joEdge.get("target").getAsString();
        String targetTypeCode = joEdge.get("targetType").getAsString();
  
        ServerGeoObjectIF source = service.getGeoObjectByCode(sourceCode, sourceTypeCode);
        ServerGeoObjectIF target = service.getGeoObjectByCode(targetCode, targetTypeCode);
  
        source.addGraphChild(target, this.graphType, this.startDate, this.endDate);
      }
    }
  }

}
