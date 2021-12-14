import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, OnDestroy } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";

import { ContextLayer, ContextList } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { LocalizationService } from "@shared/service";
import * as ColorGen from "color-generator";
import { Subscription } from "rxjs";

export const GRAPH_LAYER = 'graph';

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
    // Hack to allow the constant to be used in the html
    CONSTANT = {
        GRAPH_LAYER: GRAPH_LAYER
    }

    @Input() filter: string[] = [];
    @Input() includeGraphLayer: boolean = false;

    @Output() layerChange = new EventEmitter<LayerEvent>();
    @Output() baseLayerChange = new EventEmitter<any>();
    @Output() reorder = new EventEmitter<ContextLayer[]>();
    @Output() zoomTo = new EventEmitter<ContextLayer>();
    @Output() create = new EventEmitter<ContextLayer>();

    baselayerIconHover = false;

    lists: ContextList[] = [];
    layers: ContextLayer[] = [];

    form: { startDate: string, currentStartDate: string, endDate: string, currentEndDate: string } = {
        startDate: '',
        currentStartDate: '',
        endDate: '',
        currentEndDate: ''
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
                const layer = {
                    oid: GRAPH_LAYER,
                    forDate: this.form.endDate,
                    versionNumber: -1,
                };

                const list = {
                    oid: GRAPH_LAYER,
                    label: this.lService.decode('explorer.search.layer'),
                    versions: [layer],
                    open: false,
                }

                this.lists.unshift(list);

                this.toggleLayer(layer, list);
            }
            else {
                const index = this.lists.findIndex(v => v.oid === GRAPH_LAYER);

                if (index !== -1) {
                    const list = this.lists[index];
                    this.toggleLayer(list.versions[0], list);

                    this.lists.splice(index, 1);
                }
            }
        }
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
            const hasList = this.lists.filter(list => list.versions.findIndex(v => v.oid === layer) !== -1).length > 0;

            if (!hasList) {
                isSearchRequired = true;
            }
        });

        if (isSearchRequired) {
            // One of the enabled layers specified in the URL is not currently in the list/versions data model
            // As such we must do a new search for the valid list/versions in order to populate the option
            // into the data model. 
            // OR the search dates have been updated, so a new search must be performed.

            this.handleSearch().then(lists => {
                layers.forEach(oid => {
                    lists.forEach(list => {
                        list.versions.filter(v => v.oid === oid).forEach(v => {
                            this.toggleLayer(v, list);
                        });
                    })
                })
            });
        }
        else {
            // Determine if an existing version in the data model needs to be toggled on based on the state
            // of the URL 'layers' parameters
            layers.forEach(layer => {
                const index = this.layers.findIndex(l => l.oid === layer);

                if (index === -1) {
                    this.lists.forEach(list => {
                        list.versions.filter(v => v.oid === layer).forEach(v => {
                            this.toggleLayer(v, list);
                        });
                    });
                }
            });

            // Determine if any existing layers which need to be toggled off based on the state of the URL ''
            this.layers.filter(l => l.oid !== GRAPH_LAYER && layers.indexOf(l.oid) === -1).forEach(layer => {
                this.lists.forEach(list => {
                    list.versions.filter(v => v.oid === layer.oid).forEach(v => {
                        this.toggleLayer(v, list);
                    });
                })
            })
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
                })

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
                queryParamsHandling: 'merge'
            });

            this.handleSearch();
        }
        else {
            // A new search should null out any currently select layers and any record which has been clicked on
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { startDate: this.form.startDate, endDate: this.form.endDate, layers: null, version: null },
                queryParamsHandling: 'merge'
            });
        }
    }

    handleSearch(): Promise<ContextList[]> {
        // Remove all current lists
        this.lists.forEach(list => {
            list.versions.filter(v => v.enabled && v.oid !== GRAPH_LAYER).forEach(v => {
                this.toggleLayer(v, list);
            });
        });

        return this.service.getGeospatialVersions(this.form.startDate, this.form.endDate).then(lists => {

            this.form.currentStartDate = this.form.startDate;
            this.form.currentEndDate = this.form.endDate;

            this.lists = this.lists.filter(v => v.oid === GRAPH_LAYER).concat(lists);

            this.lists.forEach(list => {
                list.versions = list.versions.filter(v => this.filter.indexOf(v.oid) === -1);
            });

            return lists;
        });
    }

    onToggleLayer(layer: ContextLayer, list: ContextList): void {


        const index = this.layers.findIndex(l => l.oid === layer.oid);

        let layers = this.layers.filter(l => l.oid !== GRAPH_LAYER).map(l => l.oid);

        if (index === -1) {
            layers.unshift(layer.oid);
        }
        else {
            layers = layers.filter(l => l !== layer.oid);
        }


        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { layers: JSON.stringify(layers) },
            queryParamsHandling: 'merge', // remove to replace all query params by provided
        });
    }


    toggleLayer(layer: ContextLayer, list: ContextList): void {
        layer.enabled = !layer.enabled;
        layer.active = layer.enabled;

        if (layer.active && layer.color == null) {
            layer.color = ColorGen().hexString();
            layer.label = list.label;
        }

        let index: number = 0;

        if (layer.enabled) {

            if (layer.oid === GRAPH_LAYER && this.params.layers != null) {
                const i = JSON.parse(this.params.layers).indexOf(GRAPH_LAYER);

                if (i !== -1) {
                    index = i;
                }
            }

            this.layers.splice(index, 0, layer);
        }
        else {
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

    moveLayer(layer: ContextLayer, offset: number): void {
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
                queryParamsHandling: 'merge', // remove to replace all query params by provided
            });
        }
    }

}
