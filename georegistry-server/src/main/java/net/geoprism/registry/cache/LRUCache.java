/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.cache;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.map.LRUMap;
public class LRUCache<K, V> implements Cache<K, V>
{
  private Map<K, V> map;
  public LRUCache(int size)
  {
    this.map = Collections.synchronizedMap(new LRUMap<K, V>(size));
  }
  @Override
  public int size()
  {
    return this.map.size();
  }
  @Override
  public boolean isEmpty()
  {
    return this.map.isEmpty();
  }
  @Override
  public void clear()
  {
    this.map.clear();
  }
  @Override
  public boolean put(K key, V value)
  {
    this.map.put(key, value);
    return true;
  }
  public Optional<V> get(K key)
  {
    return Optional.ofNullable(this.map.get(key));
  }
  public boolean has(K key)
  {
    return this.map.containsKey(key);
  }
  public void remove(K key)
  {
    this.map.remove(key);
  }
}