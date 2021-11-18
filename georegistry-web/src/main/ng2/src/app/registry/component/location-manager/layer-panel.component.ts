import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";

import { ContextLayer, ContextLayerGroup } from "@registry/model/registry";
import { ContextLayerModalComponent } from "./context-layer-modal.component";
import { RegistryService } from "@registry/service";
import { ContextList, ListType, ListTypeVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";

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

    contextLayerGroups: ContextLayerGroup[] = [];

    // eslint-disable-next-line no-useless-constructor
    constructor(private modalService: BsModalService, public service: ListTypeService) { }

    ngOnInit(): void {
        this.service.getGeospatialVersions().then(lists => {
            this.lists = lists;

            this.updateContextGroups();
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.filter.currentValue !== changes.filter.previousValue) {
            this.updateContextGroups();
        }
    }

    updateContextGroups(): void {
        this.lists.forEach(list => {
            let contextGroup = { oid: list.oid, displayLabel: list.label, contextLayers: [] };

            list.versions.forEach(version => {
                const index = this.filter.indexOf(version.oid);

                if (index === -1) {
                    let thisContextLayer = {
                        oid: version.oid,
                        displayLabel: version.forDate,
                        versionNumber: version.versionNumber,
                        active: false,
                        enabled: false
                    };

                    contextGroup.contextLayers.push(thisContextLayer);
                }
            });

            if (contextGroup.contextLayers.length > 0) {
                this.contextLayerGroups.push(contextGroup);
            }
        });
    }

    groupHasEnabledContextLayers(group: string): boolean {
        let hasEnabled = false;
        this.contextLayerGroups.forEach(cLayerGroup => {
            if (cLayerGroup.oid === group) {
                cLayerGroup.contextLayers.forEach(cLayer => {
                    if (cLayer.enabled) {
                        hasEnabled = true;
                    }
                });
            }
        });

        return hasEnabled;
    }

    hasEnabledContextLayers(): boolean {
        let hasEnabled = false;
        this.contextLayerGroups.forEach(cLayerGroup => {
            cLayerGroup.contextLayers.forEach(cLayer => {
                if (cLayer.enabled) {
                    hasEnabled = true;
                }
            });
        });

        return hasEnabled;
    }

    toggleContextLayer(layer: ContextLayer): void {
        layer.active = !layer.active;

        this.layerChange.emit(layer);
    }

    removeContextLayer(layer: ContextLayer): void {
        layer.active = false;
        layer.enabled = false;

        this.layerChange.emit(layer);
    }

    addContextLayerModal(): void {
        let bsModalRef = this.modalService.show(ContextLayerModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            // eslint-disable-next-line quote-props
            "class": "context-layer-modal"
        });
        bsModalRef.content.contextLayerGroups = this.contextLayerGroups;

        //        bsModalRef.content.onSubmit.subscribe(() => {
        //
        //            this.contextLayerGroups.forEach(cLayerGroup => {
        //                cLayerGroup.contextLayers.forEach(cLayer => {
        //
        //                    console.log("Emitting event", cLayer);
        //                    this.layerChange.emit(cLayer);
        //                });
        //            })
        //
        //        });
    }

    toggleBaseLayer(layer: any): void {
        this.baseLayers.forEach(bl => {
            bl.active = false;
        });

        layer.active = true;

        this.baseLayerChange.emit(layer);
    }

}
