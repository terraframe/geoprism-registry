package net.geoprism.registry.view.action;

import java.util.Date;

import com.google.gson.annotations.JsonAdapter;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;

import net.geoprism.registry.RegistryJsonTimeFormatter;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateValueOverTimeView
{
  protected String oid;
  
  public enum UpdateActionType {
    DELETE,
    UPDATE,
    CREATE
  }
  
  protected UpdateActionType action;
  
  protected Object oldValue;
  
  protected Object newValue;
  
  @JsonAdapter(RegistryJsonTimeFormatter.class)
  protected Date newStartDate;
  
  @JsonAdapter(RegistryJsonTimeFormatter.class)
  protected Date newEndDate;
  
  @JsonAdapter(RegistryJsonTimeFormatter.class)
  protected Date oldStartDate;
  
  @JsonAdapter(RegistryJsonTimeFormatter.class)
  protected Date oldEndDate;
  
  public void execute(UpdateChangeOverTimeAttributeView cotView, VertexServerGeoObject go)
  {
    ValueOverTimeCollection votc = go.getValuesOverTime(cotView.getAttributeName());
    
    if (this.action.equals(UpdateActionType.DELETE))
    {
      ValueOverTime vot = votc.getValueByOid(this.getOid());
      
      if (vot == null)
      {
        // TODO throw an exception?
      }
      
      votc.remove(vot);
    }
    else if (this.action.equals(UpdateActionType.UPDATE))
    {
      ValueOverTime vot = votc.getValueByOid(this.getOid());
      
      if (newStartDate != null)
      {
        vot.setStartDate(newStartDate);
      }
      
      if (newEndDate != null)
      {
        vot.setEndDate(newEndDate);
      }
      
      if (newValue != null)
      {
        vot.setValue(newValue);
      }
    }
    else if (this.action.equals(UpdateActionType.CREATE))
    {
      ValueOverTime vot = votc.getValueOverTime(this.newStartDate, this.newEndDate);
      
      if (vot != null)
      {
        // TODO throw an exception?
      }
      
      go.setValue(cotView.getAttributeName(), newValue, newStartDate, newEndDate);
    }
    else
    {
      throw new UnsupportedOperationException("Unsupported action type [" + this.action + "].");
    }
  }

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public UpdateActionType getAction()
  {
    return action;
  }

  public void setAction(UpdateActionType action)
  {
    this.action = action;
  }

  public Object getOldValue()
  {
    return oldValue;
  }

  public void setOldValue(Object oldValue)
  {
    this.oldValue = oldValue;
  }

  public Object getNewValue()
  {
    return newValue;
  }

  public void setNewValue(Object newValue)
  {
    this.newValue = newValue;
  }

  public Date getNewStartDate()
  {
    return newStartDate;
  }

  public void setNewStartDate(Date newStartDate)
  {
    this.newStartDate = newStartDate;
  }

  public Date getNewEndDate()
  {
    return newEndDate;
  }

  public void setNewEndDate(Date newEndDate)
  {
    this.newEndDate = newEndDate;
  }

  public Date getOldStartDate()
  {
    return oldStartDate;
  }

  public void setOldStartDate(Date oldStartDate)
  {
    this.oldStartDate = oldStartDate;
  }

  public Date getOldEndDate()
  {
    return oldEndDate;
  }

  public void setOldEndDate(Date oldEndDate)
  {
    this.oldEndDate = oldEndDate;
  }
}
