package net.geoprism.registry.cache;

import java.util.Optional;
import java.util.function.Supplier;

public class CacheEntry<T>
{
  private Optional<T> value;

  public CacheEntry()
  {
  }

  public CacheEntry(Optional<T> value)
  {
    super();
    this.value = value;
  }

  public Optional<T> getValue()
  {
    return value;
  }

  public void setValue(Optional<T> value)
  {
    this.value = value;
  }

  public T orNull()
  {
    return this.value.orElse(null);
  }

  public T orElseThrow(Supplier<? extends RuntimeException> supplier)
  {
    return this.value.orElseThrow(supplier);
  }
}
