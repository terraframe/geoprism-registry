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
package net.geoprism.registry.etl;

import net.geoprism.registry.etl.upload.ImportProgressListenerIF;

public class NullImportProgressListener implements ImportProgressListenerIF
{

  @Override
  public void setWorkTotal(Long workTotal)
  {

  }

  @Override
  public Long getWorkTotal()
  {
    return 0L;
  }

  @Override
  public void setWorkProgress(Long newWorkProgress)
  {

  }

  @Override
  public Long getWorkProgress()
  {

    return 0L;
  }

  @Override
  public Long getRawWorkProgress()
  {

    return 0L;
  }

  @Override
  public void setImportedRecords(Long newImportedRecords)
  {

  }

  @Override
  public Long getImportedRecords()
  {

    return 0L;
  }

  @Override
  public void recordError(Throwable ex, String objectJson, String objectType, long rowNum)
  {

  }

  @Override
  public boolean hasValidationProblems()
  {

    return false;
  }

  @Override
  public void addReferenceProblem(ValidationProblem problem)
  {

  }

  @Override
  public void addRowValidationProblem(ValidationProblem problem)
  {

  }

  @Override
  public void applyValidationProblems()
  {

  }

  @Override
  public Long getRawImportedRecords()
  {
    return 0L;
  }

}
