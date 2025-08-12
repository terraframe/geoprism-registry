package net.geoprism.registry.jobs;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

import net.geoprism.registry.view.JsonSerializable;

public abstract class ValidationProblem extends ValidationProblemBase implements Comparable<ValidationProblem>, JsonSerializable
{
  private static final long serialVersionUID = 681333878;
  
  public static enum ValidationResolution {
    IGNORE, SYNONYM, CREATE, UNRESOLVED
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

  public JsonObject toJSON()
  {
    JsonObject json = new JsonObject();

    json.addProperty("affectedRows", this.getAffectedRows());
    json.addProperty("resolution", this.getResolution());
    json.addProperty("historyId", this.getHistory().getOid());
    json.addProperty("type", this.getValidationProblemType());
    json.addProperty("id", this.getOid());

    return json;
  }

  public void addAffectedRowNumber(long rowNum)
  {
    String sRows = this.getAffectedRows();

    if (sRows.length() > 0)
    {
      SortedSet<Long> lRows = new TreeSet<Long>();

      for (String row : StringUtils.split(sRows, ","))
      {
        lRows.add(Long.valueOf(row));
      }

      lRows.add(Long.valueOf(rowNum));

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
    if (! ( obj instanceof ValidationProblem ))
    {
      return false;
    }

    ValidationProblem vp = (ValidationProblem) obj;

    return vp.getKey().equals(this.getKey());
  }
}
