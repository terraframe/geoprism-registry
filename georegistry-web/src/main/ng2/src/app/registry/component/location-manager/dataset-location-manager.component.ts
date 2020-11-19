import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Map, NavigationControl, AttributionControl, LngLatBounds, IControl } from 'mapbox-gl';
import MapboxDraw from "@mapbox/mapbox-gl-draw";

import { ContextLayer, GeoObjectType, GeoObjectOverTime, Attribute } from '@registry/model/registry';
import { MapService, RegistryService } from '@registry/service';
import { LocalizationService, AuthService } from '@shared/service';
import { ErrorModalComponent, ErrorHandler } from '@shared/component';
import { GeoObjectEditorComponent } from '../geoobject-editor/geoobject-editor.component';

declare var acp: string;

@Component({
	selector: 'dataset-location-manager',
	templateUrl: './dataset-location-manager.component.html',
	styleUrls: ['./dataset-location-manager.css']
})
export class DatasetLocationManagerComponent implements OnInit, AfterViewInit, OnDestroy {

	datasetId: string;

	typeCode: string;

	date: string;

	code: string;

	type: GeoObjectType;

	bsModalRef: BsModalRef;

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
		// {
		// 	name: 'Streets',
		// 	label: 'Streets',
		// 	id: 'streets-v9',
		// 	sprite: 'mapbox://sprites/mapbox/basic-v9',
		// 	url: 'mapbox://mapbox.basic-v9'
		// }
	];


	mode: string = null;

	@ViewChild("simpleEditControl") simpleEditControl: IControl;
	editingControl: any;

	forDate: Date = new Date();

	// The current state of the GeoObject in the GeoRegistry
	preGeoObject: GeoObjectOverTime;

	// The state of the GeoObject after our edit has been applied
	postGeoObject: GeoObjectOverTime;

	calculatedPreObject: any = {};

	calculatedPostObject: any = {};

	attribute: Attribute = null;

	readOnly: boolean = true;

	isMaintainer: boolean;


	constructor(private mapService: MapService, public service: RegistryService, private modalService: BsModalService, private route: ActivatedRoute, authService: AuthService) {
		this.isMaintainer = authService.isAdmin() || authService.isMaintainer();
	}

	ngOnInit(): void {
		this.mapService.init();

		this.datasetId = this.route.snapshot.params["datasetId"];
		this.typeCode = this.route.snapshot.params["typeCode"];
		this.date = this.route.snapshot.params["date"];
		this.forDate = new Date(Date.parse(this.date));

		this.service.getGeoObjectTypes([this.typeCode], null).then(types => {
			this.type = types[0];
		}).catch((err: HttpErrorResponse) => {
			console.log(err);
			//                this.error( err );
		});

	}

	ngOnDestroy(): void {
		this.map.remove();
	}

	ngAfterViewInit() {

		const layer = this.baseLayers[0];

		this.map = new Map({
			container: 'map',
			style: {
				"version": 8,
				"name": layer.name,
				"metadata": {
					"mapbox:autocomposite": true
				},
				"sources": {
					"mapbox": {
						"type": "raster",
						"url": layer.url,
						"tileSize": 256
					}
				},
				"sprite": layer.sprite,
				"glyphs": window.location.protocol + '//' + window.location.host + acp + '/glyphs/{fontstack}/{range}.pbf',
				"layers": [
					{
						"id": layer.id,
						"type": 'raster',
						"source": 'mapbox',
						// "source-layer": "mapbox_satellite_full"
					}
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

	handleDateChange(): void {
		//		this.back(null);
	}

	initMap(): void {
		this.service.getDatasetBounds(this.datasetId).then(bounds => {
			let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);

			this.map.fitBounds(llb, { padding: 50 });
		})

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

		this.addVectorLayer(this.datasetId);
	}

	addLayers(): void {

		this.vectorLayers.forEach(vLayer => {
			this.addVectorLayer(vLayer);
		});
	}

	handleBasemapStyle(layer: any): void {
		// this.map.setStyle('mapbox://styles/mapbox/' + layer.id);

		this.baseLayers.forEach(baseLayer => {
			baseLayer.selected = false;
		});

		layer.selected = true;

		this.map.setStyle({
			"version": 8,
			"name": layer.name,
			"metadata": {
				"mapbox:autocomposite": true
			},
			"sources": {
				"mapbox": {
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
					"source": 'mapbox',
					// "source-layer": "mapbox_satellite_full"
				}
			]
		});
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
					"circle-color": '#800000',
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
					'fill-color': '#80cdc1',
					'fill-opacity': 0.8,
					'fill-outline-color': 'black'
				},
				filter: ['all',
					["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
				]
			}, prevLayer);


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

	handleMapClickEvent(event: any): void {
		if (event.features != null && event.features.length > 0) {
			const feature = event.features[0];

			if (feature.properties.code != null && this.code !== feature.properties.code) {
				this.code = feature.properties.code;
				this.readOnly = true;

				this.service.getGeoObjectOverTime(this.code, this.typeCode).then(geoObject => {
					this.preGeoObject = new GeoObjectOverTime(this.type, JSON.parse(JSON.stringify(geoObject)).attributes);
					this.postGeoObject = new GeoObjectOverTime(this.type, JSON.parse(JSON.stringify(this.preGeoObject)).attributes);

					this.calculate();
					this.mode = 'ATTRIBUTES';
				})


				//				let editModal = this.modalService.show(GeoObjectEditorComponent, { backdrop: true, ignoreBackdropClick: true });
				//				editModal.content.configureAsExisting(this.code, this.typeCode, this.date, false);
				//				editModal.content.setMasterListId(this.datasetId);
			}
		}
	}

	calculate(): void {
		this.calculatedPreObject = this.calculateCurrent(this.preGeoObject);
		this.calculatedPostObject = this.calculateCurrent(this.postGeoObject);
	}


	onMapEdit(): void {
		// Enable editing
		if (this.editingControl == null) {
			this.addEditLayers();
		}
	}

	addEditLayers(): void {
		if (this.calculatedPreObject.geometry != null) {
			//			this.renderGeometryAsLayer(this.calculatedPreObject.geometry.value, "pre", "#EFA22E");

			this.enableEditing();
		}
	}

	enableEditing(): void {
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

		if (this.calculatedPreObject.geometry != null) {
			this.editingControl.add(this.calculatedPreObject.geometry.value);
		}
	}


	renderGeometryAsLayer(geometry: any, prefix: string, color: string) {
		let sourceName: string = prefix + "-geoobject";

		this.map.addSource(sourceName, {
			type: 'geojson',
			data: {
				"type": "FeatureCollection",
				"features": []
			}
		});

		if (this.type.geometryType === "MULTIPOLYGON" || this.type.geometryType === "POLYGON") {
			// Polygon Layer
			this.map.addLayer({
				"id": sourceName + "-polygon",
				"type": "fill",
				"source": sourceName,
				"paint": {
					"fill-color": color,
					"fill-outline-color": "black",
					"fill-opacity": 0.7,
				},
			});
		}
		else if (this.type.geometryType === "POINT" || this.type.geometryType === "MULTIPOINT") {
			// Point layer
			this.map.addLayer({
				"id": sourceName + "-point",
				"type": "circle",
				"source": sourceName,
				"paint": {
					"circle-radius": 3,
					"circle-color": color,
					"circle-stroke-width": 2,
					"circle-stroke-color": '#FFFFFF'
				}
			});
		}
		else if (this.type.geometryType === "LINE" || this.type.geometryType === "MULTILINE") {
			this.map.addLayer({
				"id": sourceName + "-line",
				"source": sourceName,
				"type": "line",
				"layout": {
					"line-join": "round",
					"line-cap": "round"
				},
				"paint": {
					"line-color": color,
					"line-width": 2
				}
			});
		}

		(<any>this.map.getSource(sourceName)).setData(geometry);
	}



	calculateCurrent(goot: GeoObjectOverTime): any {
		const object = {};

		const time = this.forDate.getTime();

		const attr = {
			code: 'geometry',
			type: 'geometry'
		}

		//		for (let i = 0; i < this.type.attributes.length; ++i) {
		//			let attr = this.type.attributes[i];
		//			object[attr.code] = null;
		//
		//			if (attr.type === 'local') {
		//				object[attr.code] = this.lService.create();
		//			}
		//
		//			if (attr.isChangeOverTime) {
		let values = goot.attributes[attr.code].values;

		values.forEach(vot => {

			const startDate = Date.parse(vot.startDate);
			const endDate = Date.parse(vot.endDate);

			if (time >= startDate && time <= endDate) {

				if (attr.type === 'local') {
					object[attr.code] = {
						startDate: vot.startDate,
						endDate: vot.endDate,
						value: JSON.parse(JSON.stringify(vot.value))
					};
				}
				else if (attr.type === 'term' && vot.value != null && Array.isArray(vot.value) && vot.value.length > 0) {
					object[attr.code] = {
						startDate: vot.startDate,
						endDate: vot.endDate,
						value: vot.value[0]
					};
				}
				else {
					object[attr.code] = {
						startDate: vot.startDate,
						endDate: vot.endDate,
						value: vot.value
					};
				}
			}
		});
		//			}
		//			else {
		//				object[attr.code] = goot.attributes[attr.code];
		//			}
		//		}
		//
		//		for (let i = 0; i < this.type.attributes.length; ++i) {
		//			let attr = this.type.attributes[i];
		//
		//			if (attr.isChangeOverTime && object[attr.code] == null) {
		//				object[attr.code] = {
		//					startDate: null,
		//					endDate: null,
		//					value: ""
		//				}
		//			}
		//		}

		return object;
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

		return this.calculatedPostObject.geometry.value;
	}

	handleSubmit(): void {
		// Check if the geometry has been updated
		if (this.editingControl != null) {
			const geometry = this.getDrawGeometry();
			let values = this.postGeoObject.attributes['geometry'].values;
			const time = this.forDate.getTime();

			values.forEach(vot => {

				const startDate = Date.parse(vot.startDate);
				const endDate = Date.parse(vot.endDate);

				if (time >= startDate && time <= endDate) {
					vot.value = geometry;
				}
			});
		}

		this.service.applyGeoObjectEdit(null, this.postGeoObject, false, this.datasetId, null).then(() => {
			this.readOnly = true;
			this.calculatedPostObject = {};
			this.calculatedPreObject = {};
			this.postGeoObject = null;
			this.preGeoObject = null;

			this.removeVectorLayer(this.datasetId);
			this.addVectorLayer(this.datasetId);

			if (this.editingControl != null) {
				this.editingControl.deleteAll();
				this.map.removeControl(this.editingControl);
			}
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

	}

	onManageAttributeVersions(attribute: Attribute): void {
		this.attribute = attribute;
		this.mode = "VERSIONS";
	}

	onVersionChange(postGeoObject: GeoObjectOverTime): void {
		this.postGeoObject = postGeoObject;

		this.calculate();
		this.mode = 'ATTRIBUTES';
	}

	onEditAttributes(): void {
		this.readOnly = false;
	}

	public error(err: HttpErrorResponse): void {
		this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
		this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
	}

}
