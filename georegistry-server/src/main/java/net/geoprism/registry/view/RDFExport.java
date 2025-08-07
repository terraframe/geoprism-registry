package net.geoprism.registry.view;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.graph.GraphTypeReference;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.spring.DateDeserializer;
import net.geoprism.registry.spring.DateSerializer;

public class RDFExport
{
  private String                   namespace         = "";

  @JsonSerialize(using = DateSerializer.class)
  @JsonDeserialize(using = DateDeserializer.class)
  private Date                     validFor          = new Date();

  private GeometryExportType       geomExportType    = GeometryExportType.NO_GEOMETRIES;

  private List<String>             typeCodes         = new LinkedList<>();

  private List<GraphTypeReference> graphTypes        = new LinkedList<>();

  private List<String>             businessTypeCodes = new LinkedList<>();

  private List<String>             businessEdgeCodes = new LinkedList<>();

  public String getNamespace()
  {
    return namespace;
  }

  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  public Date getValidFor()
  {
    return validFor;
  }

  public void setValidFor(Date validFor)
  {
    this.validFor = validFor;
  }

  public GeometryExportType getGeomExportType()
  {
    return geomExportType;
  }

  public void setGeomExportType(GeometryExportType geomExportType)
  {
    this.geomExportType = geomExportType;
  }

  public List<String> getTypeCodes()
  {
    return typeCodes;
  }

  public void setTypeCodes(List<String> typeCodes)
  {
    this.typeCodes = typeCodes;
  }

  public List<GraphTypeReference> getGraphTypes()
  {
    return graphTypes;
  }

  public void setGraphTypes(List<GraphTypeReference> graphTypes)
  {
    this.graphTypes = graphTypes;
  }

  public List<String> getBusinessTypeCodes()
  {
    return businessTypeCodes;
  }

  public void setBusinessTypeCodes(List<String> businessTypeCodes)
  {
    this.businessTypeCodes = businessTypeCodes;
  }

  public List<String> getBusinessEdgeCodes()
  {
    return businessEdgeCodes;
  }

  public void setBusinessEdgeCodes(List<String> businessEdgeCodes)
  {
    this.businessEdgeCodes = businessEdgeCodes;
  }

  public String toDigest()
  {
    List<String> codes = new LinkedList<>();
    codes.addAll(this.getTypeCodes());
    codes.addAll(this.getGraphTypes().stream().map(ref -> ref.code).toList());
    codes.addAll(this.getBusinessTypeCodes());
    codes.addAll(this.getBusinessEdgeCodes());

    Iterator<String> it = codes.iterator();

    String codeDigest = it.next();
    while (codeDigest.length() < 100 && it.hasNext())
    {
      codeDigest += ", " + it.next();
    }

    return codeDigest;
  }

}
