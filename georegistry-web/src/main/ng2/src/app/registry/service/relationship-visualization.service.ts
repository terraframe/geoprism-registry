///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
///

import { EventEmitter, Injectable } from "@angular/core";
import { HttpClient, HttpErrorResponse, HttpParams } from "@angular/common/http";
// import 'rxjs/add/operator/toPromise';
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/registry";

import { LocalizedValue } from "@shared/model/core";
import { DataSourceProvider, GeoJsonLayer, GeoJsonLayerDataSource, GeometryService, GEO_OBJECT_LAYER_DATA_SOURCE_PROVIDER_GEO_OBJECT_CODE_SPLIT, Layer, LayerDataSource } from "./geometry.service";
import { Relationship, RELATIONSHIP_VISUALIZER_LAYER_DATASET_PROVIDER, TreeData } from "@registry/model/graph";
import { RegistryService } from "./registry.service";
import { ListTypeService } from "./list-type.service";
import { LngLatBounds, LngLatBoundsLike } from "mapbox-gl";
import { bbox } from "@turf/turf";
import { ActivatedRoute } from "@angular/router";
import { LocationManagerParams } from "@registry/component/location-manager/location-manager.component";
declare let registry: GeoRegistryConfiguration;

export const RELVIZ_DATA_SOURCE_ID_SEPARATOR = GEO_OBJECT_LAYER_DATA_SOURCE_PROVIDER_GEO_OBJECT_CODE_SPLIT;

export class RelationshipVisualizerDataSourceProvider implements DataSourceProvider {

    // Required params in order to provide data
    params: LocationManagerParams = null;

    // Some outputs you can listen to
    public onFetchRelationshipData: EventEmitter<void> = new EventEmitter<void>();
    public onLoadRelationshipData: EventEmitter<Relationship[]> = new EventEmitter<Relationship[]>();

    public onFetchTreeData: EventEmitter<void> = new EventEmitter();
    public onLoadTreeData: EventEmitter<TreeData> = new EventEmitter();

    onFetchError: EventEmitter<any> = new EventEmitter();

    // We will populate these when we get our required params
    relationship: Relationship = null;
    relationships: Relationship[] = null;
    treeData: TreeData = null;

    // eslint-disable-next-line no-use-before-define
    vizService: RelationshipVisualizationService;
    geomService: GeometryService;

    constructor(vizService: RelationshipVisualizationService, geomService: GeometryService) {
        this.vizService = vizService;
        this.geomService = geomService;

        // TODO : Unsubscribe?
        /*
        route.queryParams.subscribe(params => {
            this.params = params;
            this.detectChanges();
        });
        */
    }

    queryParamChanges(newParams: LocationManagerParams, oldParams: LocationManagerParams) {
        this.params = JSON.parse(JSON.stringify(newParams));

        if (this.relationships == null || newParams.type !== oldParams.type || newParams.graphOid !== oldParams.graphOid) {
            this.relationships = null;
            this.relationship = null;
            this.fetchRelationships();
        } else if (this.relationships != null && this.relationship && (newParams.bounds !== oldParams.bounds || newParams.code !== oldParams.code || newParams.date !== oldParams.date || newParams.uid !== oldParams.uid)) {
            this.fetchData();
        }
    }

    public fetchRelationships(): void {
        if (this.params.type != null) {
            // this.relationships = [];

            this.onFetchRelationshipData.emit();

            this.vizService.relationships(this.params.type).then(relationships => {
                this.relationships = relationships;

                if (!this.params.graphOid || this.relationships.findIndex(rel => rel.oid === this.params.graphOid) === -1) {
                    this.relationship = this.relationships[0];
                    this.params.graphOid = this.relationship.oid;
                } else {
                    this.relationship = this.relationships[this.relationships.findIndex(rel => rel.oid === this.params.graphOid)];
                    this.params.graphOid = this.relationship.oid;
                }

                this.onLoadRelationshipData.emit(relationships);
            }).catch((err: HttpErrorResponse) => {
                this.onFetchError.emit(err);
            });
        }
    }

    public fetchData(): void {
        if (this.relationship != null && this.params.code && this.params.type) {
            this.onFetchTreeData.emit();

            let mapBounds = new LngLatBounds(JSON.parse(this.params.bounds));

            this.vizService.tree(this.relationship.type, this.relationship.code, this.params.code, this.params.type, this.params.date, this.convertBoundsToWKT(mapBounds)).then(data => {
                this.treeData = data;
                this.geomService.refreshDatasets(RELATIONSHIP_VISUALIZER_LAYER_DATASET_PROVIDER);
                this.onLoadTreeData.emit(data);
            }).catch((err: HttpErrorResponse) => {
                this.onFetchError.emit(err);
            });
        }
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

    getId(): string {
        return RELATIONSHIP_VISUALIZER_LAYER_DATASET_PROVIDER;
    }

    getDataSource(dataSourceId: string): LayerDataSource {
        let provider = this;
        return {
            buildMapboxSource(): any {
                let geojson = this.getLayerData();

                if (geojson == null) {
                    return null;
                }

                return {
                    type: "geojson",
                    data: geojson
                };
            },
            getGeometryType(): string {
                return "MIXED";
            },
            getLayerData(): any {
                if (provider.treeData) {
                    for (const [typeCode, featureCollection] of Object.entries(provider.treeData.geoJson)) {
                        let oid = "GRAPH-" + typeCode + "-" + provider.relationship.code;

                        if (oid === dataSourceId) {
                            return featureCollection;
                        }
                    }
                } else {
                    return GeometryService.createEmptyGeometryValue("MULTIPOLYGON");
                }
            },
            setLayerData(data: any) {
                // eslint-disable-next-line no-console
                console.log("Cannot set data");
            },
            createLayer(oid: string, legendLabel: string, rendered: boolean, color: string): Layer {
                return new GeoJsonLayer(oid, legendLabel, this, rendered, color);
            },
            getDataSourceId(): string {
                return dataSourceId;
            },
            getDataSourceProviderId(): string {
                return RELATIONSHIP_VISUALIZER_LAYER_DATASET_PROVIDER;
            },
            getBounds(layer: Layer, registryService: RegistryService, listService: ListTypeService): Promise<LngLatBoundsLike> {
                return new Promise((resolve, reject) => {
                    if (provider.treeData == null) { resolve(null); }

                    resolve(bbox(this.getLayerData()) as LngLatBoundsLike);
                });
            }
        } as GeoJsonLayerDataSource;
    }

}

@Injectable()
export class RelationshipVisualizationService {

    dataSourceProvider: RelationshipVisualizerDataSourceProvider;

    constructor(private http: HttpClient, private eventService: EventService, private route: ActivatedRoute) {
    }

    tree(relationshipType: string, graphTypeCode: string, geoObjectCode: string, geoObjectTypeCode: string, date: string, boundsWKT: string): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.set("graphTypeCode", graphTypeCode);
        params = params.set("geoObjectCode", geoObjectCode);
        params = params.set("geoObjectTypeCode", geoObjectTypeCode);

        if (relationshipType != null) {
            params = params.set("relationshipType", relationshipType);
        }

        if (date) {
            params = params.set("date", date);
        }

        if (boundsWKT) {
            params = params.set("boundsWKT", boundsWKT);
        }

        // this.eventService.start();

        return this.http
            .get<any>(registry.contextPath + "/relationship-visualization/tree", { params: params })
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

    relationships(geoObjectTypeCode: string): Promise<{ oid:string, code: string, label: LocalizedValue, isHierarchy: boolean, type?: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("geoObjectTypeCode", geoObjectTypeCode);

        // this.eventService.start();

        return this.http
            .get<any>(registry.contextPath + "/relationship-visualization/relationships", { params: params })
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

    getDataSourceProvider(geomService: GeometryService): RelationshipVisualizerDataSourceProvider {
        if (this.dataSourceProvider == null) {
            this.dataSourceProvider = new RelationshipVisualizerDataSourceProvider(this, geomService);
        }

        return this.dataSourceProvider;
    }

}
