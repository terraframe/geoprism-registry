package net.geoprism.registry.etl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public abstract class ValidationProblem extends ValidationProblemBase implements Comparable<ValidationProblem>
{
  private static final long serialVersionUID = 681333878;
  
  public static enum ValidationResolution
  {
    IGNORE,
    SYNONYM,
    CREATE,
    UNRESOLVED
  }
  
  public ValidationProblem()
  {
    super();
  }
  
  abstract public String getValidationProblemType();
  
  @Override
  public String getKey()
  {
    String key = super.getKey();
    
    if (key == null || key.length() == 0)
    {
      return this.buildKey();
    }
    else
    {
      return key;
    }
  }
  
  public JSONObject toJSON()
  {
    JSONObject json = new JSONObject();
    
    json.put("affectedRows", this.getAffectedRows());
    json.put("resolution", this.getResolution());
    json.put("historyId", this.getHistory());
    json.put("type", this.getValidationProblemType());
    json.put("id", this.getOid());
    
    return json;
  }
  
  public void addAffectedRowNumber(long rowNum)
  {
    String sRows = this.getAffectedRows();
    
    if (sRows.length() > 0)
    {
      ArrayList<String> lRows = new ArrayList<String>();
      
      for (String row : StringUtils.split(sRows, ","))
      {
        lRows.add(row);
      }
      
      lRows.add(String.valueOf(rowNum));
      
      sRows = StringUtils.join(lRows, ",");
    }
    else
    {
      sRows = String.valueOf(rowNum);
    }
    
    this.setAffectedRows(sRows);
  }
  
  @Override
  public int compareTo(ValidationProblem problem)
  {
    return this.getKey().compareTo(problem.getKey());
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (!(obj instanceof ValidationProblem))
    {
      return false;
    }
    
    ValidationProblem vp = (ValidationProblem) obj;
    
    return vp.getKey().equals(this.getKey());
  }
}
