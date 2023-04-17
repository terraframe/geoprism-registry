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
package net.geoprism.registry.etl.upload;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.SmartException;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.etl.ImportError;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ValidationProblem;

public class ImportHistoryProgressScribe implements ImportProgressListenerIF
{
  private static Logger          logger                    = LoggerFactory.getLogger(ImportHistoryProgressScribe.class);

  public static final Integer    UPDATE_PROGRESS_EVERY_NUM = 10;

  private ImportHistory          history;

  private int                    recordedErrors            = 0;

  private Long                   rowNumber                 = Long.valueOf(0);

  private Long                   importedRecords           = Long.valueOf(0);

  private Set<ValidationProblem> referenceProblems         = new TreeSet<ValidationProblem>();

  private Set<ValidationProblem> rowValidationProblems     = new TreeSet<ValidationProblem>();

  public ImportHistoryProgressScribe(ImportHistory history)
  {
    this.history = history;
    this.rowNumber = history.getWorkProgress();
    this.importedRecords = history.getImportedRecords();
  }

  @Override
  public void setWorkTotal(Long workTotal)
  {
    this.history.appLock();
    this.history.setWorkTotal(workTotal);
    this.history.apply();

    logger.info("Starting import with total work size [" + this.history.getWorkTotal() + "] and import stage [" + this.history.getStage().get(0) + "].");
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
  public void setImportedRecords(Long newImportedRecords)
  {
    if (newImportedRecords % UPDATE_PROGRESS_EVERY_NUM == 0)
    {
      this.history.appLock();
      this.history.setImportedRecords(newImportedRecords);
      this.history.apply();
    }

    this.importedRecords = newImportedRecords;
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
  public Long getImportedRecordProgress()
  {
    return this.history.getImportedRecords();
  }

  @Override
  public Long getImportedRecords()
  {
    return this.importedRecords;
  }

  @Override
  public void recordError(Throwable ex, String objectJson, String objectType, long rowNum)
  {
    ImportError error = new ImportError();
    error.setHistory(this.history);
    error.setErrorJson(JobHistory.exceptionToJson(ex).toString());
    error.setObjectJson(objectJson);
    error.setObjectType(objectType);
    error.setRowIndex(rowNum);
    error.apply();

    this.history.appLock();
    this.history.setErrorCount(this.history.getErrorCount() + 1);
    this.history.apply();
    
    if (!(ex instanceof SmartException || ex instanceof SmartExceptionDTO))
    {
      logger.error("Unlocalized exception thrown during import on row [" + rowNum + "] with objectType [" + objectType + "] and objectJson [" + objectJson + "].", ex);
    }

    this.recordedErrors++;
  }

  public int getRecordedErrorCount()
  {
    return this.recordedErrors;
  }

  @Override
  public boolean hasValidationProblems()
  {
    return this.rowValidationProblems.size() > 0 || this.referenceProblems.size() > 0;
  }

  @Override
  public void addReferenceProblem(ValidationProblem problem)
  {
    Iterator<ValidationProblem> it = this.referenceProblems.iterator();

    while (it.hasNext())
    {
      ValidationProblem vp = it.next();

      if (vp.getKey().equals(problem.getKey()))
      {
        vp.addAffectedRowNumber(Long.parseLong(problem.getAffectedRows()));
        return;
      }
    }

    this.referenceProblems.add(problem);
  }

  @Override
  public void addRowValidationProblem(ValidationProblem problem)
  {
    Iterator<ValidationProblem> it = this.rowValidationProblems.iterator();

    while (it.hasNext())
    {
      ValidationProblem vp = it.next();

      if (vp.getKey().equals(problem.getKey()))
      {
        vp.addAffectedRowNumber(Long.parseLong(problem.getAffectedRows()));
        return;
      }
    }

    this.rowValidationProblems.add(problem);
  }

  @Override
  public void applyValidationProblems()
  {
    for (ValidationProblem problem : this.referenceProblems)
    {
      problem.setHistory(this.history);
      problem.apply();
    }
    for (ValidationProblem problem : this.rowValidationProblems)
    {
      problem.setHistory(this.history);
      problem.apply();
    }
  }

  @Override
  public void finalizeImport()
  {
    this.history.appLock();
    this.history.setImportedRecords(this.importedRecords);
    this.history.setWorkProgress(this.rowNumber);
    this.history.apply();
  }
}