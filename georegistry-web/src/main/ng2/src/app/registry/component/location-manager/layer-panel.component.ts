import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from "@angular/core";

import { ContextLayer, ContextList } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import * as ColorGen from "color-generator";

export class LayerEvent {
    layer: ContextLayer;
    prevLayer?: ContextLayer;
}

@Component({
    selector: "layer-panel",
    templateUrl: "./layer-panel.component.html",
    styleUrls: ["./location-manager.css"]
})
export class LayerPanelComponent implements OnInit, OnChanges {

    @Input() filter: string[] = [];
    @Output() layerChange = new EventEmitter<LayerEvent>();
    @Output() baseLayerChange = new EventEmitter<any>();
    @Output() reorder = new EventEmitter<ContextLayer[]>();
    @Output() zoomTo = new EventEmitter<ContextLayer>();

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

    // eslint-disable-next-line no-useless-constructor
    constructor(public service: ListTypeService) { }

    ngOnInit(): void {
    }

    ngOnChanges(changes: SimpleChanges) {
    }

    confirm(): void {
        // Remove all current lists
        this.lists.forEach(list => {
            list.versions.filter(v => v.enabled).forEach(v => {
                this.toggleContextLayer(v, list);
            });
        });

        this.service.getGeospatialVersions(this.form.startDate, this.form.endDate).then(lists => {
            this.lists = lists;

            this.lists.forEach(list => {
                list.versions = list.versions.filter(v => this.filter.indexOf(v.oid) === -1);
            });
        });
    }


    toggleContextLayer(layer: ContextLayer, list: ContextList): void {
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
