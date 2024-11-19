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
package net.geoprism.registry.cache;

public class LinkedListNode<T>
{
  T                 element;

  LinkedListNode<T> prev;

  LinkedListNode<T> next;

  public LinkedListNode(T element)
  {
    this.element = element;
    this.prev = null;
    this.next = null;
  }

  public T getElement()
  {
    return element;
  }

  public void setElement(T element)
  {
    this.element = element;
  }

  public void setNext(LinkedListNode<T> next)
  {
    this.next = next;
  }

  public void setPrev(LinkedListNode<T> prev)
  {
    this.prev = prev;
  }

  public LinkedListNode<T> getNext()
  {
    return next;
  }

  public LinkedListNode<T> getPrev()
  {
    return prev;
  }

  public boolean isEmpty()
  {
    return element == null;
  }
}
