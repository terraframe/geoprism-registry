import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { Map, LngLatBoundsLike, NavigationControl, MapboxEvent, AttributionControl } from 'mapbox-gl';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { AllGeoJSON } from '@turf/helpers'
import bbox from '@turf/bbox';

import { Subject } from 'rxjs';

import { GeoObject, MasterList, ContextLayer, ContextLayerGroup } from '@registry/model/registry';
import { LocationInformation } from '@registry/model/location-manager';

import { ContextLayerModalComponent } from './context-layer-modal.component';

import { MapService, RegistryService } from '@registry/service';

declare var acp: string;

@Component({
	selector: 'location-manager',
	templateUrl: './location-manager.component.html',
	styleUrls: ['./location-manager.css']
})
export class LocationManagerComponent implements OnInit, AfterViewInit, OnDestroy {

	bsModalRef: BsModalRef;

    /* 
     * Root nodes of the tree
     */
	data: LocationInformation = {
		types: [],
		hierarchies: [],
		geojson: { type: 'MultiPolygon', features: [] },
	};

	childType: string = null;
	hierarchy: string = null;

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

	contextLayerGroups: ContextLayerGroup[] = [];

	lists: MasterList[] = [];

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

	baselayerIconHover = false;

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
	}

	ngOnInit(): void {
		this.service.getAllMasterListVersions().then(lists => {
			this.lists = lists;

			this.convertListsToContextLayers(lists);
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

	}

	convertListsToContextLayers(lists: MasterList[]): void {
		lists.forEach(list => {
			let thisList = { oid: list.oid, displayLabel: list.displayLabel.localizedValue, contextLayers: [] };
			this.contextLayerGroups.push(thisList);
			list.versions.forEach(version => {
				let thisContextLayer = { oid: version.oid, displayLabel: version.forDate, active: false, enabled: false };
				thisList.contextLayers.push(thisContextLayer);
			});
		});
	}

	handleDateChange(): void {
		this.back(null);
	}

	initMap(): void {

		this.map.on('style.load', () => {
			this.addLayers();
			this.refresh();
		});

		this.addLayers();


		this.refresh();

		// Add zoom and rotation controls to the map.
		this.map.addControl(new NavigationControl({ 'visualizePitch': true }));
		this.map.addControl(new AttributionControl({ compact: true }), 'bottom-right');

		this.map.on('dblclick', 'children-points', (event: any) => {
			this.handleMapClickEvent(event);
		});

		this.map.on('dblclick', 'children-polygon', (event: any) => {
			this.handleMapClickEvent(event);
		});
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
				'fill-opacity': 0.8,
				'fill-outline-color': 'black'
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
				"text-font": ["NotoSansRegular"],
				"text-offset": [0, 0.6],
				"text-anchor": "top",
				"text-size": 12,
			}
		});

		this.contextLayerGroups.forEach(cLayerGroup => {
			cLayerGroup.contextLayers.forEach(cLayer => {
				if (cLayer.enabled) {
					this.addVectorLayer(cLayer.oid);
				}
			});
		});

	}

	refresh(): void {
		if (this.current == null) {
			this.mapService.roots(this.childType, this.hierarchy, this.dateStr).then(data => {
				(<any>this.map.getSource('children')).setData(data.geojson);

				this.setData(data);
			});
		} else {
			this.mapService.select(this.current.properties.code, this.current.properties.type, this.childType, this.hierarchy, this.dateStr).then(data => {
				(<any>this.map.getSource('children')).setData(data.geojson);


				this.setData(data);
			});
		}
		this.hierarchy = null;
		this.childType = null;
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

	// handleStyle(layer: any): void {

	// 	this.baseLayers.forEach(baseLayer => {
	// 		baseLayer.selected = false;
	// 	});

	// 	layer.selected = true;

	// 	this.map.setStyle({
	// 		"version": 8,
	// 		"name": layer.name,
	// 		"metadata": {
	// 			"mapbox:autocomposite": true
	// 		},
	// 		"sources": {
	// 			"mapbox": {
	// 				"type": "raster",
	// 				"url": layer.url,
	// 				"tileSize": 256
	// 			}
	// 		},
	// 		"sprite": layer.sprite,
	// 		"glyphs": window.location.protocol + '//' + window.location.host + acp + '/glyphs/{fontstack}/{range}.pbf',
	// 		"layers": [
	// 			{
	// 				"id": "background",
	// 				"type": "background",
	// 				"paint": {
	// 					"background-color": "rgb(4,7,14)"
	// 				}
	// 			},
	// 			{
	// 				"id": layer.id,
	// 				"type": "raster",
	// 				"source": "mapbox",
	// 				"source-layer": "mapbox_satellite_full"
	// 			}
	// 		]
	// 	});
	// }

	addContextLayerModal(): void {
		this.bsModalRef = this.modalService.show(ContextLayerModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
			'class': 'context-layer-modal'
		});
		this.bsModalRef.content.contextLayerGroups = this.contextLayerGroups;

		(<ContextLayerModalComponent>this.bsModalRef.content).onSubmit.subscribe(cLayerGroups => {

			// cLayerGroups.forEach(cLayerGroup => {
			// 	cLayerGroup.contextLayers.forEach(cLayer => {
			// 		this.toggleContextLayer(cLayer.oid)
			// 	});
			// })

		});
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

		this.preventSingleClick = true;
		clearTimeout(this.timer);

		this.drillDown(node);
	}

	handleMapClickEvent(event: any): void {
		if (event.features != null && event.features.length > 0) {
			const feature = event.features[0];

			const index = this.data.geojson.features.findIndex(node => { return node.properties.code === feature.properties.code });

			if (index !== -1) {
				this.drillDown(this.data.geojson.features[index]);
			}
		}
	}

	drillDown(node: GeoObject): void {
		this.mapService.select(node.properties.code, node.properties.type, null, this.hierarchy, this.dateStr).then(data => {
			this.current = node;

			this.addBreadcrumb(node);

			(<any>this.map.getSource('children')).setData(data.geojson);

			this.setData(data);
		});

		this.hierarchy = null;
		this.childType = null;
	}

	addBreadcrumb(node: GeoObject): void {

		if (this.breadcrumbs.length == 0 || this.breadcrumbs[this.breadcrumbs.length - 1].properties.code !== node.properties.code) {
			this.breadcrumbs.push(node);
		}
	}

	back(node: GeoObject): void {

		if (node != null) {
			this.mapService.select(node.properties.code, node.properties.type, null, this.hierarchy, this.dateStr).then(data => {
				var indexOf = this.breadcrumbs.findIndex(i => i.properties.code === node.properties.code);

				this.current = node;
				this.breadcrumbs.splice(indexOf + 1);

				(<any>this.map.getSource('children')).setData(data.geojson);

				this.setData(data);
			});
		}
		else if (this.breadcrumbs.length > 0) {
			this.mapService.roots(null, null, this.dateStr).then(data => {
				(<any>this.map.getSource('children')).setData(data.geojson);

				this.setData(data);

				this.current = null;
				this.breadcrumbs = [];
			});
		}

		this.hierarchy = null;
		this.childType = null;
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

	setData(data: LocationInformation): void {
		this.data = data;

		this.hierarchy = data.hierarchy;
		this.childType = data.childType;
	}


	groupHasEnabledContextLayers(group: string): boolean {
		let hasEnabled = false;
		this.contextLayerGroups.forEach(cLayerGroup => {
			if (cLayerGroup.oid === group) {
				cLayerGroup.contextLayers.forEach(cLayer => {
					if (cLayer.enabled) {
						hasEnabled = true;
					}
				});
			}
		});

		return hasEnabled;
	}

	hasEnabledContextLayers(): boolean {
		let hasEnabled = false;
		this.contextLayerGroups.forEach(cLayerGroup => {
			cLayerGroup.contextLayers.forEach(cLayer => {
				if (cLayer.enabled) {
					hasEnabled = true;
				}
			});
		});

		return hasEnabled;
	}

	removeContextLayer(cLayer: ContextLayer): void {
		if (cLayer.active) {
			this.map.removeLayer(cLayer.oid + "-points");
			this.map.removeLayer(cLayer.oid + "-polygon");
			this.map.removeLayer(cLayer.oid + "-label");
			this.map.removeSource(cLayer.oid);

			cLayer.active = false;
		}

		cLayer.enabled = false;
	}

	toggleContextLayer(source: string): void {

		this.contextLayerGroups.forEach(cLayerGroup => {
			cLayerGroup.contextLayers.forEach(cLayer => {
				if (cLayer.oid === source) {

					// WARNING: the boolean component returns the value already switched (false --> true). 
					// I'm reversing that value here so the logic below is more intuitive.
					// cLayer.active = !cLayer.active 
					//

					if (cLayer.active) {
						this.map.removeLayer(source + "-points");
						this.map.removeLayer(source + "-polygon");
						this.map.removeLayer(source + "-label");
						this.map.removeSource(source);

						cLayer.active = false;
					}
					else {
						this.addVectorLayer(source);

						cLayer.active = true;
					}
				}
			})
		})
	}

	addVectorLayer(source: string): void {
		const prevLayer = 'children-points';

		// console.log(navigator.language.toLowerCase());

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

	}
}
