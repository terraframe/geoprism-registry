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
