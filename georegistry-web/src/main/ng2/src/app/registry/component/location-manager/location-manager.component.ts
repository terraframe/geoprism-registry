import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { Map, LngLatBoundsLike, NavigationControl, MapboxEvent, AttributionControl, IControl, LngLatBounds } from "mapbox-gl";

import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { AllGeoJSON } from "@turf/helpers";
import bbox from "@turf/bbox";

import { GeoObject, ValueOverTime } from "@registry/model/registry";
import { ModalState } from "@registry/model/location-manager";

import { MapService, RegistryService, GeometryService } from "@registry/service";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler, ConfirmModalComponent, SuccessModalComponent } from "@shared/component";

import { LocalizationService } from "@shared/service";
import { ContextLayer, LayerRecord } from "@registry/model/list-type";
import { LayerEvent } from "./layer-panel.component";
import { ListTypeService } from "@registry/service/list-type.service";
import { timeout } from "d3-timer";
import { Subscription } from "rxjs";

declare let acp: string;

const DEFAULT_COLOR = "#80cdc1";
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
        VIEW: 1,
        NONE: 2
    }

    editSessionEnabled: boolean = false;

    bsModalRef: BsModalRef;

    code: string = null;

    /*
     * Root nodes of the tree
     */
    data: GeoObject[] = [];

    /*
     *  Search Text
     */
    text: string = "";

    /*
     *  MODE
     */
    mode: number = this.MODE.SEARCH;

    /*
     * Date of data for explorer
     */
    dateStr: string = null;

    forDate: Date = null;

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
     * Flag denoting the draw control is active
     */
    active: boolean = false;

    public displayDateRequiredError: boolean = false;

    vectorLayers: ContextLayer[] = [];

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

    hideSearchOptions: boolean = false;

    /*
       * Timer for determining double click vs single click
       */
    timer: any;

    @ViewChild("simpleEditControl") simpleEditControl: IControl;

    params: any = null;

    subscription: Subscription;

    private ready: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private route: ActivatedRoute,
        private modalService: BsModalService,
        private service: RegistryService,
        private listService: ListTypeService,
        private mapService: MapService,
        private geomService: GeometryService,
        private lService: LocalizationService) { }

    ngOnInit(): void {

        this.subscription = this.route.params.subscribe((params: any) => {
            this.params = params;
        });

        // this.urlSubscriber = this.route.params.subscribe(params => {
        //     let geoObjectUid = params["geoobjectuid"];
        //     let geoObjectTypeCode = params["geoobjecttypecode"];
        //     this.hideSearchOptions = params["hideSearchOptions"];
        //     this.backReference = this.route.snapshot ? this.route.snapshot.params["backReference"] : null;

        //     this.dateStr = params["datestr"];
        //     this.handleDateChange();

        //     if (geoObjectUid && geoObjectTypeCode && this.dateStr) {
        //         this.service.getGeoObject(geoObjectUid, geoObjectTypeCode).then(geoObj => {
        //             this.setData([geoObj]);
        //             this.select(geoObj, null);
        //         }).catch((err: HttpErrorResponse) => {
        //             this.error(err);
        //         });
        //     }
        // });
    }

    ngOnDestroy(): void {
        this.geomService.destroy();
        this.subscription.unsubscribe();
        // this.urlSubscriber.unsubscribe();
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
                glyphs: window.location.protocol + "//" + window.location.host + acp + "/glyphs/{fontstack}/{range}.pbf",
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

    changeMode(mode: number): void {
        this.mode = mode;

        if (this.mode === this.MODE.NONE) {
            this.isEdit = false;
        }

        this.geomService.destroy(false);

        timeout(() => {
            this.map.resize();
        }, 1);
    }

    onModeChange(value: boolean): void {
        this.isEdit = value;
    }

    handleDateChange(): void {
        if (this.dateStr != null) {
            this.forDate = new Date(Date.parse(this.dateStr));
            this.displayDateRequiredError = false;
        }
    }

    initMap(): void {
        this.map.on("style.load", () => {
            this.addLayers();
        });

        this.addLayers();

        // Add zoom and rotation controls to the map.
        this.map.addControl(new NavigationControl({ visualizePitch: true }));
        this.map.addControl(new AttributionControl({ compact: true }), "bottom-right");

        this.map.on("click", (event: any) => {
            this.handleMapClickEvent(event);
        });

        // Set map data on page load with URL params (single Geo-Object)
        if (this.data) {
            let fc = { type: "FeatureCollection", features: this.data };
            (<any>this.map.getSource("children")).setData(fc);

            this.zoomToFeature(this.data[0], null);
        }

        // Highlight the feature
        if (this.params.version != null && this.params.code != null) {
            this.getRecord(this.params.version, this.params.code);

            this.map.setFeatureState(this.feature = {
                source: this.params.version,
                sourceLayer: 'context',
                id: this.params.code
            }, {
                hover: true
            });

            this.onZoomTo(this.params.version);
        }

        // this.showOriginalGeometry();
    }


    // showOriginalGeometry() {
    //     if (this.current) {
    //         this.addVectorLayer(this.current.properties.uid, DEFAULT_COLOR);
    //     }
    // }

    // hideOriginalGeometry() {
    //     if (this.current) {
    //         this.removeVectorLayer(this.current.properties.uid);
    //     }
    // }

    onZoomTo(oid: string): void {
        this.listService.getBounds(oid).then(bounds => {
            if (bounds) {
                let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

                this.map.fitBounds(llb, { padding: 50, animate: true, maxZoom: 20 });
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }


    handleMapClickEvent(e: any): void {
        if (!this.isEdit) {
            const features = this.map.queryRenderedFeatures(e.point);

            if (features != null && features.length > 0) {
                const feature = features[0];

                if (feature.properties.code != null) {
                    if (this.feature != null) {
                        this.map.removeFeatureState(this.feature);
                    }

                    // Highlight the feature on the map
                    this.map.setFeatureState(this.feature = {
                        source: feature.source,
                        sourceLayer: feature.sourceLayer,
                        id: feature.id
                    }, {
                        hover: true
                    });

                    if (feature.source === 'children') {
                        this.select(feature, null);
                    }
                    else {
                        this.getRecord(feature.source, feature.properties.code);
                    }
                }
            }
        }
    }

    onPanelCancel(): void {
        if (this.backReference != null && this.backReference.length >= 2) {
            // let ref = this.backReference.substring(0, 2);

            // if (ref === "CR") {
            //     this.router.navigate(["/registry/change-requests"]);
            // }
        } else {
            this.changeMode(this.MODE.NONE);
        }

        // this.showOriginalGeometry();
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
                    // this.router.navigate(["/registry/change-requests", applyInfo.changeRequestId]);
                });
            } else {
                this.bsModalRef = this.modalService.show(ConfirmModalComponent, { backdrop: true, class: "error-white-space-pre" });

                this.bsModalRef.content.message = this.lService.decode("geoobject-editor.changerequest.submitted");
                this.bsModalRef.content.submitText = this.lService.decode("geoobject-editor.changerequest.view");
                this.bsModalRef.content.cancelText = this.lService.decode("geoobject-editor.cancel.returnExplorer");

                this.bsModalRef.content.onConfirm.subscribe(() => {
                    // this.router.navigate(["/registry/change-requests", applyInfo.changeRequestId]);
                });
                this.bsModalRef.content.onCancel.subscribe(() => {
                    this.changeMode(this.MODE.NONE);
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
        const source = "children";

        this.map.addSource(source, {
            type: "geojson",
            data: {
                type: "FeatureCollection",
                features: [],
            },
            promoteId: 'code'
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
                    DEFAULT_COLOR
                ],
                "fill-opacity": 0.8,
                "fill-outline-color": "black"
            },
            filter: ["all",
                ["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
            ]
        });

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
                    DEFAULT_COLOR
                ],
                "circle-stroke-width": 2,
                "circle-stroke-color": "#FFFFFF"
            },
            filter: ["all",
                ["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
            ]
        });

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
        });

        this.vectorLayers.forEach(cLayer => {
            this.addVectorLayer(cLayer);
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
            glyphs: window.location.protocol + "//" + window.location.host + acp + "/glyphs/{fontstack}/{range}.pbf",
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
        this.geomService.destroy(false);
        this.mapService.search(this.text, this.dateStr).then(data => {
            (<any>this.map.getSource("children")).setData(data);

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

    getRecord(list: string, code: string): void {
        // Get the feature data from the server and populate the left-hand panel
        this.listService.record(list, code).then(record => {
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

    select(node: any, event: MouseEvent): void {

        // Highlight the feature on the map
        this.service.getGeoObjectTypes([node.properties.type], null).then(types => {
            this.mode = this.MODE.VIEW;

            const type = types[0];
            this.record = {
                recordType: 'GEO_OBJECT',
                type: type,
                code: node.properties.code,
                forDate: this.dateStr
            };

            if (this.record.recordType === 'GEO_OBJECT') {
                this.geomService.destroy(false);

                this.geomService.initialize(this.map, this.record.type.geometryType, false);
            }


        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });

        /*
        if (this.forDate == null) {
            this.displayDateRequiredError = true;

            return;
        }
        */

        // if (event != null) {
        //     event.stopPropagation();
        // }

        // this.service.getGeoObjectTypes([node.properties.type], null).then(types => {
        //     this.type = types[0];
        //     this.current = node;
        //     this.mode = this.MODE.VIEW;

        //     this.geomService.initialize(this.map, this.type.geometryType, !this.isEdit);
        //     this.geomService.zoomToLayersExtent();

        //     //      const code = this.current.properties.code;
        //     //
        //     //      // Update the filter properties
        //     //      this.map.setFilter('children-points-selected', ['all',
        //     //        ["==", ['get', 'code'], code != null ? code : ''],
        //     //        ["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
        //     //      ]);
        //     //
        //     //      this.map.setFilter('children-polygon-selected', ['all',
        //     //        ["==", ['get', 'code'], code != null ? code : ''],
        //     //        ["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
        //     //      ]);
        // }).catch((err: HttpErrorResponse) => {
        //     this.error(err);
        // });
    }

    setData(data: GeoObject[]): void {
        this.data = data;
    }

    onContextLayerChange(event: LayerEvent): void {
        const layer = event.layer;

        if (layer.active) {
            this.addVectorLayer(layer, event.prevLayer);
        } else {
            this.removeVectorLayer(layer);
        }
    }

    onReorderLayers(layers: ContextLayer[]): void {
        for (let i = layers.length - 1; i > -1; i--) {
            const layer = layers[i];

            this.map.moveLayer(layer.oid + "-polygon", "children-polygon");
            this.map.moveLayer(layer.oid + "-points", "children-polygon");
            this.map.moveLayer(layer.oid + "-label", "children-polygon");
        };
    }


    removeVectorLayer(layer: ContextLayer): void {
        const index = this.vectorLayers.findIndex(l => l.oid === layer.oid);

        if (index !== -1) {
            const source = layer.oid;

            this.map.removeLayer(source + "-polygon");
            this.map.removeLayer(source + "-points");
            this.map.removeLayer(source + "-label");
            this.map.removeSource(source);

            this.vectorLayers.splice(index, 1);
        }
    }

    addVectorLayer(layer: ContextLayer, otherLayer?: ContextLayer): void {

        if (this.ready) {

            const source = layer.oid;
            const prevLayer = otherLayer != null ? otherLayer.oid + '-polygon' : "children-polygon";

            let protocol = window.location.protocol;
            let host = window.location.host;

            this.map.addSource(source, {
                type: "vector",
                tiles: [protocol + "//" + host + acp + "/list-type/tile?x={x}&y={y}&z={z}&config=" + encodeURIComponent(JSON.stringify({ oid: source }))],
                promoteId: 'code'
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

        this.vectorLayers.push(layer);
    }


    error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
