import { Component, OnInit, ViewChild, OnDestroy, Input } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService, MapService, GeometryService } from "@registry/service";

import { Map, NavigationControl } from "mapbox-gl";

// eslint-disable-next-line no-unused-vars
declare let acp: string;

@Component({
    selector: "geoobject-editor-map[geometryType]",
    templateUrl: "./geoobject-editor-map.component.html",
    styleUrls: ["./geoobject-editor-map.component.css"]
})

/**
 * This component is used when viewing change requests
 */
export class GeoObjectEditorMapComponent implements OnInit, OnDestroy {

    _mapHeight: number = 400;
    // eslint-disable-next-line accessor-pairs
    @Input() set mapHeight(height: number) {
        if (height > 400) {
            this._mapHeight = height;
        }
    }

    /*
     * Required. The GeometryType of the GeoJSON. Expected to be in uppercase (because that's how it is in the GeoObjectType for some reason)
     */
    @Input() geometryType: string;

    /*
     * Optional. If specified, we will fetch the bounding box from this GeoObject code.
     */
    @Input() bboxCode: string;

    /*
     * Optional. If specified, we will fetch the bounding box from this GeoObjectType at the date.
     */
    @Input() bboxType: string;

    @Input() bboxDate: string;

    /*
     * Optional. If set to true the edit controls will not be displayed. Defaults to false.
     */
    @Input() readOnly: boolean = false;

    @ViewChild("mapDiv") mapDiv;

    map: Map;

    // eslint-disable-next-line no-useless-constructor
    constructor(private geomService: GeometryService, private registryService: RegistryService, private mapService: MapService) { }

    ngOnInit(): void {
    }

    ngAfterViewInit() {
        setTimeout(() => {
            this.mapDiv.nativeElement.id = Math.floor(Math.random() * (899999)) + 100000;

            this.map = new Map({
                container: this.mapDiv.nativeElement.id,
                style: "mapbox://styles/mapbox/satellite-v9",
                zoom: 2,
                center: [110.880453, 10.897852]
            });

            this.map.on("load", () => {
                this.initMap();
            });
        }, 0);
    }

    ngOnDestroy(): void {
        this.geomService.destroy();
    }

    getIsValid(): boolean {
        return this.geomService.isValid();
    }

    initMap(): void {
        this.map.on("style.load", () => {
            // this.addLayers();
            // this.geomService.initialize(this.map, this.geometryType, this.readOnly);
        });

        this.geomService.initialize(this.map, this.geometryType, this.readOnly);

        // Add zoom and rotation controls to the map.
        this.map.addControl(new NavigationControl());

        this.zoomToBbox();
    }

    zoomToBbox(): void {
        this.geomService.zoomToLayersExtent();
    }

    public error(err: HttpErrorResponse): void {
        // TODO
        console.log("ERROR", err);
    }

}
