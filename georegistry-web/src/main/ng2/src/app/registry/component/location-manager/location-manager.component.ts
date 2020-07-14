import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { Map, LngLatBounds, NavigationControl, MapboxEvent, AttributionControl } from 'mapbox-gl';

import { Subject } from 'rxjs';

import { GeoObject, MasterList } from '../../model/registry';
import { LocationInformation } from '../../model/location-manager';

import { MapService } from '../../service/map.service';
import { RegistryService } from '../../service/registry.service';

declare var acp: string;

@Component({
	selector: 'location-manager',
	templateUrl: './location-manager.component.html',
	styles: [],
})
export class LocationManagerComponent implements OnInit, AfterViewInit, OnDestroy {

    /* 
     * Root nodes of the tree
     */
	data: LocationInformation = {
		types: [],
		hierarchies: [],
		geojson: { type: 'MultiPolygon', features: [] }
	};

    /*
     * Date of data for explorer
     */
	dateStr: string = null;

    /* 
     * Breadcrumb of previous children clicked on
     */
	breadcrumbs = [] as GeoObject[];

    /* 
     * Root nodes of the tree
     */
	current: GeoObject;

    /* 
     * mapbox-gl map
     */
	map: Map;

    /* 
     * Flag denoting the draw control is active
     */
	active: boolean = false;

	vectorLayers: string[] = [];

	lists: MasterList[] = [];

    /* 
     * List of base layers
     */
	baseLayers: any[] = [{
		label: 'Outdoors',
		id: 'outdoors-v11',
		selected: true
	}, {
		label: 'Satellite',
		id: 'satellite-v9'
	}, {
		label: 'Streets',
		id: 'streets-v11'
	}];

	baselayerIconHover = false;

	hoverFeatureId: string;

	/* 
     * debounced subject for map extent change events
     */
	subject: Subject<MapboxEvent<MouseEvent | TouchEvent | WheelEvent>>;

	constructor(private mapService: MapService, public service: RegistryService) {
	}

	ngOnInit(): void {
		this.service.getAllMasterListVersions().then(lists => {
			this.lists = lists;
		});
	}

	ngOnDestroy(): void {
		this.map.remove();
	}

	ngAfterViewInit() {

		this.map = new Map({
			container: 'map',
			style: 'mapbox://styles/mapbox/outdoors-v11',
			zoom: 2,
			attributionControl: false,
			center: [-78.880453, 42.897852]
		});

		this.map.on('load', () => {
			this.initMap();
		});

	}

	handleDateChange(): void {
		this.back(null);
	}

	initMap(): void {

		this.map.on('style.load', () => {
			this.addLayers();
			this.refresh(false);
		});

		this.addLayers();


		this.refresh(true);

		// Add zoom and rotation controls to the map.
		this.map.addControl(new NavigationControl());
		this.map.addControl(new AttributionControl({ compact: true }), 'bottom-left');

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

		// Polygon layer
		this.map.addLayer({
			'id': source + '-polygon',
			'type': 'fill',
			'source': source,
			'layout': {},
			'paint': {
				'fill-color': '#a6611a',
				'fill-opacity': 0.8
			},
			filter: ['all',
				["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
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
				"text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
				"text-offset": [0, 0.6],
				"text-anchor": "top",
				"text-size": 12,
			}
		});


		//		this.addContextLayer('c4ae30ca-1c86-4ec7-ae3f-b095520005f1');
	}

	handleExtentChange(e: MapboxEvent<MouseEvent | TouchEvent | WheelEvent>): void {
		if (this.current == null) {
			const bounds = this.map.getBounds();

			// Sometimes bounds aren't valid for 4326, so validate it before sending to server
			if (this.isValidBounds(bounds)) {
				//				this.service.roots(null, bounds).then(nodes => {
				//					this.nodes = nodes;
				//				});
			}
			else {
				// console.log("Invalid bounds", bounds);
			}
		}
	}

	isValidBounds(bounds: LngLatBounds): boolean {

		const ne = bounds.getNorthEast();
		const sw = bounds.getSouthWest();

		if (Math.abs(ne.lng) > 180 || Math.abs(sw.lng) > 180) {
			return false;
		}

		if (Math.abs(ne.lat) > 90 || Math.abs(sw.lat) > 90) {
			return false;
		}

		return true;
	}

	refresh(zoom: boolean): void {

		if (this.current == null) {
			this.mapService.roots(null, null, this.dateStr).then(data => {
				(<any>this.map.getSource('children')).setData(data.geojson);

				this.data = data;
			});
		} else {
			this.mapService.select(this.current.properties.code, this.current.properties.type, this.data.childType, this.data.hierarchy, this.dateStr).then(data => {
				(<any>this.map.getSource('children')).setData(data.geojson);

				this.data = data;
			});
		}

	}

	zoomToFeature(node: GeoObject): void {
		if (node.geometry != null) {
			this.map.flyTo({
				center: node.geometry.coordinates
			});
		}
	}


	handleStyle(layer: any): void {

		this.baseLayers.forEach(baseLayer => {
			baseLayer.selected = false;
		});

		layer.selected = true;

		this.map.setStyle('mapbox://styles/mapbox/' + layer.id);
	}

	highlightMapFeature(id: string): void {

		//		this.map.setFilter('hover-points', ['all',
		//			['==', 'oid', id]
		//		])

	}

	clearHighlightMapFeature(): void {

		//		this.map.setFilter('hover-points', ['all',
		//			['==', 'oid', "NONE"]
		//		])

	}

	onListEntityHover(event: GeoObject, site: GeoObject): void {
		if (this.current == null) {
			this.highlightMapFeature(site.properties.code);
		}
	}

	onListEntityHoverOff(): void {
		this.clearHighlightMapFeature();
	}

	//	highlightListItem(id: string): void {
	//		this.nodes.forEach(node => {
	//			if (node.properties.code === id) {
	//				this.hoverFeatureId = id;
	//			}
	//		})
	//	}
	//
	//	clearHighlightListItem(): void {
	//		if (this.hoverFeatureId) {
	//			this.nodes.forEach(node => {
	//				if (node.properties.code === this.hoverFeatureId) {
	//					this.hoverFeatureId = null;
	//				}
	//			})
	//		}
	//	}

	select(node: GeoObject, parent: GeoObject, event: any): void {

		if (event != null) {
			event.stopPropagation();
		}

		this.mapService.select(node.properties.code, node.properties.type, null, null, this.dateStr).then(data => {
			this.current = node;
			if (parent != null) {
				this.addBreadcrumb(parent);
			}

			this.addBreadcrumb(node);

			(<any>this.map.getSource('children')).setData(data.geojson);

			this.data = data;

			//			if (zoom) {
			//				let bounds = new LngLatBounds([data.bbox[0], data.bbox[1]], [data.bbox[2], data.bbox[3]]);
			//
			//				this.map.fitBounds(bounds, { padding: 50 });
			//			}
		});

	}

	addBreadcrumb(node: GeoObject): void {

		if (this.breadcrumbs.length == 0 || this.breadcrumbs[this.breadcrumbs.length - 1].properties.code !== node.properties.code) {
			this.breadcrumbs.push(node);
		}
	}

	back(node: GeoObject): void {

		if (node != null) {
			this.mapService.select(node.properties.code, node.properties.type, null, this.data.hierarchy, this.dateStr).then(data => {
				var indexOf = this.breadcrumbs.findIndex(i => i.properties.code === node.properties.code);

				this.current = node;
				this.breadcrumbs.splice(indexOf + 1);

				(<any>this.map.getSource('children')).setData(data.geojson);

				this.data = data;
			});
		}
		else if (this.breadcrumbs.length > 0) {
			this.mapService.roots(null, null, this.dateStr).then(data => {
				(<any>this.map.getSource('children')).setData(data.geojson);

				this.data = data;

				this.current = null;
				this.breadcrumbs = [];
			});
		}
	}

	expand(node: GeoObject) {
		this.current = node;
	}

	setNodes(nodes: GeoObject[]): void {
		this.data.geojson.features = [];

		nodes.forEach(node => {
			this.data.geojson.features.push(node);
		})
	}

	toggleContextLayer(source: string) {
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
					'fill-opacity': 0.8
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
						["has", "displayLabel_" + navigator.language.toLowerCase],
						["coalesce", ["string", ["get", "displayLabel_" + navigator.language.toLowerCase]], ["string", ["get", "displayLabel"]]],
						["string", ["get", "displayLabel"]]
					],
					"text-font": ["Open Sans Semibold", "Arial Unicode MS Bold"],
					"text-offset": [0, 0.6],
					"text-anchor": "top",
					"text-size": 12,
				}
			}, prevLayer);


			this.vectorLayers.push(source);
		}
		else {
			this.map.removeLayer(source + "-points");
			this.map.removeLayer(source + "-polygon");
			this.map.removeLayer(source + "-label");
			this.map.removeSource(source);

			this.vectorLayers.splice(index, 1);
		}
	}
}
