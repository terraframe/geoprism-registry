import { Component, OnInit, OnDestroy, AfterViewInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Map, NavigationControl, AttributionControl, LngLatBounds } from "mapbox-gl";

import { GeoObjectType, ValueOverTime } from "@registry/model/registry";
// MapService IS REQUIRED to set the mapbox access token for the map
import { MapService, RegistryService, GeometryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";
import { ErrorHandler, GenericModalComponent } from "@shared/component";

import { LocalizationService, AuthService } from "@shared/service";
import { ContextLayer } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { LayerEvent } from "./layer-panel.component";

declare let acp: string;

const DEFAULT_COLOR = "#80cdc1";
const SELECTED_COLOR = "#800000";

@Component({
    selector: "dataset-location-manager",
    templateUrl: "./dataset-location-manager.component.html",
    styleUrls: ["./dataset-location-manager.css"]
})
export class DatasetLocationManagerComponent implements OnInit, AfterViewInit, OnDestroy {

    coordinate: {
        longitude: number,
        latitude: number
    } = { longitude: null, latitude: null };

    MODE = {
        VERSIONS: "VERSIONS",
        ATTRIBUTES: "ATTRIBUTES",
        HIERARCHY: "HIERARCHY"
    }

    toolsIconHover: boolean = false;
    datasetId: string;
    typeCode: string;
    readOnly: boolean = false;
    editOnly: boolean = false;
    isEdit: boolean = false;
    date: string;
    code: string;
    type: GeoObjectType;
    bsModalRef: BsModalRef;
    backReference: string;

    /*
     * mapbox-gl map
     */
    map: Map;

    vectorLayers: string[] = [];

    /*
     * List of base layers
     */
    baseLayers: any[] = [
        {
            name: "Satellite",
            label: "Satellite",
            id: "satellite-v9",
            sprite: "mapbox://sprites/mapbox/satellite-v9",
            url: "mapbox://mapbox.satellite",
            selected: true
        }
        //     {
        //       name: 'Streets',
        //       label: 'Streets',
        //       id: 'streets-v11',
        //       sprite: 'mapbox://sprites/mapbox/basic-v11',
        //       url: 'mapbox://styles/mapbox/streets-v11'
        //     }
    ];

    mode: string = null;
    isMaintainer: boolean;
    forDate: Date = new Date();

    vot: ValueOverTime;

    constructor(
        private mapService: MapService,
        public listService: ListTypeService,
        public geomService: GeometryService,
        public service: RegistryService,
        private modalService: BsModalService,
        private route: ActivatedRoute,
        private lService: LocalizationService,
        private dateService: DateService,
        private router: Router,
        authService: AuthService
    ) {
        this.isMaintainer = authService.isAdmin() || authService.isMaintainer();
    }

    ngOnInit(): void {
        this.datasetId = this.route.snapshot.params["datasetId"];
        this.typeCode = this.route.snapshot.params["typeCode"];
        this.date = this.route.snapshot.params["date"];
        this.readOnly = this.route.snapshot.params["readOnly"] === "true";
        this.editOnly = this.route.snapshot.params["editOnly"] === "true";
        this.backReference = this.route.snapshot.params["backReference"];

        if (this.route.snapshot.params["code"] != null) {
            this.code = this.route.snapshot.params["code"];
        }

        this.forDate = this.dateService.getDateFromDateString(this.date);

        this.service.getGeoObjectTypes([this.typeCode], null).then(types => {
            this.type = types[0];
            this.geomService.initialize(this.map, this.type.geometryType, !this.isEdit);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onPanelCancel(): void {
        if (this.backReference != null && this.backReference.length >= 2) {
            let ref = this.backReference.substring(0, 2);

            if (ref === "ML") {
                let published = this.backReference.substring(3, 3) === "T";
                let oid = this.backReference.substring(3);

                this.router.navigate(["/registry/master-list", oid]);
            }
        }

        this.showOriginalGeometry();
    }

    onPanelSubmit(applyInfo: { isChangeRequest: boolean, geoObject?: any, changeRequestId?: string }): void {
        // Save everything first
        this.geomService.saveEdits();

        this.bsModalRef = this.modalService.show(GenericModalComponent, { backdrop: true, class: "error-white-space-pre" });

        if (applyInfo.isChangeRequest) {
            const message = this.lService.decode("geoobject-editor.changerequest.submitted");
            const buttons = [];

            buttons.push({
                label: this.lService.decode("geoobject-editor.cancel.returnList"),
                onClick: () => { this.onPanelCancel(); },
                shouldClose: true,
                class: "btn-primary"
            });

            buttons.push({
                label: this.lService.decode("geoobject-editor.changerequest.view"),
                onClick: () => {
                    this.router.navigate(["/registry/change-requests", applyInfo.changeRequestId]);
                },
                shouldClose: true,
                class: "btn-default"
            });

            buttons.push({
                label: this.lService.decode("geoobject-editor.continueEditing"),
                onClick: () => { },
                shouldClose: true,
                class: "btn-default"
            });

            this.bsModalRef.content.init(message, buttons);
        } else {
            const message = this.lService.decode("geoobject-editor.edit.submitted");
            const buttons = [];

            buttons.push({
                label: this.lService.decode("geoobject-editor.cancel.returnList"),
                onClick: () => { this.onPanelCancel(); },
                shouldClose: true,
                class: "btn-primary"
            });

            buttons.push({
                label: this.lService.decode("geoobject-editor.continueEditing"),
                onClick: () => { },
                shouldClose: true,
                class: "btn-default"
            });

            this.bsModalRef.content.init(message, buttons);
        }
    }

    ngOnDestroy(): void {
        this.geomService.destroy();
    }

    ngAfterViewInit() {
        const layer = this.baseLayers[0];
        // eslint-disable-next-line no-import-assign
        this.map = new Map({
            container: "dataset-map",
            style: {
                version: 8,
                name: layer.name,
                metadata: {
                    "mapbox:autocomposite": true
                },
                sources: {
                    "mapbox-satellite": {
                        type: "raster",
                        url: layer.url,
                        tileSize: 256
                    }
                },
                sprite: layer.sprite,
                glyphs: window.location.protocol + "//" + window.location.host + acp + "/glyphs/{fontstack}/{range}.pbf",
                layers: [
                    //          {
                    //            "id": layer.id,
                    //            "type": 'raster',
                    //            "source": 'mapbox-satellite',
                    //          }
                ]
            },
            zoom: 2,
            attributionControl: false,
            center: [-78.880453, 42.897852]
        });

        this.map.on("load", () => {
            this.initMap();
        });
    }

    onModeChange(value: boolean): void {
        this.isEdit = value;
        this.geomService.destroy(false);
    }

    initMap(): void {
        if (this.code !== "__NEW__") {
            if (this.code == null && this.datasetId !== null) {
                this.listService.getBounds(this.datasetId).then(bounds => {
                    if (bounds) {
                        let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

                        let padding = 50;
                        let maxZoom = 20;

                        // Zoom level was requested to be reduced when displaying point types as per #420
                        if (this.type.geometryType === "POINT" || this.type.geometryType === "MULTIPOINT") {
                            padding = 100;
                            maxZoom = 12;
                        }

                        this.map.fitBounds(llb, { padding: padding, animate: false, maxZoom: maxZoom });
                    }
                });
            }
            else if (this.code != null && this.typeCode != null && this.date != null) {
                this.service.getGeoObjectBoundsAtDate(this.code, this.typeCode, this.date).then(bounds => {
                    if (bounds) {
                        let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

                        let padding = 50;
                        let maxZoom = 20;

                        // Zoom level was requested to be reduced when displaying point types as per #420
                        if (this.type.geometryType === "POINT" || this.type.geometryType === "MULTIPOINT") {
                            padding = 100;
                            maxZoom = 12;
                        }

                        this.map.fitBounds(llb, { padding: padding, animate: false, maxZoom: maxZoom });
                    }
                });
            }
        }

        this.map.on("style.load", () => {
            this.addLayers();
        });

        this.addLayers();

        // Add zoom and rotation controls to the map.
        this.map.addControl(new NavigationControl({ visualizePitch: true }));
        this.map.addControl(new AttributionControl({ compact: true }), "bottom-right");

        this.map.on("click", this.datasetId + "-points", (event: any) => {
            this.handleMapClickEvent(event);
        });

        this.map.on("click", this.datasetId + "-polygon", (event: any) => {
            this.handleMapClickEvent(event);
        });

        this.showOriginalGeometry();
    }

    showOriginalGeometry() {
        this.addVectorLayer(this.datasetId, DEFAULT_COLOR);
    }

    hideOriginalGeometry() {
        this.removeVectorLayer(this.datasetId);
    }

    addLayers(): void {
        this.map.addLayer({
            type: "raster",
            id: "satellite-map",
            source: "mapbox-satellite"
        });

        this.vectorLayers.forEach(vLayer => {
            this.addVectorLayer(vLayer, DEFAULT_COLOR);
        });
    }

    handleBasemapStyle(layer: any): void {
        if (layer.id === "streets-v11") {
            this.map.setStyle(layer.url);
        } else if (layer.id === "satellite-v9") {
            this.map.setStyle({
                version: 8,
                name: layer.name,
                metadata: {
                    "mapbox:autocomposite": true
                },
                sources: {
                    "mapbox-satellite": {
                        type: "raster",
                        url: layer.url,
                        tileSize: 256
                    }
                },
                sprite: layer.sprite,
                glyphs: window.location.protocol + "//" + window.location.host + acp + "/glyphs/{fontstack}/{range}.pbf",
                layers: [
                    {
                        id: layer.id,
                        type: "raster",
                        source: "mapbox-satellite"
                    }
                ]
            });
        }
    }

    onContextLayerChange(event: LayerEvent): void {
        const layer = event.layer;

        if (layer.active) {
            this.addVectorLayer(layer.oid, layer.color, event.prevLayer);
        } else {
            this.removeVectorLayer(layer.oid);
        }
    }

    onReorderLayers(layers: ContextLayer[]): void {
        for (let i = layers.length - 1; i > -1; i--) {
            const layer = layers[i];

            this.map.moveLayer(layer.oid + "-polygon", this.datasetId + "-polygon");
            this.map.moveLayer(layer.oid + "-points", this.datasetId + "-polygon");
            this.map.moveLayer(layer.oid + "-label", this.datasetId + "-polygon");
        };
    }

    removeVectorLayer(source: string): void {
        const index = this.vectorLayers.indexOf(source);

        if (index !== -1) {
            this.map.removeLayer(source + "-polygon");
            this.map.removeLayer(source + "-points");

            if (source === this.datasetId) {
                this.map.removeLayer(source + "-polygon-selected");
                this.map.removeLayer(source + "-points-selected");
            }

            this.map.removeLayer(source + "-label");
            this.map.removeSource(source);

            this.vectorLayers.splice(index, 1);
        }
    }

    addVectorLayer(source: string, color: string, otherLayer?: ContextLayer): void {
        const index = this.vectorLayers.indexOf(source);

        if (index === -1) {
            const prevLayer = otherLayer != null ? otherLayer.oid + '-polygon' : (source !== this.datasetId) ? this.datasetId + "-polygon" : null;

            let protocol = window.location.protocol;
            let host = window.location.host;

            this.map.addSource(source, {
                type: "vector",
                tiles: [protocol + "//" + host + acp + "/list-type/tile?x={x}&y={y}&z={z}&config=" + encodeURIComponent(JSON.stringify({ oid: source }))]
            });

            // Polygon layer
            this.map.addLayer({
                id: source + "-polygon",
                type: "fill",
                source: source,
                "source-layer": "context",
                layout: {},
                paint: {
                    "fill-color": color,
                    "fill-opacity": 0.8,
                    "fill-outline-color": "black"
                },
                filter: ["all",
                    ["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
                ]
            }, prevLayer);

            // Point layer
            this.map.addLayer({
                id: source + "-points",
                type: "circle",
                source: source,
                "source-layer": "context",
                paint: {
                    "circle-radius": 10,
                    "circle-color": color,
                    "circle-stroke-width": 2,
                    "circle-stroke-color": "#FFFFFF"
                },
                filter: ["all",
                    ["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
                ]
            }, prevLayer);

            // Selected object layers
            if (source === this.datasetId) {
                this.map.addLayer({
                    id: source + "-points-selected",
                    type: "circle",
                    source: source,
                    "source-layer": "context",
                    paint: {
                        "circle-radius": 10,
                        "circle-color": SELECTED_COLOR,
                        "circle-stroke-width": 2,
                        "circle-stroke-color": "#FFFFFF"
                    },
                    filter: ["all",
                        ["==", ["get", "code"], this.code != null ? this.code : ""],
                        ["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
                    ]
                }, prevLayer);

                this.map.addLayer({
                    id: source + "-polygon-selected",
                    type: "fill",
                    source: source,
                    "source-layer": "context",
                    layout: {},
                    paint: {
                        "fill-color": SELECTED_COLOR,
                        "fill-opacity": 0.8,
                        "fill-outline-color": "black"
                    },
                    filter: ["all",
                        ["==", ["get", "code"], this.code != null ? this.code : ""],
                        ["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
                    ]
                }, prevLayer);
            }

            // Label layer
            this.map.addLayer({
                id: source + "-label",
                source: source,
                "source-layer": "context",
                type: "symbol",
                paint: {
                    "text-color": "black",
                    "text-halo-color": "#fff",
                    "text-halo-width": 2
                },
                layout: {
                    "text-field": ["case",
                        ["has", "displayLabel_" + navigator.language.toLowerCase()],
                        ["coalesce", ["string", ["get", "displayLabel_" + navigator.language.toLowerCase()]], ["string", ["get", "displayLabel"]]],
                        ["string", ["get", "displayLabel"]]
                    ],
                    "text-font": ["NotoSansRegular"],
                    "text-offset": [0, 0.6],
                    "text-anchor": "top",
                    "text-size": 12
                }
            }, prevLayer);

            this.vectorLayers.push(source);
        }
    }

    onFeatureChange(): void {
        // Refresh the layer
        this.hideOriginalGeometry();
        this.showOriginalGeometry();
    }

    handleMapClickEvent(event: any): void {
        if (!this.isEdit && event.features != null && event.features.length > 0) {
            const feature = event.features[0];

            if (feature.properties.code != null && this.code !== feature.properties.code) {
                this.code = feature.properties.code;

                // Update the filter properties
                this.map.setFilter(this.datasetId + "-points-selected", ["all",
                    ["==", ["get", "code"], this.code != null ? this.code : ""],
                    ["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
                ]);

                this.map.setFilter(this.datasetId + "-polygon-selected", ["all",
                    ["==", ["get", "code"], this.code != null ? this.code : ""],
                    ["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
                ]);
            }
        }
    }

    onNewGeoObject(): void {
        this.code = "__NEW__";
    }

    formatDate(date: Date): string {
        return this.dateService.formatDateForDisplay(date);
    }

    public error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
