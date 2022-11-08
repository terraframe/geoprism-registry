
import { Injectable } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { LocationManagerState } from "@registry/component/location-manager/location-manager.component";
import { LayerRecord, ListTypeVersion } from "@registry/model/list-type";
import { GeoObject, GeoObjectType } from "@registry/model/registry";
import { RegistryCacheService } from "@registry/service/registry-cache.service";
import { GeoObjectLayerDataSource, Layer, ListVectorLayerDataSource, RelationshipVisualizionDataSource, SearchLayerDataSource } from "./layer-data-source";
import { DateService } from "@shared/service";
import { GeometryService, RegistryService } from ".";
import * as ColorGen from "color-generator";
import { ListTypeService } from "./list-type.service";

@Injectable()
export class LocationManagerStateService {

    constructor(private route: ActivatedRoute, private router: Router, private cacheService: RegistryCacheService, private geomService: GeometryService, private dateService: DateService, private registryService: RegistryService, private listTypeService: ListTypeService) {
        this.cacheService.getTypeCache(); // Force type fetch
    }

    public selectListRecord(versionOid: string, uid: string, record: LayerRecord, state: LocationManagerState = {}): LocationManagerState {
        state.version = versionOid;
        state.uid = uid;
        state.text = null;

        // Layer for the selected record (if applicable)
        if (record.recordType === "GEO_OBJECT") {
            let geo = this.geoObjectFromRecord(record);

            let layer: Layer = this.addLayerForGeoObject(geo, null, state);

            this.geomService.zoomOnReady(layer.getId());

            state.type = geo.properties.type;
            state.code = geo.properties.code;
            state.objectType = "GEOOBJECT";
        } else {
            state.date = record.forDate;
        }

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

    addLayerForList(version: ListTypeVersion, state: LocationManagerState = {}): Layer {
        let layers: Layer[] = state.layers == null ? [] : this.geomService.deserializeLayers(state.layers);
        let dataSource = new ListVectorLayerDataSource(this.listTypeService, version.oid);
        let layer = dataSource.createLayer(version.displayLabel, true, ColorGen().hexString());

        if (layers.findIndex(l => l.getKey() === layer.getKey()) === -1) {
            layers.push(layer);
            state.layers = this.geomService.serializeLayers(layers);
        }

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
