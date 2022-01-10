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
package net.geoprism.registry.etl.fhir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.SortedSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.hl7.fhir.r4.model.Bundle;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.QueryFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import net.geoprism.gis.geoserver.SessionPredicate;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.etl.ExportJobHasErrors;
import net.geoprism.registry.etl.FhirSyncExportConfig;
import net.geoprism.registry.etl.FhirSyncLevel;
import net.geoprism.registry.etl.export.ExportErrorQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class FhirExportSynchronizationManager
{
  private FhirSyncExportConfig config;

  private ExportHistory        history;

  public FhirExportSynchronizationManager(FhirSyncExportConfig config, ExportHistory history)
  {
    this.config = config;
    this.history = history;
  }

  public void synchronize()
  {
    final FhirExternalSystem system = (FhirExternalSystem) this.config.getSystem();

    try (FhirConnection connection = FhirConnectionFactory.get(system))
    {
      SortedSet<FhirSyncLevel> levels = this.config.getLevels();

      int expectedLevel = 0;
      long exportCount = 0;

      for (FhirSyncLevel level : levels)
      {
        if (level.getLevel() != expectedLevel)
        {
          throw new ProgrammingErrorException("Unexpected level number [" + level.getLevel() + "].");
        }

        history.appLock();
        history.setWorkProgress((long) expectedLevel);
        history.setExportedRecords(exportCount);
        history.apply();

        ListTypeVersion version = ListTypeVersion.get(level.getVersionId());

        FhirDataPopulator populator = FhirFactory.getPopulator(level.getImplementation());

        ListTypeFhirExporter exporter = new ListTypeFhirExporter(version, connection, populator, true);
        long results = exporter.export();

        exportCount += results;

        expectedLevel++;
      }

      history.appLock();
      history.setWorkTotal((long) expectedLevel);
      history.setWorkProgress((long) expectedLevel);
      history.setExportedRecords(exportCount);
      history.clearStage();
      history.addStage(ExportStage.COMPLETE);
      history.apply();

      NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));

      handleExportErrors();
    }
    catch (Exception e)
    {
      throw new HttpError(e);
    }
  }

  private void handleExportErrors()
  {
    ExportErrorQuery query = new ExportErrorQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(history));
    Boolean hasErrors = query.getCount() > 0;

    if (hasErrors)
    {
      ExportJobHasErrors ex = new ExportJobHasErrors();

      throw ex;
    }
  }

  public InputStream generateZipFile() throws IOException
  {
    // Zip up the entire contents of the file
    final File directory = this.writeToFile();
    final PipedOutputStream pos = new PipedOutputStream();
    final PipedInputStream pis = new PipedInputStream(pos);

    Thread t = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          try (ZipOutputStream zipFile = new ZipOutputStream(pos))
          {
            File[] files = directory.listFiles();

            for (File file : files)
            {
              ZipEntry entry = new ZipEntry(file.getName());
              zipFile.putNextEntry(entry);

              try (FileInputStream in = new FileInputStream(file))
              {
                IOUtils.copy(in, zipFile);
              }
            }
          }
          finally
          {
            pos.close();
          }

        }
        catch (IOException e)
        {
        }
        finally
        {
          FileUtils.deleteQuietly(directory);
        }
      }
    });
    t.setDaemon(true);
    t.start();

    return pis;
  }

  public File writeToFile() throws IOException
  {
    final FhirExternalSystem system = (FhirExternalSystem) this.config.getSystem();

    try (FhirConnection connection = FhirConnectionFactory.get(system))
    {
      String name = SessionPredicate.generateId();

      File root = new File(new File(VaultProperties.getPath("vault.default"), "files"), name);
      root.mkdirs();

      Bundle bundle = this.generateBundle(connection);

      FhirContext ctx = FhirContext.forR4();
      IParser parser = ctx.newJsonParser();

      try
      {
        parser.encodeResourceToWriter(bundle, new FileWriter(new File(root, "bundle.json")));
      }
      catch (DataFormatException | IOException e)
      {
        throw new ProgrammingErrorException(e);
      }

      return root;
    }
    catch (Exception e)
    {
      throw new HttpError(e);
    }
  }

  public Bundle generateBundle(FhirConnection connection)
  {
    SortedSet<FhirSyncLevel> levels = this.config.getLevels();

    int expectedLevel = 0;

    Bundle bundle = new Bundle();

    for (FhirSyncLevel level : levels)
    {
      if (level.getLevel() != expectedLevel)
      {
        throw new ProgrammingErrorException("Unexpected level number [" + level.getLevel() + "].");
      }

      ListTypeVersion version = ListTypeVersion.get(level.getVersionId());

      FhirDataPopulator populator = FhirFactory.getPopulator(level.getImplementation());

      ListTypeFhirExporter exporter = new ListTypeFhirExporter(version, connection, populator, false);
      exporter.populateBundle(bundle);

      expectedLevel++;
    }
    return bundle;
  }

}
