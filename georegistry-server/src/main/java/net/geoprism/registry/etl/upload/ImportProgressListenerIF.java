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

import net.geoprism.registry.etl.ValidationProblem;

public interface ImportProgressListenerIF
{
  public void setWorkTotal(Long workTotal);

  public Long getWorkTotal();
  
  public void setCompletedRow(Long rowNumber);

  public Long getRowNumber();

  public void setImportedRecords(Long newImportedRecords);

  public Long getImportedRecords();
  
  public Long getWorkProgress();
  
  public Long getImportedRecordProgress();

  public void recordError(Throwable ex, String objectJson, String objectType, long rowNum);

  public boolean hasValidationProblems();

  public void addReferenceProblem(ValidationProblem problem);

  public void addRowValidationProblem(ValidationProblem problem);

  public void applyValidationProblems();
  
  public void finalizeImport();

  public void incrementImportedRecords();

}
