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
