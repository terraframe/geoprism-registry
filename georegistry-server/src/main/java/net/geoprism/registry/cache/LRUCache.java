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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCache<K, V> implements Cache<K, V>
{
  private int                                        size;

  private Map<K, LinkedListNode<CacheElement<K, V>>> linkedListNodeMap;

  private DoublyLinkedList<CacheElement<K, V>>       doublyLinkedList;

  private final ReentrantReadWriteLock               lock;

  public LRUCache(int size)
  {
    this.size = size;
    this.lock = new ReentrantReadWriteLock();
    this.linkedListNodeMap = new ConcurrentHashMap<>(size);
    this.doublyLinkedList = new DoublyLinkedList<CacheElement<K, V>>();
  }

  @Override
  public int size()
  {
    return this.linkedListNodeMap.size();
  }

  @Override
  public boolean isEmpty()
  {
    return this.linkedListNodeMap.isEmpty();
  }

  @Override
  public void clear()
  {
    this.lock.writeLock().lock();
    try
    {
      this.linkedListNodeMap = new ConcurrentHashMap<>(size);
      this.doublyLinkedList = new DoublyLinkedList<CacheElement<K, V>>();
    }
    finally
    {
      this.lock.writeLock().unlock();
    }
  }

  @Override
  public boolean put(K key, V value)
  {
    this.lock.writeLock().lock();
    try
    {
      CacheElement<K, V> item = new CacheElement<K, V>(key, value);
      LinkedListNode<CacheElement<K, V>> newNode;

      if (this.linkedListNodeMap.containsKey(key))
      {
        LinkedListNode<CacheElement<K, V>> node = this.linkedListNodeMap.get(key);
        newNode = doublyLinkedList.updateAndMoveToFront(node, item);
      }
      else
      {
        if (this.size() >= this.size)
        {
          this.evictElement();
        }

        newNode = this.doublyLinkedList.push(item);
      }

      if (newNode.isEmpty())
      {
        return false;
      }

      this.linkedListNodeMap.put(key, newNode);

      return true;
    }
    finally
    {
      this.lock.writeLock().unlock();
    }

  }

  public Optional<V> get(K key)
  {
    this.lock.readLock().lock();
    try
    {
      LinkedListNode<CacheElement<K, V>> node = this.linkedListNodeMap.get(key);
      if (node != null && !node.isEmpty())
      {
        linkedListNodeMap.put(key, this.doublyLinkedList.moveToFront(node));

        return Optional.of(node.getElement().getValue());
      }
      return Optional.empty();

    }
    finally
    {
      this.lock.readLock().unlock();
    }
  }

  public boolean has(K key)
  {
    this.lock.readLock().lock();
    try
    {
      LinkedListNode<CacheElement<K, V>> node = this.linkedListNodeMap.get(key);

      return ( node != null && !node.isEmpty() );
    }
    finally
    {
      this.lock.readLock().unlock();
    }
  }

  private boolean evictElement()
  {
    this.lock.writeLock().lock();
    try
    {
      LinkedListNode<CacheElement<K, V>> tail = this.doublyLinkedList.getTail();

      if (tail != null)
      {
        K key = tail.getElement().getKey();

        this.doublyLinkedList.removeTail();
        this.linkedListNodeMap.remove(key);

        return true;
      }

      return false;
    }
    finally
    {
      this.lock.writeLock().unlock();
    }
  }

  public void remove(K key)
  {
    this.lock.writeLock().lock();
    try
    {
      LinkedListNode<CacheElement<K, V>> entry = this.linkedListNodeMap.get(key);

      if (entry != null)
      {
        this.doublyLinkedList.remove(entry);
        this.linkedListNodeMap.remove(key);
      }
    }
    finally
    {
      this.lock.writeLock().unlock();
    }

  }

}