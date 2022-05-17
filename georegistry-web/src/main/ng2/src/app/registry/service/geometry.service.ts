
import { Injectable, Output, EventEmitter, OnDestroy } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";

import * as MapboxDraw from "@mapbox/mapbox-gl-draw";
import { Map, LngLat, LngLatBounds, AnySourceData } from "mapbox-gl";
import { Subscription } from "rxjs";

import { RelationshipVisualizationService } from "./relationship-visualization.service";
import { DataSourceFactory, GeoJsonLayer, GeoJsonLayerDataSource, Layer, LayerDataSource } from "./layer-data-source";
import { RegistryService } from "./registry.service";
import { MapService } from "./map.service";
import { ListTypeService } from "./list-type.service";

export const OLD_LAYER_COLOR = "#A4A4A4";

export const NEW_LAYER_COLOR = "#0062AA";

export const SELECTED_COLOR = "#800000";

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

    // Id of a datasource that we want to zoom to when it becomes ready
    _zoomOnReady: string[] = [];

    @Output() geometryChange = new EventEmitter<any>();

    @Output() layersChange: EventEmitter<Layer[]> = new EventEmitter();

    /*
    * Subscription for changes to the URL parameters
    */
    queryParamSubscription: Subscription;

    /*
     * URL pamaters
     */
    syncWithUrlParams: boolean = false;

    params: any = null;

    dataSourceFactory: DataSourceFactory;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private registryService: RegistryService,
        private relVizService: RelationshipVisualizationService,
        private mapService: MapService,
        private listService: ListTypeService
    ) {
        this.dataSourceFactory = new DataSourceFactory(this, this.registryService, this.relVizService, this.mapService, this.listService);
    }

    initialize(map: Map, geometryType: String, syncWithUrlParams: boolean) {
        this.syncWithUrlParams = syncWithUrlParams;
        this.map = map;
        this.geometryType = geometryType;
        // this.editingControl = null;

        if (syncWithUrlParams) {
            this.queryParamSubscription = this.route.queryParams.subscribe(params => {
                try {
                    this.handleParameterChange(params);
                } catch (err) {
                    // eslint-disable-next-line no-console
                    console.log(err); // We will be unsubscribed if we throw an unhandled error and we don't want that to happen
                }
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

        window.onbeforeunload = () => this.destroy();
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
                let deserializedLayers: any = JSON.parse(this.params.layers);

                let layers = this.dataSourceFactory.deserializeLayers(deserializedLayers);

                if (this.map) {
                    this.internalUpdateLayers(layers);
                }
            }
        }
    }

    private internalUpdateLayers(newLayers: Layer[]) {
        if (this.map) {
            // Calculate a diff
            let diffs: {type: string, index: number, moveTo?: number}[] = [];
            let iterations = newLayers.length > this.layers.length ? newLayers.length : this.layers.length;
            for (let i = 0; i < iterations; ++i) {
                if (i >= newLayers.length) {
                    let existingLayer = this.layers[i];

                    let existingLayerExistsElsewhere = newLayers.findIndex(findLayer => findLayer.getId() === existingLayer.getId());

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
                    let newLayer = newLayers[i];

                    let paramLayerExistsElsewhere = this.layers.findIndex(findLayer => findLayer.getId() === newLayer.getId());

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
                    let newLayer = newLayers[i];
                    let layer = this.layers[i];

                    if (newLayer.getId() !== layer.getId()) {
                        let paramLayerExistsElsewhere = this.layers.findIndex(findLayer => findLayer.getId() === newLayer.getId());

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
                    } else if (newLayer.rendered !== layer.rendered) {
                        diffs.push({
                            type: "RENDERED_CHANGE",
                            index: i
                        });
                    } else if (newLayer.color !== layer.color) {
                        diffs.push({
                            type: "COLOR_CHANGE",
                            index: i
                        });
                    }
                }
            }

            let fullRebuild = diffs.length > 0 || newLayers.length !== this.layers.length;

            if (diffs.length === 1 && (diffs[0].type === "RENDERED_CHANGE" || diffs[0].type === "COLOR_CHANGE")) {
                // They just toggled whether a layer was rendered or changed a layer color

                const diff = diffs[0];
                let newLayer = newLayers[diff.index];
                let oldLayer = this.layers[diff.index];
                let prevLayer = (diff.index === 0) ? null : this.layers[diff.index - 1];

                if (diff.type === "RENDERED_CHANGE") {
                    if (newLayer.rendered) {
                        this.mapLayer(newLayer, prevLayer);
                    } else {
                        this.unmapLayer(oldLayer);
                    }
                } else if (diff.type === "COLOR_CHANGE") {
                    this.unmapLayer(oldLayer);
                    this.mapLayer(newLayer, prevLayer);
                }

                fullRebuild = false;
            } else if (diffs.length === 1 && diffs[0].type === "NEW_LAYER" && diffs[0].index === this.layers.length && this.layers.length > 0) {
                // Added a layer at the end

                this.mapLayer(newLayers[newLayers.length - 1], this.layers.length > 0 ? this.layers[this.layers.length - 1] : null);
                fullRebuild = false;
            } else if (diffs.length > 0 && newLayers.length === this.layers.length && diffs.filter(diff => diff.type !== "LAYER_REORDER").length === 0) {
                // Layers changed order but are otherwise the same.

                this.layers = newLayers;
                for (let i = this.layers.length - 1; i > -1; i--) {
                    const layer = this.layers[i];

                    if (this.map.getLayer(layer.getId() + "-POLYGON")) {
                        this.map.moveLayer(layer.getId() + "-POLYGON");
                    }
                    if (this.map.getLayer(layer.getId() + "-POINT")) {
                        this.map.moveLayer(layer.getId() + "-POINT");
                    }
                    if (this.map.getLayer(layer.getId() + "-LINE")) {
                        this.map.moveLayer(layer.getId() + "-LINE");
                    }
                    if (this.map.getLayer(layer.getId() + "-LABEL")) {
                        this.map.moveLayer(layer.getId() + "-LABEL");
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

                this.layers = newLayers;
                this.mapAllLayers();
            } else {
                // Make sure attribute changes are reflected
                this.layers = newLayers;
            }
        } else {
            this.layers = newLayers;
        }

        this.layersChange.emit(this.layers);
    }

    /*
     * Notify the map that the datasets of a particular type have changed and that the data sources must be rebuilt.
     */
    refreshDatasets(type: string) {
        let otherLayer = null;
        this.getLayers().forEach(layer => {
            if (layer.dataSource.getDataSourceType() === type) {
                this.unmapLayer(layer);
                this.mapLayer(layer, otherLayer);
            }
            otherLayer = layer;
        });
    }

    public setLayers(newLayers: Layer[]) {
        if (this.syncWithUrlParams) {
            let serialized = this.dataSourceFactory.serializeLayers(newLayers);

            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { layers: JSON.stringify(serialized) },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            });
        } else {
            this.internalUpdateLayers(newLayers);
        }
    }

    public zoomOnReady(layerId: string) {
        if (this._zoomOnReady.indexOf(layerId) === -1) {
            this._zoomOnReady.push(layerId);
        }
    }

    public zoomToLayer(layer: Layer): Promise<void> {
        return layer.dataSource.getBounds(layer).then((bounds: LngLatBounds) => {
            if (bounds != null) {
                this.map.fitBounds(bounds, this.calculateZoomConfig(null));
            }
        });
    }

    private calculateZoomConfig(geometryType: string): any {
        let padding = 50;
        let maxZoom = 20;

        // Zoom level was requested to be reduced when displaying point types as per #420
        if (geometryType === "Point" || geometryType === "MultiPoint") {
            padding = 100;
            maxZoom = 12;
        }

        let config: any = { padding: padding, animate: true, maxZoom: maxZoom };

        return config;
    }

    public setGeometryType(geometryType: string) {
        this.geometryType = geometryType;
    }

    public getLayerFromMapboxLayer(mapboxLayer: any) {
        let id = mapboxLayer.id;

        if (id.endsWith("-POINT")) {
            id = id.substring(0, id.length - "-POINT".length);
        } else if (id.endsWith("-POLYGON")) {
            id = id.substring(0, id.length - "-POLYGON".length);
        } else if (id.endsWith("-LINE")) {
            id = id.substring(0, id.length - "-LINE".length);
        }

        let layers = this.getLayers().filter(l => l.getId() === id);

        if (layers.length > 0) {
            let layer: Layer = layers[0];

            return layer;
        }
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
        this.dataSourceFactory = new DataSourceFactory(this, this.registryService, this.relVizService, this.mapService, this.listService);
    }

    public getMap() {
        return this.map;
    }

    getDataSourceFactory() {
        return this.dataSourceFactory;
    }

    setDataSourceFactory(fac) {
        this.dataSourceFactory = fac;
    }

    registerDataSource(dataSource: LayerDataSource) {
        this.dataSourceFactory.registerDataSource(dataSource);
    }

    unregisterDataSource(dataSourceType: string) {
        this.dataSourceFactory.unregisterDataSource(dataSourceType);
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
                    id: this.editingLayer.getId(),
                    type: "Feature",
                    properties: {},
                    geometry: { type: "Point", coordinates: [long, lat] }
                }]
            });

            this.editingControl.changeMode("simple_select", { featureIds: this.editingLayer.getId() });

            this.saveEdits();

            /*
            this.editingLayer.value = {
              type: 'FeatureCollection',
              features: [{
              id: this.editingLayer.getId(),
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

            this.editingControl.changeMode( 'simple_select', { featureIds: this.editingLayer.getId() } );
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

    public addOrUpdateLayer(newLayer: Layer, orderingIndex?: number) {
        let newLayers = this.getLayers();

        let existingIndex = newLayers.findIndex((findLayer: Layer) => { return findLayer.getId() === newLayer.getId(); });

        if (existingIndex !== -1) {
            newLayers[existingIndex] = newLayer;
        } else {
            if (orderingIndex != null) {
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
        let newLayers = this.getLayers();

        let existingIndex = newLayers.findIndex((findLayer: Layer) => { return findLayer.getId() === oid; });

        if (existingIndex !== -1) {
            newLayers.splice(existingIndex, 1);

            this.setLayers(newLayers);
        } else {
            // eslint-disable-next-line no-console
            console.log("Could not remove layer with id " + oid + " because one does not exist.");
        }
    }

    public removeLayers(oids: string[]) {
        let newLayers = this.getLayers();

        newLayers = newLayers.filter(layer => oids.indexOf(layer.getId()) === -1);

        this.setLayers(newLayers);
    }

    getLayers(): Layer[] {
        return this.dataSourceFactory.deserializeLayers(this.dataSourceFactory.serializeLayers(this.layers));
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
            (this.editingLayer.dataSource as unknown as GeoJsonLayerDataSource).getLayerData().then(data => {
                if (data) {
                    this.editingControl.add(data);
                }
            });
        }
    }

    unmapLayer(layer: Layer): void {
        if (this.map) {
            const layerName = layer.getId();

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

            // If this source is used by other layers we don't want to remove the source
            let sourceHasOtherMappedLayers = this.layers.filter(l => layer.getId() !== l.getId() && l.dataSource.getId() === layer.dataSource.getId() && l.rendered).length > 0;

            if (!sourceHasOtherMappedLayers && this.map.getSource(layer.dataSource.getId()) != null) {
                this.map.removeSource(layer.dataSource.getId());
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

        let mapboxSource: AnySourceData = layer.dataSource.buildMapboxSource();

        if (this.map.getSource(layer.dataSource.getId()) == null) {
            this.map.addSource(layer.dataSource.getId(), mapboxSource);
        }

        // If the layer wants to load some data asynchronously
        if (layer.dataSource instanceof GeoJsonLayerDataSource) {
            layer.dataSource.getLayerData().then(geojson => {
                if (this.map.getSource(layer.dataSource.getId()) != null) {
                    (this.map.getSource(layer.dataSource.getId()) as any).setData(geojson);
                }

                if (this._zoomOnReady != null && this._zoomOnReady.length > 0 && this._zoomOnReady.indexOf(layer.getId()) !== -1) {
                    this.zoomToLayer(layer);
                    this._zoomOnReady.splice(this._zoomOnReady.indexOf(layer.getId()), 1);
                }
            });
        } else {
            if (this._zoomOnReady != null && this._zoomOnReady.length > 0 && this._zoomOnReady.indexOf(layer.getId()) !== -1) {
                this.zoomToLayer(layer);
                this._zoomOnReady.splice(this._zoomOnReady.indexOf(layer.getId()), 1);
            }
        }

        if (otherLayer && !otherLayer.rendered) {
            otherLayer = null;
        }

        if (layer.dataSource.getGeometryType() === "MIXED") {
            this.mapLayerAsType("POLYGON", layer, otherLayer);
            this.mapLayerAsType("POINT", layer, otherLayer);
            this.mapLayerAsType("LINE", layer, otherLayer);
        } else {
            this.mapLayerAsType(layer.dataSource.getGeometryType(), layer, otherLayer);
        }

        // Label layer
        let labelConfig: any = {
            id: layer.getId() + "-LABEL",
            source: layer.dataSource.getId(),
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
        };

        layer.configureMapboxLayer("LABEL", labelConfig);

        this.map.addLayer(labelConfig, otherLayer ? otherLayer.getId() + "-LABEL" : null);
    }

    mapLayerAsType(geometryType: string, layer: Layer, otherLayer?: Layer): void {
        let layerConfig: any;

        if (geometryType === "MULTIPOLYGON" || geometryType === "POLYGON") {
            // Polygon Layer
            layerConfig = {
                id: layer.getId() + "-" + this.getLayerIdGeomTypePostfix(geometryType),
                type: "fill",
                source: layer.dataSource.getId(),
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
        } else if (geometryType === "POINT" || geometryType === "MULTIPOINT") {
            // Point layer
            layerConfig = {
                id: layer.getId() + "-" + this.getLayerIdGeomTypePostfix(geometryType),
                type: "circle",
                source: layer.dataSource.getId(),
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
        } else if (geometryType === "LINE" || geometryType === "MULTILINE") {
            layerConfig = {
                id: layer.getId() + "-" + this.getLayerIdGeomTypePostfix(geometryType),
                source: layer.dataSource.getId(),
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
        } else {
            // eslint-disable-next-line no-console
            console.log("Unexpected geometry type [" + geometryType + "]");
            return;
        }

        layer.configureMapboxLayer(geometryType, layerConfig);

        this.map.addLayer(layerConfig, otherLayer ? otherLayer.getId() + "-" + this.getLayerIdGeomTypePostfix(otherLayer.dataSource.getGeometryType()) : null);
    }

    private getLayerIdGeomTypePostfix(geometryType: string) {
        if (geometryType === "MULTIPOLYGON" || geometryType === "POLYGON") {
            return "POLYGON";
        } else if (geometryType === "POINT" || geometryType === "MULTIPOINT") {
            return "POINT";
        } else if (geometryType === "LINE" || geometryType === "MULTILINE") {
            return "LINE";
        } else {
            return "POLYGON";
        }
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

        let upperType = geometryType.toUpperCase();

        if (upperType === "MULTIPOLYGON" || upperType === "MIXED") {
            value.type = "MultiPolygon";
        } else if (upperType === "POLYGON") {
            value.type = "Polygon";
        } else if (upperType === "POINT") {
            value.type = "Point";
        } else if (upperType === "MULTIPOINT") {
            value.type = "MultiPoint";
        } else if (upperType === "LINE") {
            value.type = "Line";
        } else if (upperType === "MULTILINE") {
            value.type = "MultiLine";
        }

        return value;
    }

    zoomToLayersExtent(): void {
        let layers = this.getLayers();
        let geoJsonLayer: GeoJsonLayer = null;

        layers.forEach(layer => {
            if (layer instanceof GeoJsonLayer && layer.rendered) {
                geoJsonLayer = layer as GeoJsonLayer;
            }
        });

        if (geoJsonLayer != null) {
            (geoJsonLayer.dataSource as unknown as GeoJsonLayerDataSource).getLayerData().then((geojson: any) => {
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
            });
        }
    }

}
