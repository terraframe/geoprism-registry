
import { Injectable, Output, EventEmitter, OnDestroy } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";

import * as MapboxDraw from "@mapbox/mapbox-gl-draw";
import { Map, LngLat, LngLatBounds } from "mapbox-gl";
import { Subscription } from "rxjs";

import { GeoRegistryConfiguration } from "@core/model/registry"; declare let registry: GeoRegistryConfiguration;

export const OLD_LAYER_COLOR = "#A4A4A4";

export const NEW_LAYER_COLOR = "#0062AA";

export const SELECTED_COLOR = "#800000";

export class ParamLayer {

    constructor(oid: string, legendLabel: string, rendered: boolean, color: string, dataSourceId: string, dataSourceProviderId: string) {
        this.oid = oid;
        this.legendLabel = legendLabel;
        this.rendered = rendered;
        this.color = color;
        this.dataSourceId = dataSourceId;
        this.dataSourceProviderId = dataSourceProviderId;
    }

    oid: string;
    legendLabel: string;
    rendered: boolean;
    color: string;
    dataSourceId: string;
    dataSourceProviderId: string;

}

export interface LayerDataSource {

    buildMapboxSource(): any;

    getGeometryType(): string;

    getDataSourceId(): string;

    getDataSourceProviderId(): string;

    // eslint-disable-next-line no-use-before-define
    createLayer(oid: string, legendLabel: string, rendered: boolean, color: string): Layer;

    configureMapboxLayer?(layerConfig: any): void;

}

export interface GeoJsonLayerDataSource extends LayerDataSource {

    getLayerData(): any;
    setLayerData(data: any): void;

}

export interface DataSourceProvider {

    getId(): string;
    getDataSource(dataSourceId: string): LayerDataSource;

}

export class Layer {

    constructor(oid: string, legendLabel: string, dataSource: LayerDataSource, rendered: boolean, color: string) {
        this.oid = oid;
        this.legendLabel = legendLabel;
        this.dataSource = dataSource;
        this.rendered = rendered;
        this.color = color;
    }

    oid: string;
    legendLabel: string;
    dataSource: LayerDataSource;
    rendered: boolean;
    color: string;

    toParamLayer(): ParamLayer {
        return new ParamLayer(this.oid, this.legendLabel, this.rendered, this.color, this.dataSource.getDataSourceId(), this.dataSource.getDataSourceProviderId());
    }

}

export class GeoJsonLayer extends Layer {

    constructor(oid: string, legendLabel: string, dataSource: LayerDataSource, rendered: boolean, color: string) {
        super(oid, legendLabel, dataSource, rendered, color);
        this.editing = false;
    }

    editing: boolean;

}

export const GEO_OBJECT_LAYER_DATA_SOURCE_PROVIDER_ID: string = "GEOOBJECT";

export class GeoObjectLayerDataSourceProvider implements DataSourceProvider {

    getDataSource(dataSourceId: string): LayerDataSource {
        return {

            createLayer(oid: string, legendLabel: string, rendered: boolean, color: string): Layer {
                return new Layer(oid, legendLabel, this, rendered, color);
            },

            getDataSourceProviderId(): string {
                return GEO_OBJECT_LAYER_DATA_SOURCE_PROVIDER_ID;
            },

            getDataSourceId(): string {
                return dataSourceId;
            },

            getGeometryType(): string {
                return "MIXED";
            },

            buildMapboxSource() {
                let idSplit = dataSourceId.split("~");

                let url = registry.contextPath + "/cgr/geoobject/get-code" + "?" + "code=" + idSplit[0] + "&typeCode=" + idSplit[1];

                return {
                    type: "geojson",
                    data: url
                };
            }

        };
    }

    getId(): string {
        return GEO_OBJECT_LAYER_DATA_SOURCE_PROVIDER_ID;
    }

}

/**
 * This service provides a global abstraction for mapping and editing layers across many different components (simultaneously) and
 * serializing / deserializing these layers to / from the url parameters to facilitate saving + loading of layer state.
 *
 * Layers contain references to data source providers, which are invoked when deserializing from the url param to facilitate
 * population of layer data.
 */
@Injectable()
export class GeometryService implements OnDestroy {

    map: Map;

    layers: Layer[] = [];

    geometryType: String;

    readOnly: boolean;

    editingControl: any = null;

    simpleEditControl: any = null;

    editingLayer: GeoJsonLayer;

    layerGeometries: any = {};

    @Output() geometryChange = new EventEmitter<any>();

    @Output() layersChange: EventEmitter<Layer[]> = new EventEmitter();

    /*
    * Subscription for changes to the URL parameters
    */
    queryParamSubscription: Subscription;

    /*
     * URL pamaters
     */
    syncLayersWithUrlParams: boolean = false;

    params: any = null;

    dataSourceProviders: any = {};

    // eslint-disable-next-line no-useless-constructor
    constructor(
      private route: ActivatedRoute,
      private router: Router
    ) {}

    ngOnInit() {
        // TODO : Not sure that this method is ever invoked...
        window.onbeforeunload = () => this.destroy();
    }

    initialize(map: Map, geometryType: String, syncLayersWithUrlParams: boolean) {
        this.syncLayersWithUrlParams = syncLayersWithUrlParams;
        this.map = map;
        this.geometryType = geometryType;
        this.registerDataSourceProvider(new GeoObjectLayerDataSourceProvider());
        // this.editingControl = null;

        if (syncLayersWithUrlParams) {
            this.queryParamSubscription = this.route.queryParams.subscribe(params => {
                this.handleParameterChange(params);
            });
        }

        // this.mapAllLayers();

        this.map.on("style.load", () => {
            // this.mapAllLayers();
        });

        this.map.on("draw.create", () => {
            this.saveEdits();
        });
        this.map.on("draw.delete", () => {
            this.saveEdits();
        });
        this.map.on("draw.update", () => {
            this.saveEdits();
        });
    }

    ngOnDestroy(): void {
        if (this.queryParamSubscription) {
            this.queryParamSubscription.unsubscribe();
        }
    }

    handleParameterChange(params: Params): void {
        this.params = params;

        if (this.params != null) {
            if (this.params.layers != null) {
                let paramLayers: ParamLayer[] = JSON.parse(this.params.layers);

                if (this.map) {
                    this.internalUpdateLayers(paramLayers);
                }
            }
        }
    }

    private internalUpdateLayers(paramLayers: ParamLayer[]) {
        let layers = [];
        paramLayers.forEach(paramLayer => { let layer = this.buildLayerFromParamLayer(paramLayer); if (layer) { layers.push(layer); } });

        if (this.map) {
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
                            this.mapLayer(layers[i], prevLayer);
                        } else {
                            this.unmapLayer(layer);
                        }
                    }

                    if (paramLayer.rendered) {
                        prevLayer = this.layers[i];
                    }
                }

                fullRebuild = false;
            } else if (diffs.length === 1 && diffs[0].type === "NEW_LAYER" && diffs[0].index === this.layers.length && this.layers.length > 0) {
                // Added a layer at the end

                this.mapLayer(layers[layers.length - 1], this.layers.length > 0 ? this.layers[this.layers.length - 1] : null);
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

                this.layers = layers;
                this.mapAllLayers();
            } else {
                // Make sure attribute changes are reflected
                this.layers = layers;
            }
        } else {
            this.layers = layers;
        }

        this.layersChange.emit(this.layers);
    }

    public setLayers(paramLayers: ParamLayer[]) {
        if (this.queryParamSubscription) {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { layers: JSON.stringify(paramLayers) },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            });
        } else {
            this.internalUpdateLayers(paramLayers);
        }
    }

    buildLayerFromParamLayer(pl: ParamLayer): Layer {
        let dataSource = this.getDataSource(pl);

        if (dataSource) {
            return dataSource.createLayer(pl.oid, pl.legendLabel, pl.rendered, pl.color);
        } else {
            return null;
        }
    }

    public registerDataSourceProvider(dataSourceProvider: DataSourceProvider) {
        this.dataSourceProviders[dataSourceProvider.getId()] = dataSourceProvider;
    }

    public getDataSourceProvider(dataSourceProviderId: string): DataSourceProvider {
        return this.dataSourceProviders[dataSourceProviderId];
    }

    getDataSource(paramLayer: ParamLayer): LayerDataSource {
        if (this.dataSourceProviders[paramLayer.dataSourceProviderId]) {
            let provider: DataSourceProvider = this.dataSourceProviders[paramLayer.dataSourceProviderId];
            return provider.getDataSource(paramLayer.dataSourceId);
        } else {
            // eslint-disable-next-line no-console
            console.log("ERROR? Could not find provider for dataSourceProviderId [" + paramLayer.dataSourceProviderId + "]", paramLayer);
        }
    }

    public setGeometryType(geometryType: string) {
        this.geometryType = geometryType;
    }

    destroy(destroyMap: boolean = true): void {
        if (this.editingControl != null) {
            this.map.removeControl(this.editingControl);
            this.editingControl = null;
        }

        if (this.map != null && destroyMap) {
            this.map.remove();
            this.map = null;
        } else if (this.map != null) {
            this.unmapAllLayers();
        }

        if (this.layers != null) {
            this.layers.forEach(layer => {
                if (layer instanceof GeoJsonLayer) {
                    layer.editing = false;
                }
                layer.rendered = false;
            });
        }

        this.editingLayer = null;
        this.layers = [];
    }

    public getMap() {
        return this.map;
    }

    public setLayerGeometry(oid: string, geometry: any) {
        this.layerGeometries[oid] = geometry;
    }

    public getLayerGeometry(oid: string) {
        return this.layerGeometries[oid];
    }

    startEditing(layer: GeoJsonLayer) {
        if (this.isEditing()) {
            this.stopEditing();
        }

        this.editingLayer = layer;
        this.editingLayer.editing = true;

        if (!this.readOnly) {
            this.enableEditing();
        }

        this.addEditingLayers();
    }

    stopEditing(rerender: boolean = true) {
        if (this.isEditing()) {
            this.saveEdits(rerender);

            this.editingLayer.editing = false;
            this.editingLayer = null;

            this.editingControl.deleteAll();
            this.map.removeControl(this.editingControl);

            this.editingControl = null;
        }
    }

    isEditing(): boolean {
        return this.editingLayer != null;
    }

    setPointCoordinates(lat: any, long: any) {
        if (this.editingLayer != null) {
            this.editingControl.set({
                type: "FeatureCollection",
                features: [{
                    id: this.editingLayer.oid,
                    type: "Feature",
                    properties: {},
                    geometry: { type: "Point", coordinates: [long, lat] }
                }]
            });

            this.editingControl.changeMode("simple_select", { featureIds: this.editingLayer.oid });

            this.saveEdits();

            /*
            this.editingLayer.value = {
              type: 'FeatureCollection',
              features: [{
              id: this.editingLayer.oid,
                type: 'Feature',
                properties: {},
                geometry: { type: 'Point', coordinates: [ long, lat ] }
              }]
            };
            */

            /*
            this.editingLayer.value.coordinates = [ -97.4870830718814, 41.84836050415993 ];

            this.editingControl.set(this.editingLayer.value);

            this.unmapAllLayers();
            this.mapAllLayers();

            this.editingControl.changeMode( 'simple_select', { featureIds: this.editingLayer.oid } );
            */
        }
    }

    isValid(): boolean {
        if (!this.readOnly) {
            let isValid: boolean = false;

            if (this.editingControl != null) {
                let featureCollection: any = this.editingControl.getAll();

                if (featureCollection.features.length > 0) {
                    isValid = true;
                }
            }

            return isValid;
        }

        return true;
    }

    saveEdits(rerender: boolean = true): void {
        if (this.editingLayer != null) {
            let geoJson = this.getDrawGeometry();

            (this.editingLayer.dataSource as unknown as GeoJsonLayerDataSource).setLayerData(geoJson);

            if (rerender) {
                this.unmapAllLayers();
                this.mapAllLayers();
            }
        }
    }

    public reload(): void {
        if (this.map != null) {
            this.unmapAllLayers();
            this.mapAllLayers();

            if (this.editingControl != null) {
                this.editingControl.deleteAll();
            }

            this.addEditingLayers();
        }
    }

    setEditing(isEditing: boolean, layer: GeoJsonLayer) {
        if (this.isEditing()) {
            this.stopEditing();
        }

        layer.editing = isEditing;

        if (isEditing) {
            this.startEditing(layer);
        }
    }

    public serializeAllLayers(): ParamLayer[] {
        let paramLayers: ParamLayer[] = [];

        this.layers.forEach(layer => {
            paramLayers.push(layer.toParamLayer());
        });

        return paramLayers;
    }

    public addOrUpdateLayer(newLayer: ParamLayer, orderingIndex?: number) {
        let newLayers = this.serializeAllLayers();

        let existingIndex = newLayers.findIndex((findLayer: ParamLayer) => { return findLayer.oid === newLayer.oid; });

        if (existingIndex !== -1) {
            newLayers[existingIndex] = newLayer;
        } else {
            if (orderingIndex) {
                newLayers.splice(orderingIndex, 0, newLayer);
            } else {
                newLayers.push(newLayer);
            }
        }

        if (newLayer instanceof GeoJsonLayer && newLayer.editing) {
            this.startEditing(newLayer);
        }

        this.setLayers(newLayers);
    }

    public removeLayer(oid: string) {
        let newLayers = this.serializeAllLayers();

        let existingIndex = newLayers.findIndex((findLayer: ParamLayer) => { return findLayer.oid === oid; });

        if (existingIndex !== -1) {
            newLayers.splice(existingIndex, 1);

            this.setLayers(newLayers);
        }
    }

    getLayers(): Layer[] {
        return this.layers;
    }

    getRenderedLayers(): Layer[] {
        return this.layers.filter(layer => layer.rendered);
    }

    enableEditing(): void {
        if (this.editingControl == null) {
            if (this.geometryType === "MULTIPOLYGON" || this.geometryType === "POLYGON") {
                this.editingControl = new MapboxDraw({
                    controls: {
                        point: false,
                        line_string: false,
                        polygon: true,
                        trash: true,
                        combine_features: false,
                        uncombine_features: false
                    }
                });
            } else if (this.geometryType === "POINT" || this.geometryType === "MULTIPOINT") {
                this.editingControl = new MapboxDraw({
                    userProperties: true,
                    controls: {
                        point: true,
                        line_string: false,
                        polygon: false,
                        trash: true,
                        combine_features: false,
                        uncombine_features: false
                    },
                    styles: [
                        {
                            id: "highlight-active-points",
                            type: "circle",
                            filter: ["all",
                                ["==", "$type", "Point"],
                                ["==", "meta", "feature"],
                                ["==", "active", "true"]],
                            paint: {
                                "circle-radius": 13,
                                "circle-color": "#33FFF9",
                                "circle-stroke-width": 4,
                                "circle-stroke-color": "white"
                            }
                        },
                        {
                            id: "points-are-blue",
                            type: "circle",
                            filter: ["all",
                                ["==", "$type", "Point"],
                                ["==", "meta", "feature"],
                                ["==", "active", "false"]],
                            paint: {
                                "circle-radius": 10,
                                "circle-color": "#800000",
                                "circle-stroke-width": 2,
                                "circle-stroke-color": "white"
                            }
                        }
                    ]
                });
            } else if (this.geometryType === "LINE" || this.geometryType === "MULTILINE") {
                this.editingControl = new MapboxDraw({
                    controls: {
                        point: false,
                        line_string: true,
                        polygon: false,
                        trash: true,
                        combine_features: false,
                        uncombine_features: false
                    }
                });
            } else if (this.geometryType === "MIXED") {
                this.editingControl = new MapboxDraw({
                    controls: {
                        point: true,
                        line_string: true,
                        polygon: true,
                        trash: true,
                        combine_features: false,
                        uncombine_features: false
                    }
                });
            }

            if (this.map.getSource("mapbox-gl-draw-cold") == null) {
                this.map.addControl(this.editingControl, "bottom-right");
            }
        }
    }

    addEditingLayers(): void {
        if (this.editingLayer != null && this.editingControl != null) {
            let data = (this.editingLayer.dataSource as unknown as GeoJsonLayerDataSource).getLayerData();

            if (data) {
                this.editingControl.add(data);
            }
        }
    }

    unmapLayer(layer: Layer): void {
        if (this.map) {
            const layerName = layer.oid;

            if (this.map.getLayer(layerName + "-POLYGON") != null) {
                this.map.removeLayer(layerName + "-POLYGON");
            }
            if (this.map.getLayer(layerName + "-POINT") != null) {
                this.map.removeLayer(layerName + "-POINT");
            }
            if (this.map.getLayer(layerName + "-LINE") != null) {
                this.map.removeLayer(layerName + "-LINE");
            }
            if (this.map.getLayer(layerName + "-LABEL") != null) {
                this.map.removeLayer(layerName + "-LABEL");
            }
            if (this.map.getSource(layer.dataSource.getDataSourceId()) != null) {
                this.map.removeSource(layer.dataSource.getDataSourceId());
            }
        }
    }

    unmapAllLayers(): void {
        if (this.layers != null && this.layers.length > 0) {
            let len = this.layers.length;

            for (let i = 0; i < len; ++i) {
                let layer = this.layers[i];
                this.unmapLayer(layer);
            }
        }
    }

    mapAllLayers(): void {
        if (this.layers != null && this.layers.length > 0) {
            let prevLayer = null;
            let len = this.layers.length;
            for (let i = 0; i < len; ++i) {
                let layer = this.layers[i];

                if (layer.rendered) {
                    this.mapLayer(layer, prevLayer);
                    prevLayer = layer;
                }
            }
        }
    }

    mapLayer(layer: Layer, otherLayer?: Layer): void {
        if (!this.map) { return; }

        this.map.addSource(layer.dataSource.getDataSourceId(), layer.dataSource.buildMapboxSource());

        if (layer.dataSource.getGeometryType() === "MIXED") {
            this.mapLayerAsType("POLYGON", layer, otherLayer);
            this.mapLayerAsType("POINT", layer, otherLayer);
            this.mapLayerAsType("LINE", layer, otherLayer);
        } else {
            this.mapLayerAsType(layer.dataSource.getGeometryType(), layer, otherLayer);
        }

        // Label layer
        let labelConfig: any = {
            id: layer.oid + "-LABEL",
            source: layer.dataSource.getDataSourceId(),
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
        };

        if (layer.dataSource.configureMapboxLayer) {
            layer.dataSource.configureMapboxLayer(labelConfig);
        }

        this.map.addLayer(labelConfig, otherLayer ? otherLayer.oid + "-LABEL" : null);
    }

    mapLayerAsType(geometryType: string, layer: Layer, otherLayer?: Layer): void {
        let layerConfig: any;
        let otherLayerConfig: any;

        if (geometryType === "MULTIPOLYGON" || geometryType === "POLYGON") {
            // Polygon Layer
            layerConfig = {
                id: layer.oid + "-POLYGON",
                type: "fill",
                source: layer.dataSource.getDataSourceId(),
                paint: {
                    "fill-color": [
                        "case",
                        ["boolean", ["feature-state", "selected"], false],
                        SELECTED_COLOR,
                        layer.color
                    ],
                    "fill-outline-color": "black",
                    "fill-opacity": 0.7
                },
                filter: ["all",
                    ["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
                ]
            };
            otherLayerConfig = otherLayer ? otherLayer.oid + "-POLYGON" : null;
        } else if (geometryType === "POINT" || geometryType === "MULTIPOINT") {
            // Point layer
            layerConfig = {
                id: layer.oid + "-POINT",
                type: "circle",
                source: layer.dataSource.getDataSourceId(),
                paint: {
                    "circle-radius": 10,
                    "circle-color": [
                        "case",
                        ["boolean", ["feature-state", "selected"], false],
                        SELECTED_COLOR,
                        layer.color
                    ],
                    "circle-stroke-width": 2,
                    "circle-stroke-color": "#FFFFFF"
                },
                filter: ["all",
                    ["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
                ]
            };
            otherLayerConfig = otherLayer ? otherLayer.oid + "-POINT" : null;
        } else if (geometryType === "LINE" || geometryType === "MULTILINE") {
            layerConfig = {
                id: layer.oid + "-LINE",
                source: layer.dataSource.getDataSourceId(),
                type: "line",
                layout: {
                    "line-join": "round",
                    "line-cap": "round"
                },
                paint: {
                    "line-color": [
                        "case",
                        ["boolean", ["feature-state", "selected"], false],
                        SELECTED_COLOR,
                        layer.color
                    ],
                    "line-width": 3
                },
                filter: ["all",
                    ["match", ["geometry-type"], ["LineString", "MultiLineString"], true, false]
                ]
            };
            otherLayerConfig = otherLayer ? otherLayer.oid + "-LINE" : null;
        } else {
            // eslint-disable-next-line no-console
            console.log("Unexpected geometry type [" + geometryType + "]");
            return;
        }

        if (layer.dataSource.configureMapboxLayer) {
            layer.dataSource.configureMapboxLayer(layerConfig);
        }

        this.map.addLayer(layerConfig, otherLayerConfig);
    }

    getDrawGeometry(): any {
        if (this.editingControl != null) {
            let featureCollection: any = this.editingControl.getAll();

            if (featureCollection.features.length > 0) {
                // The first Feature is our GeoObject.

                // Any additional features were created using the draw editor. Combine them into the GeoObject if its a multi-polygon.
                if (this.geometryType === "MULTIPOLYGON") {
                    let polygons = [];

                    for (let i = 0; i < featureCollection.features.length; i++) {
                        let feature = featureCollection.features[i];

                        if (feature.geometry.type === "MultiPolygon") {
                            for (let j = 0; j < feature.geometry.coordinates.length; j++) {
                                polygons.push(feature.geometry.coordinates[j]);
                            }
                        } else {
                            polygons.push(feature.geometry.coordinates);
                        }
                    }

                    return {
                        coordinates: polygons,
                        type: "MultiPolygon"
                    };
                } else if (this.geometryType === "MULTIPOINT") {
                    let points = [];

                    for (let i = 0; i < featureCollection.features.length; i++) {
                        let feature = featureCollection.features[i];

                        if (feature.geometry.type === "MultiPoint") {
                            for (let j = 0; j < feature.geometry.coordinates.length; j++) {
                                points.push(feature.geometry.coordinates[j]);
                            }
                        } else {
                            points.push(feature.geometry.coordinates);
                        }
                    }

                    return {
                        coordinates: points,
                        type: "MultiPoint"
                    };
                } else if (this.geometryType === "MULTILINE") {
                    let lines = [];

                    for (let i = 0; i < featureCollection.features.length; i++) {
                        let feature = featureCollection.features[i];

                        if (feature.geometry.type === "MultiLineString") {
                            for (let j = 0; j < feature.geometry.coordinates.length; j++) {
                                lines.push(feature.geometry.coordinates[j]);
                            }
                        } else {
                            lines.push(feature.geometry.coordinates);
                        }
                    }

                    return {
                        coordinates: lines,
                        type: "MultiLineString"
                    };
                } else {
                    return featureCollection.features[0].geometry;
                }
            }
        }

        return null;
    }

    public static createEmptyGeometryValue(geometryType: String): any {
        let value = { type: geometryType, coordinates: [] };

        if (geometryType === "MULTIPOLYGON" || geometryType === "MIXED") {
            value.type = "MultiPolygon";
        } else if (geometryType === "POLYGON") {
            value.type = "Polygon";
        } else if (geometryType === "POINT") {
            value.type = "Point";
        } else if (geometryType === "MULTIPOINT") {
            value.type = "MultiPoint";
        } else if (geometryType === "LINE") {
            value.type = "Line";
        } else if (geometryType === "MULTILINE") {
            value.type = "MultiLine";
        }

        return value;
    }

    zoomToLayersExtent(): void {
        this.layers.forEach(layer => {
            if (layer instanceof GeoJsonLayer) {
                let geojson = (layer.dataSource as unknown as GeoJsonLayerDataSource).getLayerData();

                if (geojson != null) {
                    const geometryType = geojson.type != null ? geojson.type.toUpperCase() : this.geometryType;

                    if (geometryType === "MULTIPOINT" || geometryType === "POINT") {
                        let coords = geojson.coordinates;

                        if (coords) {
                            let bounds = new LngLatBounds();
                            coords.forEach(coord => {
                                bounds.extend(coord);
                            });

                            let center = bounds.getCenter();
                            let pt = new LngLat(center.lng, center.lat);

                            this.map.flyTo({
                                center: pt,
                                zoom: 9,
                                essential: true
                            });
                        }
                    } else if (geometryType === "MULTIPOLYGON" || geometryType === "POLYGON" || geometryType === "MIXED") {
                        let coords = geojson.coordinates;

                        if (coords) {
                            let bounds = new LngLatBounds();
                            coords.forEach(polys => {
                                polys.forEach(subpoly => {
                                    subpoly.forEach(coord => {
                                        bounds.extend(coord);
                                    });
                                });
                            });

                            this.map.fitBounds(bounds, {
                                padding: 20
                            });
                        }
                    } else if (geometryType === "LINE" || geometryType === "MULTILINE") {
                        let coords = geojson.coordinates;

                        if (coords) {
                            let bounds = new LngLatBounds();
                            coords.forEach(lines => {
                                lines.forEach(subline => {
                                    subline.forEach(coord => {
                                        bounds.extend(coord);
                                    });
                                });
                            });

                            this.map.fitBounds(bounds, {
                                padding: 20
                            });
                        }
                    }
                }
            }
        });
    }

}
