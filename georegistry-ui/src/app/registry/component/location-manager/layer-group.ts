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

import { GeoObjectLayerDataSource, GEO_OBJECT_DATA_SOURCE_TYPE, Layer, ListVectorLayerDataSource, LIST_VECTOR_SOURCE_TYPE, RelationshipVisualizionDataSource, RELATIONSHIP_VISUALIZER_DATASOURCE_TYPE, SearchLayerDataSource, SEARCH_DATASOURCE_TYPE, ValueOverTimeDataSource } from "@registry/service/layer-data-source";
import { LocalizationService } from "@shared/service/localization.service";

export class LayerGroup {

    private groupName: string;

    private label: string;

    private layers: Layer[];

    constructor(layers: Layer[], label: string) {
        this.layers = layers;
        this.label = label;
    }

    public getGroupName(): string {
        return this.groupName;
    }

    public setGroupName(groupName: string) {
        this.groupName = groupName;
    }

    public getLayers(): Layer[] {
        return this.layers;
    }

    public setLayers(layers: Layer[]): void {
        this.layers = layers;
    }

    public getLabel(): string {
        return this.label;
    }

    public setLabel(label: string) {
        this.label = label;
    }

}

export interface LayerSorter {

    sortLayers(layers: Layer[]): Layer[];

}

export class LayerGroupSorter implements LayerSorter {

    private localService: LocalizationService;

    public constructor(localService: LocalizationService) {
        this.localService = localService;
    }

    public getLayerGroups(layers: Layer[]): LayerGroup[] {
        let groups: LayerGroup[] = [];

        groups.push(new LayerGroup(layers.filter(l => l.dataSource instanceof SearchLayerDataSource), this.localService.decode("explorer.layerPanel.layerGroup.search")));
        groups.push(new LayerGroup(layers.filter(l => l.dataSource instanceof GeoObjectLayerDataSource || l.dataSource instanceof ValueOverTimeDataSource), this.localService.decode("explorer.layerPanel.layerGroup.geoObject")));
        groups.push(new LayerGroup(layers.filter(l => l.dataSource instanceof RelationshipVisualizionDataSource), this.localService.decode("explorer.layerPanel.layerGroup.relationship")));
        groups.push(new LayerGroup(layers.filter(l => l.dataSource instanceof ListVectorLayerDataSource), this.localService.decode("explorer.layerPanel.layerGroup.list")));

        return groups.filter(g => g.getLayers().length > 0);
    }

    public sortLayers(layers: Layer[]): Layer[] {
        let sorted = [];
        let groups = this.getLayerGroups(layers);

        groups.forEach(group => group.getLayers().forEach(l => sorted.push(l)));

        return sorted;
    }

}
