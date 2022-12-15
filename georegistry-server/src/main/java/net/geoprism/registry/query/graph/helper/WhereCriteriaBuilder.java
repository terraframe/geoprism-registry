/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
