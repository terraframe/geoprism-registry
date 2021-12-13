import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, OnDestroy } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";

import { ContextLayer, ContextList } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
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

    form: { startDate: string, endDate: string } = {
        startDate: '',
        endDate: ''
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
        public service: ListTypeService) { }

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
                    label: 'Search Results',
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

    handleParams(): void {

        let isSearchRequired = false;

        if (this.params.startDate != null && this.params.startDate !== this.form.startDate) {
            this.form.startDate = this.params.startDate;

            isSearchRequired = true;
        }

        if (this.params.endDate != null && this.params.endDate !== this.form.endDate) {
            this.form.endDate = this.params.endDate;

            isSearchRequired = true;
        }

        let layers = [];

        if (this.params.layers != null) {
            if (Array.isArray(this.params.layers)) {
                layers = this.params.layers;
            } else {
                layers = [this.params.layers];
            }
        }

        layers.forEach(layer => {
            const hasList = this.lists.filter(list => list.versions.findIndex(v => v.oid === layer) !== -1).length > 0;

            if (!hasList) {
                isSearchRequired = true;
            }
        });

        if (isSearchRequired) {
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
            // Determine if a layer needs to be toggled on
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

            // Determine existing layers which need to be toggled off
            this.layers.filter(l => l.oid !== GRAPH_LAYER && layers.indexOf(l.oid) === -1).forEach(layer => {
                this.lists.forEach(list => {
                    list.versions.filter(v => v.oid === layer.oid).forEach(v => {
                        this.toggleLayer(v, list);
                    });
                })
            })
        }
    }

    onConfirm(): void {

        if (this.params.startDate == null && this.params.endDate == null && this.params.layers == null && this.form.startDate === null && this.form.endDate === null) {
            this.handleSearch();
        }
        else {
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { startDate: this.form.startDate, endDate: this.form.endDate, layers: null },
                queryParamsHandling: 'merge', // remove to replace all query params by provided
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

            this.lists = this.lists.filter(v => v.oid === GRAPH_LAYER).concat(lists);

            this.lists.forEach(list => {
                list.versions = list.versions.filter(v => this.filter.indexOf(v.oid) === -1);
            });

            return lists;
        });
    }

    onToggleLayer(layer: ContextLayer, list: ContextList): void {


        const index = this.layers.findIndex(l => l.oid === layer.oid);

        const layers = index === -1 ? this.layers.filter(l => l.oid !== GRAPH_LAYER).map(l => l.oid).concat(layer.oid) :
            this.layers.filter(l => l.oid !== GRAPH_LAYER && l.oid !== layer.oid).map(l => l.oid);

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { layers: layers },
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

        if (layer.enabled) {
            this.layers.unshift(layer);
        }
        else {
            const index = this.layers.findIndex(l => l.oid === layer.oid);

            if (index !== -1) {
                this.layers.splice(index, 1);
            }
        }

        this.layerChange.emit({ layer: layer });
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
            const a = this.layers[index];
            this.layers[index] = this.layers[index + offset];
            this.layers[index + offset] = a;

            this.reorder.emit(this.layers);
        }
    }

}
