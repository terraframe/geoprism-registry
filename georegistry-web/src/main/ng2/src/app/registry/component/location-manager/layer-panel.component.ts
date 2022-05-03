import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";

import { ContextLayer, ContextList, ListOrgGroup, ListTypeVersion, ListVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { LocalizationService } from "@shared/service";
import * as ColorGen from "color-generator";
import { Subscription } from "rxjs";

import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import { PANEL_SIZE_STATE } from "@registry/model/location-manager";
import { NgxSpinnerService } from "ngx-spinner";
import { OverlayerIdentifier } from "@registry/model/constants";
import { DataSourceProvider, GeometryService, Layer, LayerDataSource } from "@registry/service/geometry.service";
import { GeoRegistryConfiguration } from "@core/model/registry";
import { LngLatBounds } from "mapbox-gl";
import { HttpErrorResponse } from "@angular/common/http";
import { RegistryService } from "@registry/service/registry.service";

declare let registry: GeoRegistryConfiguration;

export const VECTOR_LAYER_DATASET_PROVIDER_ID = "LAYER_PANEL_VECTOR";

export interface BaseLayer {
    name: string,
    label: string,
    id: string,
    sprite: string,
    url: string,
    selected: boolean
}

@Component({
    selector: "layer-panel",
    templateUrl: "./layer-panel.component.html",
    styleUrls: ["./location-manager.css", "./layer-panel.css"]
})
export class LayerPanelComponent implements OnInit, OnDestroy {

    draggable = {
        // note that data is handled with JSON.stringify/JSON.parse
        // only set simple data or POJO's as methods will be lost
        data: "myDragData",
        effectAllowed: "all",
        disable: false,
        handle: false
    };

    // Hack to allow the constant to be used in the html
    CONSTANTS = {
        OVERLAY: OverlayerIdentifier.LAYER_PANEL
    }

    @Input() filter: string[] = [];
    @Input() includeSearchLayer: boolean = false;
    @Input() visualizeMode: number;

    @Output() baseLayerChange = new EventEmitter<BaseLayer>();
    @Output() zoomTo = new EventEmitter<Layer>();
    @Output() create = new EventEmitter<ContextLayer>();

    @Input() panelSize: number = PANEL_SIZE_STATE.MINIMIZED;
    @Output() panelSizeChange = new EventEmitter<number>();

    listOrgGroups: ListOrgGroup[] = [];
    // lists: ContextList[] = [];
    layers: ContextLayer[] = [];

    versionMap = {};

    graphList: ContextList = null;

    form: { startDate: string, currentStartDate: string, endDate: string, currentEndDate: string } = {
        startDate: "",
        currentStartDate: "",
        endDate: "",
        currentEndDate: ""
    };

    /*
     * List of base layers
     */
    baseLayers: BaseLayer[] = [
        {
            name: "Satellite",
            label: "baselayer.satellite",
            id: "satellite-v9",
            sprite: "mapbox://sprites/mapbox/satellite-v9",
            url: "mapbox://mapbox.satellite",
            selected: true
        }
        //         {
        //             name: 'Streets',
        //             label: 'baselayer.streets',
        //             id: 'streets-v11',
        //             sprite: 'mapbox://sprites/mapbox/streets-v11',
        //             url: 'mapbox://styles/mapbox/streets-v11'
        //         }
    ];

    subscription: Subscription;

    params: Params = null;

    vectorLayerDataSourceProvider: DataSourceProvider;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private spinner: NgxSpinnerService,
        private service: ListTypeService,
        private lService: LocalizationService,
        private geomService: GeometryService) { }

    ngOnInit(): void {
        this.subscription = this.geomService.layersChange.subscribe((layers: Layer[]) => {
            this.layersChange(layers);
        });

        this.vectorLayerDataSourceProvider = {
            getId(): string {
                return VECTOR_LAYER_DATASET_PROVIDER_ID;
            },
            getDataSource(dataSourceId: string): LayerDataSource {
                return {
                    buildMapboxSource(): any {
                        let protocol = window.location.protocol;
                        let host = window.location.host;

                        return {
                            type: "vector",
                            tiles: [protocol + "//" + host + registry.contextPath + "/list-type/tile?x={x}&y={y}&z={z}&config=" + encodeURIComponent(JSON.stringify({ oid: dataSourceId }))],
                            promoteId: "uid"
                        };
                    },
                    getGeometryType(): string {
                        return "MIXED";
                    },
                    createLayer(oid: string, legendLabel: string, rendered: boolean, color: string): Layer {
                        return new Layer(oid, legendLabel, this, rendered, color);
                    },
                    getDataSourceId(): string {
                        return dataSourceId;
                    },
                    getDataSourceProviderId(): string {
                        return VECTOR_LAYER_DATASET_PROVIDER_ID;
                    },
                    configureMapboxLayer(layerConfig: any): void {
                        layerConfig["source-layer"] = "context";
                    },
                    getBounds(layer: Layer, registryService: RegistryService, listService: ListTypeService): Promise<LngLatBounds> {
                        return listService.getBounds(layer.oid).then((bounds: number[]) => {
                            if (bounds && Array.isArray(bounds)) {
                                return new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
                            } else {
                                return null;
                            }
                        });
                    }
                };
            }
        };
        this.geomService.registerDataSourceProvider(this.vectorLayerDataSourceProvider);
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    setPanelSize(size: number) {
        this.panelSize = size;

        if (this.layers.length === 0 && this.panelSize === PANEL_SIZE_STATE.WINDOWED) {
            this.panelSize = PANEL_SIZE_STATE.FULLSCREEN;
        }
        if (this.panelSize > PANEL_SIZE_STATE.FULLSCREEN) {
            this.panelSize = 0;
        }

        this.panelSizeChange.emit(this.panelSize);

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { layersPanelSize: this.panelSize },
            queryParamsHandling: "merge"
        });
    }

    togglePanelOpen() {
        this.setPanelSize(this.panelSize === 0 ? 1 : 0);
    }

    layersChange(layers: Layer[]): void {
        this.layers = this.geomService.serializeAllLayers();

        let layersWithoutVersions = this.layers.filter(layer => this.versionMap[layer.oid] == null && layer.dataSourceProviderId === VECTOR_LAYER_DATASET_PROVIDER_ID).map(layer => layer.oid);
        if (layersWithoutVersions.length > 0) {
            this.service.fetchVersionsAsListVersion(layersWithoutVersions).then((versions: ListVersion[]) => {
                versions.forEach(version => {
                    this.versionMap[version.oid] = version;

                    let layerIndex = this.layers.findIndex(l => l.oid === version.oid);
                    if (layerIndex !== -1) {
                        let layer = this.layers[layerIndex];
                        version.layer = layer;
                        layer.forDate = version.forDate;
                        layer.versionNumber = version.versionNumber;
                    }
                });
            });
        }

        this.refreshListLayerReferences();
    }

    handleSearch(): Promise<ListOrgGroup[]> {
        this.spinner.show(this.CONSTANTS.OVERLAY);

        return this.service.getGeospatialVersions(this.form.startDate, this.form.endDate).then(listOrgGroups => {
            this.form.currentStartDate = this.form.startDate;
            this.form.currentEndDate = this.form.endDate;

            this.listOrgGroups = listOrgGroups;

            this.refreshListLayerReferences();

            return listOrgGroups;
        }).finally(() => {
            this.spinner.hide(this.CONSTANTS.OVERLAY);
        });
    }

    private refreshListLayerReferences() {
        this.listOrgGroups.forEach(listOrgGroup => {
            listOrgGroup.types.forEach(listTypeGroup => {
                listTypeGroup.lists.forEach(list => {
                    list.versions = list.versions.filter(v => this.filter.indexOf(v.oid) === -1);

                    for (let i = 0; i < list.versions.length; ++i) {
                        let version = list.versions[i];

                        let layerIndex = this.layers.findIndex(l => l.oid === version.oid);
                        if (layerIndex !== -1) {
                            let layer = this.layers[layerIndex];
                            version.layer = layer;
                            layer.forDate = version.forDate;
                            layer.versionNumber = version.versionNumber;
                        }

                        this.versionMap[version.oid] = version;
                    }
                });
            });
        });

        for (const [oid, v] of Object.entries(this.versionMap)) {
            let version: ListVersion = v as ListVersion;

            let layerIndex = this.layers.findIndex(l => l.oid === oid);
            if (layerIndex !== -1) {
                let layer = this.layers[layerIndex];
                version.layer = layer;
                layer.forDate = version.forDate;
                layer.versionNumber = version.versionNumber;
            }
        }
    }

    clickToggleLayerRendered(layer: ContextLayer, list: ContextList) {
        this.toggleLayerRendered(layer);
    }

    toggleVersionLayer(version: ListVersion, list: ContextList): void {
        if (!version.layer) {
            version.layer = new ContextLayer(version.oid, list.label, true, ColorGen().hexString(), version.oid, VECTOR_LAYER_DATASET_PROVIDER_ID);
            version.layer.versionNumber = version.versionNumber;
            version.layer.forDate = version.forDate;
            this.geomService.addOrUpdateLayer(version.layer);
        } else {
            this.geomService.removeLayer(version.layer.oid);
            delete version.layer;
        }
    }

    toggleLayerRendered(layer: ContextLayer): void {
        layer.rendered = !layer.rendered;

        this.geomService.addOrUpdateLayer(layer);
    }

    onGotoBounds(layer: ContextLayer): void {
        let layers = this.geomService.getLayers().filter(l => l.oid === layer.oid);

        if (layers.length > 0) {
            this.zoomTo.emit(layers[0]);
        }
    }

    onCreate(layer: ContextLayer): void {
        this.create.emit(layer);
    }

    toggleBaseLayer(layer: BaseLayer): void {
        this.baseLayers.forEach(bl => {
            bl.selected = false;
        });

        layer.selected = true;

        this.baseLayerChange.emit(layer);
    }

    moveLayer(eventLayers: ContextLayer[]): void {
        this.geomService.setLayers(eventLayers);
    }

    drop(event: CdkDragDrop<string[]>) {
        let eventLayers = JSON.parse(JSON.stringify(this.layers));
        moveItemInArray(eventLayers, event.previousIndex, event.currentIndex);
        this.moveLayer(eventLayers);
    }

}
