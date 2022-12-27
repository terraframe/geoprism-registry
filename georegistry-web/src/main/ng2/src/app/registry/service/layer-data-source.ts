import { AnySourceData, LngLatBounds, LngLatBoundsLike } from "mapbox-gl";
import { ListTypeService } from "./list-type.service";
import { RegistryService } from "./registry.service";

import { HttpParams } from "@angular/common/http";
import { v4 as uuid } from "uuid";
import bbox from "@turf/bbox";
import { GeoJSON } from "geojson";
import { RelationshipVisualizationService } from "./relationship-visualization.service";
import { GeometryService } from "./geometry.service";
import { ValueOverTimeCREditor } from "@registry/component/geoobject-shared-attribute-editor/ValueOverTimeCREditor";
import { MapService } from "./map.service";

import { GeoRegistryConfiguration } from "@core/model/core";
import { ObjectReference } from "@registry/model/graph";
import { environment } from 'src/environments/environment';

export abstract class LayerDataSource {

    private dataSourceType: string;

    private id: string;

    constructor(dataSourceType: string) {
        this.dataSourceType = dataSourceType;
        this.id = uuid();
    }

    public getDataSourceType(): string {
        return this.dataSourceType;
    }

    createLayer(legendLabel: string, rendered: boolean, color: string): Layer {
        return new Layer(this, legendLabel, rendered, color);
    }

    public fromJSON(obj: any) {
        Object.assign(this, obj);
    }

    public toJSON(): any {
        return {
            dataSourceType: this.dataSourceType,
            id: this.id
        };
    }

    public getId(): string {
        return this.id;
    }

    public abstract getKey(): string;

    public abstract buildMapboxSource(): AnySourceData;

    public abstract getGeometryType(): string;

    public abstract getBounds(layer: Layer): Promise<LngLatBoundsLike>;

}

export abstract class GeoJsonLayerDataSource extends LayerDataSource {

    public abstract getLayerData(): Promise<GeoJSON.GeoJSON>;
    public abstract setLayerData(data: GeoJSON.GeoJSON): void;

    public buildMapboxSource(): AnySourceData {
        return {
            type: "geojson",
            data: GeometryService.createEmptyGeometryValue(this.getGeometryType())
        };
    }

    getBounds(layer: Layer): Promise<LngLatBoundsLike> {
        return this.getLayerData().then(data => {
            try {
                return bbox(data as any) as LngLatBoundsLike;
            // eslint-disable-next-line no-console
            } catch (e) { console.log(e); }

            return null;
        });
    }

}

export class Layer {

    legendLabel: string;
    dataSource: LayerDataSource;
    rendered: boolean;
    color: string;
    pinned: boolean;

    constructor(dataSource?: LayerDataSource, legendLabel?: string, rendered?: boolean, color?: string) {
        this.dataSource = dataSource;
        this.legendLabel = legendLabel;
        this.rendered = rendered;
        this.color = color;
        this.pinned = false;
    }

    public fromJSON(obj: any) {
        Object.assign(this, obj);
    }

    public toJSON(): any {
        return {
            legendLabel: this.legendLabel,
            rendered: this.rendered,
            color: this.color,
            pinned: this.pinned
        };
    }

    public getId(): string {
        return this.dataSource.getId();
    }

    public getKey(): string {
        return this.dataSource.getKey();
    }

    public getPinned(): boolean {
        return this.pinned;
    }

    public setPinned(pinned: boolean) {
        this.pinned = pinned;
    }

    public configureMapboxLayer(layerType: string, layerConfig: any): void {

    }

}

export class GeoJsonLayer extends Layer {

    constructor(dataSource?: LayerDataSource, legendLabel?: string, rendered?: boolean, color?: string) {
        super(dataSource, legendLabel, rendered, color);
        this.editing = false;
    }

    editing: boolean;

}

export const GEO_OBJECT_DATA_SOURCE_TYPE: string = "GEOOBJ";

export class GeoObjectLayerDataSource extends LayerDataSource {

    private registryService: RegistryService;

    private code: string;

    private typeCode: string;

    private date: string;

    constructor(registryService: RegistryService, code?: string, typeCode?: string, date?: string) {
        super(GEO_OBJECT_DATA_SOURCE_TYPE);
        this.registryService = registryService;
        this.code = code;
        this.typeCode = typeCode;
        this.date = date;
    }

    public toJSON(): any {
        return Object.assign(super.toJSON(), {
            code: this.code,
            typeCode: this.typeCode,
            date: this.date
        });
    }

    getCode(): string {
        return this.code;
    }

    setCode(code: string): void {
        this.code = code;
    }

    getTypeCode(): string {
        return this.typeCode;
    }

    setTypeCode(typeCode: string): void {
        this.typeCode = typeCode;
    }

    getDate(): string {
        return this.date;
    }

    setDate(date: string): void {
        this.date = date;
    }

    getDataSourceType(): string {
        return GEO_OBJECT_DATA_SOURCE_TYPE;
    }

    getKey(): string {
        return this.getDataSourceType() + this.getCode() + this.getTypeCode() + (this.getDate() == null ? "" : this.getDate());
    }

    getGeometryType(): string {
        return "MIXED";
    }

    buildMapboxSource(): AnySourceData {
        let params: HttpParams = new HttpParams();
        params = params.set("code", this.code);
        params = params.set("typeCode", this.typeCode);

        if (this.date != null) {
            params = params.set("date", this.date);
        }

        let url = environment.apiUrl + "/api/geoobject/get-code" + "?" + params.toString();

        return {
            type: "geojson",
            data: url
        };
    }

    getBounds(layer: Layer): Promise<LngLatBoundsLike> {
        return this.registryService.getGeoObjectBoundsAtDate(this.code, this.typeCode, this.date).then((bounds: number[]) => {
            if (bounds && Array.isArray(bounds)) {
                return new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
            } else {
                return null;
            }
        });
    }

}

export const LIST_VECTOR_SOURCE_TYPE = "LISTVECT";

export class ListVectorLayerDataSource extends LayerDataSource {

    private listService: ListTypeService;

    private versionId: string;

    constructor(listService: ListTypeService, versionId?: string) {
        super(LIST_VECTOR_SOURCE_TYPE);
        this.versionId = versionId;
        this.listService = listService;
    }

    public toJSON(): any {
        return Object.assign(super.toJSON(), {
            versionId: this.versionId
        });
    }

    getVersionId(): string {
        return this.versionId;
    }

    getKey(): string {
        return this.getDataSourceType() + this.getVersionId();
    }

    createLayer(legendLabel: string, rendered: boolean, color: string): Layer {
        return new ListVectorLayer(this, legendLabel, rendered, color);
    }

    buildMapboxSource(): AnySourceData {
        let protocol = window.location.protocol;
        let host = window.location.host;

        return {
            type: "vector",
            tiles: [protocol + "//" + host + environment.apiUrl + "/api/list-type/tile?x={x}&y={y}&z={z}&config=" + encodeURIComponent(JSON.stringify({ oid: this.versionId }))],
            promoteId: "uid"
        };
    }

    getGeometryType(): string {
        return "MIXED";
    }

    getBounds(layer: Layer): Promise<LngLatBounds> {
        let objectFilter = null;
        if (layer instanceof ListVectorLayer) {
            objectFilter = (layer as ListVectorLayer).getObjectFilter();
        }

        return this.listService.getBounds(this.versionId, objectFilter).then((bounds: number[]) => {
            if (bounds && Array.isArray(bounds)) {
                return new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
            } else {
                return null;
            }
        });
    }

}

export class ListVectorLayer extends Layer {

    objectFilter: string;

    configureMapboxLayer(layerType: string, layerConfig: any): void {
        layerConfig["source-layer"] = "context";

        if (this.objectFilter != null) {
            let filter = ["match", ["get", "uid"], this.objectFilter, true, false];

            if (layerConfig["filter"] != null) {
                layerConfig["filter"].push(filter);
            } else {
                layerConfig["filter"] = filter;
            }
        }

        if (layerType === "LABEL") {
            layerConfig.layout["text-field"] = ["case",
                ["has", "displayLabel_" + navigator.language.toLowerCase()],
                ["coalesce", ["get", "displayLabel_" + navigator.language.toLowerCase()], ["get", "displayLabel"], ["get", "code"]],
                ["coalesce", ["get", "displayLabel"], ["string", ["get", "code"]]
                ]];
        }
    }

    public toJSON(): any {
        return Object.assign(super.toJSON(), {
            objectFilter: this.objectFilter
        });
    }

    getId(): string {
        return (this.objectFilter == null) ? this.dataSource.getId() : this.objectFilter + this.dataSource.getId();
    }

    public getKey(): string {
        return (this.objectFilter == null) ? this.dataSource.getKey() : this.objectFilter + this.dataSource.getKey();
    }

    setObjectFilter(objectFilter: string) {
        this.objectFilter = objectFilter;
    }

    getObjectFilter(): string {
        return this.objectFilter;
    }

}

export const CHANGE_REQUEST_SOURCE_TYPE_NEW = "CRNEW";

export const CHANGE_REQUEST_SOURCE_TYPE_OLD = "CROLD";

export class ValueOverTimeDataSource extends GeoJsonLayerDataSource {

    votEditor: ValueOverTimeCREditor;

    constructor(newOrOld: "NEW" | "OLD", votEditor: ValueOverTimeCREditor) {
        super(newOrOld === "NEW" ? CHANGE_REQUEST_SOURCE_TYPE_NEW : CHANGE_REQUEST_SOURCE_TYPE_OLD);
        this.votEditor = votEditor;
    }

    setLayerData(data: any): void {
        if (this.getDataSourceType() === CHANGE_REQUEST_SOURCE_TYPE_NEW) {
            this.votEditor.value = data;
        } else {
            // eslint-disable-next-line no-console
            console.log("ERROR. Cannot edit old geometry");
        }
    }

    getLayerData(): Promise<GeoJSON.GeoJSON> {
        return new Promise((resolve, reject) => {
            if (this.getDataSourceType() === CHANGE_REQUEST_SOURCE_TYPE_NEW) {
                resolve(this.votEditor.value);
            } else {
                resolve(this.votEditor.oldValue);
            }
        });
    }

    getGeometryType(): string {
        return this.votEditor.changeRequestAttributeEditor.changeRequestEditor.geoObjectType.geometryType;
    }

    getKey(): string {
        return this.getDataSourceType() + this.votEditor.oid;
    }

    createLayer(legendLabel: string, rendered: boolean, color: string): Layer {
        return new GeoJsonLayer(this, legendLabel, rendered, color);
    }

    getBounds(layer: Layer): Promise<LngLatBoundsLike> {
        return this.getLayerData().then(data => {
            return bbox(data as any) as LngLatBoundsLike;
        });
    }

}

export const SEARCH_DATASOURCE_TYPE = "SEARCH";

export class SearchLayerDataSource extends GeoJsonLayerDataSource {

    private mapService: MapService;

    private text: string;

    private date: string;

    private geojson: GeoJSON.GeoJSON;

    constructor(mapService: MapService, text?: string, date?: string) {
        super(SEARCH_DATASOURCE_TYPE);
        this.mapService = mapService;
        this.text = text;
        this.date = date;
    }

    public toJSON(): any {
        return Object.assign(super.toJSON(), {
            text: this.text,
            date: this.date
        });
    }

    public getText(): string {
        return this.text;
    }

    public getDate(): string {
        return this.date;
    }

    public setLayerData(data: GeoJSON.GeoJSON): void {
        throw new Error("Method not implemented.");
    }

    public getLayerData(): Promise<GeoJSON.GeoJSON> {
        if (this.geojson != null) {
            return new Promise((resolve, reject) => {
                resolve(this.geojson);
            });
        } else {
            return this.mapService.search(this.text, this.date, false).then(data => {
                this.geojson = data as any;
                return data as GeoJSON.GeoJSON;
            });
        }
    }

    getGeometryType(): string {
        return "MIXED";
    }

    getKey(): string {
        return SEARCH_DATASOURCE_TYPE + this.text + (this.date == null ? "" : this.date);
    }

}

export const RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE = "RELVIZ";

export class RelationshipVisualizionDataSource extends GeoJsonLayerDataSource {

    relationshipType: string;
    relationshipCode: string;
    sourceObject: ObjectReference;
    bounds: string;
    date: string;

    data: any;
    dataPromise: any;

    // eslint-disable-next-line no-use-before-define
    vizService: RelationshipVisualizationService;
    geomService: GeometryService;

    constructor(vizService: RelationshipVisualizationService, geomService: GeometryService, relationshipType?: string, relationshipCode?: string, sourceObject?: ObjectReference, bounds?: string, date?: string) {
        super(RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE);
        this.vizService = vizService;
        this.geomService = geomService;
        this.relationshipType = relationshipType;
        this.relationshipCode = relationshipCode;
        this.sourceObject = sourceObject;
        this.bounds = bounds;
        this.date = date;
    }

    public toJSON(): any {
        return Object.assign(super.toJSON(), {
            relationshipType: this.relationshipType,
            relationshipCode: this.relationshipCode,
            sourceObject: this.sourceObject,
            bounds: this.bounds,
            date: this.date
        });
    }

    getKey(): string {
        return RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE + this.relationshipCode + this.sourceObject.code + this.sourceObject.typeCode + this.bounds + ((this.date == null) ? "" : this.date);
    }

    createLayer(legendLabel: string, rendered: boolean, color: string): Layer {
        return new RelationshipVisualizionLayer(this, legendLabel, rendered, color);
    }

    getRelationshipType(): string {
        return this.relationshipType;
    }

    getRelationshipCode(): string {
        return this.relationshipCode;
    }

    getSourceObject(): ObjectReference {
        return this.sourceObject;
    }

    hasSameSourceObject(sourceObj: ObjectReference): boolean {
        return this.sourceObject.code === sourceObj.code && this.sourceObject.objectType === sourceObj.objectType && this.sourceObject.typeCode === sourceObj.typeCode;
    }

    getDate() {
        return this.date;
    }

    public setLayerData(data: GeoJSON): void {
        throw new Error("Method not implemented.");
    }

    public getLayerData(): Promise<GeoJSON> {
        if (this.data) {
            return new Promise((resolve, reject) => {
                resolve(this.data);
            });
        } else if (this.dataPromise != null) {
            return this.dataPromise;
        } else {
            this.dataPromise = this.vizService.treeAsGeoJson(this.relationshipType, this.relationshipCode, this.sourceObject, this.date, this.getBoundsAsWKT()).then((data: any) => {
                this.data = data;

                this.dataPromise = null;
                return this.data;
            });
            return this.dataPromise;
        }
    }

    private getBoundsAsWKT(): string {
        let wktBounds: string = null;

        if (this.bounds != null) {
            const mapBounds = new LngLatBounds(JSON.parse(this.bounds));
            wktBounds = this.convertBoundsToWKT(mapBounds);
        }

        return wktBounds;
    }

    private convertBoundsToWKT(bounds: LngLatBounds): string {
        let se = bounds.getSouthEast();
        let sw = bounds.getSouthWest();
        let nw = bounds.getNorthWest();
        let ne = bounds.getNorthEast();

        return "POLYGON ((" +
          se.lng + " " + se.lat + "," +
          sw.lng + " " + sw.lat + "," +
          nw.lng + " " + nw.lat + "," +
          ne.lng + " " + ne.lat + "," +
          se.lng + " " + se.lat +
        "))";
    }

    getGeometryType(): string {
        return "MIXED";
    }

    getBounds(layer: Layer): Promise<LngLatBoundsLike> {
        return this.getLayerData().then((geojson: any) => {
            if (geojson == null) { return null; }

            if ((layer as RelationshipVisualizionLayer).getRelatedTypeFilter() != null) {
                geojson.features = geojson.features.filter(feature => feature.properties.type === (layer as RelationshipVisualizionLayer).getRelatedTypeFilter());
            }

            return bbox(geojson) as LngLatBoundsLike;
        });
    }

}

export class RelationshipVisualizionLayer extends Layer {

    relatedTypeFilter: string;

    public toJSON(): any {
        return Object.assign(super.toJSON(), {
            relatedTypeFilter: this.relatedTypeFilter
        });
    }

    getId(): string {
        return (this.relatedTypeFilter == null) ? "" : this.relatedTypeFilter + this.dataSource.getId();
    }

    public getKey(): string {
        return (this.relatedTypeFilter == null) ? "" : this.relatedTypeFilter + this.dataSource.getKey();
    }

    setRelatedTypeFilter(relatedTypeFilter: string) {
        this.relatedTypeFilter = relatedTypeFilter;
    }

    getRelatedTypeFilter(): string {
        return this.relatedTypeFilter;
    }

    configureMapboxLayer(layerType: string, layerConfig: any): void {
        if (this.relatedTypeFilter != null) {
            let filter = ["match", ["get", "type"], this.relatedTypeFilter, true, false];

            if (layerConfig["filter"] != null) {
                layerConfig["filter"].push(filter);
            } else {
                layerConfig["filter"] = filter;
            }
        }
    }

}

export class DataSourceFactory {

    private geomService: GeometryService;

    private registryService: RegistryService;

    private vizService: RelationshipVisualizationService;

    private mapService: MapService;

    private listService: ListTypeService;

    private dataSources: { [key: string] : LayerDataSource } = {};

    constructor(geomService: GeometryService, registryService: RegistryService, vizService: RelationshipVisualizationService, mapService: MapService, listService: ListTypeService) {
        this.geomService = geomService;
        this.registryService = registryService;
        this.vizService = vizService;
        this.mapService = mapService;
        this.listService = listService;
    }

    public getRegisteredDataSource(dataSourceId: string) {
        return this.dataSources[dataSourceId];
    }

    public registerDataSource(dataSource: LayerDataSource) {
        this.dataSources[dataSource.getId()] = dataSource;
    }

    public unregisterDataSource(dataSourceType: string) {
        delete this.dataSources[dataSourceType];
    }

    public newDataSourceFromType(dataSourceType: string): LayerDataSource {
        if (dataSourceType === GEO_OBJECT_DATA_SOURCE_TYPE) {
            return new GeoObjectLayerDataSource(this.registryService);
        } else if (dataSourceType === RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE) {
            return new RelationshipVisualizionDataSource(this.vizService, this.geomService);
        } else if (dataSourceType === LIST_VECTOR_SOURCE_TYPE) {
            return new ListVectorLayerDataSource(this.listService);
        } else if (dataSourceType === SEARCH_DATASOURCE_TYPE) {
            return new SearchLayerDataSource(this.mapService);
        } else {
            // This can happen if they were editing and refreshed the map with editing layers

            // eslint-disable-next-line no-console
            console.log("Cannot find data source of type '" + dataSourceType + "'");
            return null;
        }
    }

    public deserializeDataSource(obj: any): LayerDataSource {
        let dataSource = this.newDataSourceFromType(obj.dataSourceType);

        if (dataSource == null && this.dataSources[obj.id] != null) {
            return this.dataSources[obj.id];
        } else if (dataSource == null) {
            return null;
        }

        dataSource.fromJSON(obj);

        return dataSource;
    }

    public serializeDataSource(dataSource: LayerDataSource): any {
        let sds = dataSource.toJSON();

        return sds;
    }

    public deserializeLayer(sl: any, ds: LayerDataSource): Layer {
        let layer: Layer;

        layer = ds.createLayer(sl.legendLabel, sl.rendered, sl.color);

        Object.assign(layer, sl); // This will set the dataSource on the layer to an id
        layer.dataSource = ds; // So we need to reset the dataSource

        return layer;
    }

    public serializeLayer(layer: Layer): any {
        let sl: any = layer.toJSON();

        sl.dataSource = layer.dataSource.getId();

        return sl;
    }

    public deserializeLayers(serialized: { layers: any[], dataSources: any[] }): Layer[] {
        let layers: Layer[] = [];
        let dataSources: LayerDataSource[] = [];

        serialized.dataSources.forEach(sds => {
            let ds = this.deserializeDataSource(sds);

            if (ds != null) {
                dataSources.push(ds);
            }
        });

        serialized.layers.forEach(sl => {
            let i = dataSources.findIndex(ds => ds.getId() === sl.dataSource);

            if (i !== -1) {
                layers.push(this.deserializeLayer(sl, dataSources[i]));
            }
        });

        return layers;
    }

    public serializeLayers(layers: Layer[]): { layers: any[], dataSources: any[] } {
        let ret = { layers: [], dataSources: [] };

        layers.forEach(layer => {
            if (ret.dataSources.findIndex(sds => sds.id === layer.dataSource.getId()) === -1) {
                let sds = this.serializeDataSource(layer.dataSource);

                ret.dataSources.push(sds);
            }
        });

        layers.forEach(layer => {
            let serializedLayer = this.serializeLayer(layer);

            ret.layers.push(serializedLayer);
        });

        return ret;
    }

}
