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

public class Join
{
  public static enum JoinType {
    AND, OR
  }
  
  private JoinType type;
  
  private Criteria left;
  
  private Criteria right;
  
  public Join(JoinType type, Criteria left, Criteria right)
  {
    this.type = type;
    this.left = left;
    this.right = right;
  }

  public JoinType getType()
  {
    return type;
  }

  public void setType(JoinType type)
  {
    this.type = type;
  }

  public Criteria getLeft()
  {
    return left;
  }

  public void setLeft(Criteria left)
  {
    this.left = left;
  }

  public Criteria getRight()
  {
    return right;
  }

  public void setRight(Criteria right)
  {
    this.right = right;
  }
}
