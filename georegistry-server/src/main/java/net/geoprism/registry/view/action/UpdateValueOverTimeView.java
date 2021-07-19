package net.geoprism.registry.view.action;

import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeNumericType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.wololo.jts2geojson.GeoJSONReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.graph.GraphObjectDAO;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.localization.LocalizationFacade;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.RegistryJsonTimeFormatter;
import net.geoprism.registry.action.ExecuteOutOfDateChangeRequestException;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.ServerGeoObjectType;
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
  
  protected JsonElement oldValue;
  
  protected JsonElement newValue;
  
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
    String attributeName = this.getAttributeName(cotView, go);
    
    ValueOverTimeCollection votc = go.getValuesOverTime(attributeName);
    
    if (this.action.equals(UpdateActionType.DELETE))
    {
      ValueOverTime vot = votc.getValueByOid(this.getOid());
      
      if (vot == null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      votc.remove(vot);
    }
    else if (this.action.equals(UpdateActionType.UPDATE))
    {
      ValueOverTime vot = votc.getValueByOid(this.getOid());
      
      if (vot == null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      if (newStartDate != null)
      {
        vot.setStartDate(newStartDate);
      }
      
      if (newEndDate != null)
      {
        vot.setEndDate(newEndDate);
      }
      
      this.persistValue(vot, cotView, go);
    }
    else if (this.action.equals(UpdateActionType.CREATE))
    {
      ValueOverTime vot = votc.getValueOverTime(this.newStartDate, this.newEndDate);
      
      if (vot != null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      this.persistValue(null, cotView, go);
    }
    else
    {
      throw new UnsupportedOperationException("Unsupported action type [" + this.action + "].");
    }
  }
  
  private String getAttributeName(UpdateChangeOverTimeAttributeView cotView, VertexServerGeoObject go)
  {
    String attributeName = cotView.getAttributeName();
    
    if (attributeName.equals("geometry"))
    {
      attributeName = go.getGeometryAttributeName();
    }
    
    return attributeName;
  }
  
  private void persistValue(ValueOverTime vot, UpdateChangeOverTimeAttributeView cotView, VertexServerGeoObject go)
  {
    if (cotView.getAttributeName().equals("geometry"))
    {
      Geometry convertedValue = null;
      
      if (this.newValue != null && ! this.newValue.isJsonNull())
      {
        GeoJSONReader reader = new GeoJSONReader();
        convertedValue = reader.read(this.newValue.toString());
      }
      
      if (vot != null)
      {
        vot.setValue(convertedValue);
      }
      else
      {
        go.setGeometry(convertedValue, this.newStartDate, this.newEndDate);
      }
    }
    else
    {
      ServerGeoObjectType type = go.getType();
      
      AttributeType attype = type.getAttribute(cotView.getAttributeName()).get();
      
      if (attype instanceof AttributeLocalType)
      {
        final LocalizedValue convertedValue = LocalizedValue.fromJSON(this.newValue.getAsJsonObject());
        final Set<Locale> locales = LocalizationFacade.getInstalledLocales();
        
        if (vot != null)
        {
          GraphObjectDAO votEmbeddedValue = (GraphObjectDAO) vot.getValue();
          
          votEmbeddedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, convertedValue.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));
  
          for (Locale locale : locales)
          {
            if (convertedValue.contains(locale))
            {
              votEmbeddedValue.setValue(locale.toString(), convertedValue.getValue(locale));
            }
          }
        }
        else
        {
          go.setDisplayLabel(convertedValue, this.newStartDate, this.newEndDate);
        }
      }
      else
      {
        Object convertedValue = null;
        
        if (this.newValue != null && ! this.newValue.isJsonNull())
        {
          if (attype instanceof AttributeDateType)
          {
            long epoch = this.newValue.getAsLong();
            
            convertedValue = new Date(epoch);
          }
          else if (attype instanceof AttributeTermType)
          {
            JsonArray ja = this.newValue.getAsJsonArray();
            
            if (ja.size() > 0)
            {
              String code = ja.get(0).getAsString();
              
              Term root = ( (AttributeTermType) attype ).getRootTerm();
              String parent = TermConverter.buildClassifierKeyFromTermCode(root.getCode());

              String classifierKey = Classifier.buildKey(parent, code);
              Classifier classifier = Classifier.getByKey(classifierKey);
              
              convertedValue = classifier.getOid();
            }
          }
          else if (attype instanceof AttributeBooleanType)
          {
            convertedValue = this.newValue.getAsBoolean();
          }
          else if (attype instanceof AttributeBooleanType)
          {
            convertedValue = this.newValue.getAsBoolean();
          }
          else if (attype instanceof AttributeNumericType)
          {
            convertedValue = this.newValue.getAsNumber();
          }
          else
          {
            convertedValue = this.newValue.getAsString();
          }
        }
        
        if (vot != null)
        {
          vot.setValue(convertedValue);
        }
        else
        {
          go.setValue(attype.getName(), convertedValue, this.newStartDate, this.newEndDate);
        }
      }
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

  public JsonElement getOldValue()
  {
    return oldValue;
  }

  public void setOldValue(JsonElement oldValue)
  {
    this.oldValue = oldValue;
  }

  public JsonElement getNewValue()
  {
    return newValue;
  }

  public void setNewValue(JsonElement newValue)
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
