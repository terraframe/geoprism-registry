package net.geoprism.registry.cache;

public class CacheElement<K, V>
{
  K key;

  V value;

  public CacheElement(K key, V value)
  {
    this.key = key;
    this.value = value;
  }

  public K getKey()
  {
    return key;
  }

  public V getValue()
  {
    return value;
  }

}
