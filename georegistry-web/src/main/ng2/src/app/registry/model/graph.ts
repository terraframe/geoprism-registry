import { LocalizedValue } from "@shared/model/core";

export const RELATIONSHIP_VISUALIZER_LAYER_DATASET_PROVIDER = "RelationshipVisualizer";

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
  geoJson: any
}
