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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.jobs.ValidationProblem.ValidationResolution;

public class ValidationResolveDTO
{
  @NotNull
  private ValidationResolution resolution;

  @NotBlank
  private String               validationProblemId;

  private String               label;

  // Type code for geo object issues
  private String               typeCode;

  // Code for geo object issues
  private String               code;

  // Id for classifier issues
  private String               classifierId;

  public ValidationResolution getResolution()
  {
    return resolution;
  }

  public void setResolution(ValidationResolution resolution)
  {
    this.resolution = resolution;
  }

  public String getValidationProblemId()
  {
    return validationProblemId;
  }

  public void setValidationProblemId(String validationProblemId)
  {
    this.validationProblemId = validationProblemId;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getTypeCode()
  {
    return typeCode;
  }

  public void setTypeCode(String typeCode)
  {
    this.typeCode = typeCode;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getClassifierId()
  {
    return classifierId;
  }

  public void setClassifierId(String classifierId)
  {
    this.classifierId = classifierId;
  }

}
