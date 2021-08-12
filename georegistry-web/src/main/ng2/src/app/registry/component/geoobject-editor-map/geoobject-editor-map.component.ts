import { Component, OnInit, ViewChild, SimpleChanges, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService, MapService, GeometryService} from '@registry/service';

import { Map, LngLatBounds, NavigationControl } from 'mapbox-gl';
import MapboxDraw from '@mapbox/mapbox-gl-draw';

declare var acp: string;


@Component({
	selector: 'geoobject-editor-map[geometryType]',
	templateUrl: './geoobject-editor-map.component.html',
	styleUrls: ['./geoobject-editor-map.component.css']
})

/**
 * This component is used when viewing change requests
 */
export class GeoObjectEditorMapComponent implements OnInit, OnDestroy {

    @Input() mapHeight: number = 400;

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

	constructor(private geomService: GeometryService, private registryService: RegistryService, private mapService: MapService) {

	}

	ngOnInit(): void {
	}

	ngAfterViewInit() {
		setTimeout(() => {

			this.mapDiv.nativeElement.id = Math.floor(Math.random() * (899999)) + 100000;

			this.map = new Map({
				container: this.mapDiv.nativeElement.id,
				style: 'mapbox://styles/mapbox/satellite-v9',
				zoom: 2,
				center: [110.880453, 10.897852]
			});

			this.map.on('load', () => {
				this.initMap();
			});
			
		}, 0);
	}

	ngOnDestroy(): void {
		this.map.remove();
		this.geomService.destroy();
	}

	getIsValid(): boolean {
		return this.geomService.isValid();
	}

	initMap(): void {

		this.map.on('style.load', () => {
			//this.addLayers();
			//this.geomService.initialize(this.map, this.geometryType, this.readOnly);
		});

    this.geomService.initialize(this.map, this.geometryType, this.readOnly);

		// Add zoom and rotation controls to the map.
		this.map.addControl(new NavigationControl());
		
		this.zoomToBbox();
	}

	zoomToBbox(): void {
		if (this.bboxCode != null && this.bboxType != null) {
			if (this.bboxDate == null) {
				this.registryService.getGeoObjectBounds(this.bboxCode, this.bboxType).then(boundArr => {
					let bounds = new LngLatBounds([boundArr[0], boundArr[1]], [boundArr[2], boundArr[3]]);

					this.map.fitBounds(bounds, { padding: 50 });
				}).catch((err: HttpErrorResponse) => {
					this.error(err);
				});
			}
			else {
				this.registryService.getGeoObjectBoundsAtDate(this.bboxCode, this.bboxType, this.bboxDate).then(boundArr => {
					let bounds = new LngLatBounds([boundArr[0], boundArr[1]], [boundArr[2], boundArr[3]]);

					this.map.fitBounds(bounds, { padding: 50 });
				}).catch((err: HttpErrorResponse) => {
					this.error(err);
				});
			}
		}
	}

	public error(err: HttpErrorResponse): void {
		// TODO
		console.log("ERROR", err);
	}


}
