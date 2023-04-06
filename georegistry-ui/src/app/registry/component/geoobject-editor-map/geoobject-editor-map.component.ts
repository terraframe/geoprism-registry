///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnInit, ViewChild, OnDestroy, Input } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";

import { GeometryService } from "@registry/service";

import { Map, NavigationControl } from "maplibre-gl";

// eslint-disable-next-line no-unused-vars
import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';
import { ConfigurationService } from "@core/service/configuration.service";
import EnvironmentUtil from "@core/utility/environment-util";

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
    constructor(private geomService: GeometryService, private configuration: ConfigurationService) { }

    ngOnInit(): void {
    }

    ngAfterViewInit() {
        setTimeout(() => {
            this.mapDiv.nativeElement.id = Math.floor(Math.random() * (899999)) + 100000;

            this.map = new Map({
                container: this.mapDiv.nativeElement.id,
                style: {
                    'version': 8,                                
                    'sources': {
                        'base-raster': {
                            'type': 'raster',
                            'tiles': [
                                'https://api.mapbox.com/v4/mapbox.satellite/{z}/{x}/{y}@2x.jpg90?access_token=' + this.configuration.getMapboxAccessToken()
                            ],
                            'tileSize': 512,
                        }
                    },
                    "glyphs": window.location.protocol + "//" + window.location.host + EnvironmentUtil.getApiUrl() + "/glyphs/{fontstack}/{range}.pbf",
                    'layers': [
                        {
                            'id': 'base-layer',
                            'type': 'raster',
                            'source': 'base-raster',
                            'minzoom': 0,
                            'maxzoom': 22
                        }
                    ]

                },
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

        this.geomService.initialize(this.map, this.geometryType, false);

        // Add zoom and rotation controls to the map.
        this.map.addControl(new NavigationControl({}));

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
