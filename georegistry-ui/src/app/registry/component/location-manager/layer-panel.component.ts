///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";

import { ContextList, ListOrgGroup, ListVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import * as ColorGen from "color-generator";
import { Subscription } from "rxjs";

import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import { PANEL_SIZE_STATE } from "@registry/model/location-manager";
import { NgxSpinnerService } from "ngx-spinner";
import { OverlayerIdentifier } from "@registry/model/constants";
import { GeometryService } from "@registry/service/geometry.service";
import { GEO_OBJECT_DATA_SOURCE_TYPE, Layer, ListVectorLayerDataSource, LIST_VECTOR_SOURCE_TYPE, RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE, SEARCH_DATASOURCE_TYPE } from "@registry/service/layer-data-source";
import { RegistryService } from "@registry/service/registry.service";
import { RelationshipVisualizationService } from "@registry/service/relationship-visualization.service";
import { LayerGroup, LayerGroupSorter } from "./layer-group";
import { LocalizationService } from "@shared/service/localization.service";

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
        OVERLAY: OverlayerIdentifier.LAYER_PANEL,
        SEARCH_DATASOURCE_TYPE: SEARCH_DATASOURCE_TYPE,
        GEO_OBJECT_DATA_SOURCE_TYPE: GEO_OBJECT_DATA_SOURCE_TYPE,
        RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE: RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE,
        LIST_VECTOR_SOURCE_TYPE: LIST_VECTOR_SOURCE_TYPE
    }

    @Input() filter: string[] = [];
    @Input() includeSearchLayer: boolean = false;
    @Input() visualizeMode: number;

    @Output() viewList = new EventEmitter<string>();
    @Output() baseLayerChange = new EventEmitter<BaseLayer>();
    @Output() create = new EventEmitter<Layer>();

    @Input() panelSize: number = PANEL_SIZE_STATE.MINIMIZED;
    @Output() panelSizeChange = new EventEmitter<number>();

    listOrgGroups: ListOrgGroup[] = [];

    layers: Layer[] = [];
    layerGroups: LayerGroup[] = [];

    versionMap: { [key: string]: ListVersion } = {};

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

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private spinner: NgxSpinnerService,
        private service: ListTypeService,
        private geomService: GeometryService,
        private registryService: RegistryService,
        private vizService: RelationshipVisualizationService,
        private localService: LocalizationService,
        private listService: ListTypeService) { }

    ngOnInit(): void {
        this.subscription = this.geomService.layersChange.subscribe((layers: Layer[]) => {
            this.layersChange(layers);
        });
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
            queryParamsHandling: "merge",
            replaceUrl: true
        });
    }

    togglePanelOpen() {
        this.setPanelSize(this.panelSize === 0 ? 1 : 0);
    }

    layersChange(layers: Layer[]): void {
        this.layers = this.geomService.getLayers();
        this.layerGroups = new LayerGroupSorter(this.localService).getLayerGroups(layers);

        let layersWithoutVersions = this.layers.filter(layer => this.versionMap[layer.getId()] == null && layer.dataSource.getDataSourceType() === LIST_VECTOR_SOURCE_TYPE).map(layer => (layer.dataSource as ListVectorLayerDataSource).getVersionId());
        if (layersWithoutVersions.length > 0) {
            this.service.fetchVersionsAsListVersion(layersWithoutVersions).then((versions: ListVersion[]) => {
                versions.forEach(version => {
                    let layers = this.layers.filter(l => l.dataSource.getDataSourceType() === LIST_VECTOR_SOURCE_TYPE && (l.dataSource as ListVectorLayerDataSource).getVersionId() === version.oid);
                    version.layers = layers;
                    layers.forEach(layer => {
                        this.versionMap[layer.getId()] = version;
                    });
                });
            });
        }

        this.refreshListLayerReferences();
    }

    /*
        private convertLayerToContextLayer(layer: Layer): ContextLayer {
            let cLayer: ContextLayer = new ContextLayer(layer.getId(), layer.dataSource.getDataSourceType(), layer.legendLabel, layer.rendered, layer.color);
            return cLayer;
        }

        private convertContextLayerToLayer(cLayer: ContextLayer): Layer {
            let serializedLayer: any = cLayer;
            delete serializedLayer.dataSourceType;
            serializedLayer.dataSource = { dataSourceType: cLayer.dataSourceType };

            return new DataSourceFactory(this.geomService, this.registryService, this.vizService).deserializeLayer();
        }
        */

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

                        let layers = this.layers.filter(l => l.dataSource.getDataSourceType() === LIST_VECTOR_SOURCE_TYPE && (l.dataSource as ListVectorLayerDataSource).getVersionId() === version.oid);
                        version.layers = layers;
                        layers.forEach(layer => {
                            this.versionMap[layer.getId()] = version;
                        });
                    }
                });
            });
        });

        for (const [, ver] of Object.entries(this.versionMap)) {
            let version: ListVersion = ver as ListVersion;

            let layers = this.layers.filter(l => l.dataSource.getDataSourceType() === LIST_VECTOR_SOURCE_TYPE && (l.dataSource as ListVectorLayerDataSource).getVersionId() === version.oid);
            version.layers = layers;
        }
    }

    clickToggleVersionRendered(version: ListVersion, list: ContextList) {
        let rendered = ! this.versionIsRendered(version);
      
        if (version.layers != null) {
            version.layers.forEach(layer => {
                if (layer.rendered !== rendered) {
                    this.toggleLayerRendered(layer);
                }
            });
        }
    }

    toggleLayerRendered(layer: Layer): void {
        layer.rendered = !layer.rendered;

        this.geomService.addOrUpdateLayer(layer);
    }

    onGotoBounds(layer: Layer): void {
        let layers = this.geomService.getLayers().filter(l => l.getId() === layer.getId());

        if (layers.length > 0) {
            this.geomService.zoomToLayer(layers[0]);
        }
    }

    togglePinned(layer: Layer): void {
        let layers = this.geomService.getLayers();
        let layerIndex = this.geomService.getLayers().findIndex(l => l.getId() === layer.getId());

        if (layerIndex !== -1) {
            let layer = layers[layerIndex];

            layer.setPinned(!layer.getPinned());
            this.geomService.setLayers(layers);
        }
    }

    onCreate(layer: Layer): void {
        this.create.emit(layer);
    }

    toggleVersionLayer(version: ListVersion, list: ContextList): void {
        if (!version.layers || version.layers.length == 0) {
            let dataSource = new ListVectorLayerDataSource(this.listService, version.oid);
            version.layers = [ dataSource.createLayer(list.label, true, ColorGen().hexString()) ];
            this.versionMap[version.layers[0].getId()] = version;
            this.geomService.addOrUpdateLayer(version.layers[0]);
        } else {
            version.layers.forEach(layer => {
                this.geomService.removeLayer(layer.getId());
                delete this.versionMap[layer.getId()];
            });
            delete version.layers;
        }
    }

    removeLayer(layer: Layer): void {
        this.geomService.removeLayer(layer.getId());

        let version = this.versionMap[layer.getId()];
        if (version) {
            delete this.versionMap[layer.getId()];
            version.layers = version.layers.filter(l => l.getId() !== layer.getId());
        }
    }
    
    versionIsRendered(version: ListVersion) {
        if (!version.layers || version.layers.length == 0) {
            return false;
        }
      
        let rendered = false;
        
        for (let i = 0; i < version.layers.length; ++i) {
            rendered = rendered || version.layers[i].rendered;
        }
        
        return rendered;
    }

    toggleBaseLayer(layer: BaseLayer): void {
        this.baseLayers.forEach(bl => {
            bl.selected = false;
        });

        layer.selected = true;

        this.baseLayerChange.emit(layer);
    }

    drop(event: CdkDragDrop<string[]>, group: LayerGroup) {
        moveItemInArray(group.getLayers(), event.previousIndex, event.currentIndex);

        let layers = [];
        this.layerGroups.forEach(group => group.getLayers().forEach(l => layers.push(l)));

        this.geomService.setLayers(layers);
    }

    onViewList(oid: string): void {
        this.viewList.emit(oid);
    }

}
