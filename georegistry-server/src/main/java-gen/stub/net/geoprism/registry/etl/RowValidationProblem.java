package net.geoprism.registry.etl;

import org.json.JSONObject;

import com.runwaysdk.business.SmartException;
import com.runwaysdk.system.scheduler.ExecutableJob;

public class RowValidationProblem extends RowValidationProblemBase
{
  public static final String TYPE = "RowValidationProblem";
  
  private static final long serialVersionUID = -1193007210;
  
  public RowValidationProblem()
  {
    super();
  }
  
  public RowValidationProblem(Throwable exception, Long rowNum)
  {
    this.setExceptionJson(RowValidationProblem.serializeException(exception).toString());
    this.setRowNum(rowNum);
  }
  
  public static JSONObject serializeException(Throwable exception)
  {
    JSONObject joException = new JSONObject();
    
    if (exception instanceof SmartException)
    {
      joException.put("type", ((SmartException)exception).getType());
    }
    else
    {
      joException.put("type", exception.getClass().getName());
    }
    
    String message = ExecutableJob.getMessageFromException(exception);
    joException.put("message", message);
    
    return joException;
  }

  @Override
  public String buildKey()
  {
    return String.valueOf(this.getRowNum());
  }
  
  @Override
  public String getValidationProblemType()
  {
    return TYPE;
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = new JSONObject();
    
    object.put("rowNum", this.getRowNum());
    object.put("exception", this.getExceptionJson());

    return object;
  }
}
