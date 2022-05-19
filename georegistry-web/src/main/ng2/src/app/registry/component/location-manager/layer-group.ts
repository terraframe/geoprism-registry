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
