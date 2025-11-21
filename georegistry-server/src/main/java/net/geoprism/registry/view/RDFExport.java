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
package net.geoprism.registry.view;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.graph.GraphTypeReference;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;

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
