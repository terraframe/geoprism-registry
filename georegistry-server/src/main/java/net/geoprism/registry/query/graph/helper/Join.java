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
