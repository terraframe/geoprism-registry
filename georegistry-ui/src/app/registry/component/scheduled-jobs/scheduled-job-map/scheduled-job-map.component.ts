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

import { Map, NavigationControl } from "maplibre-gl";

// eslint-disable-next-line no-unused-vars
import EnvironmentUtil from "@core/utility/environment-util";

@Component({
    selector: "scheduled-job-map",
    templateUrl: "./scheduled-job-map.component.html",
    styleUrls: ["./scheduled-job-map.component.css"],
    standalone: true
})

/**
 * This component is used when viewing change requests
 */
export class ScheduledJobMapComponent implements OnInit, OnDestroy {

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
    @Input() historyId: string;

    @ViewChild("mapDiv") mapDiv;

    map: Map;

    // eslint-disable-next-line no-useless-constructor
    constructor() { }

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
                                window.location.protocol + "//" + window.location.host + EnvironmentUtil.getApiUrl() + "/api/mapbox/v4/mapbox.satellite/{z}/{x}/{y}@2x.jpg90"
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
    }


    initMap(): void {
        // Add the layer
        this.map.addSource('features', {
            type: 'vector',
            tiles: [window.location.protocol + "//" + window.location.host + EnvironmentUtil.getApiUrl() + "/api/etl/tile?x={x}&y={y}&z={z}&historyId=" + encodeURIComponent(this.historyId)],
            promoteId: "uid"

        });
        this.map.addLayer({
            'id': 'polygon-layer',
            "type": "fill",
            'source': 'features',
            'source-layer': 'polygon',
            'paint': {
                "fill-color": "#0062AA",
                "fill-outline-color": "black",
                "fill-opacity": 0.7
            }
        });

        this.map.addLayer({
            'id': 'polygon-labels',
            'type': 'symbol',
            'source': 'features',
            'source-layer': 'polygon',
            layout: {
                "text-field": "{label}",
                "text-font": ["NotoSansRegular"],
                "text-offset": [0, 0.6],
                "text-anchor": "top",
                "text-size": 12
            },
            paint: {
                "text-color": "black",
                "text-halo-color": "#fff",
                "text-halo-width": 2
            },
        });

        this.map.addLayer({
            'id': 'point-layer',
            'source': 'features',
            'source-layer': 'point',
            type: "circle",
            paint: {
                "circle-radius": 13,
                "circle-color": "#33FFF9",
                "circle-stroke-width": 4,
                "circle-stroke-color": "white"
            }
        });

        this.map.addLayer({
            'id': 'point-labels',
            'type': 'symbol',
            'source': 'features',
            'source-layer': 'point',
            layout: {
                "text-field": "{label}",
                "text-font": ["NotoSansRegular"],
                "text-offset": [0, 0.6],
                "text-anchor": "top",
                "text-size": 12
            },
            paint: {
                "text-color": "black",
                "text-halo-color": "#fff",
                "text-halo-width": 2
            },
        });

        this.map.addLayer({
            'id': 'line-layer',
            'source': 'features',
            'source-layer': 'point',
            type: "line",
            layout: {
                "line-join": "round",
                "line-cap": "round"
            },
            paint: {
                "line-color": "#800000",
                "line-width": 3
            },
    });

        this.map.addLayer({
            'id': 'line-labels',
            'type': 'symbol',
            'source': 'features',
            'source-layer': 'point',
            layout: {
                "text-field": "{label}",
                "text-font": ["NotoSansRegular"],
                "text-offset": [0, 0.6],
                "text-anchor": "top",
                "text-size": 12
            },
            paint: {
                "text-color": "black",
                "text-halo-color": "#fff",
                "text-halo-width": 2
            },
        });

        // Add zoom and rotation controls to the map.
        this.map.addControl(new NavigationControl({}));
    }
    public error(err: HttpErrorResponse): void {
        // TODO
        console.log("ERROR", err);
    }

}
