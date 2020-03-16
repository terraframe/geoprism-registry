package net.geoprism.registry.etl;

import org.json.JSONObject;

public class TermReferenceProblem extends TermReferenceProblemBase
{
  public static final String TYPE = "TermReferenceProblem";
  
  private static final long serialVersionUID = -1226008655;
  
  public TermReferenceProblem()
  {
    super();
  }
  
  public TermReferenceProblem(String label, String parentCode, String mdAttributeId, String attributeCode, String attributeLabel)
  {
    this.setLabel(label);
    this.setMdAttributeId(mdAttributeId);
    this.setParentCode(parentCode);
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
    return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + this.getMdAttributeOid() + "-" + this.getLabel();
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = super.toJSON();
    
    object.put("label", this.getLabel());
    object.put("parentCode", this.getParentCode());
    object.put("mdAttributeId", this.getMdAttributeOid());
    object.put("attributeCode", this.getAttributeCode());
    object.put("attributeLabel", this.getAttributeLabel());

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
