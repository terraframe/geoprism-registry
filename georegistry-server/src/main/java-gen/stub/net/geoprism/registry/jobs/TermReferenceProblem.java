package net.geoprism.registry.jobs;

import com.google.gson.JsonObject;

public class TermReferenceProblem extends TermReferenceProblemBase
{
public static final String TYPE = "TermReferenceProblem";
  
  private static final long serialVersionUID = -1226008655;
  
  public TermReferenceProblem()
  {
    super();
  }
  
  public TermReferenceProblem(String label, String parentCode, String typeCode, String attributeCode, String attributeLabel)
  {
    this.setLabel(label);
    this.setParentCode(parentCode);
    this.setObjectTypeCode(typeCode);
    this.setAttributeCode(attributeCode);
    this.setAttributeLabel(attributeLabel);
  }
  
  public String getValidationProblemType()
  {
    return TYPE;
  }
  
  @Override
  protected String buildKey()
  {
    return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + this.getObjectTypeCode() + "-" + this.getAttributeCode() + "-" + this.getLabel();
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    
    object.addProperty("importType", this.getImportType());
    object.addProperty("label", this.getLabel());
    object.addProperty("parentCode", this.getParentCode());
    object.addProperty("typeCode", this.getObjectTypeCode());
    object.addProperty("attributeCode", this.getAttributeCode());
    object.addProperty("attributeLabel", this.getAttributeLabel());

    return object;
  }
  
  @Override
  public void apply()
  {
    if (this.getSeverity() == null || this.getSeverity() == 0)
    {
      this.setSeverity(1);
    }
    
    super.apply();
  }
}
