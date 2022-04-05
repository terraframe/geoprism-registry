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

  private Date                startDate;

  private Date                endDate;

  private boolean             validate;

  public EdgeJsonImporter(ApplicationResource resource, Date startDate, Date endDate, boolean validate)
  {
    this.resource = resource;
    this.startDate = startDate;
    this.endDate = endDate;
    this.validate = validate;
  }

  public void importData() throws JsonSyntaxException, IOException
  {
    ServerGeoObjectService service = new ServerGeoObjectService();

    try (InputStream stream = resource.openNewStream())
    {
      JsonObject data = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();

      JsonArray graphTypes = data.get("graphTypes").getAsJsonArray();

      for (int i = 0; i < graphTypes.size(); ++i)
      {
        JsonObject joGraphType = graphTypes.get(i).getAsJsonObject();

        final String graphTypeClass = joGraphType.get("graphTypeClass").getAsString();
        final String code = joGraphType.get("code").getAsString();

        final GraphType graphType = GraphType.getByCode(graphTypeClass, code);

        JsonArray edges = joGraphType.get("edges").getAsJsonArray();

        logger.info("About to import [" + edges.size() + "] edges as MdEdge [" + graphType.getCode() + "].");

        for (int j = 0; j < edges.size(); ++j)
        {
          JsonObject joEdge = edges.get(j).getAsJsonObject();

          String sourceCode = joEdge.get("source").getAsString();
          String sourceTypeCode = joEdge.get("sourceType").getAsString();
          String targetCode = joEdge.get("target").getAsString();
          String targetTypeCode = joEdge.get("targetType").getAsString();

          ServerGeoObjectIF source = service.getGeoObjectByCode(sourceCode, sourceTypeCode);
          ServerGeoObjectIF target = service.getGeoObjectByCode(targetCode, targetTypeCode);

          source.addGraphChild(target, graphType, this.startDate, this.endDate, this.validate);
        }
      }
    }
  }

}
