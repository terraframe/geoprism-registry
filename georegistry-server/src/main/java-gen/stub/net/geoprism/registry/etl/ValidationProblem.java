package net.geoprism.registry.etl;

import org.json.JSONObject;

public abstract class ValidationProblem extends ValidationProblemBase implements Comparable<ValidationProblem>
{
  private static final long serialVersionUID = 681333878;
  
  public ValidationProblem()
  {
    super();
  }
  
  abstract public String getValidationProblemType();
  
  public JSONObject toJSON()
  {
    JSONObject json = new JSONObject();
    
    json.put("resolution", this.getResolution());
    json.put("historyId", this.getHistory());
    json.put("type", this.getValidationProblemType());
    
    return json;
  }
  
  public int compareTo(ValidationProblem problem)
  {
    return this.getKey().compareTo(problem.getKey());
  }
}
