package net.geoprism.registry.view;

import net.geoprism.registry.model.EdgeType;

public class TypeInfo
{
  public static enum TypeClass {
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

}
