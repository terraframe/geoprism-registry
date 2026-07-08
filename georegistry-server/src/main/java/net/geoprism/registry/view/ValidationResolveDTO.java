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
