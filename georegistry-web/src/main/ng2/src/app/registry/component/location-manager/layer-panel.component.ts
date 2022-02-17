import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, OnDestroy } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";

import { ContextLayer, ContextList, ListOrgGroup } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { LocalizationService } from "@shared/service";
import * as ColorGen from "color-generator";
import { Subscription } from "rxjs";

import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import { PANEL_SIZE_STATE } from "@registry/model/location-manager";

export const GRAPH_LAYER = "graph";

export class LayerEvent {

    layer: ContextLayer;
    prevLayer?: ContextLayer;

}

@Component({
    selector: "layer-panel",
    templateUrl: "./layer-panel.component.html",
    styleUrls: ["./location-manager.css"]
})
export class LayerPanelComponent implements OnInit, OnDestroy, OnChanges {

    draggable = {
        // note that data is handled with JSON.stringify/JSON.parse
        // only set simple data or POJO's as methods will be lost
        data: "myDragData",
        effectAllowed: "all",
        disable: false,
        handle: false
    };

    // Hack to allow the constant to be used in the html
    CONSTANT = {
        GRAPH_LAYER: GRAPH_LAYER
    }

    @Input() filter: string[] = [];
    @Input() includeGraphLayer: boolean = false;
    @Input() visualizeMode: number;

    @Output() layerChange = new EventEmitter<LayerEvent>();
    @Output() baseLayerChange = new EventEmitter<any>();
    @Output() reorder = new EventEmitter<ContextLayer[]>();
    @Output() zoomTo = new EventEmitter<ContextLayer>();
    @Output() create = new EventEmitter<ContextLayer>();

    @Input() panelSize: number = PANEL_SIZE_STATE.MINIMIZED;
    @Output() panelSizeChange = new EventEmitter<number>();

    listOrgGroups: ListOrgGroup[] = [];
    // lists: ContextList[] = [];
    layers: ContextLayer[] = [];

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
    baseLayers: any[] = [
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
        private service: ListTypeService,
        private lService: LocalizationService) { }

    ngOnInit(): void {
        this.subscription = this.route.queryParams.subscribe(params => {
            this.params = params;

            this.handleParams();
        });
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.includeGraphLayer != null) {
            if (changes.includeGraphLayer.currentValue) {
                let graphLayer = {
                    oid: GRAPH_LAYER,
                    forDate: this.form.endDate,
                    versionNumber: -1
                };

                this.graphList = {
                    oid: GRAPH_LAYER,
                    label: this.lService.decode("explorer.search.layer"),
                    versions: [graphLayer],
                    open: false
                };

                this.toggleLayer(graphLayer, this.graphList);
            } else {
                if (this.graphList != null) {
                    this.toggleLayer(this.graphList.versions[0], this.graphList);
                }
            }
        }
    }

    togglePanelOpen() {
        this.panelSize = this.panelSize + 1;

        if (this.layers.length === 0 && this.panelSize === PANEL_SIZE_STATE.WINDOWED) {
            this.panelSize = PANEL_SIZE_STATE.FULLSCREEN;
        }
        if (this.panelSize > PANEL_SIZE_STATE.FULLSCREEN) {
            this.panelSize = 0;
        }

        this.panelSizeChange.emit(this.panelSize);
    }

    /**
     *
     * Method responsible for parsing the state from the URL parameters and determining if
     * the model of the widget needs to be updated or not.
     *
     * */
    handleParams(): void {
        let isSearchRequired = false;

        if (this.params.startDate != null && this.params.startDate !== this.form.currentStartDate) {
            this.form.startDate = this.params.startDate;

            isSearchRequired = true;
        }

        if (this.params.endDate != null && this.params.endDate !== this.form.currentEndDate) {
            this.form.endDate = this.params.endDate;

            isSearchRequired = true;
        }

        const layers = this.params.layers != null ? JSON.parse(this.params.layers) : [];

        layers.forEach(layer => {
            if (layer !== GRAPH_LAYER && this.findVersionById(layer) == null) {
                isSearchRequired = true;
            }
        });

        if (isSearchRequired) {
            // One of the enabled layers specified in the URL is not currently in the list/versions data model
            // As such we must do a new search for the valid list/versions in order to populate the option
            // into the data model.
            // OR the search dates have been updated, so a new search must be performed.

            this.handleSearch().then(lists => {
                layers.reverse().forEach(oid => {
                    this.toggleLayersWithCondition(v => v.oid === oid);
                });
            });
        } else {
            // Determine if an existing version in the data model needs to be toggled on based on the state
            // of the URL 'layers' parameters
            layers.forEach(layer => {
                const index = this.layers.findIndex(l => l.oid === layer);

                if (index === -1) {
                    this.toggleLayersWithCondition(v => v.oid === layer);
                }
            });

            // Determine if any existing layers which need to be toggled off based on the state of the URL ''
            this.layers.filter(l => l.oid !== GRAPH_LAYER && layers.indexOf(l.oid) === -1).forEach(layer => {
                this.toggleLayersWithCondition(v => v.oid === layer.oid);
            });
        }

        // Determine if the order of the layers has changed
        if (this.params.layers != null) {
            let isEqual = true;
            for (let i = 0; i < this.layers.length; i++) {
                if (this.layers[i].oid !== layers[i]) {
                    isEqual = false;
                }
            }

            if (!isEqual) {
                const indecies = {};
                for (let i = 0; i < layers.length; i++) {
                    indecies[layers[i]] = i;
                }

                this.layers = this.layers.sort((a, b) => {
                    return indecies[a.oid] - indecies[b.oid];
                });

                this.reorder.emit(this.layers);
            }
        }
    }

    onConfirm(): void {
        if (this.params.startDate == null && this.params.endDate == null && this.params.layers == null && this.form.startDate === null && this.form.endDate === null) {
            // A new search should null out any currently select layers and any record which has been clicked on
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { layers: null, version: null },
                queryParamsHandling: "merge"
            });

            this.handleSearch();
        } else {
            // A new search should null out any currently select layers and any record which has been clicked on
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { startDate: this.form.startDate, endDate: this.form.endDate, layers: null, version: null },
                queryParamsHandling: "merge"
            });
        }
    }

    handleSearch(): Promise<ListOrgGroup[]> {
        return this.service.getGeospatialVersions(this.form.startDate, this.form.endDate).then(listOrgGroups => {
            // Remove all current lists
            this.toggleLayersWithCondition(v => v.enabled && v.oid !== GRAPH_LAYER);

            this.form.currentStartDate = this.form.startDate;
            this.form.currentEndDate = this.form.endDate;

            this.listOrgGroups = listOrgGroups;

            this.listOrgGroups.forEach(listOrgGroup => {
                listOrgGroup.types.forEach(listTypeGroup => {
                    listTypeGroup.lists.forEach(list => {
                        list.versions = list.versions.filter(v => this.filter.indexOf(v.oid) === -1);
                    });
                });
            });

            return listOrgGroups;
        });
    }

    findVersionById(id: string): ContextLayer {
        let response: ContextLayer = null;

        this.listOrgGroups.forEach(listOrgGroup => {
            listOrgGroup.types.forEach(listTypeGroup => {
                listTypeGroup.lists.forEach(list => {
                    list.versions.forEach(version => {
                        if (version.oid === id) {
                            response = version;
                        }
                    });
                });
            });
        });

        return response;
    }

    toggleLayersWithCondition(condition: (layer: ContextLayer) => boolean) {
        this.listOrgGroups.forEach(listOrgGroup => {
            listOrgGroup.types.forEach(listTypeGroup => {
                listTypeGroup.lists.forEach(list => {
                    list.versions.filter(condition).forEach(v => {
                        this.toggleLayer(v, list);
                    });
                });
            });
        });
    }

    onToggleLayer(layer: ContextLayer, list: ContextList): void {
        const index = this.layers.findIndex(l => l.oid === layer.oid);

        let layers = this.layers.filter(l => l.oid !== GRAPH_LAYER).map(l => l.oid);

        if (index === -1) {
            layers.unshift(layer.oid);
        } else {
            layers = layers.filter(l => l !== layer.oid);
        }

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { layers: JSON.stringify(layers) },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    toggleLayer(layer: ContextLayer, list: ContextList): void {
        //if (!this.params) {
        //    return;
        //}

        layer.enabled = !layer.enabled;
        layer.active = layer.enabled;

        if (layer.active && layer.color == null) {
            layer.color = ColorGen().hexString();
            layer.label = list.label;
        }

        let index: number = 0;

        if (layer.enabled) {
            if (layer.oid === GRAPH_LAYER && this.params && this.params.layers != null) {
                const i = JSON.parse(this.params.layers).indexOf(GRAPH_LAYER);

                if (i !== -1) {
                    index = i;
                }
            }

            this.layers.splice(index, 0, layer);
        } else {
            const index = this.layers.findIndex(l => l.oid === layer.oid);

            if (index !== -1) {
                this.layers.splice(index, 1);
            }
        }

        this.layerChange.emit({ layer: layer });

        if (index !== 0) {
            this.reorder.emit(this.layers);
        }
    }

    toggleActive(layer: ContextLayer): void {
        layer.active = !layer.active;

        const event: LayerEvent = {
            layer: layer
        };

        if (layer.active) {
            const index = this.layers.findIndex(l => l.oid === layer.oid);

            // Find the first active layer
            for (let i = (index - 1); i >= 0; i--) {
                if (event.prevLayer == null && this.layers[i].active) {
                    event.prevLayer = this.layers[i];
                }
            }
        }

        this.layerChange.emit(event);
    }

    onGotoBounds(layer: ContextLayer): void {
        this.zoomTo.emit(layer);
    }

    onCreate(layer: ContextLayer): void {
        this.create.emit(layer);
    }

    toggleBaseLayer(layer: any): void {
        this.baseLayers.forEach(bl => {
            bl.active = false;
        });

        layer.active = true;

        this.baseLayerChange.emit(layer);
    }

    moveLayerIncrementally(layer: ContextLayer, offset: number): void {
        const index = this.layers.findIndex(l => l.oid === layer.oid);
        const target = (index + offset);

        if (index !== -1 && target > -1 && target <= this.layers.length - 1) {
            let layers = this.layers.map(l => l.oid);

            const a = layers[index];
            layers[index] = layers[index + offset];
            layers[index + offset] = a;

            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { layers: JSON.stringify(layers) },
                queryParamsHandling: "merge" // remove to replace all query params by provided
            });
        }
    }

    moveLayer(oldLayers: ContextLayer[]): void {
        let layers = oldLayers.map(l => l.oid);

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { layers: JSON.stringify(layers) },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    drop(event: CdkDragDrop<string[]>) {
        let oldLayers = JSON.parse(JSON.stringify(this.layers));
        moveItemInArray(oldLayers, event.previousIndex, event.currentIndex);
        this.moveLayer(oldLayers);
    }

}
