/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.etl.export.fhir;

import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.etl.ExportJobHasErrors;
import net.geoprism.registry.etl.FhirSyncConfig;
import net.geoprism.registry.etl.FhirSyncLevel;
import net.geoprism.registry.etl.export.ExportErrorQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class FhirSynchronizationManager
{
  private FhirSyncConfig config;

  private ExportHistory  history;

  private Date           date;

  public FhirSynchronizationManager(FhirSyncConfig config, ExportHistory history)
  {
    this.config = config;
    this.date = todaysDate();
    this.history = history;
  }

  private Date todaysDate()
  {
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    cal.setTime(new Date());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    return cal.getTime();
  }

  public void synchronize()
  {
    final FhirExternalSystem system = (FhirExternalSystem) this.config.getSystem();

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

      MasterListVersion version = MasterListVersion.get(level.getVersionId());

      FhirDataPopulator populator = FhirExportFactory.getPopulator(version);

      MasterListFhirExporter exporter = new MasterListFhirExporter(version, system, populator);
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

  // private void recordExportError(FhirSyncError ee, ExportHistory history)
  // {
  // FhirImportResponse resp = ee.response;
  // Throwable ex = ee.error;
  // String geoObjectCode = ee.geoObjectCode;
  //
  // ExportError exportError = new ExportError();
  //
  // if (ee.submittedJson != null)
  // {
  // exportError.setSubmittedJson(ee.submittedJson);
  // }
  //
  // if (resp != null)
  // {
  // if (resp.getResponse() != null && resp.getResponse().length() > 0)
  // {
  // exportError.setResponseJson(resp.getResponse());
  //
  // if (resp.hasErrorReports())
  // {
  // List<ErrorReport> reports = resp.getErrorReports();
  //
  // ErrorReport report = reports.get(0);
  //
  // exportError.setErrorMessage(report.getMessage());
  // }
  // }
  //
  // exportError.setErrorCode(resp.getStatusCode());
  // }
  //
  // exportError.setCode(geoObjectCode);
  //
  // if (ex != null)
  // {
  // exportError.setErrorJson(JobHistory.exceptionToJson(ex).toString());
  // }
  //
  // exportError.setRowIndex(ee.rowIndex);
  //
  // exportError.setHistory(history);
  //
  // exportError.apply();
  // }
}
