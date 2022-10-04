import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild, HostListener, Injector, ApplicationRef, ComponentFactoryResolver } from "@angular/core";
import { Location } from "@angular/common";
import { ActivatedRoute, Router } from "@angular/router";
import { Map, NavigationControl, AttributionControl, IControl, LngLatBounds } from "mapbox-gl";

import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import * as ColorGen from "color-generator";

import { GeoObject, GeoObjectType, GeoObjectTypeCache } from "@registry/model/registry";
import { ModalState, PANEL_SIZE_STATE } from "@registry/model/location-manager";

import { MapService, RegistryService, GeometryService } from "@registry/service";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler, ConfirmModalComponent, SuccessModalComponent } from "@shared/component";

import { AuthService, DateService, LocalizationService } from "@shared/service";
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
import { GEO_OBJECT_DATA_SOURCE_TYPE, Layer, ListVectorLayerDataSource, SearchLayerDataSource, LIST_VECTOR_SOURCE_TYPE, SEARCH_DATASOURCE_TYPE, RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE, GeoObjectLayerDataSource, ValueOverTimeDataSource, RelationshipVisualizionDataSource } from "@registry/service/layer-data-source";
import { BusinessObject, BusinessType } from "@registry/model/business-type";
import { BusinessObjectService } from "@registry/service/business-object.service";
import { Vertex } from "@registry/model/graph";
import { LocalizedValue } from "@shared/model/core";
import { LocationManagerService } from "@registry/service/location-manager.service";

declare let registry: GeoRegistryConfiguration;

class SelectedObject {

    objectType: string;
    code: string;

    // If geo object
    forDate?: string;

    // If business object
    businessObject?: BusinessObject;
    businessType?: BusinessType;

}

class SelectedList {

    versionId: string;
    uid?: string;

}

export interface LocationManagerState {
    layers?: string,
    graphPanelOpen?: boolean,
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
    attrPanelOpen?: boolean,
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
        VIEW: 1
    };

    CONSTANTS = {
        SEARCH_OVERLAY: OverlayerIdentifier.SEARCH_PANEL
    };

    bsModalRef: BsModalRef;

    /*
     * Search results from the server
     */
    data: GeoObject[] = [];

    state: LocationManagerState = { attrPanelOpen: true };

    /*
     * Currently selected record
     */
    current: SelectedObject;

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

    searchFieldText: string;

    dateFieldValue: string;

    list: SelectedList = null;
    recordContext: string = "MAP";

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
        private dateService: DateService,
        private locationManagerService: LocationManagerService,
        private businessObjectService: BusinessObjectService,
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

        // this.subscription = this.route.queryParams.subscribe(state => { this.handleStateChange(state); });
        this.subscription = this.locationManagerService.stateChange$.subscribe(state => this.handleStateChange(state));

        this.searchEnabled = registry.searchEnabled && (this.authService.isRC(false) || this.authService.isRM() || this.authService.isRA());
        this.graphVisualizerEnabled = registry.graphVisualizerEnabled || false;

        this.typeCache = this.cacheService.getTypeCache();

        this.geomService.dumpLayers();

        // const version = this.route.snapshot.queryParamMap.get("version");

        // if (version != null) {
        //     this.onViewList(version);
        // }
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

        if (this.state.bounds != null && this.state.bounds.length > 0) {
            mapConfig.bounds = new LngLatBounds(JSON.parse(this.state.bounds));
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

    updateState(newState: LocationManagerState, pushBackHistory: boolean = false): void {
        this.locationManagerService.setState(newState, pushBackHistory);
    }

    onGraphNodeSelect(node: Vertex): void {
        this.closeEditSessionSafeguard().then(() => {
            (node as any).selectAnimation(() => {
                if (node.objectType === "GEOOBJECT") {
                    let mockGeo = {
                        properties: {
                            type: node.typeCode,
                            uid: node.id,
                            code: node.code,
                            displayLabel: new LocalizedValue(node.label, [])
                        }
                    } as GeoObject;

                    this.selectGeoObject(mockGeo);
                } else if (node.objectType === "BUSINESS") {
                    this.updateState({ type: node.typeCode, code: node.code, objectType: node.objectType, uid: null, version: null, text: null }, true);
                }
            });
        });
    }

    /**
    *
    * Method responsible for parsing the state from the URL parameters and determining if
    * the model of the widget needs to be updated or not.
    *
    * */
    handleStateChange(newState: LocationManagerState): void {
        newState = JSON.parse(JSON.stringify(newState));
        let oldState = JSON.parse(JSON.stringify(this.state));

        if (this.ready) {
            this.state = newState;

            let mode = this.MODE.SEARCH;
            let showPanel: boolean = (newState.attrPanelOpen || newState.attrPanelOpen === undefined);

            if (newState != null) {
                if (newState.date !== oldState.date) {
                    this.dateFieldValue = newState.date;
                }

                // Handle parameters for searching for a geo object
                if (newState.text != null) {
                    if (newState.text !== oldState.text || newState.date !== oldState.date) {
                        this.searchFieldText = newState.text;

                        this.loadSearchFromState();
                    }

                    showPanel = true;
                }

                // Handle parameters for selecting a geo object
                if ((newState.objectType == null || newState.objectType === "GEOOBJECT") && newState.type != null && newState.code != null) {
                    if (oldState.type !== newState.type || oldState.code !== newState.code || oldState.date !== newState.date) {
                        this.loadGeoObjectFromState();
                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

                // Handle parameters for selecting a business object
                if (newState.objectType != null && newState.objectType === "BUSINESS" && newState.type && newState.code) {
                    if (this.current == null || this.current.businessObject == null || this.current.businessObject.code !== newState.code || this.current.businessType.code !== newState.type) {
                        this.selectBusinessObject();
                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

                // Handle parameters for select a record from a context layer
                if (newState.version != null && newState.uid != null) {
                    if (this.current == null || this.feature == null || this.feature.version !== newState.version || this.feature.id !== newState.uid) {
                        if (newState.bounds === oldState.bounds) {
                            this.handleRecord(newState.version, newState.uid);
                        }
                    }

                    showPanel = true;
                    mode = this.MODE.VIEW;
                }

                if (newState.pageContext) {
                    this.pageMode = newState.pageContext;
                }

                if (newState.layersPanelSize) {
                    this.layersPanelSize = Number.parseInt(newState.layersPanelSize);
                } else {
                    this.layersPanelSize = (this.pageMode === "EXPLORER") ? PANEL_SIZE_STATE.FULLSCREEN : this.layersPanelSize;
                }

                if (newState.attrPanelOpen != null) {
                    showPanel = newState.attrPanelOpen;
                }
            }

            this.changeMode(mode);
            if (oldState.attrPanelOpen !== showPanel) {
                this.setPanel(showPanel);
            }

            if (newState.bounds != null && newState.bounds.length > 0 && !this.geomService.isMapZooming()) {
                const bounds = JSON.parse(newState.bounds);

                this.mapBounds = this.convertMapBounds(new LngLatBounds(bounds));
                const llb = this.convertMapBounds(this.map.getBounds());

                if (llb.toString() !== this.mapBounds.toString()) {
                    this.map.fitBounds(this.mapBounds, { animate: false });
                }
            }
        }
    }

    selectBusinessObject() {
        this.businessObjectService.getTypeAndObject(this.state.type, this.state.code).then(resp => {
            this.current = {
                objectType: "BUSINESS",
                code: this.state.code,
                businessObject: resp.object,
                businessType: resp.type
            };

            this.mode = this.MODE.VIEW;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        }).finally(() => {
            this.spinner.hide(OverlayerIdentifier.FEATURE_PANEL);
        });
    }

    setPanel(showPanel: boolean): void {
        if (this.state.attrPanelOpen !== showPanel) {
            this.updateState({ attrPanelOpen: showPanel }, false);

            timeout(() => {
                this.map.resize();
            }, 1);
        }
    }

    togglePanel(): void {
        this.setPanel(!(this.state.attrPanelOpen));
    }

    changeMode(mode: number): void {
        if (mode !== this.mode) {
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
            } else if (this.mode === this.MODE.VIEW) {
                // empty
            }
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
            const mapBounds = this.convertMapBounds(this.map.getBounds());

            if (this.mapBounds == null || this.mapBounds.toString() !== mapBounds.toString()) {
                const array = mapBounds.toArray();

                this.updateState({ bounds: JSON.stringify(array) });
            }
        });

        this.handleStateChange(this.locationManagerService.getState());
    }

    onCreate(layer: any): void {
        if (layer.dataSource.dataSourceType === "LISTVECT") {
            this.closeEditSessionSafeguard().then(() => {
                this.listService.getVersion(layer.dataSource.versionId).then(version => {
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
                                if ((this.current == null || feature.properties == null || this.state.code !== feature.properties.code || this.state.type !== feature.properties.type)) {
                                    let geoObject: GeoObject = JSON.parse(JSON.stringify(feature));
                                    geoObject.properties.displayLabel = feature.properties.displayLabel != null ? JSON.parse(feature.properties.displayLabel) : null;

                                    this.selectGeoObject(geoObject);
                                }
                            } else {
                                if (layer.dataSource.getDataSourceType() === LIST_VECTOR_SOURCE_TYPE) {
                                    this.recordContext = "MAP";

                                    const versionId = (layer.dataSource as ListVectorLayerDataSource).getVersionId();

                                    if (this.state.version == null || this.state.uid == null ||
                                        this.state.version !== versionId ||
                                        this.state.uid !== feature.properties.uid) {
                                        this.updateState({ version: versionId, uid: feature.properties.uid }, false);
                                    } else {
                                        this.handleRecord(versionId, feature.properties.uid);
                                    }
                                } else if (layer.dataSource.getDataSourceType() === GEO_OBJECT_DATA_SOURCE_TYPE) {
                                    let geoObject: GeoObject = JSON.parse(JSON.stringify(feature));
                                    geoObject.properties.displayLabel = feature.properties.displayLabel != null ? JSON.parse(feature.properties.displayLabel) : null;

                                    this.selectGeoObject(geoObject);
                                } else if (layer.dataSource.getDataSourceType() === RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE) {
                                    let geoObject: GeoObject = JSON.parse(JSON.stringify(feature));
                                    geoObject.properties.displayLabel = feature.properties.displayLabel != null ? JSON.parse(feature.properties.displayLabel) : null;

                                    this.selectGeoObject(geoObject);
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
        let layers = this.geomService.getLayers();

        // Check for an existing search layer with the same data
        let index = layers.findIndex(layer => layer.dataSource instanceof SearchLayerDataSource);
        if (index !== -1) {
            let existingSearchLayer = layers[index];
            let ds = existingSearchLayer.dataSource as SearchLayerDataSource;

            if (ds.getText() === this.searchFieldText && ds.getDate() === this.dateFieldValue) {
                return;
            }
        }

        // Remove any existing search layer(s)
        layers = layers.filter(layer => layer.getPinned() ||
            (!(layer.dataSource instanceof SearchLayerDataSource) && !(layer.dataSource instanceof RelationshipVisualizionDataSource)) ||
            ((layer.dataSource instanceof SearchLayerDataSource) && (layer.dataSource as SearchLayerDataSource).getText() === this.searchFieldText && (layer.dataSource as SearchLayerDataSource).getDate() === this.dateFieldValue)
        );

        // Add our search layer
        let dataSource = new SearchLayerDataSource(this.mapService, this.searchFieldText, this.dateFieldValue);
        let layer = dataSource.createLayer(this.lService.decode("explorer.search.layer") + " (" + this.searchFieldText + ")", true, ColorGen().hexString());
        layers.splice(0, 0, layer);
        this.geomService.zoomOnReady(layer.getId());

        layers = layers.filter(l =>
            l.getPinned() || // Always keep pinned layers
            (
                !(l.dataSource instanceof ValueOverTimeDataSource) && !(l.dataSource instanceof GeoObjectLayerDataSource) && // Remove All Geo-Object layers
                !(l.dataSource instanceof RelationshipVisualizionDataSource) // Remove all Relationship Visualization layers
            )
        );

        let newState: LocationManagerState = {
            text: this.searchFieldText,
            date: this.dateFieldValue,
            type: null,
            code: null,
            version: null,
            uid: null,
            layers: this.geomService.serializeLayers(layers)
        };

        this.updateState(newState, true);
    }

    loadSearchFromState(): void {
        this.geomService.stopEditing();

        this.spinner.show(this.CONSTANTS.SEARCH_OVERLAY);

        let dataSource = new SearchLayerDataSource(this.mapService, this.state.text, this.state.date);

        dataSource.getLayerData().then((data: any) => {
            this.spinner.hide(this.CONSTANTS.SEARCH_OVERLAY);

            this.data = data.features;
        }).catch(() => {
            this.spinner.hide(this.CONSTANTS.SEARCH_OVERLAY);
            this.state.text = "";
            this.state.date = "";
        });
    }

    handleRecord(list: string, uid: string): void {
        // Get the feature data from the server and populate the left-hand panel
        this.listService.record(list, uid, false).then(record => {
            if (this.feature != null) {
                this.map.removeFeatureState(this.feature);

                this.feature = null;
            }

            window.setTimeout(() => {
                const index = this.geomService.getLayers().findIndex(lFind => lFind.dataSource.getDataSourceType() === LIST_VECTOR_SOURCE_TYPE && record.version === (lFind.dataSource as ListVectorLayerDataSource).getVersionId());

                if (index !== -1) {
                    const layer = this.geomService.getLayers()[index];

                    if (this.map.getSource(layer.dataSource.getId()) != null) {
                        // Highlight the feature on the map
                        this.feature = {
                            source: layer.dataSource.getId(),
                            sourceLayer: "context",
                            id: uid,
                            version: list
                        };

                        this.map.setFeatureState(this.feature, {
                            selected: true
                        });
                    }
                }
            }, 5);

            if (record.recordType === "GEO_OBJECT") {
                this.selectGeoObject({
                    properties: {
                        type: record.typeCode,
                        code: record.code,
                        uid: record.uid,
                        displayLabel: record.displayLabel
                    }
                } as GeoObject);
            } else if (record.recordType === "LIST") {
                if (this.recordContext === "MAP") {
                    this.list = null;

                    timeout(() => {
                        this.list = {
                            versionId: list,
                            uid: uid
                        };
                    }, 10);
                } else {
                    const bounds = record.bbox;

                    if (bounds && Array.isArray(bounds)) {
                        let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
                        let config: any = { padding: { top: 10, bottom: 10, left: 10, right: 10 }, animate: true, maxDuration: 5000, maxZoom: 20 };

                        this.map.fitBounds(llb, config);
                    }
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
        this.updateState({ type: null, code: null, version: null, uid: null }, false);
    }

    featurePanelForDateChange(date: string) {
        // if (date !== null) {
        this.geomService.stopEditing();

        this.updateState({ date: date }, false);
        // }
    }

    select(node: any, event: MouseEvent): void {
        if (!this.isEdit) {
            if (this.feature != null) {
                this.map.removeFeatureState(this.feature);
                this.feature = null;
            }

            this.updateState({ type: node.properties.type, code: node.properties.code, objectType: "GEOOBJECT", uid: node.properties.uid, version: null }, false);

            // this.zoomToFeature(node, null);
        }
    }

    selectGeoObject(geoObject: GeoObject, date: string = null): void {
        this.typeCache.waitOnTypes().then(() => {
            this.closeEditSessionSafeguard().then(() => {
                let newLayerState = this.addLayerForGeoObject(geoObject, date);

                let newState: LocationManagerState = {
                    type: geoObject.properties.type,
                    code: geoObject.properties.code,
                    objectType: "GEOOBJECT",
                    uid: geoObject.properties.uid,
                    version: null,
                    text: null,
                    layers: this.geomService.serializeLayers(newLayerState)
                };

                if (date != null) {
                    newState.date = date;
                }

                this.updateState(newState, true);
            });
        });
    }

    loadGeoObjectFromState(): void {
        this.typeCache.waitOnTypes().then(() => {
            const type: GeoObjectType = this.typeCache.getTypeByCode(this.state.type);

            this.geomService.stopEditing();
            this.geomService.setGeometryType(null);

            this.geomService.setGeometryType(type.geometryType);

            this.current = {
                objectType: "GEOOBJECT",
                code: this.state.code,
                forDate: this.state.date === "" ? null : this.state.date
            };

            if (this.state.code !== "__NEW__") {
                // this.zoomToFeature(this.current.geoObject, null);
            }
        });
    }

    addLayerForGeoObject(geoObject: GeoObject, paramDate: string = null): Layer[] {
        // this.service.getGeoObjectByCode(code, typeCode).then(geoObject => {

        const type: GeoObjectType = this.typeCache.getTypeByCode(geoObject.properties.type);

        // Add layer
        let layers: Layer[] = this.geomService.getLayers();

        let date = (paramDate != null) ? paramDate : (this.state == null ? null : this.state.date);
        let dataSource = new GeoObjectLayerDataSource(this.service, geoObject.properties.code, geoObject.properties.type, date);

        let displayLabel = geoObject.properties.displayLabel.localizedValue;
        let typeLabel = type.label.localizedValue;
        let sDate = date == null ? "" : " " + this.dateService.formatDateForDisplay(date);
        let label = displayLabel + " " + sDate + "(" + typeLabel + ")";

        let layer = dataSource.createLayer(label, true, ColorGen().hexString());

        // Remove any existing Geo-Object layer(s)
        layers = layers.filter(l =>
            !(l.dataSource instanceof GeoObjectLayerDataSource) ||
            l.getKey() === layer.getKey() ||
            l.getPinned()
        );

        // Remove search layer
        layers = layers.filter(layer => layer.getPinned() || (!(layer.dataSource instanceof SearchLayerDataSource) && !(layer.dataSource instanceof RelationshipVisualizionDataSource)));

        if (layers.findIndex(l => l.getKey() === layer.getKey()) === -1) {
            // Add our search layer
            layers.splice(0, 0, layer);

            this.geomService.zoomOnReady(layer.getId());
        }

        return layers;
    }

    onFeatureSelect(event: any): void {
        if (!this.isEdit) {
            this.state.text = event.item.name;

            const feature = event.item.feature;

            if (feature.properties.uid != null) {
                this.listService.getBounds(feature.version, feature.properties.uid).then(bounds => {
                    if (bounds && Array.isArray(bounds)) {
                        let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

                        this.map.fitBounds(llb, { padding: 50, animate: true, maxZoom: 20 });
                    }
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            }

            if (feature.properties.uid != null) {
                this.updateState({ type: null, code: null, version: feature.version, uid: feature.properties.uid }, false);
            }
        }
    }

    toggleGraphPanel(): void {
        this.updateState({ graphPanelOpen: !this.state.graphPanelOpen }, false);
    }

    convertMapBounds(llb: LngLatBounds): LngLatBounds {
        const ne = llb.getNorthEast();
        const sw = llb.getSouthWest();

        const bounds = LngLatBounds.convert([
            [parseFloat(sw.lng.toFixed(10)), parseFloat(sw.lat.toFixed(10))],
            [parseFloat(ne.lng.toFixed(10)), parseFloat(ne.lat.toFixed(10))]
        ]);

        return bounds;
    }

    onViewList(oid: string): void {
        this.list = null;

        timeout(() => {
            this.list = {
                versionId: oid
            };
        }, 10);
    }

    onRowSelect(event: { version: string, uid: string }): void {
        this.recordContext = "ROW";

        if (this.state.version == null || this.state.uid == null ||
            this.state.version !== event.version ||
            this.state.uid !== event.uid) {
            this.updateState({ version: event.version, uid: event.uid });
        } else {
            this.handleRecord(event.version, event.uid);
        }
    }

    onListPanelClose(): void {
        this.list = null;
    }

    isAttributePanelOpen(): boolean {
        return (this.state.attrPanelOpen && ((this.mode === this.MODE.VIEW && this.current != null) || (this.mode === this.MODE.SEARCH && this.searchEnabled && this.data.length > 0)));
    }

    error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
