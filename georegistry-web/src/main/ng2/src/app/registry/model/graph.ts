import { LocalizedValue } from "@shared/model/core";

export interface Relationship {
    oid: string,
    label: LocalizedValue,
    isHierarchy: boolean,
    code: string,
    type?: string
}

export interface Vertex {
    code: string,
    typeCode: string,
    objectType: "BUSINESS" | "GEOOBJECT",
    id: string,
    label: string,
    relation: "PARENT" | "CHILD" | "SELECTED"
}

export interface Edge {
    id: string,
    label: string,
    source: string,
    target: string
}

export interface TreeData {
  edges: Edge[],
  verticies: Vertex[],
  relatedTypes: [{ code: string, label: string }]
}
