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
package net.geoprism.registry.dhis2;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.RunwayException;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.registry.dhis2.DHIS2FeatureService.DHIS2SyncError;
import net.geoprism.registry.etl.export.ExportError;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.export.dhis2.NoParentException;

public class SynchronizationHistoryProgressScribe implements SynchronizationProgressMonitorIF
{
  private static Logger          logger                    = LoggerFactory.getLogger(SynchronizationHistoryProgressScribe.class);

  public static final Integer    UPDATE_PROGRESS_EVERY_NUM = 10;
  
  private ExportHistory          history;

  private int                    recordedErrors            = 0;

  private Long                   rowNumber                 = Long.valueOf(0);

  private Long                   exportedRecords           = Long.valueOf(0);

  private Map<String, ExportError> errorCache = new HashMap<String, ExportError>();

  public SynchronizationHistoryProgressScribe(ExportHistory history)
  {
    this.history = history;
    this.rowNumber = history.getWorkProgress() == null ? 0L : history.getWorkProgress();
    this.exportedRecords = history.getExportedRecords() == null ? 0L : history.getExportedRecords();
  }

  @Override
  public void setWorkTotal(Long workTotal)
  {
    this.history.appLock();
    this.history.setWorkTotal(workTotal);
    this.history.apply();

    logger.info("Starting synchronization with total work size [" + this.history.getWorkTotal() + "] and export stage [" + this.history.getStage().get(0) + "].");
  }

  @Override
  public void setRowNumber(Long newRowNum)
  {
    if (newRowNum % UPDATE_PROGRESS_EVERY_NUM == 0)
    {
      this.history.appLock();
      this.history.setWorkProgress(newRowNum);
      this.history.apply();
    }

    this.rowNumber = newRowNum;
  }

  @Override
  public void setExportedRecords(Long newExportedRecords)
  {
    if (newExportedRecords % UPDATE_PROGRESS_EVERY_NUM == 0)
    {
      this.history.appLock();
      this.history.setExportedRecords(newExportedRecords);
      this.history.apply();
    }

    this.exportedRecords = newExportedRecords;
  }

  @Override
  public Long getWorkTotal()
  {
    return this.history.getWorkTotal();
  }

  @Override
  public Long getRowNumber()
  {
    return this.rowNumber;
  }

  @Override
  public Long getWorkProgress()
  {
    return this.history.getWorkProgress();
  }

  @Override
  public Long getExportedRecords()
  {
    return this.exportedRecords;
  }

  @Override
  public void recordError(String errorMessage, int errorCode, String responseJson, String submittedJson, long rowNum, String goCode, ErrorType type)
  {
    ExportError ee = new ExportError();
    ee.setErrorType(type.name());
    ee.setResponseJson(responseJson);
    ee.setSubmittedJson(submittedJson);
    ee.setRowIndex(rowNum);
    ee.setErrorMessage(errorMessage);
    ee.setErrorCode(errorCode);
    ee.setCode(goCode);
    ee.setHistory(history);
    ee.apply();

    this.recordedErrors++;
  }
  
  public void addAffectedRow(ExportError exportError, long rowNum, String geoObjectCode)
  {
    exportError.appLock();
    
    // Add row number to list
    String sRows = exportError.getAffectedRows();

    if (sRows.length() > 0)
    {
      SortedSet<Long> lRows = new TreeSet<Long>();

      for (String row : StringUtils.split(sRows, ","))
      {
        lRows.add(Long.valueOf(row));
      }

      lRows.add(Long.valueOf(rowNum));

      sRows = StringUtils.join(lRows, ",");
    }
    else
    {
      sRows = String.valueOf(rowNum);
    }

    exportError.setAffectedRows(sRows);
    
    
    // Add code to codes
    String sCodes = exportError.getCode();
    
    if (sCodes.length() > 0)
    {
      SortedSet<String> lCodes = new TreeSet<String>();

      for (String code : StringUtils.split(sCodes, ","))
      {
        lCodes.add(code);
      }

      lCodes.add(geoObjectCode);

      sCodes = StringUtils.join(lCodes, ",");
    }
    else
    {
      sCodes = geoObjectCode;
    }
    
    exportError.setCode(sCodes);
    
    exportError.apply();
  }
  
  @Override
  public void recordError(DHIS2SyncError ee, ErrorType type)
  {
    DHIS2Response resp = ee.response;
    Throwable ex = ee.error;
    String geoObjectCode = ee.geoObjectCode;
    
    ExportError exportError = new ExportError();
    
    exportError.setErrorType(type.name());

    if (ee.submittedJson != null)
    {
      exportError.setSubmittedJson(ee.submittedJson);
    }
    
    if (resp != null)
    {
      if (resp.getResponse() != null && resp.getResponse().length() > 0)
      {
        exportError.setResponseJson(resp.getResponse());
        
        exportError.setErrorMessage(resp.getMessage());
      }
      
      exportError.setErrorCode(resp.getStatusCode());
    }
    
    exportError.setCode(geoObjectCode);
    
    if (ex != null)
    {
      exportError.setErrorJson(JobHistory.exceptionToJson(ex).toString());
      
      if (exportError.getErrorMessage() == null || exportError.getErrorMessage().length() == 0)
      {
        exportError.setErrorMessage(RunwayException.localizeThrowable(ex, Session.getCurrentLocale()));
      }
    }
    
    // Merge with an existing exception if the message is the same
    if (this.errorCache.containsKey(exportError.getErrorMessage()))
    {
      exportError = this.errorCache.get(exportError.getErrorMessage());
      
      this.addAffectedRow(exportError, ee.rowIndex, ee.geoObjectCode);
      
      return;
    }
    else
    {
      this.errorCache.put(exportError.getErrorMessage(), exportError);
    }
    
    if (ee.rowIndex != null && ee.rowIndex >= 0)
    {
      exportError.setRowIndex(ee.rowIndex);
      exportError.setAffectedRows(String.valueOf(ee.rowIndex));
    }
    
    exportError.setHistory(history);
    
    exportError.apply();
  }

  public int getRecordedErrorCount()
  {
    return this.recordedErrors;
  }

  @Override
  public void finalize()
  {
    this.history.appLock();
    this.history.setExportedRecords(this.exportedRecords);
    this.history.clearStage();
    this.history.addStage(ExportStage.COMPLETE);
    this.history.setWorkProgress(this.rowNumber);
    this.history.apply();
  }
}