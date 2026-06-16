package net.geoprism.registry.view;

import net.geoprism.registry.model.EdgeType;
import net.geoprism.registry.model.VertexComponentType;

public class TypeInfo implements Comparable<TypeInfo>
{
  public static enum TypeClass implements Comparable<TypeClass> {
    GEO_OBJECT_TYPE("GEO_OBJECT_TYPE"), BUSINESS_TYPE("BUSINESS_TYPE"), BUSINESS_EDGE(EdgeType.BUSINESS_EDGE_TYPE), DAG(EdgeType.DIRECTED_ACYCLIC_GRAPH_TYPE), UNDIRECTED_GRAPH(EdgeType.UNDIRECTED_GRAPH_TYPE), HIERARCHY(EdgeType.HIERARCHY_TYPE);

    private String code;

    private TypeClass(String code)
    {
      this.code = code;
    }

    public String getCode()
    {
      return code;
    }
  }

  private TypeClass typeClass;

  private String    typeCode;

  public TypeInfo()
  {
    super();
  }

  public TypeInfo(TypeClass typeClass, String typeCode)
  {
    super();
    this.typeClass = typeClass;
    this.typeCode = typeCode;
  }

  public TypeInfo(VertexComponentType typeClass, String typeCode)
  {
    super();
    this.typeClass = typeClass.equals(VertexComponentType.GEO_OBJECT) ? TypeClass.GEO_OBJECT_TYPE : TypeClass.BUSINESS_TYPE;
    this.typeCode = typeCode;
  }

  public TypeClass getTypeClass()
  {
    return typeClass;
  }

  public void setTypeClass(TypeClass typeClass)
  {
    this.typeClass = typeClass;
  }

  public String getTypeCode()
  {
    return typeCode;
  }

  public void setTypeCode(String typeCode)
  {
    this.typeCode = typeCode;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof TypeInfo)
    {
      TypeInfo type = (TypeInfo) obj;

      return this.typeClass.equals(type.getTypeClass()) && this.typeCode.equals(type.getTypeCode());
    }

    return false;
  }

  @Override
  public int compareTo(TypeInfo arg0)
  {
    int compareTo = this.typeClass.compareTo(arg0.typeClass);

    if (compareTo == 0)
    {
      return this.typeCode.compareTo(arg0.typeCode);
    }

    return compareTo;
  }

}
