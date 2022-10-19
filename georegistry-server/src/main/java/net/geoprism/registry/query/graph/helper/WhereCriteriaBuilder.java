package net.geoprism.registry.query.graph.helper;

public class WhereCriteriaBuilder
{
  private Criteria first;
  
  private Criteria last;
  
  public WhereCriteriaBuilder(Criteria... criteria)
  {
    if (criteria != null && criteria.length > 0)
    {
        this.first = criteria[0];
        this.last = criteria[criteria.length - 1];
    }
  }
  
  public Criteria AND(String sql)
  {
    if (this.last == null)
    {
      this.last = new Criteria(sql);
      this.first = this.last;
      return this.last;
    }
    
    this.last = this.last.AND(sql);
    
    return this.last;
  }
  
  public Criteria OR(String sql)
  {
    if (this.last == null)
    {
      this.last = new Criteria(sql);
      this.first = this.last;
      return this.last;
    }
    
    this.last = this.last.OR(sql);
    
    return this.last;
  }

  public String getSQL()
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append(" WHERE ");
    
    Criteria node = this.first;
    
    while (node != null)
    {
      sb.append(node.getSQL());
      
      Join join = node.getRight();
      
      if (join != null)
      {
        sb.append(" " + join.getType().name() + " ");
        
        node = join.getRight();
      }
      else
      {
        node = null;
      }
    }
    
    return sb.toString() + " ";
  }
}
