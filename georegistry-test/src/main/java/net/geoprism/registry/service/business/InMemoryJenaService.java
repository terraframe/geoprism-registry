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
package net.geoprism.registry.service.business;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import net.geoprism.registry.etl.JenaExportConfig;
import net.geoprism.registry.service.business.RemoteJenaServiceIF;

/**
 * Test implementation of {@link RemoteJenaServiceIF} which stores all data in
 * an in-memory Jena dataset instead of a remote triple store. Each export
 * config's graph is stored as a named graph and incoming models are merged
 * into the existing data for that graph.
 */
@Service
@Primary
public class InMemoryJenaService implements RemoteJenaServiceIF
{
  private final Dataset dataset = DatasetFactory.createTxnMem();

  public Dataset getDataset()
  {
    return dataset;
  }

  /**
   * @return A copy of the current contents of the graph for the given config
   */
  public Model getModel(JenaExportConfig config)
  {
    return Txn.calculateRead(this.dataset, () -> ModelFactory.createDefaultModel().add(this.dataset.getNamedModel(config.getGraph())));
  }

  /**
   * Removes all data from every graph
   */
  public void reset()
  {
    Txn.executeWrite(this.dataset, () -> this.dataset.asDatasetGraph().clear());
  }

  @Override
  public void load(Model model, JenaExportConfig config)
  {
    Txn.executeWrite(this.dataset, () -> this.dataset.getNamedModel(config.getGraph()).add(model));
  }

  @Override
  public void update(List<String> statements, JenaExportConfig config)
  {
    Txn.executeWrite(this.dataset, () -> {
      for (String statement : statements)
      {
        UpdateAction.execute(UpdateFactory.create(statement), this.dataset);
      }
    });
  }

  @Override
  public void clear(JenaExportConfig config)
  {
    Txn.executeWrite(this.dataset, () -> this.dataset.removeNamedModel(config.getGraph()));
  }

  @Override
  public Optional<String> query(String statement, JenaExportConfig config)
  {
    return Txn.calculateRead(this.dataset, () -> {
      try (QueryExecution query = QueryExecutionFactory.create(statement, this.dataset); ByteArrayOutputStream ostream = new ByteArrayOutputStream())
      {
        ResultSetFormatter.outputAsJSON(ostream, query.execSelect());

        return Optional.of(ostream.toString());
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    });
  }
}
