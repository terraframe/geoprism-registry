/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIx;

import com.google.gson.JsonObject;

import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.SynchronizationConfig.Type;
import net.geoprism.registry.graph.JenaExternalSystem;

public class JenaExportConfig extends ExternalSystemSyncConfig
{

  private String publishUid;

  private String namespace;

  private String graph;

  public JenaExportConfig()
  {
    this.namespace = "http://terraframe.com";
    this.graph = this.namespace + "/g1";
  }

  public String getPublishUid()
  {
    return publishUid;
  }

  public void setPublishUid(String publishUid)
  {
    this.publishUid = publishUid;
  }

  public String getNamespace()
  {
    return namespace;
  }

  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }

  public String getGraph()
  {
    return graph;
  }

  public void setGraph(String graph)
  {
    this.graph = graph;
  }

  @Override
  public JenaExternalSystem getSystem()
  {
    return (JenaExternalSystem) super.getSystem();
  }

  @Override
  public void populate(SynchronizationConfig config)
  {
    super.populate(config);

    JsonObject json = config.getConfigurationJson();

    if (json != null && json.has("publishUid"))
    {
      this.setPublishUid(json.get("publishUid").getAsString());
    }

    if (json != null && json.has("namespace"))
    {
      this.setNamespace(json.get("namespace").getAsString());
    }

    if (json != null && json.has("graph"))
    {
      this.setGraph(json.get("graph").getAsString());
    }
  }

  /**
   * Neptune requires absolute IRIs for both the graph and the generated
   * resource IRIs, which are of the form <namespace>#<code>.
   */
  public void validate()
  {
    validateIri("Namespace", this.namespace);
    validateIri("Graph", this.graph);

    if (this.namespace.contains("#"))
    {
      throw new IllegalArgumentException("Namespace [" + this.namespace + "] may not contain a fragment (#)");
    }
  }

  private static void validateIri(String label, String value)
  {
    if (StringUtils.isBlank(value))
    {
      throw new IllegalArgumentException(label + " is required");
    }

    try
    {
      if (IRIx.create(value).isRelative())
      {
        throw new IllegalArgumentException(label + " [" + value + "] must be an absolute IRI, e.g. urn:example or https://example.com/graph");
      }
    }
    catch (IRIException e)
    {
      throw new IllegalArgumentException(label + " [" + value + "] is not a valid IRI: " + e.getMessage(), e);
    }
  }

  @Override
  public String getSynchronizationType()
  {
    return Type.JENA.name();
  }

}
