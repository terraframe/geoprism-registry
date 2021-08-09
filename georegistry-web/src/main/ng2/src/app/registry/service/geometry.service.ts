import { Injectable, Output, EventEmitter } from "@angular/core";

import { VersionOverTimeLayer } from "@registry/model/registry";

declare var acp: any;

@Injectable()
export class GeometryService {
  renderedLayers: VersionOverTimeLayer[] = [];

  @Output() layersChange: EventEmitter<VersionOverTimeLayer[]> = new EventEmitter();

  // eslint-disable-next-line no-useless-constructor
  constructor() { }

  getRenderedLayers(): VersionOverTimeLayer[] {
    return this.renderedLayers;
  }
  
  setRenderedLayers(layers: VersionOverTimeLayer[]): void {
    this.renderedLayers = layers;
    
    this.layersChange.emit(this.renderedLayers);
  }
}
