import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { Map, LngLatBoundsLike, NavigationControl, MapboxEvent, AttributionControl } from 'mapbox-gl';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { AllGeoJSON } from '@turf/helpers'
import bbox from '@turf/bbox';

import { Subject } from 'rxjs';

import { GeoObject, ContextLayer, GeoObjectType } from '@registry/model/registry';

import { MapService, RegistryService } from '@registry/service';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler, ErrorModalComponent } from '@shared/component';

declare var acp: string;

@Component({
	selector: 'location-manager',
	templateUrl: './location-manager.component.html',
	styleUrls: ['./location-manager.css']
})
export class LocationManagerComponent implements OnInit, AfterViewInit, OnDestroy {

	MODE = {
		SEARCH: 0,
		VIEW: 1,
	}

	bsModalRef: BsModalRef;

    /* 
     * Root nodes of the tree
     */
	data: GeoObject[] = [];

    /* 
     *  Search Text
     */
	text: string = '';

    /* 
     *  MODE
     */
	mode: number = this.MODE.SEARCH;

    /*
     * Date of data for explorer
     */
	dateStr: string = null;

	forDate: Date = new Date();


    /* 
     * Currently selected geo object
     */
	current: GeoObject;

    /* 
     * Currently selected geo object type
     */
	type: GeoObjectType;

    /* 
     * mapbox-gl map
     */
	map: Map;

    /* 
     * Flag denoting the draw control is active
     */
	active: boolean = false;

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

	hoverFeatureId: string;

	preventSingleClick: boolean = false;

	/* 
     * Timer for determining double click vs single click
     */
	timer: any;

	/* 
     * debounced subject for map extent change events
     */
	subject: Subject<MapboxEvent<MouseEvent | TouchEvent | WheelEvent>>;

	constructor(private modalService: BsModalService, private mapService: MapService, public service: RegistryService) {
		mapService.init();
	}

	ngOnInit(): void {
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

	}

	handleDateChange(): void {
		this.forDate = new Date(Date.parse(this.dateStr));
	}

	initMap(): void {

		this.map.on('style.load', () => {
			this.addLayers();
		});

		this.addLayers();

		// Add zoom and rotation controls to the map.
		this.map.addControl(new NavigationControl({ 'visualizePitch': true }));
		this.map.addControl(new AttributionControl({ compact: true }), 'bottom-right');

		//		this.map.on('dblclick', 'children-points', (event: any) => {
		//			this.handleMapClickEvent(event);
		//		});
		//
		//		this.map.on('dblclick', 'children-polygon', (event: any) => {
		//			this.handleMapClickEvent(event);
		//		});
	}

	addLayers(): void {

		const source = 'children';

		this.map.addSource(source, {
			type: 'geojson',
			data: {
				"type": "FeatureCollection",
				"features": []
			}
		});

		// Polygon layer
		this.map.addLayer({
			'id': source + '-polygon',
			'type': 'fill',
			'source': source,
			'layout': {},
			'paint': {
				'fill-color': '#a6611a',
				'fill-opacity': 0.8,
				'fill-outline-color': 'black'
			},
			filter: ['all',
				["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
			]
		});

		// Point layer
		this.map.addLayer({
			"id": source + "-points",
			"type": "circle",
			"source": source,
			"paint": {
				"circle-radius": 10,
				"circle-color": '#a6611a',
				"circle-stroke-width": 2,
				"circle-stroke-color": '#FFFFFF'
			},
			filter: ['all',
				["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
			]
		});


		// Label layer
		this.map.addLayer({
			"id": source + "-label",
			"source": source,
			"type": "symbol",
			"paint": {
				"text-color": "black",
				"text-halo-color": "#fff",
				"text-halo-width": 2
			},
			"layout": {
				"text-field": ['get', 'localizedValue', ['get', 'displayLabel']],
				"text-font": ["NotoSansRegular"],
				"text-offset": [0, 0.6],
				"text-anchor": "top",
				"text-size": 12,
			}
		});

		this.vectorLayers.forEach(cLayer => {
			this.addVectorLayer(cLayer);
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

	search(): void {
		this.mapService.search(this.text, this.dateStr).then(data => {
			(<any>this.map.getSource('children')).setData(data);


			this.setData(data.features);
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

	}


	zoomToFeature(node: GeoObject, event: MouseEvent): void {
		if (event != null) {
			event.stopPropagation();
		}

		this.preventSingleClick = false;
		const delay = 200;

		this.timer = setTimeout(() => {
			if (!this.preventSingleClick) {
				if (node.geometry != null) {
					const bounds = bbox(node as AllGeoJSON) as LngLatBoundsLike;

					this.map.fitBounds(bounds);
				}
			}
		}, delay);
	}

	select(node: GeoObject, event: MouseEvent): void {

		if (event != null) {
			event.stopPropagation();
		}

		this.service.getGeoObjectTypes([node.properties.type], null).then(types => {
			this.type = types[0];
			this.current = node;
			this.mode = this.MODE.VIEW;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});


		//			this.preventSingleClick = true;
		//			clearTimeout(this.timer);
		//	
		//			this.drillDown(node);
	}
	//
	//	handleMapClickEvent(event: any): void {
	//		if (event.features != null && event.features.length > 0) {
	//			const feature = event.features[0];
	//
	//			const index = this.data.geojson.features.findIndex(node => { return node.properties.code === feature.properties.code });
	//
	//			if (index !== -1) {
	//				this.drillDown(this.data.geojson.features[index]);
	//			}
	//		}
	//	}
	//
	setData(data: GeoObject[]): void {
		this.data = data;
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
			const prevLayer = 'children-points';

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

	error(err: HttpErrorResponse): void {
		const bsModalRef: any = this.modalService.show(ErrorModalComponent, { backdrop: true });
		bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
	}

}
