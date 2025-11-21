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
package net.geoprism.graph;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.JsonObject;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class RDFExportJob extends RDFExportJobBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1536685436;

  public RDFExportJob()
  {
    super();
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    ImportHistory history = (ImportHistory) executionContext.getJobHistoryRecord().getChild();

    LabeledPropertyGraphTypeVersion version = this.getVersion();
    LabeledPropertyGraphType type = version.getGraphType();
    GeometryExportType geomType = GeometryExportType.valueOf(this.getGeometryExportType());

    JsonObject config = new JsonObject();
    config.addProperty(ImportConfiguration.FILE_NAME, "Export RDF from LPG [" + type.getDisplayLabel().getValue() + "] for [" + GeoRegistryUtil.formatDate(version.getForDate(), false) + "]");
    config.addProperty(ImportConfiguration.OBJECT_TYPE, "RDF-LPG");

    history.appLock();
    history.setConfigJson(config.toString());
    history.apply();

    String progressId = history.getOid();

    LabeledPropertyGraphRDFExportBusinessServiceIF service = ServiceFactory.getBean(LabeledPropertyGraphRDFExportBusinessServiceIF.class);

    try (var tmp = new CloseableFile(Files.createTempFile(progressId, ".ttl").toFile()))
    {
      try (OutputStream os = new FileOutputStream(tmp))
      {
        try (ZipOutputStream zos = new ZipOutputStream(os))
        {
          ZipEntry entry = new ZipEntry(progressId + ".ttl");
          zos.putNextEntry(entry);

          service.export(history, version, geomType, zos);
        }
      }

      try (FileInputStream is = new FileInputStream(tmp))
      {
        var vf = VaultFile.createAndApply(progressId + ".zip", is);

        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT_RESOLVE);
        history.setImportFile(vf);
        history.apply();
        executionContext.setStatus(AllJobStatus.FEEDBACK);
      }
    }
  }

  @Override
  public JobHistory createNewHistory()
  {
    ImportHistory history = new ImportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ImportStage.VALIDATE);
    history.setWorkProgress(0L);
    history.setCompletedRowsJson("");
    history.setImportedRecords(0L);
    history.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));

    return history;
  }

  public static ImportHistory runNewJob(String versionId, GeometryExportType geomExportType)
  {
    RDFExportJob job = new RDFExportJob();
    job.setGeometryExportType(geomExportType.name());
    job.setVersionId(versionId);
    job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    job.apply();

    return (ImportHistory) job.start();
  }

}
