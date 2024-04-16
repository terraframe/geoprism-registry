package net.geoprism.registry.test;

public class TestCachedObject<T>
{
  private T object;

  protected T getCachedObject()
  {
    return object;
  }

  protected void setCachedObject(T object)
  {
    this.object = object;
  }

  public void clear()
  {
    this.object = null;
  }

}
