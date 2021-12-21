import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";
import { Map, LngLatBoundsLike, NavigationControl, AttributionControl, IControl, LngLatBounds } from "mapbox-gl";

import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { AllGeoJSON } from "@turf/helpers";
import bbox from "@turf/bbox";

import { GeoObject } from "@registry/model/registry";
import { ModalState } from "@registry/model/location-manager";

import { MapService, RegistryService, GeometryService } from "@registry/service";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler, ConfirmModalComponent, SuccessModalComponent } from "@shared/component";

import { AuthService, LocalizationService } from "@shared/service";
import { ContextLayer, LayerRecord } from "@registry/model/list-type";
import { GRAPH_LAYER, LayerEvent } from "./layer-panel.component";
import { ListTypeService } from "@registry/service/list-type.service";
import { timeout } from "d3-timer";
import { Subscription } from "rxjs";
import { SelectTypeModalComponent } from "./select-type-modal.component";

import { GeoRegistryConfiguration } from "@core/model/registry";
declare let registry: GeoRegistryConfiguration;

const SELECTED_COLOR = "#800000";

@Component({
    selector: "location-manager",
    templateUrl: "./location-manager.component.html",
    styleUrls: ["./location-manager.css"]
})
export class LocationManagerComponent implements OnInit, AfterViewInit, OnDestroy {

    coordinate: {
        longitude: number,
        latitude: number
    } = { longitude: null, latitude: null };

    MODE: ModalState = {
        SEARCH: 0,
        VIEW: 1
    }

    bsModalRef: BsModalRef;

    /*
     * Search results from the server
     */
    data: GeoObject[] = [];


    state: {
        text: string,
        currentText: string,
        date: string,
        currentDate: string
    } = { text: '', currentText: '', date: '', currentDate: '' }

    /*
     * Currently selected record
     */
    record: LayerRecord;

    /*
     * Currently highlighted feature
     */
    feature: any;

    /*
     * Flag denoting if an object is currently being editted
     */
    isEdit: boolean = false;

    /*
     * mapbox-gl map
     */
    map: Map;

    /*
     *  Mode used to determine what is being show on the left hand panel
     */
    mode: number = this.MODE.SEARCH;

    /*
    *  Flag to indicate if the left handle panel should be displayed or not
     */
    showPanel: boolean = false;

    layers: ContextLayer[] = [];

    backReference: string;

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
    ];

    preventSingleClick: boolean = false;

    /*
     * Timer for determining double click vs single click
     */
    timer: any;

    /*
     * URL pamaters of the component
     */
    params: any = null;

    /*
    * Subscription for changes to the URL parameters
    */
    subscription: Subscription;

    // Flag denoting if the map in loaded and initialized     
    ready: boolean = false;

    // Flag denoting if the search and results panel is enabled at all
    searchEnabled: boolean = true;

    @ViewChild("simpleEditControl") simpleEditControl: IControl;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private modalService: BsModalService,
        private service: RegistryService,
        private listService: ListTypeService,
        private mapService: MapService,
        private geomService: GeometryService,
        private lService: LocalizationService,
        private authService: AuthService) { }

    ngOnInit(): void {

        this.subscription = this.route.queryParams.subscribe(params => {
            this.handleParameterChange(params);
        });

        this.searchEnabled = registry.searchEnabled && (this.authService.isRC(false) || this.authService.isRM() || this.authService.isRA());
    }

    ngOnDestroy(): void {
        this.geomService.destroy();
        this.subscription.unsubscribe();
    }

    ngAfterViewInit() {
        const layer = this.baseLayers[0];

        this.map = new Map({
            container: "map",
            style: {
                version: 8,
                name: layer.name,
                metadata: {
                    "mapbox:autocomposite": true
                },
                sources: {
                    mapbox: {
                        type: "raster",
                        url: layer.url,
                        tileSize: 256
                    }
                },
                sprite: layer.sprite,
                glyphs: window.location.protocol + "//" + window.location.host + registry.contextPath + "/glyphs/{fontstack}/{range}.pbf",
                layers: [
                    {
                        id: layer.id,
                        type: "raster",
                        source: "mapbox"
                        // "source-layer": "mapbox_satellite_full"
                    }
                ]
            },
            zoom: 2,
            attributionControl: false,
            center: [-78.880453, 42.897852]
        });

        this.map.on("load", () => {
            this.ready = true;

            this.initMap();
        });

        if (this.simpleEditControl) {
            this.map.addControl(this.simpleEditControl);
        }
    }

    /**
    * 
    * Method responsible for parsing the state from the URL parameters and determining if
    * the model of the widget needs to be updated or not.
    *  
    * */
    handleParameterChange(params: Params): void {
        this.params = params;

        if (this.ready) {

            let mode = this.MODE.SEARCH;
            let showPanel = false;

            if (this.params != null) {

                // Handle parameters for searching for a geo object
                if (this.params.text != null) {
                    if (this.params.text != this.state.currentText || this.params.date != this.state.currentDate) {
                        this.state.text = this.params.text;
                        this.state.date = this.params.date;

                        this.handleSearch(this.params.text, this.params.date);
                    }

                    showPanel = true;
                }

                // Handle parameters for selecting a geo object
                if (this.params.type != null && this.params.code != null) {

                    if (this.record == null || this.record.type == null || this.record.type.code != this.params.type || this.record.code != this.params.code) {
                        this.handleSelect(this.params.type, this.params.code, this.params.uid);
                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

                // Handle parameters for select a record from a context layer
                if (this.params.version != null && this.params.uid != null) {

                    if (this.record == null || this.feature == null || this.feature.source != this.params.version || this.feature.id != this.params.uid) {
                        this.handleRecord(this.params.version, this.params.uid);

                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

            }

            this.changeMode(mode);
            this.setPanel(showPanel);
        }
    }

    setPanel(showPanel: boolean): void {
        if (this.showPanel !== showPanel) {
            this.showPanel = showPanel;

            timeout(() => {
                this.map.resize();
            }, 1);

        }
    }

    togglePanel(): void {
        this.setPanel(!this.showPanel);
    }

    changeMode(mode: number): void {
        this.mode = mode;

        if (this.isEdit) {
            this.geomService.destroy(false);
        }

        if (this.mode === this.MODE.SEARCH) {
            this.isEdit = false;

            if (this.feature != null) {
                this.map.removeFeatureState(this.feature);
            }

            this.record = null;
            this.feature = null;
        }
    }

    onModeChange(value: boolean): void {
        this.isEdit = value;
    }

    initMap(): void {
        this.map.on("style.load", () => {
            this.addLayers();
        });

        this.addLayers();

        // Add zoom and rotation controls to the map.
        this.map.addControl(new NavigationControl({ visualizePitch: true }), "bottom-right");
        this.map.addControl(new AttributionControl({ compact: true }), "bottom-right");

        this.map.on("click", (event: any) => {
            this.handleMapClickEvent(event);
        });

        this.handleParameterChange(this.params);
    }

    onZoomTo(oid: string): void {
        this.listService.getBounds(oid).then(bounds => {
            if (bounds && Array.isArray(bounds)) {

                let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

                this.map.fitBounds(llb, { padding: 50, animate: true, maxZoom: 20 });
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreate(layer: ContextLayer): void {
        if (!this.isEdit) {

            this.listService.getVersion(layer.oid).then(version => {
                if (!version.isAbstract) {
                    this.select({
                        properties: {
                            type: version.typeCode,
                            code: '__NEW__'
                        }
                    }, null);
                }
                else {
                    this.bsModalRef = this.modalService.show(SelectTypeModalComponent, {
                        animated: true,
                        backdrop: true,
                        ignoreBackdropClick: true
                    });
                    this.bsModalRef.content.init(version, typeCode => {
                        this.select({
                            properties: {
                                type: typeCode,
                                code: '__NEW__'
                            }
                        }, null);
                    });
                }
            });
        }
    }

    handleMapClickEvent(e: any): void {
        if (!this.isEdit) {
            const features = this.map.queryRenderedFeatures(e.point);

            if (features != null && features.length > 0) {
                const feature = features[0];

                if (feature.properties.uid != null) {
                    if (feature.source === GRAPH_LAYER) {
                        this.select(feature, null);
                    }
                    else {
                        this.router.navigate([], {
                            relativeTo: this.route,
                            queryParams: { type: null, code: null, version: feature.source, uid: feature.properties.uid },
                            queryParamsHandling: 'merge', // remove to replace all query params by provided
                        });
                    }
                }
            }
        }
    }

    onPanelCancel(): void {
        this.clearRecord();
    }

    onPanelSubmit(applyInfo: { isChangeRequest: boolean, geoObject?: any, changeRequestId?: string }): void {
        // Save everything first
        this.geomService.saveEdits();

        if (applyInfo.isChangeRequest) {
            if (this.backReference != null && this.backReference.length >= 2 && this.backReference.substring(0, 2) === "CR") {
                this.bsModalRef = this.modalService.show(SuccessModalComponent, { backdrop: true, class: "error-white-space-pre" });

                this.bsModalRef.content.message = this.lService.decode("geoobject-editor.changerequest.submitted");
                this.bsModalRef.content.submitText = this.lService.decode("geoobject-editor.changerequest.view");

                this.bsModalRef.content.onConfirm.subscribe(() => {
                    this.router.navigate(["/registry/change-requests", applyInfo.changeRequestId]);
                });
            } else {
                this.bsModalRef = this.modalService.show(ConfirmModalComponent, { backdrop: true, class: "error-white-space-pre" });

                this.bsModalRef.content.message = this.lService.decode("geoobject-editor.changerequest.submitted");
                this.bsModalRef.content.submitText = this.lService.decode("geoobject-editor.changerequest.view");
                this.bsModalRef.content.cancelText = this.lService.decode("geoobject-editor.cancel.returnExplorer");

                this.bsModalRef.content.onConfirm.subscribe(() => {
                    this.router.navigate(["/registry/change-requests", applyInfo.changeRequestId]);
                });
                this.bsModalRef.content.onCancel.subscribe(() => {
                    this.clearRecord();
                });
            }
        } else {
            this.bsModalRef = this.modalService.show(SuccessModalComponent, { backdrop: true, class: "error-white-space-pre" });

            this.bsModalRef.content.message = this.lService.decode("geoobject-editor.edit.submitted");
            this.bsModalRef.content.submitText = this.lService.decode("geoobject-editor.cancel.returnExplorer");

            this.bsModalRef.content.onConfirm.subscribe(() => {
                this.onPanelCancel();
            });
        }
    }

    addLayers(): void {

        this.layers.forEach(cLayer => {
            this.addLayer(cLayer);
        });
    }

    handleBasemapStyle(layer: any): void {
        // this.map.setStyle('mapbox://styles/mapbox/' + layer.id);

        this.baseLayers.forEach(baseLayer => {
            baseLayer.selected = false;
        });

        layer.selected = true;

        this.map.setStyle({
            version: 8,
            name: layer.name,
            metadata: {
                "mapbox:autocomposite": true
            },
            sources: {
                mapbox: {
                    type: "raster",
                    url: layer.url,
                    tileSize: 256
                }
            },
            sprite: layer.sprite,
            glyphs: window.location.protocol + "//" + window.location.host + registry.contextPath + "/glyphs/{fontstack}/{range}.pbf",
            layers: [
                {
                    id: layer.id,
                    type: "raster",
                    source: "mapbox"
                    // "source-layer": "mapbox_satellite_full"
                }
            ]
        });
    }

    search(): void {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { text: this.state.text, date: this.state.date, type: null, code: null, version: null, uid: null },
            queryParamsHandling: 'merge', // remove to replace all query params by provided
        });
    }

    handleSearch(text: string, date: string): void {
        this.geomService.destroy(false);
        this.mapService.search(text, date).then(data => {
            this.state.currentText = text;
            this.state.currentDate = date;

            this.showPanel = true;

            if (this.data.length > 0) {
                (<any>this.map.getSource(GRAPH_LAYER)).setData(data);
            }

            this.setData(data.features);


        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    zoomToFeature(node: GeoObject, event: MouseEvent): void {
        if (event != null) {
            event.stopPropagation();
        }

        this.preventSingleClick = false;
        const delay = 200;

        this.timer = setTimeout(() => {
            if (!this.preventSingleClick) {
                if (node && node.geometry != null) {
                    const bounds = bbox(node as AllGeoJSON) as LngLatBoundsLike;

                    let padding = 50;
                    let maxZoom = 20;

                    // Zoom level was requested to be reduced when displaying point types as per #420
                    if (node.geometry.type === "Point" || node.geometry.type === "MultiPoint") {
                        padding = 100;
                        maxZoom = 12;
                    }

                    this.map.fitBounds(bounds, { padding: padding, animate: true, maxZoom: maxZoom });
                }
            }
        }, delay);
    }

    handleRecord(list: string, uid: string): void {

        // Get the feature data from the server and populate the left-hand panel
        this.listService.record(list, uid).then(record => {

            if (this.feature != null) {
                this.map.removeFeatureState(this.feature);
            }

            // Highlight the feature on the map
            this.map.setFeatureState(this.feature = {
                source: list,
                sourceLayer: 'context',
                id: uid
            }, {
                hover: true
            });

            this.mode = this.MODE.VIEW;
            this.record = record;

            if (this.record.recordType === 'GEO_OBJECT') {
                this.geomService.destroy(false);

                this.geomService.initialize(this.map, record.type.geometryType, false);
            }

        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    clearRecord() {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { type: null, code: null, version: null, uid: null },
            queryParamsHandling: 'merge', // remove to replace all query params by provided
        });
    }

    select(node: any, event: MouseEvent): void {
        if (!this.isEdit) {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { type: node.properties.type, code: node.properties.code, uid: node.properties.uid, version: null },
                queryParamsHandling: 'merge', // remove to replace all query params by provided
            });
        }
    }

    handleSelect(type: string, code: string, uid: string) {

        // Highlight the feature on the map
        this.service.getGeoObjectTypes([type], null).then(types => {
            if (this.feature != null) {
                this.map.removeFeatureState(this.feature);
            }

            // Highlight the feature on the map
            this.map.setFeatureState(this.feature = {
                source: GRAPH_LAYER,
                id: uid
            }, {
                hover: true
            });

            this.mode = this.MODE.VIEW;

            const type = types[0];
            this.record = {
                recordType: 'GEO_OBJECT',
                type: type,
                code: code,
                forDate: this.state.currentDate
            };

            if (this.record.recordType === 'GEO_OBJECT') {
                this.geomService.destroy(false);

                this.geomService.initialize(this.map, this.record.type.geometryType, false);
            }

        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    setData(data: GeoObject[]): void {
        this.data = data;
    }

    onLayerChange(event: LayerEvent): void {
        const layer = event.layer;

        if (layer.active) {
            this.addLayer(layer, event.prevLayer);
        } else {
            this.removeLayer(layer);
        }
    }

    onReorderLayers(layers: ContextLayer[]): void {
        for (let i = layers.length - 1; i > -1; i--) {
            const layer = layers[i];

            this.map.moveLayer(layer.oid + "-polygon");
            this.map.moveLayer(layer.oid + "-points");
            this.map.moveLayer(layer.oid + "-label");
        };
    }

    removeLayer(layer: ContextLayer): void {
        const index = this.layers.findIndex(l => l.oid === layer.oid);

        if (index !== -1) {
            const source = layer.oid;

            this.map.removeLayer(source + "-polygon");
            this.map.removeLayer(source + "-points");
            this.map.removeLayer(source + "-label");
            this.map.removeSource(source);

            this.layers.splice(index, 1);
        }
    }

    addLayer(layer: ContextLayer, otherLayer?: ContextLayer): void {
        if (layer.oid === GRAPH_LAYER) {

            if (this.ready) {
                const source = layer.oid;
                const prevLayer = otherLayer != null ? otherLayer.oid + '-polygon' : null;

                this.map.addSource(source, {
                    type: "geojson",
                    data: {
                        type: "FeatureCollection",
                        features: this.data as any,
                    },
                    promoteId: 'uid'
                });

                // Polygon layer
                this.map.addLayer({
                    id: source + "-polygon",
                    type: "fill",
                    source: source,
                    layout: {},
                    paint: {
                        "fill-color": [
                            'case',
                            ['boolean', ['feature-state', 'hover'], false],
                            SELECTED_COLOR,
                            layer.color
                        ],
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
                    paint: {
                        "circle-radius": 10,
                        "circle-color": [
                            'case',
                            ['boolean', ['feature-state', 'hover'], false],
                            SELECTED_COLOR,
                            layer.color
                        ],
                        "circle-stroke-width": 2,
                        "circle-stroke-color": "#FFFFFF"
                    },
                    filter: ["all",
                        ["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
                    ]
                }, prevLayer);

                // Label layer
                this.map.addLayer({
                    id: source + "-label",
                    source: source,
                    type: "symbol",
                    paint: {
                        "text-color": "black",
                        "text-halo-color": "#fff",
                        "text-halo-width": 2
                    },
                    layout: {
                        "text-field": ["get", "localizedValue", ["get", "displayLabel"]],
                        "text-font": ["NotoSansRegular"],
                        "text-offset": [0, 0.6],
                        "text-anchor": "top",
                        "text-size": 12
                    }
                }, prevLayer);

            }

            this.layers.push(layer);
        }
        else {
            this.addVectorLayer(layer, otherLayer);
        }
    }

    addVectorLayer(layer: ContextLayer, otherLayer?: ContextLayer): void {

        if (this.ready) {

            const source = layer.oid;
            const prevLayer = otherLayer != null ? otherLayer.oid + '-polygon' : null;

            let protocol = window.location.protocol;
            let host = window.location.host;

            this.map.addSource(source, {
                type: "vector",
                tiles: [protocol + "//" + host + registry.contextPath + "/list-type/tile?x={x}&y={y}&z={z}&config=" + encodeURIComponent(JSON.stringify({ oid: source }))],
                promoteId: 'uid'
            });

            // Polygon layer
            this.map.addLayer({
                id: source + "-polygon",
                type: "fill",
                source: source,
                "source-layer": "context",
                layout: {},
                paint: {
                    "fill-color": [
                        'case',
                        ['boolean', ['feature-state', 'hover'], false],
                        SELECTED_COLOR,
                        layer.color
                    ],
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
                    "circle-color": [
                        'case',
                        ['boolean', ['feature-state', 'hover'], false],
                        SELECTED_COLOR,
                        layer.color
                    ],
                    "circle-stroke-width": 2,
                    "circle-stroke-color": "#FFFFFF"
                },
                filter: ["all",
                    ["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
                ]
            }, prevLayer);

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
        }

        this.layers.push(layer);
    }


    error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
