package net.geoprism.registry.cache;

public class DoublyLinkedList<T>
{
  LinkedListNode<T> dummyNode;

  LinkedListNode<T> head;

  LinkedListNode<T> tail;

  public DoublyLinkedList()
  {
    this.dummyNode = new LinkedListNode<T>(null);
    this.head = null;
    this.tail = null;
  }

  public LinkedListNode<T> moveToFront(LinkedListNode<T> node)
  {
    return node.isEmpty() ? dummyNode : updateAndMoveToFront(node, node.getElement());
  }

  public LinkedListNode<T> updateAndMoveToFront(LinkedListNode<T> node, T newValue)
  {
    if (node.isEmpty())
    {
      return new LinkedListNode<T>(null);
    }

    detach(node);

    push(newValue);

    return head;
  }

  private void detach(LinkedListNode<T> node)
  {
    LinkedListNode<T> prev = node.getPrev();
    LinkedListNode<T> next = node.getNext();

    if (prev != null)
    {
      prev.setNext(null);
    }

    if (next != null)
    {
      next.setPrev(null);
    }

    if (prev != null && next != null)
    {
      prev.setNext(next);
      next.setPrev(prev);
    }
  }

  public LinkedListNode<T> push(T data)
  {
    LinkedListNode<T> node = new LinkedListNode<T>(data);

    push(node);

    return node;
  }

  protected void push(LinkedListNode<T> node)
  {
    if (head == null)
    {
      head = node;
      tail = node;
    }
    else
    {
      node.next = head;
      head.prev = node;
      head = node;
    }
  }

  public void insert(T data, int position)
  {
    LinkedListNode<T> temp = new LinkedListNode<T>(data);
    if (position == 1)
    {
      push(data);
    }
    else
    {
      LinkedListNode<T> current = head;
      int currPosition = 1;

      while (current != null && currPosition < position)
      {
        current = current.next;
        currPosition++;
      }

      if (current == null)
      {
        add(data);
      }
      else
      {
        temp.next = current;
        temp.prev = current.prev;
        current.prev.next = temp;
        current.prev = temp;
      }
    }
  }

  public void add(T data)
  {
    LinkedListNode<T> temp = new LinkedListNode<T>(data);
    if (tail == null)
    {
      head = temp;
      tail = temp;
    }
    else
    {
      tail.next = temp;
      temp.prev = tail;
      tail = temp;
    }
  }

  public void removeHead()
  {
    if (head == null)
    {
      return;
    }

    if (head == tail)
    {
      head = null;
      tail = null;
      return;
    }

    LinkedListNode<T> temp = head;
    head = head.next;
    head.prev = null;
    temp.next = null;
  }

  public void remove(int pos)
  {
    if (head == null)
    {
      return;
    }

    if (pos == 1)
    {
      removeHead();
      return;
    }

    LinkedListNode<T> current = head;
    int count = 1;

    while (current != null && count != pos)
    {
      current = current.next;
      count++;
    }

    if (current == null)
    {
      System.out.println("Position wrong");
      return;
    }

    if (current == tail)
    {
      removeTail();
      return;
    }

    current.prev.next = current.next;
    current.next.prev = current.prev;
    current.prev = null;
    current.next = null;
  }

  public void removeTail()
  {
    if (tail == null)
    {
      return;
    }

    if (head == tail)
    {
      head = null;
      tail = null;
      return;
    }

    LinkedListNode<T> temp = tail;
    tail = tail.prev;
    tail.next = null;
    temp.prev = null;
  }

  public LinkedListNode<T> getHead()
  {
    return head;
  }

  public LinkedListNode<T> getTail()
  {
    return tail;
  }

}
