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
  
  public RowValidationProblem(Throwable exception)
  {
    this.setExceptionJson(RowValidationProblem.serializeException(exception).toString());
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
    JSONObject err = new JSONObject(this.getExceptionJson());
    
    return this.getValidationProblemType() + "-" + this.getHistoryOid() + "-" + err.getString("message");
  }
  
  @Override
  public String getValidationProblemType()
  {
    return TYPE;
  }

  @Override
  public JSONObject toJSON()
  {
    JSONObject object = super.toJSON();
    
    object.put("exception", new JSONObject(this.getExceptionJson()));

    return object;
  }
  
  @Override
  public void apply()
  {
    if (this.getSeverity() == null || this.getSeverity() == 0)
    {
      this.setSeverity(100);
    }
    
    super.apply();
  }
}
