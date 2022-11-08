package net.geoprism.registry.query.graph.helper;

import net.geoprism.registry.query.graph.helper.Join.JoinType;

public class Criteria
{
  private String sql;
  
  private Join left;
  
  private Join right;
  
  public Criteria(String sql)
  {
    this.sql = sql;
  }
  
  public Criteria AND(String sql)
  {
    Criteria criteria = new Criteria(sql);
    
    Join join = new Join(JoinType.AND, this, criteria);
    
    this.right = join;
    criteria.left = join;
    
    return criteria;
  }
  
  public Criteria OR(String sql)
  {
    Criteria criteria = new Criteria(sql);
    
    Join join = new Join(JoinType.OR, this, criteria);
    
    this.right = join;
    criteria.left = join;
    
    return criteria;
  }
  
  public String getSQL()
  {
    return sql;
  }

  public void setSQL(String sql)
  {
    this.sql = sql;
  }

  public Join getLeft()
  {
    return left;
  }

  public void setLeft(Join left)
  {
    this.left = left;
  }

  public Join getRight()
  {
    return right;
  }

  public void setRight(Join right)
  {
    this.right = right;
  }
}
