/* eslint-disable indent */
import { Component, OnInit, Input, Output, SimpleChanges, EventEmitter } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ErrorHandler } from "@shared/component";

import { GeoObject } from "@registry/model/registry";
import { Subject } from "rxjs";
import { RelationshipVisualizationService } from "@registry/service/relationship-visualization.service";
import { Layout, Orientation } from "@swimlane/ngx-graph";

import { DagreNodesOnlyLayout } from "./relationship-viz-layout";

import * as shape from "d3-shape";
import { LocalizedValue } from "@shared/model/core";
import { PANEL_SIZE_STATE } from "@registry/model/location-manager";

export const DRAW_SCALE_MULTIPLIER: number = 1.0;

export const GRAPH_GO_LABEL_COLOR: string = "black";
export const GRAPH_CIRCLE_FILL: string = "#999";
export const GRAPH_LINE_COLOR: string = "#999";

export interface Relationship {
  oid: string,
  label: LocalizedValue,
  isHierarchy: boolean,
  code: string,
  type?:string
}

@Component({

    selector: "relationship-visualizer",
    templateUrl: "./relationship-visualizer.component.html",
    styleUrls: ["./relationship-visualizer.css"]
})
export class RelationshipVisualizerComponent implements OnInit {

  // Dumb hack to use this imported enum
  public ORIENTATION = Orientation;

  /*
   * Reference to the modal current showing
  */
  private bsModalRef: BsModalRef;

  @Input() params: {geoObject: GeoObject, graphOid: string, date: string, searchPanelOpen: boolean} = null;

  geoObject: GeoObject = null;

  graphOid: string = null;

  relationship: Relationship = null;

  @Output() changeGeoObject = new EventEmitter<{id:string, code: string, typeCode: string}>();

  @Output() changeRelationship = new EventEmitter<string>();

  private data: any = null;

  relationships: Relationship[];

  public panelOpen: boolean = false;

  public searchPanelOpen: boolean = false;

  public left: number = 10;
  public top: number = 40;

  public svgHeight: number = null;
  public svgWidth: number = null;

  panToNode$: Subject<string> = new Subject();

  update$: Subject<boolean> = new Subject();

  public layout: Layout = new DagreNodesOnlyLayout();

  public curve = shape.curveLinear;

  // eslint-disable-next-line no-useless-constructor
  constructor(private modalService: BsModalService, private vizService: RelationshipVisualizationService) {}

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
      if (changes.params && changes.params.previousValue !== changes.params.currentValue) {
          this.graphOid = this.params.graphOid;
          this.geoObject = this.params.geoObject;
          this.searchPanelOpen = this.params.searchPanelOpen;

          if (this.relationships == null ||
              changes.params.previousValue == null ||
              changes.params.previousValue.geoObject.properties.type !== changes.params.currentValue.geoObject.properties.type) {
                  this.fetchRelationships();
          } else if (this.relationships != null && this.relationship) {
              this.fetchData();
          }
      }
  }

  // Thanks to https://stackoverflow.com/questions/52172067/create-svg-hexagon-points-with-only-only-a-length
  public getHexagonPoints(node: {dimension: {width: number, height: number}}): string {
      let radius = node.dimension.width / 2;
      let height = node.dimension.height;
      let width = node.dimension.width;

      // let radius = 50;
      // let height = 200;
      // let width = 200;

      let points = [0, 1, 2, 3, 4, 5, 6].map((n, i) => {
          let angleDeg = 60 * i - 30;
          let angleRad = Math.PI / 180 * angleDeg;
          return [width / 2 + radius * Math.cos(angleRad), height / 2 + radius * Math.sin(angleRad)];
        }).map((p) => p.join(","))
        .join(" ");

      return points;
  }

  toggleSize(event: MouseEvent): void {
      if (event != null) {
          event.stopPropagation();
      }

      this.panelOpen = !this.panelOpen;

      window.setTimeout(() => {
          let graphContainer = document.getElementById("graph-container");

          if (graphContainer) {
              this.svgHeight = graphContainer.clientHeight;
              this.svgWidth = graphContainer.clientWidth;
              // this.panToNode(this.geoObject.properties.uid);
          }
      }, 10);
  }

  private fetchRelationships(): void {
      if (this.geoObject != null) {
        this.relationships = [];

        this.vizService.relationships(this.geoObject.properties.type).then(relationships => {
            this.relationships = relationships;

            if (this.relationships && this.relationships.length > 0) {
                if (!this.graphOid || this.relationships.findIndex(rel => rel.oid === this.graphOid) === -1) {
                    this.relationship = this.relationships[0];
                    this.graphOid = this.relationship.oid;
                    this.onSelectRelationship();
                } else {
                    this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.graphOid)];
                    this.fetchData();
                }
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
      }
  }

  private onSelectRelationship() {
      this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.graphOid)];

    //   this.fetchData();
      this.changeRelationship.emit(this.graphOid);
  }

  private fetchData(): void {
      if (this.relationship != null) {
        this.vizService.tree(this.relationship.type, this.relationship.code, this.geoObject.properties.code, this.geoObject.properties.type, this.params.date).then(data => {
          this.data = null;

          window.setTimeout(() => {
              this.data = data;
            }, 0);

            let graphContainer = document.getElementById("graph-container");

            if (graphContainer) {
                this.svgHeight = graphContainer.clientHeight;
                this.svgWidth = graphContainer.clientWidth;

                if (this.geoObject != null) {
                    // this.panToNode(this.geoObject.properties.uid);
                }
            }
        });
    }
  }

  collapseAnimation(id: string): Promise<void> {
      if (!this.geoObject) { return new Promise<void>((resolve, reject) => { resolve(); }); }

      let activeEl = document.getElementById(id) as unknown as SVGGraphicsElement;
      if (!activeEl) { return new Promise<void>((resolve, reject) => { resolve(); }); }

      let bbox = this.getBBox(activeEl, true);

      let all = document.querySelectorAll("g.nodes > g");

      all.forEach((el: SVGGraphicsElement) => {
          if (el.id !== activeEl.id) {
              let bbox2 = this.getBBox(el, false);
              let translate = "translate(" + (bbox.x - bbox2.x) + "," + (bbox.y - bbox2.y) + ")";
              el.setAttribute("transform", translate);
          }
      });

      document.querySelectorAll("g.links > g").forEach(el => {
          el.remove();
      });

      let promise = new Promise<void>((resolve, reject) => {
          setTimeout(() => {
              all.forEach((el: SVGGraphicsElement) => {
                  if (el.id !== activeEl.id) {
                      el.remove();
                  }
              });

              resolve();
          }, 500);
      });

      return promise;
  }

  private getBBox(el: SVGGraphicsElement, includeTransform: boolean = true): DOMRect {
      if (!includeTransform) {
          return el.getBBox();
      }

      let cloned = el.cloneNode(true) as unknown as SVGGraphicsElement;

      let newParent = document.createElementNS("http://www.w3.org/2000/svg", "g") as unknown as SVGGraphicsElement;
      document.querySelector("svg").appendChild(newParent);

      newParent.appendChild(cloned);
      let bbox = newParent.getBBox();
      cloned.remove();
      newParent.remove();

      return bbox;
  }

  /*
   * We can't predict when the graph will be finished loading and it will be ready to pan. So we're just telling it to
   * pan over and over again just in case it takes a little while to load. To my knowledge there is no way to fix this,
   * because:
   *  1. ngx graph does not provide any sort of "on ready" event we can listen to
   *  2. Checking if the element exists first in the dom before we call pan to node does not work. The graph might still
   *     not be ready, even if the element exists.
   */
  /*
  private panToNode(uid: string, retryNum: number = 10) {
      window.setTimeout(() => {
          if (document.getElementById("g-" + uid) != null) {
              this.panToNode$.next("g-" + uid);
              this.update$.next(); // https://github.com/swimlane/ngx-graph/issues/319

              if (retryNum > 0) {
                  this.panToNode(uid, retryNum - 1);
              }
          }
      }, 50);
  }
  */

  public onClickNode(node: any): void {
      this.collapseAnimation(node.id).then(() => {
          this.changeGeoObject.emit({ id: node.id.substring(2), code: node.code, typeCode: node.typeCode });
      });
  }

  private stringify(data: any): string {
    return JSON.stringify(data);
  }

  public error(err: HttpErrorResponse): void {
      this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
  }

}
