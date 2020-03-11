package net.geoprism.registry.etl;

import org.json.JSONArray;
import org.json.JSONObject;

import net.geoprism.registry.model.ServerGeoObjectType;

public class ParentReferenceProblem extends ParentReferenceProblemBase
{
  public static final Integer DEFAULT_SEVERITY = 10;
  
  public static final String TYPE = "ParentReferenceProblem";
  
  private static final long serialVersionUID = 1693574723;
  
  public ParentReferenceProblem()
  {
    super();
  }
  
  public ParentReferenceProblem(String typeCode, String label, String parentCode, JSONArray context)
  {
    this.setTypeCode(typeCode);
    this.setLabel(label);
    this.setContext(context.toString());
    this.setParentCode(parentCode);
  }
  
  @Override
  protected String buildKey()
  {
    if (this.getParentCode() != null && this.getParentCode().length() > 0)
    {
      return this.getValidationProblemType() + this.getParentCode() + "-" + this.getLabel();
    }
    else
    {
      return this.getValidationProblemType() + this.getLabel();
    }
  }
  
  public String getValidationProblemType()
  {
    return TYPE;
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = super.toJSON();
    
    ServerGeoObjectType type = ServerGeoObjectType.get(this.getTypeCode());
    
    object.put("label", this.getLabel());
    object.put("type", this.getTypeCode());
    object.put("typeLabel", type.getLabel());
    object.put("context", this.getContext());

    if (this.getParentCode() != null && this.getParentCode().length() > 0)
    {
      object.put("parent", this.getParentCode());
    }

    return object;
  }
  
  @Override
  public void apply()
  {
    if (this.getSeverity() == null || this.getSeverity() == 0)
    {
      this.setSeverity(DEFAULT_SEVERITY);
    }
    
    super.apply();
  }
}
