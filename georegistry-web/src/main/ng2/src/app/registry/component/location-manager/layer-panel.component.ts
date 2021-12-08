import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, OnDestroy } from "@angular/core";
import { ActivatedRoute } from "@angular/router";

import { ContextLayer, ContextList } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import * as ColorGen from "color-generator";
import { Subscription } from "rxjs";

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


    // eslint-disable-next-line no-useless-constructor
    constructor(
        private route: ActivatedRoute,
        public service: ListTypeService) { }

    ngOnInit(): void {

        this.subscription = this.route.params.subscribe((params: any) => {
            if (params.version != null) {
                this.confirm().then(lists => {
                    lists.forEach(list => {
                        list.versions.filter(v => v.oid === params.version).forEach(v => {
                            this.toggleLayer(v, list);
                        });
                    })
                });
            }
        });
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    ngOnChanges(changes: SimpleChanges) {

        if (changes.includeGraphLayer != null) {
            if (changes.includeGraphLayer.currentValue) {
                const layer = {
                    oid: 'graph',
                    forDate: this.form.endDate,
                    versionNumber: -1,
                };

                const list = {
                    oid: 'graph',
                    label: 'Search Results',
                    versions: [layer],
                    open: false,
                }

                this.lists.unshift(list);

                this.toggleLayer(layer, list);
            }
            else {       
                const index = this.lists.findIndex(v => v.oid === 'graph');

                if(index !== -1) {
                    const list = this.lists[index];
                    this.toggleLayer(list.versions[0], list);

                    this.lists.splice(index, 1);
                }
            }
        }
    }

    confirm(): Promise<ContextList[]> {
        // Remove all current lists
        this.lists.forEach(list => {
            list.versions.filter(v => v.enabled && v.oid !== 'graph').forEach(v => {
                this.toggleLayer(v, list);
            });
        });

        return this.service.getGeospatialVersions(this.form.startDate, this.form.endDate).then(lists => {
        
            this.lists = this.lists.filter(v => v.oid === 'graph').concat(lists);

            this.lists.forEach(list => {
                list.versions = list.versions.filter(v => this.filter.indexOf(v.oid) === -1);
            });

            return lists;
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
