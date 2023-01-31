package net.geoprism.registry.masterlist;

public interface ListColumnVisitor
{
  public void accept(ListAttribute attribute);

  public void accept(ListAttributeGroup group);
}
