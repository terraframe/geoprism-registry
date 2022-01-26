import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild } from "@angular/core";
import { Location } from "@angular/common";
import { ActivatedRoute, Params, Router } from "@angular/router";
import { Map, LngLatBoundsLike, NavigationControl, AttributionControl, IControl, LngLatBounds } from "mapbox-gl";

import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { AllGeoJSON } from "@turf/helpers";
import bbox from "@turf/bbox";

import { GeoObject } from "@registry/model/registry";
import { ModalState, VisualizeState } from "@registry/model/location-manager";

import { MapService, RegistryService, GeometryService } from "@registry/service";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler, ConfirmModalComponent, SuccessModalComponent } from "@shared/component";

import { AuthService, LocalizationService } from "@shared/service";
import { ContextLayer, LayerRecord } from "@registry/model/list-type";
import { GRAPH_LAYER, LayerEvent } from "./layer-panel.component";
import { ListTypeService } from "@registry/service/list-type.service";
import { timeout } from "d3-timer";
import { Observable, Observer, Subscription } from "rxjs";
import { SelectTypeModalComponent } from "./select-type-modal.component";

import { GeoRegistryConfiguration } from "@core/model/registry";
declare let registry: GeoRegistryConfiguration;

const SELECTED_COLOR = "#800000";

@Component({
    selector: "location-manager",
    providers: [Location],
    templateUrl: "./location-manager.component.html",
    styleUrls: ["./location-manager.css"]
})
export class LocationManagerComponent implements OnInit, AfterViewInit, OnDestroy {

    pageMode: string = "";

    coordinate: {
        longitude: number,
        latitude: number
    } = { longitude: null, latitude: null };

    MODE: ModalState = {
        SEARCH: 0,
        VIEW: 1
    }

    VISUALIZE_MODE: VisualizeState = {
        MAP: 0,
        HIERARCHY: 1,
        GRAPH: 2
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
        currentDate: string,
        featureText?: string
    } = { text: "", currentText: "", date: "", currentDate: "" }

    /*
     * Currently selected record
     */
    record: LayerRecord;

    /*
     * If we're visualizing relationships of a Geo-Object then this is that GO.
     */
    current: GeoObject;

    filterDate: string = null;

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

    visualizeMode: number = this.VISUALIZE_MODE.MAP;

    visualizingRelationship: string = null;

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

    // Flag denoting if the map in loaded and initialized
    searchFeatures: boolean = false;

    // Flag denoting if the search and results panel is enabled at all
    searchEnabled: boolean = true;

    typeahead: Observable<any> = null;

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
        private authService: AuthService,
        private location: Location) {
        this.location = location;
    }

    ngOnInit(): void {
        this.subscription = this.route.queryParams.subscribe(params => {
            this.handleParameterChange(params);
        });

        this.searchEnabled = registry.searchEnabled && (this.authService.isRC(false) || this.authService.isRM() || this.authService.isRA());

        this.typeahead = new Observable((observer: Observer<any>) => {
            this.handleFeatureSearch(observer);
        });
    }

    ngOnDestroy(): void {
        this.geomService.destroy();
        this.subscription.unsubscribe();
    }

    ngAfterViewInit() {
        if (this.visualizeMode === this.VISUALIZE_MODE.MAP) {
            this.initializeMap();
        }
    }

    setFilterDate(date: string) {
        this.filterDate = date;
    }

    initializeMap() {
        const layer = this.baseLayers[0];

        const mapConfig: any = {
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
            center: [-41.44427718989905, 41.897852]
        };

        if (this.params.bounds != null && this.params.bounds.length > 0) {
            mapConfig.bounds = new LngLatBounds(JSON.parse(this.params.bounds));
        }

        mapConfig.logoPosition = "bottom-right";

        this.map = new Map(mapConfig);

        this.map.on("load", () => {
            this.ready = true;

            this.initMap();
        });

        if (this.simpleEditControl) {
            this.map.addControl(this.simpleEditControl);
        }
    }

    onChangeGeoObject(event: {id: string, code: string, typeCode: string}): void {
        this.service.getGeoObject(event.id, event.typeCode).then(geoObj => {
            this.setData([geoObj]);
            this.select(geoObj, null);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
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
                    if (this.params.text !== this.state.currentText || this.params.date !== this.state.currentDate) {
                        this.state.text = this.params.text;
                        this.state.date = this.params.date;

                        this.handleSearch(this.params.text, this.params.date);
                    }

                    showPanel = true;
                }

                // Handle parameters for selecting a geo object
                if (this.params.type != null && this.params.code != null) {
                    if (this.record == null || this.record.type == null || this.record.type.code !== this.params.type || this.record.code !== this.params.code) {
                        this.handleSelect(this.params.type, this.params.code, this.params.uid);
                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

                // Handle parameters for select a record from a context layer
                if (this.params.version != null && this.params.uid != null) {
                    if (this.record == null || this.feature == null || this.feature.source !== this.params.version || this.feature.id !== this.params.uid) {
                        this.handleRecord(this.params.version, this.params.uid);
                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

                if (this.params.pageContext) {
                    this.pageMode = this.params.pageContext;
                }

                // Keep the sidebar open if toggling a context layer when the sidebar is already open.
                // This only happens on a fresh page load when sidebar is open (no search results or obj focus)
                if (this.showPanel && this.pageMode === "EXPLORER") {
                    showPanel = true;
                }

                if (this.params.visualizeMode) {
                    this.visualizeMode = parseInt(this.params.visualizeMode);
                }
            }

            this.changeMode(mode);
            this.setPanel(showPanel);
        }
    }

    handleFeatureSearch(observer: Observer<any>): void {
        const localeProperty = "displayLabel_" + navigator.language.toLowerCase();

        // Search features
        if (this.ready && this.map != null && this.state.featureText != null) {
            const value = this.state.featureText.toLocaleLowerCase();
            const features = this.map.queryRenderedFeatures().filter(feature => {
                if (feature.source !== GRAPH_LAYER) {
                    const localizedName = feature.properties[localeProperty];
                    let name = localizedName != null && localizedName.length > 0 ? localizedName : feature.properties.displayLabel;
                    name = name.toLowerCase();
                    const code = feature.properties.code.toLowerCase();

                    if (name.includes(value) || code.includes(value)) {
                        return true;
                    }
                }

                return false;
            }).filter((value, index, self) => self.findIndex(feature => {
                return feature.source === value.source && feature.properties.uid === value.properties.uid;
            }) === index).map(feature => {
                const localizedName = feature.properties[localeProperty];

                let name = localizedName != null && localizedName.length > 0 ? localizedName : feature.properties.displayLabel;

                const index = this.layers.findIndex(l => l.oid === feature.source);
                const layer = this.layers[index];

                return {
                    id: feature.properties.uid,
                    code: feature.properties.code,
                    layer: layer,
                    name: name,
                    feature: feature
                };
            }).sort((a, b) => (a.name > b.name) ? 1 : -1);

            observer.next(features);
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

    changeVisualizeMode(visualizeMode: number): void {
        if (this.visualizeMode !== this.VISUALIZE_MODE.MAP && visualizeMode === this.VISUALIZE_MODE.MAP) {
            window.setTimeout(() => {
                this.geomService.destroy();
                this.ngAfterViewInit();
            }, 10);
        }

        this.visualizeMode = visualizeMode;

        // if (this.visualizeMode === this.VISUALIZE_MODE.MAP && this.map == null) {
        //    window.setTimeoutthis.initializeMap();
        // }

        // this.location.replaceState("/registry/location-manager/" + this.current.properties.uid + "/" + this.current.properties.type + "/" + this.visualizeMode);
    }

    visualizeRelationships(node: GeoObject, visualizeMode: number, event: MouseEvent): void {
        if (event != null) {
            event.stopPropagation();
        }

        this.current = node;

        this.changeVisualizeMode(visualizeMode);
    }

    initMap(): void {
        this.map.on("style.load", () => {
            this.addLayers();
        });

        this.addLayers();

        // Add zoom and rotation controls to the map.
        this.map.addControl(new NavigationControl({ visualizePitch: true }), "top-right");
        this.map.addControl(new AttributionControl({ compact: true }), "top-right");

        this.map.on("click", (event: any) => {
            this.handleMapClickEvent(event);
        });

        this.map.on("moveend", (event: any) => {
            const bounds: LngLatBounds = this.map.getBounds();
            const array = bounds.toArray();

            let url = this.router.createUrlTree([], {
                relativeTo: this.route,
                queryParams: { bounds: JSON.stringify(array) },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            }).toString();

            this.location.go(url);
        });

        // if (this.params.bounds != null && this.params.bounds.length > 0) {
        //     const bounds = JSON.parse(this.params.bounds);

        //     this.map.fitBounds(new LngLatBounds(bounds), { animate: false });
        // }

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
                            code: "__NEW__"
                        }
                    }, null);
                } else {
                    this.bsModalRef = this.modalService.show(SelectTypeModalComponent, {
                        animated: true,
                        backdrop: true,
                        ignoreBackdropClick: true
                    });
                    this.bsModalRef.content.init(version, typeCode => {
                        this.select({
                            properties: {
                                type: typeCode,
                                code: "__NEW__"
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
                    } else {
                        this.router.navigate([], {
                            relativeTo: this.route,
                            queryParams: { type: null, code: null, version: feature.source, uid: feature.properties.uid },
                            queryParamsHandling: "merge" // remove to replace all query params by provided
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

    onToggleSearch(): void {
        this.searchFeatures = !this.searchFeatures;
    }

    search(): void {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { text: this.state.text, date: this.state.date, type: null, code: null, version: null, uid: null },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    handleSearch(text: string, date: string): void {
        this.geomService.destroy(false);
        this.mapService.search(text, date).then(data => {
            this.state.currentText = text;
            this.state.currentDate = date;

            this.showPanel = true;

            if (this.data.length > 0) {
                (<any> this.map.getSource(GRAPH_LAYER)).setData(data);
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

        this.changeVisualizeMode(this.VISUALIZE_MODE.MAP);

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
            this.feature = {
                source: list,
                sourceLayer: "context",
                id: uid
            };
            if (this.layers.findIndex(lFind => this.feature.source === lFind.oid) !== -1) {
                this.map.setFeatureState(this.feature, {
                    hover: true
                });
            }

            this.mode = this.MODE.VIEW;
            this.record = record;

            if (this.record.recordType === "GEO_OBJECT") {
                this.geomService.destroy(false);

                this.geomService.initialize(this.map, record.type.geometryType, false);

                this.service.getGeoObjectByCode(record.code, record.type.code).then(geoObject => {
                    this.current = geoObject;
                    this.filterDate = record.forDate;
                    this.zoomToFeature(this.current, null);
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    clearRecord() {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { type: null, code: null, version: null, uid: null },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    select(node: any, event: MouseEvent): void {
        if (!this.isEdit) {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { type: node.properties.type, code: node.properties.code, uid: node.properties.uid, version: null }
            });
        }
    }

    handleSelect(type: string, code: string, uid: string) {
        // Highlight the feature on the map
        this.service.getGeoObjectTypes([type], null).then(types => {
            if (this.visualizeMode === this.VISUALIZE_MODE.MAP && this.feature != null) {
                this.map.removeFeatureState(this.feature);
            }

            // Highlight the feature on the map
            if (this.visualizeMode === this.VISUALIZE_MODE.MAP && this.feature && code !== "__NEW__") {
                this.map.setFeatureState(this.feature = {
                    source: GRAPH_LAYER,
                    id: uid
                }, {
                    hover: true
                });
            }

            this.mode = this.MODE.VIEW;

            const type = types[0];
            this.record = {
                recordType: "GEO_OBJECT",
                type: type,
                code: code,
                forDate: this.state.currentDate
            };

            if (this.visualizeMode === this.VISUALIZE_MODE.MAP && this.record.recordType === "GEO_OBJECT") {
                this.geomService.destroy(false);

                this.geomService.initialize(this.map, this.record.type.geometryType, false);
            }

            // Relationship Viz TODO
            // this.location.replaceState("/registry/location-manager/" + this.current.properties.uid + "/" + this.current.properties.type + "/" + this.visualizeMode);

            this.service.getGeoObjectByCode(code, type.code).then(geoObject => {
                this.current = geoObject;
                this.filterDate = this.record.forDate;
                this.zoomToFeature(this.current, null);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
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
            let existingIndex = this.layers.findIndex((findLayer: any) => { return findLayer.oid === layer.oid; });
            
            if (existingIndex !== -1) {
                this.removeLayer(layer)
            }
          
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
        }
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
                const prevLayer = otherLayer != null ? otherLayer.oid + "-polygon" : null;

                this.map.addSource(source, {
                    type: "geojson",
                    data: {
                        type: "FeatureCollection",
                        features: this.data as any
                    },
                    promoteId: "uid"
                });

                // Polygon layer
                this.map.addLayer({
                    id: source + "-polygon",
                    type: "fill",
                    source: source,
                    layout: {},
                    paint: {
                        "fill-color": [
                            "case",
                            ["boolean", ["feature-state", "hover"], false],
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
                            "case",
                            ["boolean", ["feature-state", "hover"], false],
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

            if (this.layers.findIndex(lFind => layer.oid === lFind.oid) === -1) {
                this.layers.push(layer);
            }
        } else {
            this.addVectorLayer(layer, otherLayer);
        }
    }

    addVectorLayer(layer: ContextLayer, otherLayer?: ContextLayer): void {
        if (this.ready) {
            const source = layer.oid;
            const prevLayer = otherLayer != null ? otherLayer.oid + "-polygon" : null;

            let protocol = window.location.protocol;
            let host = window.location.host;

            this.map.addSource(source, {
                type: "vector",
                tiles: [protocol + "//" + host + registry.contextPath + "/list-type/tile?x={x}&y={y}&z={z}&config=" + encodeURIComponent(JSON.stringify({ oid: source }))],
                promoteId: "uid"
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
                        "case",
                        ["boolean", ["feature-state", "hover"], false],
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
                        "case",
                        ["boolean", ["feature-state", "hover"], false],
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
            
            
            // Highlight
            if (this.feature && this.feature.source === source) {
                this.map.setFeatureState(this.feature, {
                    hover: true
                });
            }
        }

        if (this.layers.findIndex(lFind => layer.oid === lFind.oid) === -1) {
            this.layers.push(layer);
        }
    }

    onFeatureSelect(event: any): void {
        if (!this.isEdit) {
            this.state.featureText = event.item.name;

            const feature = event.item.feature;

            if (feature.properties.uid != null) {
                this.listService.getBounds(feature.source, feature.properties.uid).then(bounds => {
                    if (bounds && Array.isArray(bounds)) {
                        let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

                        this.map.fitBounds(llb, { padding: 50, animate: true, maxZoom: 20 });
                    }
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            }

            if (feature.properties.uid != null) {
                this.router.navigate([], {
                    relativeTo: this.route,
                    queryParams: { type: null, code: null, version: feature.source, uid: feature.properties.uid },
                    queryParamsHandling: "merge" // remove to replace all query params by provided
                });
            }
        }
    }

    error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
