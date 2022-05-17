/* eslint-disable indent */
import { Component, OnInit, Output, EventEmitter } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService } from "ngx-bootstrap/modal";

import { ErrorHandler } from "@shared/component";

import { GeoObjectTypeCache } from "@registry/model/registry";
import { Subject, Subscription } from "rxjs";
import { RelationshipVisualizationService } from "@registry/service/relationship-visualization.service";
import { Layout, Orientation } from "@swimlane/ngx-graph";

import { DagreNodesOnlyLayout } from "./relationship-viz-layout";

import * as shape from "d3-shape";
import { NgxSpinnerService } from "ngx-spinner";
import { OverlayerIdentifier } from "@registry/model/constants";
import * as ColorGen from "color-generator";
import { RegistryCacheService } from "@registry/service/registry-cache.service";
import { GeometryService } from "@registry/service";
import { Router, ActivatedRoute } from "@angular/router";
import { LngLatBounds } from "mapbox-gl";
import { ObjectReference, Relationship, TreeData, Vertex } from "@registry/model/graph";
import { LocationManagerParams } from "../location-manager/location-manager.component";
import { Layer, RelationshipVisualizionDataSource, RelationshipVisualizionLayer, RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE } from "@registry/service/layer-data-source";

export const DRAW_SCALE_MULTIPLIER: number = 1.0;

export const SELECTED_NODE_COLOR: string = "#4287f5";

export const GRAPH_GO_LABEL_COLOR: string = "black";
export const GRAPH_CIRCLE_FILL: string = "#999";
export const GRAPH_LINE_COLOR: string = "#999";

export const COLLAPSE_ANIMATION_TIME: number = 500; // in ms

export const DIMENSIONS = {
    NODE: { WIDTH: 30, HEIGHT: 30 },
    LABEL: { WIDTH: 100, HEIGHT: 60, FONTSIZE: 14 },
    PADDING: {
        BETWEEN_NODES: 0,
        NODE_LABEL: 5,
        NODE_EDGE: 5
    }
};

@Component({

    selector: "relationship-visualizer",
    templateUrl: "./relationship-visualizer.component.html",
    styleUrls: ["./relationship-visualizer.css"]
})
export class RelationshipVisualizerComponent implements OnInit {

    // Hack to allow the constant to be used in the html
    CONSTANTS = {
        OVERLAY: OverlayerIdentifier.VISUALIZER_PANEL,
        ORIENTATION: Orientation
    }

    params: LocationManagerParams = {};

    @Output() nodeSelect = new EventEmitter<{ objectType: "BUSINESS" | "GEOOBJECT", id: string, code: string, typeCode: string, selectAnimation:(resolve) => void }>();

    @Output() changeRelationship = new EventEmitter<string>();

    public DIMENSIONS = DIMENSIONS;

    public SELECTED_NODE_COLOR = SELECTED_NODE_COLOR;

    public svgHeight: number = null;
    public svgWidth: number = null;

    panToNode$: Subject<string> = new Subject();

    update$: Subject<boolean> = new Subject();

    public layout: Layout = new DagreNodesOnlyLayout();

    public curve = shape.curveLinear;

    public typeLegend: { [key: string]: { label: string, color: string } } = {};

    public typeCache: GeoObjectTypeCache;

    relationship: Relationship = null;
    relationships: Relationship[];

    graphOid: string;

    data: TreeData = null;

    onFetchErrorSub: Subscription;

    queryParamSub: Subscription;

    panelOpen: boolean = true;

    loading: boolean = true;

    // eslint-disable-next-line no-useless-constructor
    constructor(private modalService: BsModalService,
        private spinner: NgxSpinnerService,
        private vizService: RelationshipVisualizationService,
        private cacheService: RegistryCacheService,
        private geomService: GeometryService,
        private router: Router,
        private route: ActivatedRoute) { }

    ngOnInit(): void {
        this.typeCache = this.cacheService.getTypeCache();

        this.queryParamSub = this.route.queryParams.subscribe((params) => { this.queryParamChanges(params); });

        // Angular keeps invoking our queryParamChanges in the early stages of component loading. We don't want to make expensive
        // data requests unless we're certain that all the params are loaded.
        window.setTimeout(() => {
            this.loading = false;

            this.queryParamChanges(this.params);
        }, 10);
    }

    ngOnDestroy(): void {
        this.queryParamSub.unsubscribe();
    }

    queryParamChanges(params) {
        if (params.type == null || params.code == null) {
            return;
        }

        let newParams = JSON.parse(JSON.stringify(params));
        let oldParams = JSON.parse(JSON.stringify(this.params));
        this.params = newParams;

        this.panelOpen = newParams.graphPanelOpen === "true";

        if (newParams.graphOid && newParams.graphOid !== oldParams.graphOid && this.relationships != null) {
            this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.graphOid)];
        }

        if (!this.loading) {
            if (this.relationships == null || this.relationship == null || newParams.objectType !== oldParams.objectType || newParams.type !== oldParams.type) {
                this.relationships = null;
                this.relationship = null;
                this.graphOid = null;
                this.data = null;
                this.fetchRelationships();
            } else if (this.relationships != null && this.relationship && (newParams.bounds !== oldParams.bounds || newParams.code !== oldParams.code || newParams.date !== oldParams.date || newParams.uid !== oldParams.uid || newParams.graphOid !== oldParams.graphOid)) {
                this.fetchData();
            }
        }

        if (this.panelOpen) {
            window.setTimeout(() => {
                this.resizeDimensions();
            }, 1);
        }
    }

    resizeDimensions(): void {
        let graphContainer = document.getElementById("graph-container");

        if (graphContainer) {
            this.svgHeight = graphContainer.clientHeight - 50;
            this.svgWidth = graphContainer.clientWidth;
        }
    }

    // Thanks to https://stackoverflow.com/questions/52172067/create-svg-hexagon-points-with-only-only-a-length
    public getHexagonPoints(node: { dimension: { width: number, height: number }, relation: string }): string {
        let y = (this.DIMENSIONS.LABEL.HEIGHT / 2) - this.DIMENSIONS.NODE.HEIGHT / 2;
        let x = (this.relationship.layout === "VERTICAL")
            ? (node.relation === "CHILD" ? (this.DIMENSIONS.LABEL.WIDTH / 2) - this.DIMENSIONS.NODE.WIDTH / 2 : (this.DIMENSIONS.LABEL.WIDTH + DIMENSIONS.PADDING.NODE_LABEL + this.DIMENSIONS.NODE.WIDTH) / 2 - this.DIMENSIONS.NODE.WIDTH / 2)
            : node.relation === "PARENT" ? (this.DIMENSIONS.LABEL.WIDTH + this.DIMENSIONS.PADDING.NODE_LABEL + this.DIMENSIONS.PADDING.NODE_EDGE) : 0;

        let radius = this.DIMENSIONS.NODE.WIDTH / 2;
        let height = this.DIMENSIONS.NODE.HEIGHT;
        let width = this.DIMENSIONS.NODE.WIDTH;

        let points = [0, 1, 2, 3, 4, 5, 6].map((n, i) => {
            let angleDeg = 60 * i - 30;
            let angleRad = Math.PI / 180 * angleDeg;
            return [(width / 2 + radius * Math.cos(angleRad)) + x, (height / 2 + radius * Math.sin(angleRad)) + y];
        }).map((p) => p.join(","))
            .join(" ");

        return points;
    }

    private fetchRelationships(): void {
        if (this.params.type != null) {
            this.relationships = [];
            this.spinner.show(this.CONSTANTS.OVERLAY);

            this.vizService.relationships(this.params.objectType, this.params.type).then(relationships => {
                this.relationships = relationships;

                if (this.relationships && this.relationships.length > 0) {
                    if (!this.params.graphOid || this.relationships.findIndex(rel => rel.oid === this.params.graphOid) === -1) {
                        this.relationship = this.relationships[0];
                        this.graphOid = this.relationship.oid;
                        this.onSelectRelationship(true);
                    } else {
                        this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.params.graphOid)];
                        this.graphOid = this.params.graphOid;
                        this.fetchData();
                    }
                }
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            }).finally(() => {
                this.spinner.hide(this.CONSTANTS.OVERLAY);
            });
        }
    }

    private onSelectRelationship(updateUrl: boolean) {
        this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.graphOid)];

        //   this.fetchData();
        this.changeRelationship.emit(this.graphOid);

        if (updateUrl) {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { graphOid: this.graphOid },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            });
        }
    }

    private fetchData(): void {
        if (this.relationship != null) {
            this.spinner.show(this.CONSTANTS.OVERLAY);

            let source = { code: this.params.code, typeCode: this.params.type, objectType: this.params.objectType } as Vertex;

            this.vizService.tree(this.relationship.type, this.relationship.code, source, this.params.date, this.getBoundsAsWKT()).then(data => {
                this.data = null;

                window.setTimeout(() => {
                    this.data = data;
                    this.resizeDimensions();
                    this.calculateTypeLegend(this.data.relatedTypes);
                    this.addLayers(this.data.relatedTypes);
                }, 0);

                this.resizeDimensions();
            }).finally(() => {
                this.spinner.hide(this.CONSTANTS.OVERLAY);
            });
        }
    }

    private addLayers(relatedTypes: [{ code: string, label: string }]) {
        if (this.relationship.type === "BUSINESS" || (this.params.objectType === "BUSINESS" && this.relationship.type !== "GEOOBJECT")) {
            let layers: Layer[] = this.geomService.getLayers().filter(layer => layer.dataSource.getDataSourceType() !== RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE);
            this.geomService.setLayers(layers);
            return;
        }

        let layers: Layer[] = this.geomService.getLayers();

        let sourceObject = { code: this.params.code, typeCode: this.params.type, objectType: this.params.objectType } as ObjectReference;
        let dataSource = new RelationshipVisualizionDataSource(this.vizService, this.geomService, this.relationship.type, this.relationship.code, sourceObject, this.params.bounds, this.params.date);

        // Remove any existing layer from map that is graph related that isn't part of this new data
        layers = layers.filter(layer => layer.dataSource.getDataSourceType() !== RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE ||
            (relatedTypes.map(relatedType => relatedType.code).indexOf((layer as RelationshipVisualizionLayer).getRelatedTypeFilter()) !== -1));

        // If the type is already rendered at a specific position in the layer stack, we want to preserve that positioning and overwrite any layer currently in that position
        let existingRelatedTypes: { [key: string]: { index: number, rendered: boolean } } = {};
        for (let i = 0; i < layers.length; ++i) {
            if (layers[i].dataSource.getDataSourceType() === RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE) {
                let layer: RelationshipVisualizionLayer = layers[i] as RelationshipVisualizionLayer;

                existingRelatedTypes[layer.getRelatedTypeFilter()] = { index: i, rendered: layer.rendered };
            }
        }

        if (sourceObject.objectType === "GEOOBJECT") {
          relatedTypes.forEach(relatedType => {
              let layer: RelationshipVisualizionLayer = dataSource.createLayer(this.relationship.label.localizedValue + " " + relatedType.label, true, this.typeLegend[relatedType.code].color) as RelationshipVisualizionLayer;
              layer.setRelatedTypeFilter(relatedType.code);

              if (layers.findIndex(l => l.getKey() === layer.getKey()) === -1) {
                  let existingRelatedType = existingRelatedTypes[relatedType.code];

                  if (existingRelatedType == null) {
                      layers.push(layer);
                  } else {
                      layer.rendered = existingRelatedType.rendered;
                      layers.splice(existingRelatedType.index, 1, layer);
                  }
              }
          });
        } else {
            let layer: RelationshipVisualizionLayer = dataSource.createLayer(this.relationship.label.localizedValue, true, ColorGen().hexString()) as RelationshipVisualizionLayer;

            if (layers.findIndex(l => l.getKey() === layer.getKey()) === -1) {
                layers.push(layer);
            }
        }

        this.geomService.setLayers(layers);
    }

    private getBoundsAsWKT(): string {
        let wktBounds: string = null;

        if (this.params.bounds != null) {
            const mapBounds = new LngLatBounds(JSON.parse(this.params.bounds));
            wktBounds = this.convertBoundsToWKT(mapBounds);
        }

        return wktBounds;
    }

    private convertBoundsToWKT(bounds: LngLatBounds): string {
        let se = bounds.getSouthEast();
        let sw = bounds.getSouthWest();
        let nw = bounds.getNorthWest();
        let ne = bounds.getNorthEast();

        return "POLYGON ((" +
            se.lng + " " + se.lat + "," +
            sw.lng + " " + sw.lat + "," +
            nw.lng + " " + nw.lat + "," +
            ne.lng + " " + ne.lat + "," +
            se.lng + " " + se.lat +
            "))";
    }

    calculateTypeLegend(relatedTypes: [{ code: string, label: string }]) {
        let oldTypeLegend = this.typeLegend;
        this.typeLegend = {};

        // If we already have layers which are using specific colors then we want to use those same colors
        const layers = this.geomService.getLayers();

        relatedTypes.forEach(relatedType => {
            if (!this.typeLegend[relatedType.code]) {
                let color: string;

                let existingIndex = layers.findIndex(layer => layer instanceof RelationshipVisualizionLayer && (layer as RelationshipVisualizionLayer).getRelatedTypeFilter() === relatedType.code);

                if (existingIndex !== -1) {
                    color = layers[existingIndex].color;
                } else if (oldTypeLegend != null && oldTypeLegend[relatedType.code] != null) {
                    color = oldTypeLegend[relatedType.code].color;
                } else {
                    color = ColorGen().hexString();
                }

                this.typeLegend[relatedType.code] = { color: color, label: relatedType.label };
            }
        });

        if (!this.typeLegend[this.params.type]) {
            this.typeLegend[this.params.type] = { color: SELECTED_NODE_COLOR, label: this.params.type };
        }
    }

    collapseAnimation(id: string): Promise<void> {
        if (!this.params.type) { return new Promise<void>((resolve, reject) => { resolve(); }); }

        let activeEl = document.getElementById(id) as unknown as SVGGraphicsElement;
        if (!activeEl) { return new Promise<void>((resolve, reject) => { resolve(); }); }

        let bbox = this.getBBox(activeEl, true);

        let all = document.querySelectorAll("g.nodes > g");

        all.forEach((el: SVGGraphicsElement) => {
            if (el.id !== activeEl.id) {
                let bbox2 = this.getBBox(el, false);

                // let translate = "translate(" + (bbox.x - bbox2.x) + "," + (bbox.y - bbox2.y) + ")";
                // el.setAttribute("transform", translate);

                let animateTransform = document.createElementNS("http://www.w3.org/2000/svg", "animateTransform") as unknown as SVGAnimateTransformElement;

                animateTransform.setAttribute("attributeName", "transform");
                animateTransform.setAttribute("attributeType", "XML");
                animateTransform.setAttribute("type", "translate");
                animateTransform.setAttribute("fill", "freeze");
                // animateTransform.setAttribute("from", 0 + " " + 0);
                animateTransform.setAttribute("to", (bbox.x - bbox2.x) + " " + (bbox.y - bbox2.y));
                animateTransform.setAttribute("begin", "indefinite");
                animateTransform.setAttribute("additive", "replace");
                animateTransform.setAttribute("dur", COLLAPSE_ANIMATION_TIME + "ms");
                animateTransform.setAttribute("repeatCount", "0");

                el.appendChild(animateTransform);

                (animateTransform as any).beginElement(); // Tells the element to animate now
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
            }, COLLAPSE_ANIMATION_TIME);
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
        if (node.code !== this.params.code ||
            node.typeCode !== this.params.type) {
            let doIt = (resolve) => {
                this.collapseAnimation(node.id).then(() => {
                    resolve();
                });
            };

            this.nodeSelect.emit({ objectType: node.objectType, id: node.id.substring(2), code: node.code, typeCode: node.typeCode, selectAnimation: doIt });
        }
    }

    public error(err: HttpErrorResponse): void {
        ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
