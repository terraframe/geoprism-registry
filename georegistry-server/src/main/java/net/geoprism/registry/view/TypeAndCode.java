package net.geoprism.registry.view;

import net.geoprism.graph.GraphTypeSnapshot;

public class TypeAndCode implements Comparable<TypeAndCode>
{
  public static enum Type {
    GEO_OBJECT, HIERARCHY, DAG, UNDIRECTED, BUSINESS, BUSINESS_EDGE
  }

  private String code;

  private Type   type;

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public Type getType()
  {
    return type;
  }

  public void setType(Type type)
  {
    this.type = type;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof TypeAndCode)
    {
      return ( (TypeAndCode) obj ).type.equals(type) && ( (TypeAndCode) obj ).code.equals(code);
    }

    return super.equals(obj);
  }

  @Override
  public int compareTo(TypeAndCode arg0)
  {
    int compareTo = this.type.compareTo(arg0.type);

    if (compareTo == 0)
    {
      return this.code.compareTo(arg0.code);
    }

    return compareTo;
  }

  public static TypeAndCode build(String code, Type type)
  {
    TypeAndCode typeCode = new TypeAndCode();
    typeCode.setCode(code);
    typeCode.setType(type);

    return typeCode;
  }

  public static Object build(String code, String edgeTypeCode)
  {
    String typeCode = edgeTypeCode;

    if (edgeTypeCode.equals(GraphTypeSnapshot.DIRECTED_ACYCLIC_GRAPH_TYPE))
    {
      typeCode = Type.DAG.name();
    }
    else if (edgeTypeCode.equals(GraphTypeSnapshot.UNDIRECTED_GRAPH_TYPE))
    {
      typeCode = Type.UNDIRECTED.name();
    }
    if (edgeTypeCode.equals(GraphTypeSnapshot.HIERARCHY_TYPE))
    {
      typeCode = Type.HIERARCHY.name();
    }

    TypeAndCode typeAndCode = new TypeAndCode();
    typeAndCode.setCode(code);
    typeAndCode.setType(Type.valueOf(typeCode));

    return typeAndCode;
  }
}
