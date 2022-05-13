import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, HostListener, Injector, ApplicationRef, ComponentFactoryResolver, EmbeddedViewRef } from "@angular/core";
import { Location } from "@angular/common";
import { ActivatedRoute, Router } from "@angular/router";
import { Map, LngLatBoundsLike, NavigationControl, AttributionControl, IControl, LngLatBounds, Popup } from "mapbox-gl";

import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import bbox from "@turf/bbox";
import * as ColorGen from "color-generator";

import { GeoObject, GeoObjectType, GeoObjectTypeCache } from "@registry/model/registry";
import { ModalState, PANEL_SIZE_STATE } from "@registry/model/location-manager";

import { MapService, RegistryService, GeometryService } from "@registry/service";
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
import { GEO_OBJECT_DATA_SOURCE_TYPE, Layer, ListVectorLayerDataSource, SearchLayerDataSource, LIST_VECTOR_SOURCE_TYPE, SEARCH_DATASOURCE_TYPE, RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE } from "@registry/service/layer-data-source";
import { BusinessObject, BusinessType } from "@registry/model/business-type";

declare let registry: GeoRegistryConfiguration;

class SelectedObject {

    type: GeoObjectType;
    code: string;
    forDate: string;
    geoObject: GeoObject;

}

export interface LocationManagerParams {
    graphPanelOpen?: string,
    graphOid?: string,
    date?: string,
    type?: string,
    code?: string,
    objectType?: "BUSINESS" | "GEOOBJECT",
    bounds?: string,
    text?: string,
    layersPanelSize?: string,
    pageContext?: string,
    version?: string,
    attrPanelOpen?: string,
    uid?: string
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
        VIEW: 1,
        BUSINESS: 2
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
     * Currently selected business object
     */
    businessObject: BusinessObject;

    /*
     * Currently selected business type
     */
    businessType: BusinessType;

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
    params: LocationManagerParams = null;

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
            this.ready = true;

            this.initMap();
        });

        if (this.simpleEditControl) {
            this.map.addControl(this.simpleEditControl);
        }
    }

    onGraphNodeSelect(node): void {
        this.closeEditSessionSafeguard().then(() => {
            node.selectAnimation(() => {
                this.spinner.show(this.CONSTANTS.OVERLAY);

                if (node.nodeType === "GEOOBJECT") {
                    this.service.getGeoObject(node.id, node.typeCode, false).then(geoObj => {
                        this.changeGeoObject(node.typeCode, node.code, node.id, geoObj);

                        this.router.navigate([], {
                            relativeTo: this.route,
                            queryParams: { type: node.typeCode, code: node.code, objectType: "GEOOBJECT", uid: node.id, version: null, text: node.code },
                            queryParamsHandling: "merge" // remove to replace all query params by provided
                        });
                    }).catch((err: HttpErrorResponse) => {
                        this.error(err);
                    }).finally(() => {
                        this.spinner.hide(this.CONSTANTS.OVERLAY);
                    });
                } else if (node.nodeType === "BUSINESS") {
                    console.log(node);
                }
            });
        });
    }

    onChangeGeoObject(event: { id: string, code: string, typeCode: string, doIt: any }): void {
        this.closeEditSessionSafeguard().then(() => {
            event.doIt(() => {
                this.spinner.show(this.CONSTANTS.OVERLAY);

                this.service.getGeoObject(event.id, event.typeCode, false).then(geoObj => {
                    this.changeGeoObject(event.typeCode, event.code, event.id, geoObj);

                    this.router.navigate([], {
                        relativeTo: this.route,
                        queryParams: { type: event.typeCode, code: event.code, objectType: "GEOOBJECT", uid: event.id, version: null, text: event.code },
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
    handleParameterChange(params: LocationManagerParams): void {
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
            this.geomService.stopEditing();
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

            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { bounds: JSON.stringify(array) },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            });

            /*
                        let url = this.router.createUrlTree([], {
                            relativeTo: this.route,
                            queryParams: { bounds: JSON.stringify(array) },
                            queryParamsHandling: "merge" // remove to replace all query params by provided
                        }).toString();

                        this.location.go(url);
                        */
        });

        // if (this.params.bounds != null && this.params.bounds.length > 0) {
        //     const bounds = JSON.parse(this.params.bounds);

        //     this.map.fitBounds(new LngLatBounds(bounds), { animate: false });
        // }

        this.handleParameterChange(this.params);

        if (this.current != null && this.current.geoObject != null) {
            this.zoomToFeature(this.current.geoObject, null);
        } else {
            let layers = this.geomService.getLayers();

            if (layers && layers.length > 0) {
                this.onZoomTo(layers[0]);
            }
        }
    }

    onZoomTo(layer: Layer): void {
        if (layer && layer.dataSource) {
            layer.dataSource.getBounds(layer).then((bounds: LngLatBounds) => {
                if (bounds != null) {
                    this.map.fitBounds(bounds, this.calculateZoomConfig(null));
                }
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
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
                    if (feature.layer) {
                        let layer: Layer = this.geomService.getLayerFromMapboxLayer(feature.layer);

                        if (layer) {
                            if (layer.dataSource.getDataSourceType() === SEARCH_DATASOURCE_TYPE) {
                                if ((this.current == null || this.current.geoObject == null || feature.properties == null || this.current.geoObject.properties.uid !== feature.properties.uid)) {
                                    this.select(feature, null);
                                }
                            } else {
                                if (layer.dataSource.getDataSourceType() === LIST_VECTOR_SOURCE_TYPE) {
                                    const versionId = (layer.dataSource as ListVectorLayerDataSource).getVersionId();

                                    if (this.params.version == null || this.params.uid == null ||
                                        this.params.version !== versionId ||
                                        this.params.uid !== feature.properties.uid) {
                                        this.router.navigate([], {
                                            relativeTo: this.route,
                                            queryParams: { type: null, code: null, version: versionId, uid: feature.properties.uid },
                                            queryParamsHandling: "merge" // remove to replace all query params by provided
                                        });
                                    } else {
                                        this.handleRecord(versionId, feature.properties.uid);
                                    }
                                } else if (layer.dataSource.getDataSourceType() === GEO_OBJECT_DATA_SOURCE_TYPE) {
                                    this.onChangeGeoObject({ typeCode: feature.properties.type, code: feature.properties.code, id: feature.properties.uid, doIt: (fun) => { fun(); } });
                                } else if (layer.dataSource.getDataSourceType() === RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE) {
                                    this.onChangeGeoObject({ typeCode: feature.properties.type, code: feature.properties.code, id: feature.properties.uid, doIt: (fun) => { fun(); } });
                                }
                            }
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
        this.geomService.stopEditing();

        let dataSource = new SearchLayerDataSource(this.mapService, text, date);
        dataSource.getLayerData().then((data: any) => {
            let layers = this.geomService.getLayers();

            // Remove any existing search layer
            let index = layers.findIndex(layer => layer.dataSource instanceof SearchLayerDataSource);
            if (index !== -1) {
                let existingSearchLayer = layers[index];
                let ds = existingSearchLayer.dataSource as SearchLayerDataSource;

                if (ds.getText() === text && ds.getDate() === date) {
                    return;
                } else {
                    layers.splice(index, 1);
                }
            }

            // Add our search layer
            layers.splice(0, 0, dataSource.createLayer(this.lService.decode("explorer.search.layer") + " (" + text + ")", true, ColorGen().hexString()));

            this.geomService.setLayers(layers);

            this.data = data.features;
            this.state.currentText = text;
            this.state.currentDate = date;
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

                this.feature = null;
            }

            const index = this.geomService.getLayers().findIndex(lFind => lFind.dataSource.getDataSourceType() === LIST_VECTOR_SOURCE_TYPE && record.version === (lFind.dataSource as ListVectorLayerDataSource).getVersionId());

            if (index !== -1) {
                const layer = this.geomService.getLayers()[index];

                // Highlight the feature on the map
                this.feature = {
                    source: layer.dataSource.getId(),
                    sourceLayer: "context",
                    id: uid
                };

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
                        // const uid: string = record.data["originalOid"];

                        // this.handleSelect(record.typeCode, code, uid);

                        this.router.navigate([], {
                            relativeTo: this.route,
                            queryParams: { type: record.typeCode, code: code, uid: uid, version: record.version },
                            queryParamsHandling: "merge" // remove to replace all query params by provided
                        });

                        // this.zoomToFeature(node, null);
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
            this.geomService.stopEditing();
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
            this.geomService.stopEditing();
        }
    }

    select(node: any, event: MouseEvent): void {
        if (!this.isEdit) {
            if (this.feature != null) {
                this.map.removeFeatureState(this.feature);
                this.feature = null;
            }

            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { type: node.properties.type, code: node.properties.code, objectType: "GEOOBJECT", uid: node.properties.uid, version: null },
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
        // if (this.feature != null) {
        //    this.map.removeFeatureState(this.feature);
        // }

        // Highlight the feature on the map
        /*
        if (this.feature && code !== "__NEW__" && this.map.getSource(SEARCH_LAYER) != null) {
            this.feature = {
                source: SEARCH_LAYER,
                id: uid
            };
            this.map.setFeatureState(this.feature, {
                selected: true
            });
        }
        */

        this.typeCache.waitOnTypes().then(() => {
            const type: GeoObjectType = this.typeCache.getTypeByCode(typeCode);

            this.current = {
                type: type,
                code: code,
                forDate: this.state.currentDate === "" ? null : this.state.currentDate,
                geoObject: geoObject
            };

            this.geomService.stopEditing();
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

}
