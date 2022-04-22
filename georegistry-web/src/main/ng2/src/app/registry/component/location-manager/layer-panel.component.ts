import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";

import { ContextLayer, ContextList, ListOrgGroup, ListVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { LocalizationService } from "@shared/service";
import * as ColorGen from "color-generator";
import { Subscription } from "rxjs";

import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";
import { PANEL_SIZE_STATE } from "@registry/model/location-manager";
import { NgxSpinnerService } from "ngx-spinner";
import { OverlayerIdentifier } from "@registry/model/constants";

export const SEARCH_LAYER = "search";

export interface LayerEvent {

    layer: ContextLayer;
    prevLayer?: ContextLayer;

}

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
        SEARCH_LAYER: SEARCH_LAYER,
        OVERLAY: OverlayerIdentifier.LAYER_PANEL
    }

    @Input() filter: string[] = [];
    @Input() includeSearchLayer: boolean = false;
    @Input() visualizeMode: number;

    @Output() baseLayerChange = new EventEmitter<BaseLayer>();
    @Output() zoomTo = new EventEmitter<ContextLayer>();
    @Output() create = new EventEmitter<ContextLayer>();

    @Input() panelSize: number = PANEL_SIZE_STATE.MINIMIZED;
    @Output() panelSizeChange = new EventEmitter<number>();

    layerVersionMap = {};

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
            queryParamsHandling: "merge"
        });
    }

    togglePanelOpen() {
        this.setPanelSize(this.panelSize === 0 ? 1 : 0);
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

        const layers: ContextLayer[] = this.params.layers != null ? JSON.parse(this.params.layers) : [];
        this.layers = layers;

        layers.forEach(layer => {
            if (layer.oid !== SEARCH_LAYER && !layer.oid.startsWith("GRAPH-") && this.findVersionById(layer.oid) == null) {
                isSearchRequired = true;
            }
        });

        if (isSearchRequired) {
            // One of the enabled layers specified in the URL is not currently in the list/versions data model
            // As such we must do a new search for the valid list/versions in order to populate the option
            // into the data model.
            // OR the search dates have been updated, so a new search must be performed.

            this.handleSearch();
        }

        this.refreshListLayerReferences();
    }

    onConfirm(): void {
        if (this.params.startDate == null && this.params.endDate == null && this.params.layers == null && this.form.startDate === null && this.form.endDate === null) {
            // A new search should null out any record which has been clicked on
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { version: null },
                queryParamsHandling: "merge"
            });

            this.handleSearch();
        } else {
            // A new search should null out any record which has been clicked on
            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { startDate: this.form.startDate, endDate: this.form.endDate, version: null },
                queryParamsHandling: "merge"
            });
        }
    }

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
        this.layerVersionMap = {};

        this.listOrgGroups.forEach(listOrgGroup => {
            listOrgGroup.types.forEach(listTypeGroup => {
                listTypeGroup.lists.forEach(list => {
                    list.versions = list.versions.filter(v => this.filter.indexOf(v.oid) === -1);

                    for (let i = 0; i < list.versions.length; ++i) {
                        let version = list.versions[i];

                        let layerIndex = this.layers.findIndex(l => l.oid === version.oid);
                        if (layerIndex !== -1) {
                            let layer = this.layers[layerIndex];
                            version.layer = layer;
                            this.layerVersionMap[layer.oid] = layer;
                        }
                    }
                });
            });
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

    clickToggleLayerRendered(layer: ContextLayer, list: ContextList) {
        this.toggleLayerRendered(layer);
    }

    clickToggleLayerShowOnLegend(layer: ContextLayer, list: ContextList): void {
        const index = this.layers.findIndex(l => l.oid === layer.oid);

        if (index === -1) {
            layer.rendered = true;
            layer.showOnLegend = true;
            layer.label = list.label;
            layer.color = ColorGen().hexString();
            this.layers.push(layer);
        } else {
            this.layers = this.layers.filter(l => l.oid !== layer.oid);
        }

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { layers: JSON.stringify(this.layers) },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    toggleLayerRendered(layer: ContextLayer): void {
        layer.rendered = !layer.rendered;

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { layers: JSON.stringify(this.layers) },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    onGotoBounds(layer: ContextLayer): void {
        this.zoomTo.emit(layer);
    }

    onCreate(layer: ContextLayer): void {
        this.create.emit(layer);
    }

    toggleBaseLayer(layer: BaseLayer): void {
        this.baseLayers.forEach(bl => {
            bl.selected = false;
        });

        layer.selected = true;

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

    moveLayer(newLayers: ContextLayer[]): void {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { layers: JSON.stringify(newLayers) },
            queryParamsHandling: "merge" // remove to replace all query params by provided
        });
    }

    drop(event: CdkDragDrop<string[]>) {
        let oldLayers = JSON.parse(JSON.stringify(this.layers));
        moveItemInArray(oldLayers, event.previousIndex, event.currentIndex);
        this.moveLayer(oldLayers);
    }

}
