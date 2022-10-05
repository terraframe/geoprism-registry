
import { Injectable, Output, EventEmitter, OnDestroy } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";

import * as MapboxDraw from "@mapbox/mapbox-gl-draw";
import { Map, LngLat, LngLatBounds, AnySourceData, LngLatBoundsLike } from "mapbox-gl";
import { Subscription } from "rxjs";

import { RelationshipVisualizationService } from "./relationship-visualization.service";
import { DataSourceFactory, GeoJsonLayer, GeoJsonLayerDataSource, Layer, LayerDataSource } from "./layer-data-source";
import { RegistryService } from "./registry.service";
import { MapService } from "./map.service";
import { ListTypeService } from "./list-type.service";
import { LayerGroupSorter, LayerSorter } from "@registry/component/location-manager/layer-group";
import { LocalizationService } from "@shared/service/localization.service";
import { LayerDiffingStrategy } from "./layer-diffing-strategy";
import { LocationManagerState } from "@registry/component/location-manager/location-manager.component";
import { PANEL_SIZE_STATE } from "@registry/model/location-manager";
import { debounce } from "ts-debounce";

export const OLD_LAYER_COLOR = "#A4A4A4";

export const NEW_LAYER_COLOR = "#0062AA";

export const SELECTED_COLOR = "#800000";

/**
 * This service provides a global abstraction for mapping and editing layers across many different components (simultaneously) and
 * serializing / deserializing these layers to / from the url parameters to facilitate saving + loading of layer state.
 *
 * Layers contain references to data sources, which are invoked when deserializing from the url param to facilitate
 * population of layer data.
 */
@Injectable()
export class GeometryService implements OnDestroy {

    map: Map;

    layers: Layer[] = [];

    currentMapState: Layer[] = [];

    geometryType: String;

    readOnly: boolean;

    editingControl: any = null;

    simpleEditControl: any = null;

    editingLayer: GeoJsonLayer;

    // Id of a datasource that we want to zoom to when it becomes ready
    _zoomOnReady: string[] = [];

    isZooming: boolean = false;

    @Output() geometryChange = new EventEmitter<any>();

    @Output() layersChange: EventEmitter<Layer[]> = new EventEmitter();

    /*
    * Subscription for changes to the url parameters
    */
    private urlSub: Subscription;

    /*
     * URL pamaters
     */
    syncWithUrlParams: boolean = false;

    state: LocationManagerState = { attrPanelOpen: true };

    dataSourceFactory: DataSourceFactory;

    layerSorter: LayerSorter;

    public syncMapState: () => void;

    public stateChange$: EventEmitter<LocationManagerState>;

    setState: (state: any, pushBackHistory: boolean) => void;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private registryService: RegistryService,
        private relVizService: RelationshipVisualizationService,
        private mapService: MapService,
        private listService: ListTypeService,
        private localService: LocalizationService
    ) {
        this.dataSourceFactory = new DataSourceFactory(this, this.registryService, this.relVizService, this.mapService, this.listService);
        this.layerSorter = new LayerGroupSorter(this.localService);
        this.syncMapState = debounce(this._syncMapState, 50);

        this.stateChange$ = new EventEmitter();
        this.setState = debounce(this._setState, 50);
    }

    public initialize(map: Map, geometryType: String, syncWithUrlParams: boolean) {
        this.syncWithUrlParams = syncWithUrlParams;
        this.map = map;
        this.geometryType = geometryType;
        // this.editingControl = null;

        if (syncWithUrlParams) {
            this.urlSub = this.route.queryParams.subscribe(urlParams => {
                try {
                    let newState = JSON.parse(JSON.stringify(urlParams));

                    newState.graphPanelOpen = (newState.graphPanelOpen === "true");
                    newState.attrPanelOpen = (newState.attrPanelOpen === "true" || newState.attrPanelOpen === undefined);

                    this.stateChange(newState);
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
        this.map.on("zoomstart", () => {
            this.isZooming = true;
        });
        this.map.on("zoomend", () => {
            this.isZooming = false;
        });

        window.onbeforeunload = () => this.destroy();

        this.syncMapState();
    }

    ngOnDestroy(): void {
        if (this.urlSub) {
            this.urlSub.unsubscribe();
        }
    }

    public serializeLayers(newLayers: Layer[]) {
        let sorted;

        if (this.layerSorter != null) {
            sorted = this.layerSorter.sortLayers(newLayers);
        } else {
            sorted = newLayers;
        }

        let serialized = this.dataSourceFactory.serializeLayers(sorted);

        return JSON.stringify(serialized);
    }

    public deserializeLayers(layerState: string): Layer[] {
        return this.dataSourceFactory.deserializeLayers(JSON.parse(layerState));
    }

    public getState(): LocationManagerState {
        return this.state;
    }

    public _setState(state: LocationManagerState, pushBackHistory: boolean): void {
        Object.assign(this.state, state);

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: JSON.parse(JSON.stringify(this.state)),
            queryParamsHandling: "merge",
            replaceUrl: !pushBackHistory
        });
    }

    stateChange(state: LocationManagerState): void {
        this.state = state;

        if (this.state != null) {
            if (this.state.layers != null) {
                let deserializedLayers: any = JSON.parse(this.state.layers);

                let oldLayers = this.layers;

                let deserialized = this.dataSourceFactory.deserializeLayers(deserializedLayers);

                if (this.layerSorter != null) {
                    this.layers = this.layerSorter.sortLayers(deserialized);
                } else {
                    this.layers = deserialized;
                }

                if (new LayerDiffingStrategy(this.layers, oldLayers).getDiffs().length > 0) {
                    this.layersChange.emit(this.getLayers());
                }

                this.syncMapState();
            }

            this.stateChange$.emit(JSON.parse(JSON.stringify(this.state)));
        }
    }

    private _syncMapState() {
        if (this.map) {
            let strategy = new LayerDiffingStrategy(this.layers, this.currentMapState);

            let diffs = strategy.getDiffs();

            let fullRebuild = diffs.length > 0 || this.layers.length !== this.currentMapState.length;

            if (diffs.length === 1 && (diffs[0].type === "RENDERED_CHANGE" || diffs[0].type === "COLOR_CHANGE")) {
                // They just toggled whether a layer was rendered or changed a layer color

                const diff = diffs[0];

                let prevLayer = null;
                if (diff.oldLayerIndex > 0) {
                    for (let i = 0; i < diff.oldLayerIndex; ++i) {
                        prevLayer = this.currentMapState[i];
                    }
                }

                if (diff.type === "RENDERED_CHANGE") {
                    if (diff.newLayer.rendered) {
                        this.mapboxShowLayer(diff.newLayer);
                    } else {
                        this.mapboxHideLayer(diff.oldLayer);
                    }
                } else if (diff.type === "COLOR_CHANGE") {
                    this.mapboxUnmapLayer(diff.oldLayer);
                    this.mapboxMapLayer(diff.newLayer, prevLayer);
                }

                fullRebuild = false;
            } else if (diffs.filter(diff => diff.type === "NEW_LAYER").length === 1 && diffs.filter(diff => diff.type !== "NEW_LAYER" && diff.type !== "LAYER_REORDER").length === 0 && this.layers.length === this.currentMapState.length + 1) {
                // Added a layer
                const diff = diffs.filter(diff => diff.type === "NEW_LAYER")[0];

                let prevLayer = null;
                if (diff.newLayerIndex > 0) {
                    for (let i = 0; i < diff.newLayerIndex; ++i) {
                        prevLayer = this.currentMapState[i];
                    }
                }

                this.mapboxMapLayer(this.layers[diff.newLayerIndex], prevLayer);
                fullRebuild = false;
            } else if (diffs.filter(diff => diff.type === "REMOVE_LAYER").length === 1 && diffs.filter(diff => diff.type !== "REMOVE_LAYER" && diff.type !== "LAYER_REORDER").length === 0 && this.layers.length === this.currentMapState.length - 1) {
                // Removed a layer
                const diff = diffs.filter(diff => diff.type === "REMOVE_LAYER")[0];

                this.mapboxUnmapLayer(diff.oldLayer);
                fullRebuild = false;
            } else if (diffs.length > 0 && this.layers.length === this.currentMapState.length && diffs.filter(diff => diff.type !== "LAYER_REORDER").length === 0) {
                // Layers changed order but are otherwise the same.

                this.currentMapState = this.layers;
                for (let i = this.currentMapState.length - 1; i > -1; i--) {
                    const layer = this.currentMapState[i];

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
            } else if (diffs.length === 2 && diffs.filter(diff => diff.type === "REMOVE_LAYER").length === 1 && diffs.filter(diff => diff.type === "NEW_LAYER").length === 1 && this.layers.length === this.currentMapState.length && diffs.filter(diff => diff.type !== "REMOVE_LAYER" && diff.type !== "NEW_LAYER").length === 0) {
                // Added a layer and removed a layer
                const newLayerDiff = diffs.filter(diff => diff.type === "NEW_LAYER")[0];
                const removeLayerDiff = diffs.filter(diff => diff.type === "REMOVE_LAYER")[0];

                if (removeLayerDiff.oldLayer != null && newLayerDiff.newLayer != null) {
                    this.mapboxUnmapLayer(removeLayerDiff.oldLayer);

                    let prevLayer = null;
                    if (newLayerDiff.newLayerIndex > 0) {
                        for (let i = 0; i < newLayerDiff.newLayerIndex; ++i) {
                            prevLayer = this.currentMapState[i];
                        }
                    }

                    this.mapboxMapLayer(newLayerDiff.newLayer, prevLayer);

                    fullRebuild = false;
                }
            }

            if (fullRebuild) {
                this.unmapAllLayers();

                this.currentMapState = this.layers;
                this.mapAllLayers();
            } else {
                // Make sure attribute changes are reflected
                this.currentMapState = this.layers;
            }

            // Zoom to layers
            if (this._zoomOnReady != null && this._zoomOnReady.length > 0 && !this.isZooming) {
                for (let i = 0; i < this._zoomOnReady.length; ++i) {
                    let layerId = this._zoomOnReady[i];

                    let layerIndex = this.layers.findIndex(l => l.getId() === layerId);

                    if (layerIndex !== -1) {
                        let layer = this.layers[layerIndex];

                        this.zoomToLayer(layer);
                        this._zoomOnReady.splice(this._zoomOnReady.indexOf(layer.getId()), 1);

                        break;
                    }
                }
            }
        }
    }

    public dumpLayers(): void {
        this.layers = [];
        this.currentMapState = [];
    }

    public isMapZooming(): boolean {
        return this.isZooming;
    }

    /*
     * Notify the map that the datasets of a particular type have changed and that the data sources must be rebuilt.
     */
    public refreshDatasets(type: string) {
        let otherLayer = null;
        this.getLayers().forEach(layer => {
            if (layer.dataSource.getDataSourceType() === type) {
                this.mapboxUnmapLayer(layer);
                this.mapboxMapLayer(layer, otherLayer);
            }
            otherLayer = layer;
        });
    }

    public setLayers(newLayers: Layer[]) {
        if (this.layerSorter != null) {
            this.layers = this.layerSorter.sortLayers(newLayers);
        } else {
            this.layers = newLayers;
        }

        if (this.syncWithUrlParams) {
            let serialized = this.dataSourceFactory.serializeLayers(newLayers);

            this.setState({ layers: JSON.stringify(serialized) }, false);
        } else {
            this.syncMapState();
        }

        this.layersChange.emit(this.getLayers());
    }

    public zoomOnReady(layerId: string) {
        if (this._zoomOnReady.indexOf(layerId) === -1) {
            this._zoomOnReady.push(layerId);
        }
    }

    public zoomToLayer(layer: Layer): Promise<void> {
        return layer.dataSource.getBounds(layer).then((bounds: LngLatBoundsLike) => {
            if (bounds != null) {
                let zoomConfig = this.calculateZoomConfig(null);

                this.isZooming = true;
                this.map.fitBounds(bounds, zoomConfig);

                // If they switch pages before the zoom finishes isZooming can remain set. So set a max timeout
                window.setTimeout(() => {
                    this.isZooming = false;
                }, 6000);
            }
        });
    }

    private calculateZoomConfig(geometryType: string): any {
        let config: any = { padding: { top: 10, bottom: 10, left: 10, right: 10 }, animate: true, maxDuration: 5000, maxZoom: 20 };

        // Zoom level was requested to be reduced when displaying point types as per #420
        if (geometryType === "Point" || geometryType === "MultiPoint") {
            config.padding = { top: 50, bottom: 50, left: 50, right: 50 };
            config.maxZoom = 12;
        }

        if (this.state.graphPanelOpen && !this.state.attrPanelOpen) {
            // If graph panel is open, but not attribute panel (takes up half of the left screen)
            config.padding.left += Math.round(window.innerWidth / 2);
        } else if (this.state.attrPanelOpen && (this.state.text != null) && !this.state.graphPanelOpen) {
            // If attribute panel is open, but not the graph panel (takes up half of the left screen)
            config.padding.left += Math.round(window.innerWidth / 3);
        }

        if (this.state.layersPanelSize != null) {
            let layerPanelSize = Number.parseInt(this.state.layersPanelSize);

            if (layerPanelSize === PANEL_SIZE_STATE.WINDOWED || PANEL_SIZE_STATE.FULLSCREEN) {
                config.padding.right += 50;

                /*
                config.padding.top += 37 * this.layers.length;
                config.padding.top += layerPanelSize === PANEL_SIZE_STATE.FULLSCREEN ? 50 : 10;
                */
            }
        }

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
        } else if (id.endsWith("-LABEL")) {
            id = id.substring(0, id.length - "-LABEL".length);
        }

        let layers = this.getLayers().filter(l => l.getId() === id);

        if (layers.length > 0) {
            let layer: Layer = layers[0];

            return layer;
        }
    }

    public destroy(destroyMap: boolean = true): void {
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
            });
        }

        this.editingLayer = null;
        this.layers = [];
        this.currentMapState = [];
        this.dataSourceFactory = new DataSourceFactory(this, this.registryService, this.relVizService, this.mapService, this.listService);
    }

    public getMap() {
        return this.map;
    }

    public getDataSourceFactory() {
        return this.dataSourceFactory;
    }

    public setDataSourceFactory(fac) {
        this.dataSourceFactory = fac;
    }

    public registerDataSource(dataSource: LayerDataSource) {
        this.dataSourceFactory.registerDataSource(dataSource);
    }

    public unregisterDataSource(dataSourceType: string) {
        this.dataSourceFactory.unregisterDataSource(dataSourceType);
    }

    public startEditing(layer: GeoJsonLayer) {
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

    public stopEditing(rerender: boolean = true) {
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

    public saveEdits(rerender: boolean = true): void {
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

    public getLayers(): Layer[] {
        return this.dataSourceFactory.deserializeLayers(this.dataSourceFactory.serializeLayers(this.layers));
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

    private mapboxUnmapLayer(layer: Layer): void {
        if (this.map) {
            this.getMapboxLayerIdsForLayer(layer).forEach(id => {
                if (this.map.getLayer(id) != null) {
                    this.map.removeLayer(id);
                } else {
                    // eslint-disable-next-line no-console
                    console.log("Attempted to remove a layer [" + id + "] which does not exist.");
                }
            });

            // If this source is used by other layers we don't want to remove the source
            let sourceHasOtherMappedLayers = this.currentMapState.filter(l => layer.getId() !== l.getId() && l.dataSource.getId() === layer.dataSource.getId()).length > 0;

            if (!sourceHasOtherMappedLayers && this.map.getSource(layer.dataSource.getId()) != null) {
                this.map.removeSource(layer.dataSource.getId());
            }
        }
    }

    private unmapAllLayers(): void {
        if (this.currentMapState != null && this.currentMapState.length > 0) {
            let len = this.currentMapState.length;

            for (let i = 0; i < len; ++i) {
                let layer = this.currentMapState[i];
                this.mapboxUnmapLayer(layer);
            }
        }
    }

    private mapAllLayers(): void {
        if (this.currentMapState != null && this.currentMapState.length > 0) {
            let prevLayer = null;
            let len = this.currentMapState.length;
            for (let i = 0; i < len; ++i) {
                let layer = this.currentMapState[i];

                this.mapboxMapLayer(layer, prevLayer);
                prevLayer = layer;
            }
        }
    }

    private mapboxHideLayer(layer: Layer): void {
        if (!this.map) { return; }

        this.getMapboxLayerIdsForLayer(layer).forEach(id => {
            if (this.map.getLayer(id) != null) {
                this.map.setLayoutProperty(id, "visibility", "none");
            } else {
                // eslint-disable-next-line no-console
                console.log("Attempted to hide a layer [" + id + "] which does not exist.");
            }
        });
    }

    private mapboxShowLayer(layer: Layer): void {
        if (!this.map) { return; }

        this.getMapboxLayerIdsForLayer(layer).forEach(id => {
            if (this.map.getLayer(id) != null) {
                this.map.setLayoutProperty(id, "visibility", "visible");
            } else {
                // eslint-disable-next-line no-console
                console.log("Attempted to show a layer [" + id + "] which does not exist.");
            }
        });
    }

    private mapboxMapLayer(layer: Layer, otherLayer?: Layer): void {
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
            });
        }

        if (layer.dataSource.getGeometryType() === "MIXED") {
            this.mapboxMapLayerAsType("POLYGON", layer, otherLayer);
            this.mapboxMapLayerAsType("POINT", layer, otherLayer);
            this.mapboxMapLayerAsType("LINE", layer, otherLayer);
        } else {
            this.mapboxMapLayerAsType(layer.dataSource.getGeometryType(), layer, otherLayer);
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

        if (!layer.rendered) {
            labelConfig.layout.visibility = "none";
        }

        layer.configureMapboxLayer("LABEL", labelConfig);

        this.map.addLayer(labelConfig, otherLayer ? otherLayer.getId() + "-LABEL" : null);
    }

    private mapboxMapLayerAsType(geometryType: string, layer: Layer, otherLayer?: Layer): void {
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

        if (!layer.rendered) {
            layerConfig.layout = (layerConfig.layout == null) ? {} : layerConfig.layout;
            layerConfig.layout.visibility = "none";
        }

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

    private getMapboxLayerIdsForLayer(layer: Layer): string[] {
        let ids = [];

        if (layer.dataSource.getGeometryType() === "MIXED") {
            ids = ["POLYGON", "POINT", "LINE", "LABEL"];
        } else {
            ids = [this.getLayerIdGeomTypePostfix(layer.dataSource.getGeometryType()), "LABEL"];
        }

        return ids.map(id => layer.getId() + "-" + id);
    }

    public getDrawGeometry(): any {
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

    public zoomToLayersExtent(): void {
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
