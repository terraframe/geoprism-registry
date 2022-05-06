/* eslint-disable indent */
import { Component, OnInit, Input, Output, SimpleChanges, EventEmitter } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService } from "ngx-bootstrap/modal";

import { ErrorHandler } from "@shared/component";

import { GeoObjectTypeCache } from "@registry/model/registry";
import { Subject, Subscription } from "rxjs";
import { RelationshipVisualizationService, RelationshipVisualizerDataSourceProvider } from "@registry/service/relationship-visualization.service";
import { Layout, Orientation } from "@swimlane/ngx-graph";

import { DagreNodesOnlyLayout } from "./relationship-viz-layout";

import * as shape from "d3-shape";
import { NgxSpinnerService } from "ngx-spinner";
import { OverlayerIdentifier } from "@registry/model/constants";
import * as ColorGen from "color-generator";
import { RegistryCacheService } from "@registry/service/registry-cache.service";
import { GeometryService, ParamLayer } from "@registry/service";
import { Router, ActivatedRoute } from "@angular/router";
import { LngLatBounds } from "mapbox-gl";
import { Relationship, RELATIONSHIP_VISUALIZER_LAYER_DATASET_PROVIDER, TreeData } from "@registry/model/graph";
import { LocationManagerParams } from "../location-manager/location-manager.component";

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

    @Output() changeGeoObject = new EventEmitter<{ id: string, code: string, typeCode: string, doIt: any }>();

    @Output() changeRelationship = new EventEmitter<string>();

    public DIMENSIONS = DIMENSIONS;

    public SELECTED_NODE_COLOR = SELECTED_NODE_COLOR;

    public svgHeight: number = null;
    public svgWidth: number = null;

    panToNode$: Subject<string> = new Subject();

    update$: Subject<boolean> = new Subject();

    public layout: Layout = new DagreNodesOnlyLayout();

    public curve = shape.curveLinear;

    public colorSchema: any = {};

    public typeCache: GeoObjectTypeCache;

    relationship: Relationship = null;
    relationships: Relationship[];

    data: TreeData = null;

    public dataSourceProvider: RelationshipVisualizerDataSourceProvider;

    loadTreeDataSub: Subscription;
    fetchTreeDataSub: Subscription;

    loadRelationshipDataSub: Subscription;
    fetchRelationshipDataSub: Subscription;

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

        this.dataSourceProvider = this.vizService.getDataSourceProvider(this.geomService);

        this.loadTreeDataSub = this.dataSourceProvider.onLoadTreeData.subscribe((data) => {
            this.spinner.hide(this.CONSTANTS.OVERLAY);

            this.data = null;

            window.setTimeout(() => {
                this.data = data;

                this.resizeDimensions();
                this.calculateColorSchema();
                this.addLayers(this.data.geoJson);
            }, 0);

            this.resizeDimensions();
        });

        this.fetchTreeDataSub = this.dataSourceProvider.onFetchTreeData.subscribe(() => {
            this.spinner.show(this.CONSTANTS.OVERLAY);
        });

        this.loadRelationshipDataSub = this.dataSourceProvider.onLoadRelationshipData.subscribe((relationships) => {
            this.spinner.hide(this.CONSTANTS.OVERLAY);

            this.relationships = null;

            window.setTimeout(() => { // If we don't set a timeout here then angular html doesn't update properly
                this.relationships = relationships;

                if (this.relationships && this.relationships.length > 0) {
                    if (!this.params.graphOid || this.relationships.findIndex(rel => rel.oid === this.params.graphOid) === -1) {
                        this.relationship = this.relationships[0];
                        this.params.graphOid = this.relationship.oid;
                        // this.changeRelationship.emit(this.params.graphOid);

                        this.router.navigate([], {
                            relativeTo: this.route,
                            queryParams: { graphOid: this.params.graphOid },
                            queryParamsHandling: "merge" // remove to replace all query params by provided
                        });
                    } else {
                        this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.params.graphOid)];
                        this.params.graphOid = this.relationship.oid;
                    }
                }

                if (this.data == null) {
                    this.dataSourceProvider.fetchData();
                }
            }, 0);
        });

        this.fetchRelationshipDataSub = this.dataSourceProvider.onFetchRelationshipData.subscribe(() => {
            this.spinner.show(this.CONSTANTS.OVERLAY);
        });

        this.onFetchErrorSub = this.dataSourceProvider.onFetchError.subscribe((err) => {
            this.spinner.hide(this.CONSTANTS.OVERLAY);
            this.error(err);
        });

        this.queryParamSub = this.route.queryParams.subscribe((params) => { this.queryParamChanges(params); });

        // Angular keeps invoking our queryParamChanges in the early stages of component loading. We don't want to make expensive
        // data requests unless we're certain that all the params are loaded.
        window.setTimeout(() => {
            this.loading = false;

            this.data = this.dataSourceProvider.treeData;
            this.relationships = this.dataSourceProvider.relationships;
            this.relationship = this.dataSourceProvider.relationship;
            if (this.data != null && this.relationship != null) {
                this.calculateColorSchema();
            }

            this.queryParamChanges(this.params);
        }, 10);
    }

    ngOnDestroy(): void {
        this.loadTreeDataSub.unsubscribe();
        this.fetchTreeDataSub.unsubscribe();
        this.loadRelationshipDataSub.unsubscribe();
        this.fetchRelationshipDataSub.unsubscribe();
        this.queryParamSub.unsubscribe();
    }

    queryParamChanges(params) {
        if (params.type == null || params.code == null) {
            return;
        }

        params = JSON.parse(JSON.stringify(params));

        this.panelOpen = params.graphPanelOpen === "true";

        if (params.graphOid && params.graphOid !== this.params.graphOid) {
            this.params.graphOid = params.graphOid;

            this.onSelectRelationship(false);
        }

        if (!this.loading) {
          this.dataSourceProvider.queryParamChanges(params, this.params);
        }

        if (this.panelOpen) {
            window.setTimeout(() => {
                this.resizeDimensions();
            }, 1);
        }

        this.params = params;
    }

    resizeDimensions():void {
        let graphContainer = document.getElementById("graph-container");

        if (graphContainer) {
            this.svgHeight = graphContainer.clientHeight - 50;
            this.svgWidth = graphContainer.clientWidth;
        }
    }

    // Thanks to https://stackoverflow.com/questions/52172067/create-svg-hexagon-points-with-only-only-a-length
    public getHexagonPoints(node: { dimension: { width: number, height: number }, relation: string }): string {
        let y = (this.DIMENSIONS.LABEL.HEIGHT / 2) - this.DIMENSIONS.NODE.HEIGHT / 2;
        let x = this.relationship.isHierarchy
            ? (node.relation === "CHILD" ? (this.DIMENSIONS.LABEL.WIDTH / 2 - this.DIMENSIONS.NODE.WIDTH / 2) : (this.DIMENSIONS.LABEL.WIDTH + DIMENSIONS.PADDING.NODE_LABEL + this.DIMENSIONS.NODE.WIDTH) / 2 - this.DIMENSIONS.NODE.WIDTH / 2)
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

    private onSelectRelationship(updateUrl: boolean = true) {
        if (this.relationships == null) {
            return;
        }

        this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.params.graphOid)];

        this.dataSourceProvider.relationship = this.relationship;

        if (!this.loading) {
            this.dataSourceProvider.fetchData();
        }

        this.changeRelationship.emit(this.params.graphOid);

        if (updateUrl) {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { graphOid: this.params.graphOid },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            });
        }
    }

    private addLayers(typeCollections: any) {
        let layers: ParamLayer[] = this.geomService.serializeAllLayers();

        // Remove any existing layer from map that is graph related that isn't part of this new data
        layers = layers.filter(layer => layer.dataSourceProviderId !== RELATIONSHIP_VISUALIZER_LAYER_DATASET_PROVIDER ||
            (
              layer.oid.split("-").length === 3 &&
              Object.keys(typeCollections).indexOf(layer.oid.split("-")[1]) !== -1 &&
              layer.oid.split("-")[2] === this.relationship.code
            )
        );

        for (const [typeCode] of Object.entries(typeCollections)) {
          let oid = "GRAPH-" + typeCode + "-" + this.relationship.code;

          if (layers.findIndex(l => l.oid === oid) === -1) {
              layers.splice(0, 0, new ParamLayer(oid, this.relationship.label.localizedValue + " " + typeCode, true, this.colorSchema[typeCode], oid, RELATIONSHIP_VISUALIZER_LAYER_DATASET_PROVIDER));
          }
        }

        this.geomService.setLayers(layers);
    }

    calculateColorSchema() {
        this.colorSchema = {};

        // If we already have layers which are using specific colors then we want to use those same colors
        const layers = this.geomService.getLayers();

        this.data.verticies.forEach(vertex => {
            if (!this.colorSchema[vertex.typeCode]) { // vertex.id.substring(2) !== this.geoObject.properties.uid &&
                let existingIndex = layers.findIndex(layer => layer.oid === "GRAPH-" + vertex.typeCode + "-" + this.relationship.code);

                if (existingIndex !== -1) {
                    this.colorSchema[vertex.typeCode] = layers[existingIndex].color;
                } else {
                    this.colorSchema[vertex.typeCode] = ColorGen().hexString();
                }
            }
        });

        if (!this.colorSchema[this.params.type]) {
            this.colorSchema[this.params.type] = SELECTED_NODE_COLOR;
        }
    }

    collapseAnimation(id: string): Promise<void> {
        if (!this.params.code) { return new Promise<void>((resolve, reject) => { resolve(); }); }

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
        if (node.code !== this.params.code &&
            node.typeCode !== this.params.type) {
            let doIt = (resolve) => {
                this.collapseAnimation(node.id).then(() => {
                    resolve();
                });
            };

            this.changeGeoObject.emit({ id: node.id.substring(2), code: node.code, typeCode: node.typeCode, doIt: doIt });
        }
    }

    public error(err: HttpErrorResponse): void {
        ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
