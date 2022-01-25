/* eslint-disable indent */
import { Component, OnInit, Input, Output, SimpleChanges, EventEmitter } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

import { HierarchyService, IOService } from "@registry/service";
import { GeoObject } from "@registry/model/registry";
import { Subject } from "rxjs";
import { RelationshipVisualizationService } from "@registry/service/relationship-visualization.service";
import { Layout } from "@swimlane/ngx-graph";

import { DagreNodesOnlyLayout } from "./relationship-viz-layout";

import * as shape from "d3-shape";
import { LocalizedValue } from "@shared/model/core";

export const DRAW_SCALE_MULTIPLIER: number = 1.0;

export const GRAPH_GO_LABEL_COLOR: string = "black";
export const GRAPH_CIRCLE_FILL: string = "#999";
export const GRAPH_LINE_COLOR: string = "#999";

@Component({

    selector: "relationship-visualizer",
    templateUrl: "./relationship-visualizer.component.html",
    styleUrls: ["./relationship-visualizer.css"]
})
export class RelationshipVisualizerComponent implements OnInit {

  /*
   * Reference to the modal current showing
  */
  private bsModalRef: BsModalRef;

  @Input() params: {geoObject: GeoObject, mdEdgeOid: string, date: string} = null;

  geoObject: GeoObject = null;

  mdEdgeOid: string = null;

  @Output() changeGeoObject = new EventEmitter<{id:string, code: string, typeCode: string}>();

  @Output() changeRelationship = new EventEmitter<{oid:string}>();

  private data: any = null;

  relationships: {oid: string, label: LocalizedValue, isHierarchy: boolean}[];

  private width: number = 500;
  private height: number = 500;

  panToNode$: Subject<string> = new Subject();

  update$: Subject<boolean> = new Subject();

  public layout: Layout = new DagreNodesOnlyLayout();

  public curve = shape.curveLinear;

  // eslint-disable-next-line no-useless-constructor
  constructor(private hierarchyService: HierarchyService, private modalService: BsModalService, private ioService: IOService,
      localizeService: LocalizationService, private vizService: RelationshipVisualizationService, private authService: AuthService) {}

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
      if (changes.params && changes.params.previousValue !== changes.params.currentValue) {
          this.data = null;
          this.mdEdgeOid = this.params.mdEdgeOid;
          this.geoObject = this.params.geoObject;

          if (this.relationships == null) {
              this.fetchRelationships();
          }
      }
  }

  private fetchRelationships(): void {
      if (this.geoObject != null) {
        this.vizService.relationships(this.geoObject.properties.type).then(relationships => {
            this.relationships = relationships;

            if (!this.mdEdgeOid && this.relationships && this.relationships.length > 0) {
                // window.setTimeout(() => {
                    this.mdEdgeOid = this.relationships[0].oid;
                    this.onSelectRelationship();
                // }, 2);
            } else if (this.mdEdgeOid && this.relationships) {
                this.onSelectRelationship();
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
      }
  }

  private onSelectRelationship() {
      this.fetchData();
      this.changeRelationship.emit({ oid: this.mdEdgeOid });
  }

  private fetchData(): void {
      this.vizService.tree(this.mdEdgeOid, this.geoObject.properties.code, this.geoObject.properties.type, this.params.date).then(data => {
          let graphContainer = document.getElementById("graph-container");
          this.height = graphContainer.clientHeight;
          this.width = graphContainer.clientWidth;

          this.data = data;

          if (this.geoObject != null) {
              window.setTimeout(() => {
                  this.panToNode$.next("g-" + this.geoObject.properties.uid);
              }, 10);
          }
      });
  }

  public onClickNode(node: any): void {
      this.changeGeoObject.emit({ id: node.id.substring(2), code: node.code, typeCode: node.typeCode });
  }

  private stringify(data: any): string {
    return JSON.stringify(data);
  }

  public error(err: HttpErrorResponse): void {
      this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
  }

}
