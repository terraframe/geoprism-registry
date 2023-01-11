
import { Injectable } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { LocationManagerState } from "@registry/component/location-manager/location-manager.component";
import { LayerRecord, ListTypeVersion } from "@registry/model/list-type";
import { GeoObject, GeoObjectType } from "@registry/model/registry";
import { RegistryCacheService } from "@registry/service/registry-cache.service";
import { GeoObjectLayerDataSource, Layer, ListVectorLayer, ListVectorLayerDataSource, RelationshipVisualizionDataSource, SearchLayerDataSource } from "./layer-data-source";
import { DateService } from "@shared/service";
import { GeometryService, RegistryService } from ".";
import * as ColorGen from "color-generator";
import { ListTypeService } from "./list-type.service";
import { SELECTED_COLOR } from "./geometry.service";

@Injectable()
export class LocationManagerStateService {

    constructor(private route: ActivatedRoute, private router: Router, private cacheService: RegistryCacheService, private geomService: GeometryService, private dateService: DateService, private registryService: RegistryService, private listTypeService: ListTypeService) {
        this.cacheService.getTypeCache(); // Force type fetch
    }

    public selectListRecord(version: ListTypeVersion, uid: string, record: LayerRecord, state: LocationManagerState = {}): LocationManagerState {
        state.version = version.oid;
        state.uid = uid;
        state.text = null;

        // Layer for the selected record (if applicable)
        if (record != null && record.recordType === "GEO_OBJECT") {
            let geo = this.geoObjectFromRecord(record);

            let layer: Layer = this.addLayerForGeoObject(geo, null, state);

            this.geomService.zoomOnReady(layer.getId());

            this.addLayerForList(version, null, state);

            state.type = geo.properties.type;
            state.code = geo.properties.code;
            state.objectType = "GEOOBJECT";
        } else {
            let zoomLayer;

            if (record != null) {
                // TODO : This LayerRecord interface is horrifically overloaded
                let label;

                if (record.displayLabel != null && record.displayLabel.localizedValue != null) {
                    label = record.displayLabel.localizedValue;
                } else if (record.data != null && record.data.displayLabelDefaultLocale != null) {
                    label = record.data.displayLabelDefaultLocale;
                } else {
                    throw new Error("Unexpected 'record' object.");
                }

                this.addLayerForList(version, null, state);
                zoomLayer = this.addLayerForList(version, { label: label, uid: uid }, state);

                state.date = record.forDate;
            } else {
                zoomLayer = this.addLayerForList(version, null, state);
            }

            this.geomService.zoomOnReady(zoomLayer.getId());
        }

        return state;
    }

    public clearListRecord(state: LocationManagerState = {}): LocationManagerState {
        state.version = null;
        state.uid = null;
        state.text = null;

        return state;
    }

    geoObjectFromRecord(record: LayerRecord): GeoObject {
        return {
            properties: {
                type: record.typeCode,
                uid: record.uid,
                code: record.code,
                displayLabel: record.displayLabel
                // displayLabel: new LocalizedValue(record.displayLabel, [])
            }
        } as GeoObject;
    }

    addLayerForList(version: ListTypeVersion, objectFilter: { label: string, uid: string } = null, state: LocationManagerState = {}): Layer {
        let layers: Layer[] = state.layers == null ? [] : this.geomService.deserializeLayers(state.layers);
        let dataSource = new ListVectorLayerDataSource(this.listTypeService, version.oid);

        let label = version.displayLabel;
        if (objectFilter != null) {
            label = objectFilter.label + " (" + label + ")";
        }

        let color;
        if (objectFilter == null) {
            color = ColorGen().hexString();
        } else {
            color = SELECTED_COLOR;
        }

        let layer: ListVectorLayer = dataSource.createLayer(label, true, color) as ListVectorLayer;

        if (objectFilter != null) {
            layer.setObjectFilter(objectFilter.uid);
        }

        // Remove any existing List layer(s)
        layers = layers.filter(l =>
            !(l.dataSource instanceof ListVectorLayerDataSource) ||
            l.getKey() === layer.getKey() ||
            ((l as ListVectorLayer).getObjectFilter() == null && (l.dataSource as ListVectorLayerDataSource).getVersionId() === version.oid) ||
            l.getPinned()
        );

        // Remove search layer
        layers = layers.filter(layer => layer.getPinned() || (!(layer.dataSource instanceof SearchLayerDataSource) && !(layer.dataSource instanceof RelationshipVisualizionDataSource)));

        if (layers.findIndex(l => l.getKey() === layer.getKey()) === -1) {
            layers.splice(0, 0, layer);
        }

        state.layers = this.geomService.serializeLayers(layers);

        return layer;
    }

    addLayerForGeoObject(geoObject: GeoObject, date: string = null, state: LocationManagerState = {}): Layer {
        let layers: Layer[] = state.layers == null ? [] : this.geomService.deserializeLayers(state.layers);
        const type: GeoObjectType = this.cacheService.getTypeCache().getTypeByCode(geoObject.properties.type);

        let dataSource = new GeoObjectLayerDataSource(this.registryService, geoObject.properties.code, geoObject.properties.type, date);

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
            layers.splice(0, 0, layer);
        }

        state.layers = this.geomService.serializeLayers(layers);

        return layer;
    }

}
