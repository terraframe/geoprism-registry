import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { Map, LngLatBounds, NavigationControl, MapboxEvent, AttributionControl } from 'mapbox-gl';

import { Subject } from 'rxjs';

import { GeoObject } from '../../model/registry';
import { LocationInformation } from '../../model/location-manager';
import { MapService } from '../../service/map.service';

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

	constructor(private mapService: MapService) {
	}

	ngOnInit(): void {
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

		//		this.map.on('mousemove', e => {
		//			// e.point is the x, y coordinates of the mousemove event relative
		//			// to the top-left corner of the map.
		//			// e.lngLat is the longitude, latitude geographical position of the event
		//			let coord = e.lngLat.wrap();
		//
		//			// EPSG:3857 = WGS 84 / Pseudo-Mercator
		//			// EPSG:4326 = WGS 84 
		//			// let coord4326 = window.proj4(window.proj4.defs('EPSG:3857'), window.proj4.defs('EPSG:4326'), [coord.lng, coord.lat]);
		//			// let text = "Long: " + coord4326[0] + " Lat: " + coord4326[1];
		//
		//			let text = "Lat: " + coord.lat + " Long: " + coord.lng;
		//			let mousemovePanel = document.getElementById("mousemove-panel");
		//			mousemovePanel.textContent = text;
		//
		//
		//			let features = this.map.queryRenderedFeatures(e.point, { layers: ['points'] });
		//
		//			if (this.current == null) {
		//				if (features.length > 0) {
		//					let focusFeatureId = features[0].properties.oid; // just the first
		//					this.map.setFilter('hover-points', ['all',
		//						['==', 'oid', focusFeatureId]
		//					])
		//
		//					this.highlightListItem(focusFeatureId)
		//				}
		//				else {
		//					this.map.setFilter('hover-points', ['all',
		//						['==', 'oid', "NONE"]
		//					])
		//
		//					this.clearHighlightListItem();
		//				}
		//			}
		//		});
		//
		//		this.map.on('zoomend', (e) => {
		//			this.subject.next(e);
		//		});
		//
		//		this.map.on('moveend', (e) => {
		//			this.subject.next(e);
		//		});
		//
		//		// MapboxGL doesn't have a good way to detect when moving off the map
		//		let sidebar = document.getElementById("navigator-left-sidebar");
		//		sidebar.addEventListener("mouseenter", function() {
		//			let mousemovePanel = document.getElementById("mousemove-panel");
		//			mousemovePanel.textContent = "";
		//		});
	}

	addLayers(): void {

		this.map.addSource('children', {
			type: 'geojson',
			data: {
				"type": "FeatureCollection",
				"features": []
			}
		});


		// Point layer
		this.map.addLayer({
			"id": "points",
			"type": "circle",
			"source": 'children',
			"paint": {
				"circle-radius": 10,
				"circle-color": '#800000',
				"circle-stroke-width": 2,
				"circle-stroke-color": '#FFFFFF'
			},
			filter: ['all',
				["match", ["geometry-type"], ["Point", "MultiPont"], true, false]
			]
		});
		//
		//
		//		// Hover style
		//		this.map.addLayer({
		//			"id": "hover-points",
		//			"type": "circle",
		//			"source": 'children',
		//			"paint": {
		//				"circle-radius": 13,
		//				"circle-color": '#cf0000',
		//				"circle-stroke-width": 2,
		//				"circle-stroke-color": '#FFFFFF'
		//			},
		//			filter: ['all',
		//				['==', 'id', 'NONE'] // start with a filter that doesn't select GeoObjectthing
		//			]
		//		});

		// Polygon layer
		this.map.addLayer({
			'id': 'polygon',
			'type': 'fill',
			'source': 'children',
			'layout': {},
			'paint': {
				'fill-color': '#088',
				'fill-opacity': 0.8
			},
			filter: ['all',
				["match", ["geometry-type"], ["Polygon", "MultiPolygon"], true, false]
			]
		});


		// Label layer
		this.map.addLayer({
			"id": "points-label",
			"source": 'children',
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
		this.mapService.roots(null, null).then(data => {
			(<any>this.map.getSource('children')).setData(data.geojson);

			this.data = data;

			//			if (zoom) {
			//				let bounds = new LngLatBounds([data.bbox[0], data.bbox[1]], [data.bbox[2], data.bbox[3]]);
			//
			//				this.map.fitBounds(bounds, { padding: 50 });
			//			}
		});
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

		this.mapService.select(node.properties.code, node.properties.type, null, null).then(data => {
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
			this.mapService.select(node.properties.code, node.properties.type, null, this.data.hierarchy).then(data => {
				var indexOf = this.breadcrumbs.findIndex(i => i.properties.code === node.properties.code);

				this.current = node;
				this.breadcrumbs.splice(indexOf + 1);

				(<any>this.map.getSource('children')).setData(data.geojson);

				this.data = data;
			});
		}
		else if (this.breadcrumbs.length > 0) {
			this.mapService.roots(null, null).then(data => {
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
}
