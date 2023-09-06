///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

/* eslint-disable indent */
import { Component, OnInit, Output, EventEmitter, OnDestroy } from "@angular/core";
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
import { LngLatBounds } from "maplibre-gl";
import { ObjectReference, RelatedType, Relationship, TreeData, Vertex } from "@registry/model/graph";
import { LocationManagerState } from "../location-manager/location-manager.component";
import { Layer, RelationshipVisualizionDataSource, RelationshipVisualizionLayer, RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE } from "@registry/service/layer-data-source";

import { calculateTextWidth } from "@registry/component/hierarchy/d3/svg-util";

export const DRAW_SCALE_MULTIPLIER: number = 1.0;

export const SELECTED_NODE_COLOR: string = "#4287f5";

export const GRAPH_GO_LABEL_COLOR: string = "black";
export const GRAPH_CIRCLE_FILL: string = "#999";
export const GRAPH_LINE_COLOR: string = "#999";

export const COLLAPSE_ANIMATION_TIME: number = 500; // in ms

export const DIMENSIONS = {
    NODE: { WIDTH: 30, HEIGHT: 30 },
    LABEL: { WIDTH: 170, HEIGHT: 60, FONTSIZE: 14 },
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
export class RelationshipVisualizerComponent implements OnInit, OnDestroy {

    // Hack to allow the constant to be used in the html
    CONSTANTS = {
        OVERLAY: OverlayerIdentifier.VISUALIZER_PANEL,
        ORIENTATION: Orientation
    }

    state: LocationManagerState = {};

    @Output() nodeSelect = new EventEmitter<Vertex>();

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

    stateSub: Subscription;

    panelOpen: boolean = true;

    loading: boolean = true;

    restrictToMapBounds: boolean = false;

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

        this.stateSub = this.geomService.stateChange$.subscribe(state => this.stateChange(state));

        // Angular keeps invoking our listener in the early stages of component loading. We don't want to make expensive
        // data requests unless we're certain that all the state are loaded.
        window.setTimeout(() => {
            this.loading = false;

            this.stateChange(this.geomService.getState());
        }, 10);
    }

    ngOnDestroy(): void {
        this.stateSub.unsubscribe();
    }

    stateChange(state) {
        if (state.type == null || state.code == null) {
            return;
        }

        let newState = JSON.parse(JSON.stringify(state));
        let oldState = JSON.parse(JSON.stringify(this.state));
        this.state = newState;

        this.panelOpen = newState.graphPanelOpen === "true";

        if (newState.graphOid && newState.graphOid !== oldState.graphOid && this.relationships != null) {
            this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.graphOid)];
        }

        if (!this.loading) {
            if (this.relationships == null || this.relationship == null || newState.objectType !== oldState.objectType || newState.type !== oldState.type) {
                this.relationships = null;
                this.graphOid = null;
                this.data = null;
                this.fetchRelationships();
            } else if (this.relationships != null && this.relationship && ((this.restrictToMapBounds && newState.bounds !== oldState.bounds) || newState.code !== oldState.code || newState.date !== oldState.date || newState.uid !== oldState.uid || newState.graphOid !== oldState.graphOid)) {
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
        if (this.state.type != null) {
            this.relationships = [];
            this.spinner.show(this.CONSTANTS.OVERLAY);

            this.vizService.relationships(this.state.objectType, this.state.type).then(relationships => {
                this.relationships = relationships;

                if (this.relationships && this.relationships.length > 0) {
                    if (!this.state.graphOid || this.relationships.findIndex(rel => rel.oid === this.state.graphOid) === -1) {
                        // If we got here by selecting a business object from a GeoObject
                        if (this.relationship != null && this.relationship.code === "BUSINESS" && this.state.objectType === "BUSINESS" && this.relationships.findIndex(rel => rel.code === "GEOOBJECT") !== -1) {
                            // Then we can default to the "Associated GeoObjects" relationship
                            this.relationship = this.relationships[this.relationships.findIndex(rel => rel.code === "GEOOBJECT")];
                        } else if (this.relationship != null && this.relationship.code === "GEOOBJECT" && this.state.objectType === "GEOOBJECT" && this.relationships.findIndex(rel => rel.code === "BUSINESS") !== -1) {
                            // Then we can default to the "Associated Business Objects" relationship
                            this.relationship = this.relationships[this.relationships.findIndex(rel => rel.code === "BUSINESS")];
                        } else {
                            // We have no idea which relationship makes the most sense. Just pick the first one
                            this.relationship = this.relationships[0];
                        }

                        this.graphOid = this.relationship.oid;
                        this.onSelectRelationship();
                    } else {
                        this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.state.graphOid)];
                        this.graphOid = this.state.graphOid;
                        this.fetchData();
                    }
                } else {
                    this.relationship = null;
                }
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            }).finally(() => {
                this.spinner.hide(this.CONSTANTS.OVERLAY);
            });
        }
    }

    onSelectRelationship(): void {
        this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.graphOid)];

        //   this.fetchData();

        let newState = { graphOid: this.graphOid };

        this.geomService.setState(newState, false);
    }

    fetchData(): void {
        if (this.relationship != null) {
            this.spinner.show(this.CONSTANTS.OVERLAY);

            let source = { code: this.state.code, typeCode: this.state.type, objectType: this.state.objectType } as Vertex;

            this.vizService.tree(this.relationship.type, this.relationship.code, source, this.state.date, this.getBoundsAsWKT()).then(data => {
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

    private addLayers(relatedTypes: RelatedType[]) {
        if (this.relationship.type === "BUSINESS" || (this.state.objectType === "BUSINESS" && this.relationship.type !== "GEOOBJECT")) {
            let layers: Layer[] = this.geomService.getLayers().filter(layer => layer.getPinned() || layer.dataSource.getDataSourceType() !== RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE);
            this.geomService.setLayers(layers);
            return;
        }

        let layers: Layer[] = this.geomService.getLayers();

        let sourceObject = { code: this.state.code, typeCode: this.state.type, objectType: this.state.objectType } as ObjectReference;
        let bounds = this.restrictToMapBounds ? this.state.bounds : null;
        let dataSource = new RelationshipVisualizionDataSource(this.vizService, this.geomService, this.relationship.type, this.relationship.code, sourceObject, bounds, this.state.date);

        // Remove any existing layer from map that is graph related that isn't part of this new data
        layers = layers.filter(layer => layer.getPinned() ||
            layer.dataSource.getDataSourceType() !== RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE ||
            ((layer.dataSource as RelationshipVisualizionDataSource).getRelationshipCode() === this.relationship.code && (layer.dataSource as RelationshipVisualizionDataSource).getRelationshipType() === this.relationship.type &&
                (relatedTypes.map(relatedType => relatedType.code).indexOf((layer as RelationshipVisualizionLayer).getRelatedTypeFilter()) !== -1)));

        // If the type is already rendered at a specific position in the layer stack, we want to preserve that positioning and overwrite any layer currently in that position
        let existingRelatedTypes: { [key: string]: { index: number, layer: Layer } } = {};
        for (let i = 0; i < layers.length; ++i) {
            if (layers[i].dataSource.getDataSourceType() === RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE) {
                let layer: RelationshipVisualizionLayer = layers[i] as RelationshipVisualizionLayer;

                existingRelatedTypes[layer.getRelatedTypeFilter()] = { index: i, layer: layer };
            }
        }

        relatedTypes.forEach(relatedType => {
            if (relatedType.objectType === "GEOOBJECT") {
                let layer: RelationshipVisualizionLayer = dataSource.createLayer(this.relationship.label.localizedValue + " " + relatedType.label, true, this.typeLegend[relatedType.code].color) as RelationshipVisualizionLayer;
                layer.setRelatedTypeFilter(relatedType.code);

                if (layers.findIndex(l => l.getKey() === layer.getKey()) === -1) {
                    //if (layers.findIndex(l => l.legendLabel === layer.legendLabel) === -1) {
                    let existingRelatedType = existingRelatedTypes[relatedType.code];

                    if (existingRelatedType == null || existingRelatedType.layer.getPinned()) {
                        layers.push(layer);
                    } else {
                        layer.rendered = existingRelatedType.layer.rendered;
                        layers.splice(existingRelatedType.index, 1, layer);
                    }
                    /*
                } else {
                    // TODO : This is definitely a hack. But I can't get zooming to work up and down rivers with the 'flows through'
                    //        relationship without doing it this way since this way doesn't interrupt zooming behaviour
                    window.setTimeout(() => {
                        let existingLayer = layers[layers.findIndex(l => l.legendLabel === layer.legendLabel)] as RelationshipVisualizionLayer;

                        dataSource.getLayerData().then((data) => {
                            let map = this.geomService.getMap();

                            if (map) {
                                let source = map.getSource(existingLayer.dataSource.getId());

                                if (source) {
                                    (<any> source).setData(data);
                                }
                            }
                        });
                    }, 10);
                    */
                }
            }
        });

        this.geomService.setLayers(layers);
    }

    private getBoundsAsWKT(): string {
        let wktBounds: string = null;

        if (this.state.bounds != null && this.restrictToMapBounds) {
            const mapBounds = new LngLatBounds(JSON.parse(this.state.bounds));
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

    calculateTypeLegend(relatedTypes: RelatedType[]) {
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

        if (!this.typeLegend[this.state.type]) {
            this.typeLegend[this.state.type] = { color: SELECTED_NODE_COLOR, label: this.state.type };
        }
    }

    collapseAnimation(id: string): Promise<void> {
        if (!this.state.type) { return new Promise<void>((resolve, reject) => { resolve(); }); }

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
        if ((node.code !== this.state.code ||
            node.typeCode !== this.state.type) && node.readable) {
            let doIt = (resolve) => {
                this.collapseAnimation(node.id).then(() => {
                    resolve();
                });
            };

            this.nodeSelect.emit({
                objectType: node.objectType,
                id: node.id.substring(2),
                code: node.code,
                typeCode: node.typeCode,
                label: node.label,
                readable: node.readable,
                selectAnimation: doIt
            } as any);
        }
    }

    public getLabelWidth(node: any) {
        if (this.relationship.layout === "HORIZONTAL" && node.relation === "SELECTED") {
            return Math.min(DIMENSIONS.LABEL.WIDTH, calculateTextWidth(node.label, DIMENSIONS.LABEL.FONTSIZE, "svg.ngx-charts")) + DIMENSIONS.PADDING.NODE_LABEL;
        } else {
            return DIMENSIONS.LABEL.WIDTH + DIMENSIONS.PADDING.NODE_LABEL;
        }
    }

    public error(err: HttpErrorResponse): void {
        ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
