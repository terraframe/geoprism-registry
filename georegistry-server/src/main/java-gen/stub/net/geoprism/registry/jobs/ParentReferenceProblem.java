package net.geoprism.registry.jobs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
  
  public ParentReferenceProblem(String typeCode, String label, String parentCode, String context)
  {
    this.setTypeCode(typeCode);
    this.setLabel(label);
    this.setContext(context);
    this.setParentCode(parentCode);
  }
  
  @Override
  protected String buildKey()
  {
    if (this.getParentCode() != null && this.getParentCode().length() > 0)
    {
      return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + this.getParentCode() + "-" + this.getLabel();
    }
    else
    {
      return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + this.getLabel();
    }
  }
  
  public String getValidationProblemType()
  {
    return TYPE;
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    
    ServerGeoObjectType type = ServerGeoObjectType.get(this.getTypeCode());
    
    object.addProperty("label", this.getLabel());
    object.addProperty("typeCode", this.getTypeCode());
    object.add("typeLabel", type.getLabel().toJSON());
    object.add("context", JsonParser.parseString(this.getContext()));

    if (this.getParentCode() != null && this.getParentCode().length() > 0)
    {
      object.addProperty("parent", this.getParentCode());
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
