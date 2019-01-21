package net.geoprism.georegistry.testframework;

import java.util.Iterator;
import java.util.List;

import com.runwaysdk.query.OIterator;

public class ListIterator<T> implements OIterator<T>
{

  private List<T>     list;

  private Iterator<T> it;

  public ListIterator(List<T> list)
  {
    this.list = list;
    this.it = list.iterator();
  }

  @Override
  public Iterator<T> iterator()
  {
    return this.list.iterator();
  }

  @Override
  public T next()
  {
    return this.it.next();
  }

  @Override
  public void remove()
  {
    this.it.remove();
  }

  @Override
  public boolean hasNext()
  {
    return this.it.hasNext();
  }

  @Override
  public void close()
  {
  }

  @Override
  public List<T> getAll()
  {
    return this.list;
  }

}
