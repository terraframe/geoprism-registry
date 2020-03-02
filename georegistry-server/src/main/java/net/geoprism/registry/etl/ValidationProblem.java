package net.geoprism.registry.etl;

import org.json.JSONObject;

abstract public class ValidationProblem implements Comparable<ValidationProblem>
{
  protected abstract String getKey();
  
  protected abstract JSONObject toJSON();
  
  public int compareTo(ValidationProblem problem)
  {
    return this.getKey().compareTo(problem.getKey());
  }
}
