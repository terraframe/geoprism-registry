import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from "@angular/core";

import { ContextLayer, ContextList } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import * as ColorGen from "color-generator";

@Component({
    selector: "layer-panel",
    templateUrl: "./layer-panel.component.html",
    styleUrls: ["./location-manager.css"]
})
export class LayerPanelComponent implements OnInit, OnChanges {

    @Input() filter: string[] = [];
    @Output() layerChange = new EventEmitter<ContextLayer>();
    @Output() baseLayerChange = new EventEmitter<any>();

    baselayerIconHover = false;

    lists: ContextList[] = [];

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
            list.versions.filter(v => v.active).forEach(v => {
                this.toggleContextLayer(v);
            });
        });


        this.service.getGeospatialVersions(this.form.startDate, this.form.endDate).then(lists => {
            this.lists = lists;
        });
    }


    toggleContextLayer(layer: ContextLayer): void {
        layer.active = !layer.active;

        if (layer.active && layer.color == null) {
            layer.color = ColorGen().hexString();
        }

        this.layerChange.emit(layer);
    }


    toggleBaseLayer(layer: any): void {
        this.baseLayers.forEach(bl => {
            bl.active = false;
        });

        layer.active = true;

        this.baseLayerChange.emit(layer);
    }

}
