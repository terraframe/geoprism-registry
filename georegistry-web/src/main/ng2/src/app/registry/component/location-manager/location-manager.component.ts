import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, HostListener, Injector, ApplicationRef, ComponentFactoryResolver, EmbeddedViewRef } from "@angular/core";
import { Location } from "@angular/common";
import { ActivatedRoute, Params, Router } from "@angular/router";
import { Map, LngLatBoundsLike, NavigationControl, AttributionControl, IControl, LngLatBounds, Popup } from "mapbox-gl";

import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import bbox from "@turf/bbox";
import * as ColorGen from "color-generator";

import { GeoObject, GeoObjectType, GeoObjectTypeCache } from "@registry/model/registry";
import { ModalState, PANEL_SIZE_STATE } from "@registry/model/location-manager";

import { MapService, RegistryService, GeometryService, ParamLayer, DataSourceProvider, LayerDataSource, GeoJsonLayerDataSource, Layer, GeoJsonLayer } from "@registry/service";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler, ConfirmModalComponent, SuccessModalComponent } from "@shared/component";

import { AuthService, LocalizationService } from "@shared/service";
import { ContextLayer } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { timeout } from "d3-timer";
import { Observable, Subscription } from "rxjs";
import { SelectTypeModalComponent } from "./select-type-modal.component";

import { GeoRegistryConfiguration } from "@core/model/registry";
import { OverlayerIdentifier } from "@registry/model/constants";
import { NgxSpinnerService } from "ngx-spinner";
import { ModalTypes } from "@shared/model/modal";
import { FeaturePanelComponent } from "./feature-panel.component";
import { RegistryCacheService } from "@registry/service/registry-cache.service";
import { RecordPopupComponent } from "./record-popup.component";

declare let registry: GeoRegistryConfiguration;

export const SEARCH_LAYER = "search";

class SelectedObject {

    type: GeoObjectType;
    code: string;
    forDate: string;
    geoObject: GeoObject;

}

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
    };

    CONSTANTS = {
        OVERLAY: OverlayerIdentifier.FEATURE_PANEL
    };

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
    current: SelectedObject;

    requestedDate: string = null;

    calculatedDate: string = null;

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

    visualizingRelationship: string = null;

    graphPanelOpen: boolean = false;

    /*
    *  Flag to indicate if the left handle panel should be displayed or not
     */
    showPanel: boolean = true;

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

    graphVisualizerEnabled: boolean = false;

    typeahead: Observable<any> = null;

    typeCache: GeoObjectTypeCache;

    dataSourceProvider: DataSourceProvider;

    public layersPanelSize: number = PANEL_SIZE_STATE.MINIMIZED;

    @ViewChild("simpleEditControl") simpleEditControl: IControl;

    @ViewChild("FeaturePanel") featurePanel: FeaturePanelComponent;

    windowWidth: number;
    windowHeight: number;

    private mapBounds: LngLatBounds;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private modalService: BsModalService,
        private spinner: NgxSpinnerService,
        private service: RegistryService,
        private cacheService: RegistryCacheService,
        private listService: ListTypeService,
        private mapService: MapService,
        private geomService: GeometryService,
        private lService: LocalizationService,
        private authService: AuthService,
        private location: Location,
        private componentFactoryResolver: ComponentFactoryResolver,
        private appRef: ApplicationRef,
        private injector: Injector
    ) {
        this.location = location;
    }

    ngOnInit(): void {
        this.windowWidth = window.innerWidth;
        this.windowHeight = window.innerHeight;

        this.subscription = this.route.queryParams.subscribe(params => {
            this.handleParameterChange(params);
        });

        this.searchEnabled = registry.searchEnabled && (this.authService.isRC(false) || this.authService.isRM() || this.authService.isRA());
        this.graphVisualizerEnabled = registry.graphVisualizerEnabled || false;

        this.typeCache = this.cacheService.getTypeCache();

        let locationManager = this;
        this.dataSourceProvider = {
            getId(): string {
                return SEARCH_LAYER;
            },
            getDataSource(dataSourceId: string): LayerDataSource {
                if (dataSourceId === SEARCH_LAYER) {
                    return {
                        buildMapboxSource(): any {
                            return {
                                type: "geojson",
                                data: {
                                    type: "FeatureCollection",
                                    features: this.getLayerData()
                                }
                            };
                        },
                        getGeometryType(): string {
                            return "MIXED";
                        },
                        getLayerData(): any {
                            return locationManager.data;
                        },
                        setLayerData(data: any) {
                            this.data = data;
                        },
                        createLayer(oid: string, legendLabel: string, rendered: boolean, color: string): Layer {
                            return new GeoJsonLayer(oid, legendLabel, this, rendered, color);
                        },
                        getDataSourceId(): string {
                            return dataSourceId;
                        },
                        getDataSourceProviderId(): string {
                            return SEARCH_LAYER;
                        }
                    } as GeoJsonLayerDataSource;
                }
            }
        };
    }

    ngOnDestroy(): void {
        this.geomService.destroy();
        this.subscription.unsubscribe();
    }

    ngAfterViewInit() {
        this.initializeMap();
    }

    @HostListener("window:resize", ["$event"])
    resizeWindow() {
        this.windowWidth = window.innerWidth;
        this.windowHeight = window.innerHeight;
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
            attributionControl: false,
            bounds: registry.defaultMapBounds
        };

        if (this.params.bounds != null && this.params.bounds.length > 0) {
            mapConfig.bounds = new LngLatBounds(JSON.parse(this.params.bounds));
        }

        mapConfig.logoPosition = "bottom-right";

        this.map = new Map(mapConfig);

        this.map.on("load", () => {
            this.geomService.initialize(this.map, null, true);
            this.geomService.registerDataSourceProvider(this.dataSourceProvider);
            this.ready = true;

            this.initMap();
        });

        if (this.simpleEditControl) {
            this.map.addControl(this.simpleEditControl);
        }
    }

    onChangeGeoObject(event: { id: string, code: string, typeCode: string, doIt: any }): void {
        this.closeEditSessionSafeguard().then(() => {
            event.doIt(() => {
                this.spinner.show(this.CONSTANTS.OVERLAY);

                this.service.getGeoObject(event.id, event.typeCode, false).then(geoObj => {
                    this.setData([geoObj]);
                    this.changeGeoObject(event.typeCode, event.code, event.id, geoObj);

                    this.router.navigate([], {
                        relativeTo: this.route,
                        queryParams: { type: event.typeCode, code: event.code, uid: event.id, version: null, text: event.code },
                        queryParamsHandling: "merge" // remove to replace all query params by provided
                    });
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                }).finally(() => {
                    this.spinner.hide(this.CONSTANTS.OVERLAY);
                });
            });
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
            let showPanel = this.showPanel;

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
                    if (this.current == null || this.current.type == null || this.current.type.code !== this.params.type || this.current.code !== this.params.code) {
                        this.handleSelect(this.params.type, this.params.code, this.params.uid);
                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

                // Handle parameters for select a record from a context layer
                if (this.params.version != null && this.params.uid != null) {
                    if (this.current == null || this.feature == null || this.feature.source !== this.params.version || this.feature.id !== this.params.uid) {
                        this.handleRecord(this.params.version, this.params.uid);
                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

                if (this.params.pageContext) {
                    this.pageMode = this.params.pageContext;
                }

                if (this.params.layersPanelSize) {
                    this.layersPanelSize = Number.parseInt(this.params.layersPanelSize);
                } else {
                    this.layersPanelSize = (this.pageMode === "EXPLORER") ? PANEL_SIZE_STATE.FULLSCREEN : this.layersPanelSize;
                }

                if (this.params.attrPanelOpen) {
                    showPanel = this.params.attrPanelOpen === "true";
                }

                if (this.params.graphPanelOpen) {
                    this.graphPanelOpen = this.params.graphPanelOpen === "true";
                }
            }

            this.changeMode(mode);
            this.setPanel(showPanel);
        }
    }

    setPanel(showPanel: boolean): void {
        if (this.showPanel !== showPanel) {
            this.showPanel = showPanel;

            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { attrPanelOpen: this.showPanel },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            });

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

            this.current = null;
            this.feature = null;
        }
    }

    onModeChange(value: boolean): void {
        this.isEdit = value;
    }

    initMap(): void {
        // Add zoom and rotation controls to the map.
        this.map.addControl(new AttributionControl({ compact: true }), "bottom-right");
        this.map.addControl(new NavigationControl({ visualizePitch: true }), "bottom-right");

        this.map.on("click", (event: any) => {
            this.handleMapClickEvent(event);
        });

        this.map.on("moveend", (event: any) => {
            this.mapBounds = this.map.getBounds();
            const array = this.mapBounds.toArray();

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

        if (this.current != null && this.current.geoObject != null) {
            this.zoomToFeature(this.current.geoObject, null);
        } else {
            let layers = this.geomService.getLayers().filter(layer => layer.rendered && layer.oid !== SEARCH_LAYER);

            if (layers && layers.length > 0) {
                this.onZoomTo(layers[0].oid);
            }
        }
    }

    onZoomTo(oid: string): void {
        if (oid.startsWith("GRAPH-")) {
            console.log("TODO: Zooming on a graph layer?");
            return;
        }

        this.listService.getBounds(oid).then(bounds => {
            if (bounds && Array.isArray(bounds)) {
                let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

                this.map.fitBounds(llb, this.calculateZoomConfig(null));
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreate(layer: ContextLayer): void {
        this.closeEditSessionSafeguard().then(() => {
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
        });
    }

    closeEditSessionSafeguard(): Promise<void> {
        if (!this.isEdit) {
            return new Promise((resolve, reject) => { resolve(); });
        }

        let confirmBsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        confirmBsModalRef.content.message = this.lService.decode("explorer.edit.loseAllChanges");
        confirmBsModalRef.content.data = {};
        confirmBsModalRef.content.submitText = this.lService.decode("modal.button.ok");
        confirmBsModalRef.content.type = ModalTypes.danger;

        let resolver = (subsription: Subscription, resolve: Function, result: void) => { this.cancelEditingSession(); resolve(result); subsription.unsubscribe(); };
        let rejecter = (subsription: Subscription, reject: Function, error: any) => { reject(error); subsription.unsubscribe(); };

        return new Promise((resolve, reject) => {
            let subscription = confirmBsModalRef.content.onConfirm.subscribe(
                result => { resolver(subscription, resolve, result); },
                error => { rejecter(subscription, reject, error); }
            );
        });
    }

    handleMapClickEvent(e: any): void {
        const features = this.map.queryRenderedFeatures(e.point);

        if (features != null && features.length > 0) {
            const feature = features[0];

            if (feature.properties.uid != null) {
                this.closeEditSessionSafeguard().then(() => {
                    if (feature.source === SEARCH_LAYER) {
                        if ((this.current == null || this.current.geoObject == null || this.current.geoObject.properties.uid !== feature.properties.uid)) {
                            this.select(feature, null);
                        }
                    } else {
                        if (this.params.version == null || this.params.uid == null ||
                            this.params.version !== feature.source ||
                            this.params.uid !== feature.properties.uid) {
                            this.router.navigate([], {
                                relativeTo: this.route,
                                queryParams: { type: null, code: null, version: feature.source, uid: feature.properties.uid },
                                queryParamsHandling: "merge" // remove to replace all query params by provided
                            });
                        } else {
                            this.handleRecord(feature.source, feature.properties.uid);
                        }
                    }
                });
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

    getGeoObjectTypeLabel(geoObject: GeoObject) {
        const type: GeoObjectType = this.typeCache.getTypeByCode(geoObject.properties.type);

        return type == null ? "" : type.label.localizedValue;
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
        this.mapService.search(text, date, false).then(data => {
            this.state.currentText = text;
            this.state.currentDate = date;

            this.setData(data.features);

            if (this.data.length > 0) {
                let source = (<any> this.map.getSource(SEARCH_LAYER));

                if (source != null) {
                    source.setData(data);
                }
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    zoomToFeature(geoObject: GeoObject, event: MouseEvent): void {
        if (event != null) {
            event.stopPropagation();
        }

        this.preventSingleClick = false;
        const delay = 200;

        let geometry = geoObject.geometry;

        this.timer = setTimeout(() => {
            if (!this.preventSingleClick) {
                if (geometry != null) {
                    const bounds = bbox(geometry) as LngLatBoundsLike;

                    this.map.fitBounds(bounds, this.calculateZoomConfig(geometry.type));
                }
            }
        }, delay);
    }

    calculateZoomConfig(geometryType: string): any {
        let padding = 50;
        let maxZoom = 20;

        // Zoom level was requested to be reduced when displaying point types as per #420
        if (geometryType === "Point" || geometryType === "MultiPoint") {
            padding = 100;
            maxZoom = 12;
        }

        let config: any = { padding: padding, animate: true, maxZoom: maxZoom };

        if (this.graphPanelOpen && !this.showPanel) {
            config.padding = {
                top: (this.layersPanelSize !== PANEL_SIZE_STATE.MINIMIZED ? ((37 * this.geomService.getLayers().length) + 45) : 0) + 10,
                bottom: 10,
                left: (Math.round(this.windowWidth / 2) + 10),
                right: 10
            };
        }

        return config;
    }

    handleRecord(list: string, uid: string): void {
        // Get the feature data from the server and populate the left-hand panel
        this.listService.record(list, uid, false).then(record => {
            if (this.feature != null) {
                this.map.removeFeatureState(this.feature);
            }

            // Highlight the feature on the map
            this.feature = {
                source: list,
                sourceLayer: "context",
                id: uid
            };
            if (this.geomService.getLayers().findIndex(lFind => this.feature.source === lFind.oid) !== -1) {
                this.map.setFeatureState(this.feature, {
                    selected: true
                });
            }

            if (record.recordType === "LIST") { // this happens when list type is NOT working
                const bounds = record.bbox;

                this.requestedDate = record.forDate === "" || record.forDate === undefined ? null : record.forDate;

                if (bounds && Array.isArray(bounds)) {
                    // 1. Create a component reference from the component
                    const componentRef = this.componentFactoryResolver
                        .resolveComponentFactory(RecordPopupComponent)
                        .create(this.injector);

                    componentRef.instance.record = record;
                    componentRef.instance.edit.subscribe(() => {
                        const code: string = record.data["code"];
                        const uid: string = record.data["uid"];

                        this.handleSelect(record.typeCode, code, uid);
                    });

                    // 2. Attach component to the appRef so that it's inside the ng component tree
                    this.appRef.attachView(componentRef.hostView);

                    // 3. Get DOM element from component
                    const domElem = (componentRef.hostView as EmbeddedViewRef<any>)
                        .rootNodes[0] as HTMLElement;

                    // 4. Append DOM element to the body
                    let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

                    new Popup({ closeOnClick: true, closeButton: false })
                        .setLngLat(llb.getCenter())
                        .setDOMContent(domElem)
                        .addTo(this.map);
                }
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    cancelEditingSession() {
        if (this.isEdit) {
            this.geomService.destroy(false);
        }

        this.isEdit = false;

        if (this.feature != null) {
            this.map.removeFeatureState(this.feature);
        }

        this.featurePanel.setEditMode(false);
        this.feature = null;
    }

    clearRecord() {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { type: null, code: null, version: null, uid: null },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    featurePanelForDateChange(date: string) {
        if (date !== null) {
            this.geomService.destroy(false);
        }
    }

    select(node: any, event: MouseEvent): void {
        if (!this.isEdit) {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { type: node.properties.type, code: node.properties.code, uid: node.properties.uid, version: null },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            });

            this.zoomToFeature(node, null);
        }
    }

    handleSelect(typeCode: string, code: string, uid: string, geoObject: GeoObject = null) {
        this.mode = this.MODE.VIEW;

        this.changeGeoObject(typeCode, code, uid, geoObject);
    }

    changeGeoObject(typeCode: string, code: string, uid: string, geoObject: GeoObject = null) {
        // Highlight the feature on the map
        if (this.feature != null) {
            this.map.removeFeatureState(this.feature);
        }

        // Highlight the feature on the map
        if (this.feature && code !== "__NEW__" && this.map.getSource(SEARCH_LAYER) != null) {
            this.map.setFeatureState(this.feature = {
                source: SEARCH_LAYER,
                id: uid
            }, {
                selected: true
            });
        }

        this.typeCache.waitOnTypes().then(() => {
            const type: GeoObjectType = this.typeCache.getTypeByCode(typeCode);

            this.current = {
                type: type,
                code: code,
                forDate: this.state.currentDate === "" ? null : this.state.currentDate,
                geoObject: geoObject
            };

            this.geomService.destroy(false);
            this.geomService.setGeometryType(this.current.type.geometryType);

            if (geoObject == null) {
                if (code !== "__NEW__") {
                    this.spinner.show(this.CONSTANTS.OVERLAY);

                    this.service.getGeoObjectByCode(code, type.code).then(geoObject => {
                        this.current.geoObject = geoObject;
                        this.requestedDate = this.current.forDate === "" ? null : this.current.forDate;
                        this.zoomToFeature(this.current.geoObject, null);
                    }).catch((err: HttpErrorResponse) => {
                        this.error(err);
                    }).finally(() => {
                        this.spinner.hide(this.CONSTANTS.OVERLAY);
                    });
                }
            } else {
                this.requestedDate = this.current.forDate === "" ? null : this.current.forDate;
                this.zoomToFeature(this.current.geoObject, null);
            }
        });
    }

    setData(data: GeoObject[]): void {
        this.data = data;

        if (this.data && this.geomService.getLayers().findIndex(layer => layer.oid === SEARCH_LAYER) === -1) {
            this.geomService.addOrUpdateLayer(new ParamLayer(SEARCH_LAYER, this.lService.decode("explorer.search.layer"), true, ColorGen().hexString(), SEARCH_LAYER, SEARCH_LAYER));
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

    toggleGraphPanel(): void {
        this.graphPanelOpen = !this.graphPanelOpen;

        // window.setTimeout(() => {
        //     let graphContainer = document.getElementById("graph-container");

        //     if (graphContainer) {
        //         this.svgHeight = graphContainer.clientHeight;
        //         this.svgWidth = graphContainer.clientWidth;
        //         // this.panToNode(this.geoObject.properties.uid);
        //     }
        // }, 10);

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { graphPanelOpen: this.graphPanelOpen },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

/*
    updateLayersFromParameters(paramLayers: ContextLayer[]) {
        if (this.ready) {
            // Calculate a diff
            let diffs = [];
            let iterations = paramLayers.length > this.layers.length ? paramLayers.length : this.layers.length;
            for (let i = 0; i < iterations; ++i) {
                if (i >= paramLayers.length) {
                    let existingLayer = this.layers[i];

                    let existingLayerExistsElsewhere = paramLayers.findIndex(findLayer => findLayer.oid === existingLayer.oid);

                    if (existingLayerExistsElsewhere !== -1) {
                        diffs.push({
                            type: "LAYER_REORDER",
                            index: i,
                            moveTo: existingLayerExistsElsewhere
                        });
                    } else {
                        diffs.push({
                            type: "REMOVE_LAYER",
                            index: i
                        });
                    }
                } else if (i >= this.layers.length) {
                    let paramLayer = paramLayers[i];

                    let paramLayerExistsElsewhere = this.layers.findIndex(findLayer => findLayer.oid === paramLayer.oid);

                    if (paramLayerExistsElsewhere !== -1) {
                        diffs.push({
                            type: "LAYER_REORDER",
                            index: i,
                            moveTo: paramLayerExistsElsewhere
                        });
                    } else {
                        diffs.push({
                            type: "NEW_LAYER",
                            index: i
                        });
                    }
                } else {
                    let paramLayer = paramLayers[i];
                    let layer = this.layers[i];

                    if (paramLayer.oid !== layer.oid) {
                        let paramLayerExistsElsewhere = this.layers.findIndex(findLayer => findLayer.oid === paramLayer.oid);

                        if (paramLayerExistsElsewhere !== -1) {
                            diffs.push({
                                type: "LAYER_REORDER",
                                index: i,
                                moveTo: paramLayerExistsElsewhere
                            });
                        } else {
                            diffs.push({
                                type: "NEW_LAYER",
                                index: i
                            });
                        }
                    } else if (paramLayer.rendered !== layer.rendered) {
                        diffs.push({
                            type: "RENDERED_CHANGE",
                            index: i
                        });
                    }
                }
            }

            let fullRebuild = diffs.length > 0 || paramLayers.length !== this.layers.length;

            if (diffs.length === 1 && diffs[0].type === "RENDERED_CHANGE") {
                // They just toggled whether a layer was rendered

                let prevLayer = null;
                const layerCount = this.layers.length;
                for (let i = 0; i < layerCount; ++i) {
                    let paramLayer = paramLayers[i];
                    let layer = this.layers[i];

                    if (paramLayer.rendered !== layer.rendered) {
                        if (paramLayer.rendered) {
                            this.mapLayer(paramLayer, prevLayer);
                        } else {
                            this.unmapLayer(layer);
                        }

                        layer.rendered = paramLayer.rendered;
                    } else {
                        this.layers[i] = JSON.parse(JSON.stringify(paramLayer));
                    }

                    if (paramLayer.rendered) {
                        prevLayer = this.layers[i];
                    }
                }

                fullRebuild = false;
            } else if (diffs.length === 1 && diffs[0].type === "NEW_LAYER" && diffs[0].index === this.layers.length) {
                // Added a layer at the end

                this.mapLayer(paramLayers[paramLayers.length - 1], this.layers.length > 0 ? this.layers[this.layers.length - 1] : null);
                fullRebuild = false;
            } else if (diffs.length > 0 && paramLayers.length === this.layers.length && diffs.filter(diff => diff.type !== "LAYER_REORDER").length === 0) {
                // Layers changed order but are otherwise the same.

                this.layers = JSON.parse(JSON.stringify(paramLayers));
                for (let i = this.layers.length - 1; i > -1; i--) {
                    const layer = this.layers[i];

                    if (this.map.getLayer(layer.oid + "-POLYGON")) {
                        this.map.moveLayer(layer.oid + "-POLYGON");
                    }
                    if (this.map.getLayer(layer.oid + "-POINT")) {
                        this.map.moveLayer(layer.oid + "-POINT");
                    }
                    if (this.map.getLayer(layer.oid + "-LINE")) {
                        this.map.moveLayer(layer.oid + "-LINE");
                    }
                    if (this.map.getLayer(layer.oid + "-LABEL")) {
                        this.map.moveLayer(layer.oid + "-LABEL");
                    }
                }
                fullRebuild = false;
            }

            if (fullRebuild) {
                // Remove all existing layers
                let len = this.layers.length;
                for (let i = 0; i < len; ++i) {
                    if (this.layers[i].rendered) {
                        this.unmapLayer(this.layers[i]);
                    }
                }

                this.layers = JSON.parse(JSON.stringify(paramLayers));
                this.mapAllLayers();
            } else {
                // Make sure attribute changes are reflected
                this.layers = JSON.parse(JSON.stringify(paramLayers));
            }
        } else {
            this.layers = JSON.parse(JSON.stringify(paramLayers));
        }
    }

    mapLayer(layer: ContextLayer, otherLayer?: ContextLayer): void {
        if (layer.oid === SEARCH_LAYER || layer.oid.startsWith("GRAPH-")) {
            if (this.ready) {
                let data: any = {
                    type: "FeatureCollection",
                    features: this.data as any
                };
                if (layer.oid.startsWith("GRAPH-")) {
                    data = this.geomService.getLayerGeometry(layer.oid);

                    if (!data) {
                        data = {
                            type: "FeatureCollection",
                            features: []
                        };
                    }
                }

                const source = layer.oid;
                const prevLayer = otherLayer != null ? otherLayer.oid + "-POLYGON" : null;

                this.map.addSource(source, {
                    type: "geojson",
                    data: data,
                    promoteId: "uid"
                });

                // Polygon layer
                this.map.addLayer({
                    id: source + "-POLYGON",
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

                // Line layer
                this.map.addLayer({
                    id: source + "-LINE",
                    type: "line",
                    source: source,
                    paint: {
                        "line-width": 3,
                        "line-color": [
                            "case",
                            ["boolean", ["feature-state", "hover"], false],
                            SELECTED_COLOR,
                            layer.color
                        ]
                    },
                    filter: ["all",
                        ["match", ["geometry-type"], ["LineString", "MultiLineString"], true, false]
                    ]
                }, prevLayer);

                // Point layer
                this.map.addLayer({
                    id: source + "-POINT",
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
                    id: source + "-LABEL",
                    source: source,
                    type: "symbol",
                    paint: {
                        "text-color": "black",
                        "text-halo-color": "#fff",
                        "text-halo-width": 2
                    },
                    layout: {
                        // "text-field": ["get", "localizedValue", ["get", "displayLabel"]],
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
        } else {
            this.mapVectorLayer(layer, otherLayer);
        }
    }

    mapVectorLayer(layer: ContextLayer, otherLayer?: ContextLayer): void {
        if (this.ready) {
            const source = layer.oid;

            let prevLayer = null;
            if (otherLayer) {
                if (this.map.getLayer(otherLayer.oid + "-POLYGON")) {
                    prevLayer = otherLayer.oid + "-POLYGON";
                } else if (this.map.getLayer(otherLayer.oid + "-POINT")) {
                    prevLayer = otherLayer.oid + "-POINT";
                } else if (this.map.getLayer(otherLayer.oid + "-LINE")) {
                    prevLayer = otherLayer.oid + "-LINE";
                }
            }

            let protocol = window.location.protocol;
            let host = window.location.host;

            this.map.addSource(source, {
                type: "vector",
                tiles: [protocol + "//" + host + registry.contextPath + "/list-type/tile?x={x}&y={y}&z={z}&config=" + encodeURIComponent(JSON.stringify({ oid: source }))],
                promoteId: "uid"
            });

            // Polygon layer
            this.map.addLayer({
                id: source + "-POLYGON",
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

            // Line layer
            this.map.addLayer({
                id: source + "-LINE",
                type: "line",
                source: source,
                "source-layer": "context",
                paint: {
                    "line-width": 3,
                    "line-color": [
                        "case",
                        ["boolean", ["feature-state", "hover"], false],
                        SELECTED_COLOR,
                        layer.color
                    ]
                },
                filter: ["all",
                    ["match", ["geometry-type"], ["LineString", "MultiLineString"], true, false]
                ]
            }, prevLayer);

            // Point layer
            this.map.addLayer({
                id: source + "-POINT",
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
                id: source + "-LABEL",
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
                    selected: true
                });
            }
        }
    }
*/
}
