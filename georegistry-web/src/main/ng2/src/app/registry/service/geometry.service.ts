import { Injectable, Output, EventEmitter } from "@angular/core";

import MapboxDraw from '@mapbox/mapbox-gl-draw';
import { Map, IControl } from 'mapbox-gl';

import { ValueOverTimeEditPropagator } from '../component/geoobject-shared-attribute-editor/manage-versions.component';

declare var acp: any;

/**
 * This is a generic service used for rendering layers from ValueOverTime objects
 */
@Injectable()
export class GeometryService {
  map: Map;
  
  layers: ValueOverTimeEditPropagator[] = [];
  
  geometryType: String;
  
  readOnly: boolean;

  //@Output() layersChange: EventEmitter<VersionOverTimeLayer[]> = new EventEmitter();
  
  editingControl: any = null;
  
  simpleEditControl: any = null;
  
  editable: ValueOverTimeEditPropagator;
  
  @Output() geometryChange = new EventEmitter<any>();

  // eslint-disable-next-line no-useless-constructor
  constructor() { }

  initialize(map: Map, geometryType: String, readOnly: boolean) {
    this.map = map;
    this.geometryType = geometryType;
    this.editingControl = null;
    
    this.addLayers();
    
    this.map.on('draw.create', () => {
      this.saveEdits();
    });
    this.map.on('draw.delete', () => {
      this.saveEdits();
    });
    this.map.on('draw.update', () => {
      this.saveEdits();
    });
  }
  
  destroy(): void {
    this.map = null;
    this.editingControl = null;
  }
  
  addEditButton(): void {
    this.simpleEditControl.editEmitter.subscribe(versionObj => {
      //this.onClickEdit.emit();
    });

    this.map.addControl(this.simpleEditControl);
  }
  
  startEditing(editPropagator: ValueOverTimeEditPropagator)
  {
    if (this.editable != null && this.editingControl != null)
    {
      this.saveEdits();
    
      this.editingControl.deleteAll();
    }
    
    this.editable = editPropagator;
    
    if (!this.readOnly) {
      this.enableEditing();
    }
    else {
      this.addEditButton();
    }
    
    this.addEditingLayers();
  }
  
  stopEditing(editPropagator: ValueOverTimeEditPropagator)
  {
    this.saveEdits();
  
    this.editable = null;
    
    this.editingControl.deleteAll();
  }
  
  isValid(): boolean {
    if (!this.readOnly) {
      let isValid: boolean = false;

      if (this.editingControl != null) {
        let featureCollection: any = this.editingControl.getAll();

        if (featureCollection.features.length > 0) {
          isValid = true;
        }
      }

      return isValid;
    }

    return true;
  }
  
  saveEdits(): void {
    if (this.editable != null)
    {
      let geoJson = this.getDrawGeometry();
      
      this.editable.value = geoJson;
    }
  }
  
  public reload(): void {
    if (this.map != null) {
      this.removeLayers();
      this.addLayers();
      this.editingControl.deleteAll();
      this.addEditingLayers();
    }
  }

  getLayers(): ValueOverTimeEditPropagator[] {
    return this.layers;
  }
  
  setLayers(layers: ValueOverTimeEditPropagator[]): void {
    this.removeLayers();
    if (this.editingControl != null)
    {
      this.editingControl.deleteAll();
    }
  
    this.layers = layers;
    
    this.addLayers();
    this.addEditingLayers();
  }
  
  enableEditing(): void {
    if (this.editingControl == null)
    {
      if (this.geometryType === "MULTIPOLYGON" || this.geometryType === "POLYGON") {
        this.editingControl = new MapboxDraw({
          controls: {
            point: false,
            line_string: false,
            polygon: true,
            trash: true,
            combine_features: false,
            uncombine_features: false
          }
        });
      }
      else if (this.geometryType === "POINT" || this.geometryType === "MULTIPOINT") {
        this.editingControl = new MapboxDraw({
          controls: {
            point: true,
            line_string: false,
            polygon: false,
            trash: true,
            combine_features: false,
            uncombine_features: false
          }
        });
      }
      else if (this.geometryType === "LINE" || this.geometryType === "MULTILINE") {
        this.editingControl = new MapboxDraw({
          controls: {
            point: false,
            line_string: true,
            polygon: false,
            trash: true,
            combine_features: false,
            uncombine_features: false
          }
        });
      }
      this.map.addControl(this.editingControl);
    }

    this.addEditingLayers();
  }
  
  addEditingLayers(): void {
    if (this.editable != null && this.editingControl != null) {
      this.editingControl.add(this.editable.value);
    }
  }
  
  removeSource(prefix: string): void {
    if (!this.map) {
      return;
    }
  
    let sourceName: string = prefix + "-geoobject";

    if (this.geometryType === "MULTIPOLYGON" || this.geometryType === "POLYGON") {
      if (this.map.getLayer(sourceName + "-polygon") != null)
      {
        this.map.removeLayer(sourceName + "-polygon");
      }
    }
    else if (this.geometryType === "POINT" || this.geometryType === "MULTIPOINT") {
      if (this.map.getLayer(sourceName + "-point") != null)
      {
        this.map.removeLayer(sourceName + "-point");
      }
    }
    else if (this.geometryType === "LINE" || this.geometryType === "MultiLine") {
      if (this.map.getLayer(sourceName + "-line") != null)
      {
        this.map.removeLayer(sourceName + "-line");
      }
    }

    if (this.map.getSource(sourceName) != null)
    {
      this.map.removeSource(sourceName);
    }
  }
  
  removeLayers(): void {
    if (this.layers != null && this.layers.length > 0) {
      let len = this.layers.length;
      
      for (let i = 0; i < len; ++i)
      {
        let layer = this.layers[i];
        this.removeSource(layer.oid);
      }
    }
  }

  addLayers(): void {
    // red : "#800000"
    // brown : "#EFA22E"
    
    if (this.layers != null && this.layers.length > 0) {
      let len = this.layers.length;
      for (let i = 0; i < len; ++i)
      {
        let layer = this.layers[i];
        this.renderGeometryAsLayer(layer.value, layer.oid, "#EFA22E");
      }
    }
  }

  renderGeometryAsLayer(geometry: any, prefix: string, color: string) {
    let sourceName: string = prefix + "-geoobject";
    
    if (!this.map) {
      return;
    }
    
    this.map.addSource(sourceName, {
      type: 'geojson',
      data: {
        "type": "FeatureCollection",
        "features": []
      }
    });

    if (this.geometryType === "MULTIPOLYGON" || this.geometryType === "POLYGON") {
      // Polygon Layer
      this.map.addLayer({
        "id": sourceName + "-polygon",
        "type": "fill",
        "source": sourceName,
        "paint": {
          "fill-color": color,
          "fill-outline-color": "black",
          "fill-opacity": 0.7,
        },
      });
    }
    else if (this.geometryType === "POINT" || this.geometryType === "MULTIPOINT") {
      // Point layer
      this.map.addLayer({
        "id": sourceName + "-point",
        "type": "circle",
        "source": sourceName,
        "paint": {
          "circle-radius": 3,
          "circle-color": color,
          "circle-stroke-width": 2,
          "circle-stroke-color": '#FFFFFF'
        }
      });
    }
    else if (this.geometryType === "LINE" || this.geometryType === "MULTILINE") {
      this.map.addLayer({
        "id": sourceName + "-line",
        "source": sourceName,
        "type": "line",
        "layout": {
          "line-join": "round",
          "line-cap": "round"
        },
        "paint": {
          "line-color": color,
          "line-width": 2
        }
      });
    }

    (<any>this.map.getSource(sourceName)).setData(geometry);
  }
  
  getDrawGeometry(): any {
    if (this.editingControl != null) {
      let featureCollection: any = this.editingControl.getAll();

      if (featureCollection.features.length > 0) {

        // The first Feature is our GeoObject.

        // Any additional features were created using the draw editor. Combine them into the GeoObject if its a multi-polygon.
        if (this.geometryType === "MULTIPOLYGON") {
          let polygons = [];

          for (let i = 0; i < featureCollection.features.length; i++) {
            let feature = featureCollection.features[i];

            if (feature.geometry.type === 'MultiPolygon') {
              for (let j = 0; j < feature.geometry.coordinates.length; j++) {
                polygons.push(feature.geometry.coordinates[j]);
              }
            }
            else {
              polygons.push(feature.geometry.coordinates);
            }
          }

          return {
            coordinates: polygons,
            type: 'MultiPolygon'
          };
        }
        else if (this.geometryType === "MULTIPOINT") {
          let points = [];

          for (let i = 0; i < featureCollection.features.length; i++) {
            let feature = featureCollection.features[i];

            if (feature.geometry.type === 'MultiPoint') {
              for (let j = 0; j < feature.geometry.coordinates.length; j++) {
                points.push(feature.geometry.coordinates[j]);
              }
            }
            else {
              points.push(feature.geometry.coordinates);
            }
          }

          return {
            coordinates: points,
            type: 'MultiPoint'
          };
        }
        else if (this.geometryType === "MULTILINE") {
          let lines = [];

          for (let i = 0; i < featureCollection.features.length; i++) {
            let feature = featureCollection.features[i];

            if (feature.geometry.type === 'MultiLineString') {
              for (let j = 0; j < feature.geometry.coordinates.length; j++) {
                lines.push(feature.geometry.coordinates[j]);
              }
            }
            else {
              lines.push(feature.geometry.coordinates);
            }
          }

          return {
            coordinates: lines,
            type: 'MultiLineString'
          };
        }
        else {
          return featureCollection.features[0].geometry;
        }
      }
    }

    return null;
  }
}
