import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Map, NavigationControl, AttributionControl, LngLatBounds, IControl } from 'mapbox-gl';
import MapboxDraw from '@mapbox/mapbox-gl-draw';

import { ContextLayer, GeoObjectType, ValueOverTime } from '@registry/model/registry';
import { MapService, RegistryService } from '@registry/service';
import { DateService } from '@shared/service/date.service';
import { AuthService } from '@shared/service';
import { ErrorHandler } from '@shared/component';
import { ConfirmModalComponent } from '@shared/component';
import { Subject } from 'rxjs';

import { LocalizationService } from '@shared/service';

declare var acp: string;

const DEFAULT_COLOR = "#80cdc1";
const SELECTED_COLOR = "#800000";

@Component({
	selector: 'dataset-location-manager',
	templateUrl: './dataset-location-manager.component.html',
	styleUrls: ['./dataset-location-manager.css']
})
export class DatasetLocationManagerComponent implements OnInit, AfterViewInit, OnDestroy {
	MODE = {
		VERSIONS: 'VERSIONS',
		ATTRIBUTES: 'ATTRIBUTES',
		HIERARCHY: 'HIERARCHY'
	}
	
	editSessionEnabled: boolean = false;
	toolsIconHover: boolean = false;
	datasetId: string;
	typeCode: string;
	readOnly: boolean = false;
	editOnly: boolean = false;
	isEdit: boolean = false;
	date: string;
	code: string;
	type: GeoObjectType;
	bsModalRef: BsModalRef;
	backReference: string;

    /* 
     * mapbox-gl map
     */
	map: Map;

	vectorLayers: string[] = [];

    /* 
     * List of base layers
     */
	baseLayers: any[] = [
		{
			name: 'Satellite',
			label: 'Satellite',
			id: 'satellite-v9',
			sprite: 'mapbox://sprites/mapbox/satellite-v9',
			url: 'mapbox://mapbox.satellite',
			selected: true
		},
//		 {
//		 	name: 'Streets',
//		 	label: 'Streets',
//		 	id: 'streets-v11',
//		 	sprite: 'mapbox://sprites/mapbox/basic-v11',
//		 	url: 'mapbox://styles/mapbox/streets-v11'
//		 }
	];


	mode: string = null;
	isMaintainer: boolean;
	forDate: Date = new Date();

	@ViewChild("simpleEditControl") simpleEditControl: IControl;
	editingControl: any;

	geometryChange: Subject<any> = new Subject();
	vot: ValueOverTime;

	constructor(private mapService: MapService, public service: RegistryService, private modalService: BsModalService, private route: ActivatedRoute, 
		authService: AuthService, private lService: LocalizationService, private dateService: DateService, private router: Router) {
			this.isMaintainer = authService.isAdmin() || authService.isMaintainer();
	}

	ngOnInit(): void {

		this.datasetId = this.route.snapshot.params["datasetId"];
		this.typeCode = this.route.snapshot.params["typeCode"];
		this.date = this.route.snapshot.params["date"];
		this.readOnly = this.route.snapshot.params["readOnly"] === 'true';
		this.editOnly = this.route.snapshot.params["editOnly"] === 'true';
		this.backReference = this.route.snapshot.params["backReference"];

		if (this.route.snapshot.params["code"] != null) {
			this.code = this.route.snapshot.params["code"];
		}

		this.forDate = this.dateService.getDateFromDateString(this.date) // new Date(Date.parse(this.date));

		this.service.getGeoObjectTypes([this.typeCode], null).then(types => {
			this.type = types[0];
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

	}
	
	onPanelCancel(): void {
	  if (this.backReference != null && this.backReference.length >= 2)
	  {
	    let ref = this.backReference.substring(0,2);
	    
	    if (ref === "ML")
	    {
	      let published = this.backReference.substring(3,3) === "T";
	      let oid = this.backReference.substring(3);
	    
	      this.router.navigate(['/registry/master-list', oid, published]);
	    }
	  }
  }
  
  onPanelSubmit(applyInfo: {isChangeRequest:boolean, geoObject?: any, changeRequestId?: string}): void {
    this.bsModalRef = this.modalService.show(ConfirmModalComponent, { backdrop: true, class:"error-white-space-pre" });
      
    if (applyInfo.isChangeRequest)
    {
      this.bsModalRef.content.message = this.lService.decode("geoobject-editor.changerequest.submitted");
      this.bsModalRef.content.submitText = this.lService.decode("geoobject-editor.cancel.returnList");
      this.bsModalRef.content.cancelText = this.lService.decode("geoobject-editor.changerequest.view");
    }
    else
    {
      this.bsModalRef.content.message = this.lService.decode("geoobject-editor.edit.submitted");
      this.bsModalRef.content.submitText = this.lService.decode("geoobject-editor.cancel.returnList");
      this.bsModalRef.content.cancelText = this.lService.decode("geoobject-editor.continueEditing");
    }
    
    this.bsModalRef.content.onConfirm.subscribe( () => {
      this.onPanelCancel();
    } );
    
    this.bsModalRef.content.onCancel.subscribe( () => {
      if (applyInfo.isChangeRequest)
      {
        this.router.navigate(['/registry/change-requests', applyInfo.changeRequestId]);
      }
      else
      {
        // do nothing
      }
    } );
  }

	ngOnDestroy(): void {
		this.map.remove();
	}

	ngAfterViewInit() {

		const layer = this.baseLayers[0];

		this.map = new Map({
			container: 'dataset-map',
			style: {
				"version": 8,
				"name": layer.name,
				"metadata": {
					"mapbox:autocomposite": true
				},
				"sources": {
					"mapbox-satellite": {
						"type": "raster",
						"url": layer.url,
						"tileSize": 256
					}
				},
				"sprite": layer.sprite,
				"glyphs": window.location.protocol + '//' + window.location.host + acp + '/glyphs/{fontstack}/{range}.pbf',
				"layers": [
//					{
//						"id": layer.id,
//						"type": 'raster',
//						"source": 'mapbox-satellite',
//					}
				]
			},
			zoom: 2,
			attributionControl: false,
			center: [-78.880453, 42.897852]
		});

		this.map.on('load', () => {
			this.initMap();
		});

		this.map.addControl(this.simpleEditControl);
	}

	onModeChange(value: boolean): void {
		this.isEdit = value;
	}

	initMap(): void {
	  if (this.code !== '__NEW__')
	  {
  		this.service.getGeoObjectBoundsAtDate(this.code, this.typeCode, this.date).then(bounds => {
	
			if(bounds){
	  			let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
	  
	  			let padding = 50;
	  			let maxZoom = 20;
	  
	  			// Zoom level was requested to be reduced when displaying point types as per #420
	  			if (this.type.geometryType === "POINT" || this.type.geometryType === "MULTIPOINT") {
	  				padding = 100;
	  				maxZoom = 12;
	  			}
	  
	  			this.map.fitBounds(llb, { padding: padding, animate: false, maxZoom: maxZoom });
			}
  		});
		}


		this.map.on('style.load', () => {
			this.addLayers();
		});

		this.addLayers();

		// Add zoom and rotation controls to the map.
		this.map.addControl(new NavigationControl({ 'visualizePitch': true }));
		this.map.addControl(new AttributionControl({ compact: true }), 'bottom-right');

		this.map.on('click', this.datasetId + '-points', (event: any) => {
			this.handleMapClickEvent(event);
		});

		this.map.on('click', this.datasetId + '-polygon', (event: any) => {
			this.handleMapClickEvent(event);
		});


		this.map.on('draw.selectionchange', (e: any) => {
			if(e.features.length > 0 || e.points.length > 0) {
				this.editSessionEnabled = true;
			}
			else {
				this.editSessionEnabled = false;
			}
		});

		this.addVectorLayer(this.datasetId);
	}

	addLayers(): void {
			
		this.map.addLayer({
			"type": "raster",
			"id": 'satellite-map',
			"source": "mapbox-satellite"
		});
			
		this.vectorLayers.forEach(vLayer => {
			this.addVectorLayer(vLayer);
		});
	}

	handleBasemapStyle(layer: any): void {

		if(layer.id === "streets-v11"){
			this.map.setStyle(layer.url);
		}
		else if(layer.id === "satellite-v9"){
			this.map.setStyle({
				"version": 8,
				"name": layer.name,
				"metadata": {
					"mapbox:autocomposite": true
				},
				"sources": {
					"mapbox-satellite": {
						"type": "raster",
						"url": layer.url,
						"tileSize": 256
					},
				},
				"sprite": layer.sprite,
				"glyphs": window.location.protocol + '//' + window.location.host + acp + '/glyphs/{fontstack}/{range}.pbf',
				"layers": [
					{
						"id": layer.id,
						"type": 'raster',
						"source": 'mapbox-satellite',
					}
				]
			});
		}
	}

	onContextLayerChange(layer: ContextLayer): void {

		if (layer.active) {
			this.addVectorLayer(layer.oid);
		}
		else {
			this.removeVectorLayer(layer.oid);
		}

	}

	removeVectorLayer(source: string): void {

		const index = this.vectorLayers.indexOf(source);

		if (index !== -1) {
			this.map.removeLayer(source + "-points");
			this.map.removeLayer(source + "-polygon");
			this.map.removeLayer(source + "-points-selected");
			this.map.removeLayer(source + "-polygon-selected");
			this.map.removeLayer(source + "-label");
			this.map.removeSource(source);

			this.vectorLayers.splice(index, 1);
		}

	}

	addVectorLayer(source: string): void {
		const index = this.vectorLayers.indexOf(source);

		if (index === -1) {
			const prevLayer = (source !== this.datasetId) ? this.datasetId + '-points' : null;

			var protocol = window.location.protocol;
			var host = window.location.host;

			this.map.addSource(source, {
				type: 'vector',
				tiles: [protocol + '//' + host + acp + '/master-list/tile?x={x}&y={y}&z={z}&config=' + encodeURIComponent(JSON.stringify({ oid: source }))]
			});

			// Point layer
			this.map.addLayer({
				"id": source + "-points",
				"type": "circle",
				"source": source,
				"source-layer": 'context',
				"paint": {
					"circle-radius": 10,
					"circle-color": DEFAULT_COLOR,
					"circle-stroke-width": 2,
					"circle-stroke-color": '#FFFFFF'
				},
				filter: ['all',
					["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
				]
			}, prevLayer);

			// Polygon layer
			this.map.addLayer({
				'id': source + '-polygon',
				'type': 'fill',
				'source': source,
				"source-layer": 'context',
				'layout': {},
				'paint': {
					'fill-color': DEFAULT_COLOR,
					'fill-opacity': 0.8,
					'fill-outline-color': 'black'
				},
				filter: ['all',
					["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
				]
			}, prevLayer);

			// Selected object layers
			if (source === this.datasetId) {
				this.map.addLayer({
					"id": source + "-points-selected",
					"type": "circle",
					"source": source,
					"source-layer": 'context',
					"paint": {
						"circle-radius": 10,
						"circle-color": SELECTED_COLOR,
						"circle-stroke-width": 2,
						"circle-stroke-color": '#FFFFFF'
					},
					filter: ['all',
						["==", ['get', 'code'], this.code != null ? this.code : ''],
						["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
					]
				}, prevLayer);

				this.map.addLayer({
					'id': source + '-polygon-selected',
					'type': 'fill',
					'source': source,
					"source-layer": 'context',
					'layout': {},
					'paint': {
						'fill-color': SELECTED_COLOR,
						'fill-opacity': 0.8,
						'fill-outline-color': 'black'
					},
					filter: ['all',
						["==", ['get', 'code'], this.code != null ? this.code : ''],
						["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
					]
				}, prevLayer);
			}

			// Label layer
			this.map.addLayer({
				"id": source + "-label",
				"source": source,
				"source-layer": 'context',
				"type": "symbol",
				"paint": {
					"text-color": "black",
					"text-halo-color": "#fff",
					"text-halo-width": 2
				},
				"layout": {
					"text-field": ["case",
						["has", "displayLabel_" + navigator.language.toLowerCase()],
						["coalesce", ["string", ["get", "displayLabel_" + navigator.language.toLowerCase()]], ["string", ["get", "displayLabel"]]],
						["string", ["get", "displayLabel"]]
					],
					"text-font": ["NotoSansRegular"],
					"text-offset": [0, 0.6],
					"text-anchor": "top",
					"text-size": 12,
				}
			}, prevLayer);


			this.vectorLayers.push(source);
		}
	}


	/*
	 * EDIT FUNCTIONALITY
	 */

	clearGeometryEditing(): void {
		if (this.editingControl != null) {
			this.editingControl.deleteAll();
			this.map.removeControl(this.editingControl);
		}

		this.editingControl = null;
		
		this.editSessionEnabled = false;
	}

	onFeatureChange(): void {
		// Refresh the layer
		this.removeVectorLayer(this.datasetId);
		this.addVectorLayer(this.datasetId);
	}

	handleMapClickEvent(event: any): void {
		if (!this.isEdit && event.features != null && event.features.length > 0) {
			const feature = event.features[0];

			if (feature.properties.code != null && this.code !== feature.properties.code) {
				this.code = feature.properties.code;

				// Update the filter properties
				this.map.setFilter(this.datasetId + '-points-selected', ['all',
					["==", ['get', 'code'], this.code != null ? this.code : ''],
					["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
				]);

				this.map.setFilter(this.datasetId + '-polygon-selected', ['all',
					["==", ['get', 'code'], this.code != null ? this.code : ''],
					["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
				]);
			}
		}
	}

	onMapSave(): void {
		const geometry = this.getDrawGeometry();

		this.editingControl.deleteAll();
		this.map.removeControl(this.editingControl);
		this.geometryChange.next(geometry);

		this.vot.value = geometry;
//		this.vot = null;

		this.editingControl = null;
		
		this.editSessionEnabled = false;
	}

	onGeometryEdit(vot: ValueOverTime): void {
		this.clearGeometryEditing();

		this.vot = vot;

		this.addEditLayers(vot);
	}

	//	onMapEdit(): void {
	//		// Enable editing
	//		if (this.editingControl == null) {
	//			this.addEditLayers();
	//		}
	//	}

	addEditLayers(vot: ValueOverTime): void {
		if (vot != null) {
			this.enableEditing(vot);
		}
	}

	enableEditing(vot: ValueOverTime): void {
		if (this.type.geometryType === "MULTIPOLYGON" || this.type.geometryType === "POLYGON") {
			this.editingControl = new MapboxDraw({
				controls: {
					point: false,
					line_string: false,
					polygon: true,
					trash: true,
					combine_features: false,
					uncombine_features: false
				}
			});
		}
		else if (this.type.geometryType === "POINT" || this.type.geometryType === "MULTIPOINT") {
			this.editingControl = new MapboxDraw({
				controls: {
					point: true,
					line_string: false,
					polygon: false,
					trash: true,
					combine_features: false,
					uncombine_features: false
				}
			});
		}
		else if (this.type.geometryType === "LINE" || this.type.geometryType === "MULTILINE") {
			this.editingControl = new MapboxDraw({
				controls: {
					point: false,
					line_string: true,
					polygon: false,
					trash: true,
					combine_features: false,
					uncombine_features: false
				}
			});
		}
		this.map.addControl(this.editingControl);

		if (vot.value != null) {
			this.editingControl.add(vot.value);
		}
	}


	getDrawGeometry(): any {
		if (this.editingControl != null) {
			let featureCollection: any = this.editingControl.getAll();

			if (featureCollection.features.length > 0) {

				// The first Feature is our GeoObject.

				// Any additional features were created using the draw editor. Combine them into the GeoObject if its a multi-polygon.
				if (this.type.geometryType === "MULTIPOLYGON") {
					let polygons = [];

					for (let i = 0; i < featureCollection.features.length; i++) {
						let feature = featureCollection.features[i];

						if (feature.geometry.type === 'MultiPolygon') {
							for (let j = 0; j < feature.geometry.coordinates.length; j++) {
								polygons.push(feature.geometry.coordinates[j]);
							}
						}
						else {
							polygons.push(feature.geometry.coordinates);
						}
					}

					return {
						coordinates: polygons,
						type: 'MultiPolygon'
					};
				}
				else if (this.type.geometryType === "MULTIPOINT") {
					let points = [];

					for (let i = 0; i < featureCollection.features.length; i++) {
						let feature = featureCollection.features[i];

						if (feature.geometry.type === 'MultiPoint') {
							for (let j = 0; j < feature.geometry.coordinates.length; j++) {
								points.push(feature.geometry.coordinates[j]);
							}
						}
						else {
							points.push(feature.geometry.coordinates);
						}
					}

					return {
						coordinates: points,
						type: 'MultiPoint'
					};
				}
				else if (this.type.geometryType === "MULTILINE") {
					let lines = [];

					for (let i = 0; i < featureCollection.features.length; i++) {
						let feature = featureCollection.features[i];

						if (feature.geometry.type === 'MultiLineString') {
							for (let j = 0; j < feature.geometry.coordinates.length; j++) {
								lines.push(feature.geometry.coordinates[j]);
							}
						}
						else {
							lines.push(feature.geometry.coordinates);
						}
					}

					return {
						coordinates: lines,
						type: 'MultiLineString'
					};
				}
				else {
					return featureCollection.features[0].geometry;
				}
			}
		}

		return null;
	}

	onNewGeoObject(): void {
		this.code = '__NEW__';
	}
	
	formatDate(date: Date): string {
		return this.dateService.formatDateForDisplay(date);
	}


	public error(err: HttpErrorResponse): void {
		this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
	}

}
