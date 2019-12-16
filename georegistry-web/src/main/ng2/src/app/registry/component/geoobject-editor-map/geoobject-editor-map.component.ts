import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';

import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, GeoObjectOverTime, Attribute, AttributeTerm, AttributeDecimal, Term, ParentTreeNode } from '../../model/registry';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

import { Map, LngLatBounds, NavigationControl, ImageSource } from 'mapbox-gl';
import * as MapboxDraw from '@mapbox/mapbox-gl-draw';
import * as StaticMode from '@mapbox/mapbox-gl-draw-static-mode';
import * as mapboxgl from 'mapbox-gl';

declare var acp: string;


@Component( {
    selector: 'geoobject-editor-map[geometryType][geometryChange]',
    templateUrl: './geoobject-editor-map.component.html',
    styleUrls: ['./geoobject-editor-map.component.css']
} )

/**
 * This component is used in the master list when editing a row. In the future it will also be used by the navigator and has
 * potential to also be used in the submit change request and manage change requests.
 */
export class GeoObjectEditorMapComponent implements OnInit, OnDestroy {

    /*
     * Required. The GeometryType of the GeoJSON. Typically this can be found on the GeoObjectType.
     */
    @Input() geometryType: string;
    
    /*
     * Required. We will invoke this event with GeoJSON when the user makes an edit to the geometry.
     */
    @Output() geometryChange = new EventEmitter<string>();
    
    /*
     * Optional. If specified, we will diff based on this GeoJSON geometry.
     */
    @Input() preGeometry: string;
    
    /*
     * Optional. This is the actual editable GeoJSON geometry which will be mapped.
     */
    @Input() postGeometry: string;
    
    /*
     * Optional. If specified, we will fetch the bounding box from this GeoObject code.
     */
    @Input() boundCode: string;
    
    /*
     * Optional. If specified, we will fetch the bounding box from this GeoObjectType code.
     */
    @Input() boundType: string;
    
    /*
     * Optional. If set to true the edit controls will not be displayed. Defaults to false.
     */
    @Input() readOnly: boolean = false;
    
    map: Map;

    editingControl: any;

    constructor( private registryService: RegistryService ) {

    }

    ngOnInit(): void
    {
    }

    ngAfterViewInit() {
        setTimeout(() => {
            //this.registryService.getGeoObjectOverTime( "22", "Province" )
            //.then( geoObject => {

            ( mapboxgl as any ).accessToken = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ';

            this.map = new Map( {
                container: 'map',
                style: 'mapbox://styles/mapbox/satellite-v9',
                zoom: 2,
                center: [110.880453, 10.897852]
            } );

            this.map.on( 'load', () => {
                this.initMap();
            } );

            this.map.on( 'draw.create', () => {
                this.onValidChange();
            } );
            this.map.on( 'draw.delete', () => {
                this.onValidChange();
            } );
            this.map.on( 'draw.update', () => {
                this.onValidChange();
            } );

            //} ).catch(( err: HttpErrorResponse ) => {
            //    this.error( err );
            //} );
        }, 10 );
    }

    ngOnDestroy(): void {
        this.map.remove();
    }

    getIsValid(): boolean {
        if ( !this.readOnly ) {
            let isValid: boolean = false;

            if ( this.editingControl != null ) {
                let featureCollection: any = this.editingControl.getAll();

                if ( featureCollection.features.length > 0 ) {
                    isValid = true;
                }
            }

            return isValid;
        }

        return true;
    }

    private onValidChange(): void {
      this.geometryChange.emit(this.saveDraw());
    }

    initMap(): void {

        this.map.on( 'style.load', () => {
            this.addLayers();
            this.onValidChange();
        } );

        this.addLayers();

        this.zoomToBbox();

        // Add zoom and rotation controls to the map.
        this.map.addControl( new NavigationControl() );

        if ( !this.readOnly ) {
          this.enableEditing();
        }
        
        this.onValidChange();
    }

    enableEditing(): void {
	    if ( this.geometryType === "MULTIPOLYGON" || this.geometryType === "POLYGON" ) {
	        this.editingControl = new MapboxDraw( {
	            controls: {
	                point: false,
	                line_string: false,
	                polygon: true,
	                trash: true,
	                combine_features: false,
	                uncombine_features: false
	            }
	        } );
	    }
	    else if ( this.geometryType === "POINT" || this.geometryType === "MULTIPOINT" ) {
	        this.editingControl = new MapboxDraw( {
	            controls: {
	                point: true,
	                line_string: false,
	                polygon: false,
	                trash: true,
	                combine_features: false,
	                uncombine_features: false
	            }
	        } );
	    }
	    else if ( this.geometryType === "LINE" || this.geometryType === "MULTILINE" ) {
	        this.editingControl = new MapboxDraw( {
	            controls: {
	                point: false,
	                line_string: true,
	                polygon: false,
	                trash: true,
	                combine_features: false,
	                uncombine_features: false
	            }
	        } );
	    }
	    this.map.addControl( this.editingControl );
	
	    if (this.postGeometry != null)
	    {
	      this.editingControl.add( this.postGeometry );
	    }
    }

    removeSource( prefix: string ): void {
        let sourceName: string = prefix + "-geoobject";

        if ( this.geometryType === "MULTIPOLYGON" || this.geometryType === "POLYGON" ) {
            this.map.removeLayer( sourceName + "-polygon" );
        }
        else if ( this.geometryType === "POINT" || this.geometryType === "MULTIPOINT" ) {
            this.map.removeLayer( sourceName + "-point" );
        }
        else if ( this.geometryType === "LINE" || this.geometryType === "MULTILINE" ) {
            this.map.removeLayer( sourceName + "-line" );
        }

        this.map.removeSource( sourceName );
    }

    addLayers(): void {
        if ( this.preGeometry != null ) {
            this.renderGeometryAsLayer( this.preGeometry, "pre", "#EFA22E" )
        }
        if ( this.readOnly && this.postGeometry != null ) {
            this.renderGeometryAsLayer( this.postGeometry, "post", "#3368EF" );
        }
    }

    renderGeometryAsLayer( geometry: string, prefix: string, color: string ) {
        let sourceName: string = prefix + "-geoobject";

        this.map.addSource( sourceName, {
            type: 'geojson',
            data: {
                "type": "FeatureCollection",
                "features": []
            }
        } );
        
        if ( this.geometryType === "MULTIPOLYGON" || this.geometryType === "POLYGON" ) {
            // Polygon Layer
            this.map.addLayer( {
                "id": sourceName + "-polygon",
                "type": "fill",
                "source": sourceName,
                "paint": {
                    "fill-color": color,
                    "fill-outline-color": "black",
                    "fill-opacity": 0.7,
                },
            } );
        }
        else if ( this.geometryType === "POINT" || this.geometryType === "MULTIPOINT" ) {
            // Point layer
            this.map.addLayer( {
                "id": sourceName + "-point",
                "type": "circle",
                "source": sourceName,
                "paint": {
                    "circle-radius": 3,
                    "circle-color": color,
                    "circle-stroke-width": 2,
                    "circle-stroke-color": '#FFFFFF'
                }
            } );
        }
        else if ( this.geometryType === "LINE" || this.geometryType === "MULTILINE" ) {
            this.map.addLayer( {
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
            } );
        }

        ( <any>this.map.getSource( sourceName ) ).setData( geometry );
    }

    zoomToBbox(): void {
      if ( this.boundCode != null && this.boundType != null ) {
        this.registryService.getGeoObjectBounds( this.boundCode, this.boundType ).then( boundArr => {
            let bounds = new LngLatBounds( [boundArr[0], boundArr[1]], [boundArr[2], boundArr[3]] );

            this.map.fitBounds( bounds, { padding: 50 } );
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
      }
    }

    saveDraw(): GeoObjectOverTime {
        if ( this.editingControl != null ) {
            let featureCollection: any = this.editingControl.getAll();

            if ( featureCollection.features.length > 0 ) {

                // The first Feature is our GeoObject.

                // Any additional features were created using the draw editor. Combine them into the GeoObject if its a multi-polygon.
                if ( this.geometryType === "MULTIPOLYGON" ) {
                    let polygons = [];

                    for ( let i = 0; i < featureCollection.features.length; i++ ) {
                        let feature = featureCollection.features[i];

                        if ( feature.geometry.type === 'MultiPolygon' ) {
                            for ( let j = 0; j < feature.geometry.coordinates.length; j++ ) {
                                polygons.push( feature.geometry.coordinates[j] );
                            }
                        }
                        else {
                            polygons.push( feature.geometry.coordinates );
                        }
                    }

                    return {
                        coordinates: polygons,
                        type: 'MultiPolygon'
                    };
                }
                else if ( this.geometryType === "MULTIPOINT" ) {
                    let points = [];

                    for ( let i = 0; i < featureCollection.features.length; i++ ) {
                        let feature = featureCollection.features[i];

                        if ( feature.geometry.type === 'MultiPoint' ) {
                            for ( let j = 0; j < feature.geometry.coordinates.length; j++ ) {
                                points.push( feature.geometry.coordinates[j] );
                            }
                        }
                        else {
                            points.push( feature.geometry.coordinates );
                        }
                    }

                    return {
                        coordinates: points,
                        type: 'MultiPoint'
                    };
                }
                else if ( this.geometryType === "MULTILINE" ) {
                    let lines = [];

                    for ( let i = 0; i < featureCollection.features.length; i++ ) {
                        let feature = featureCollection.features[i];

                        if ( feature.geometry.type === 'MultiLineString' ) {
                            for ( let j = 0; j < feature.geometry.coordinates.length; j++ ) {
                                lines.push( feature.geometry.coordinates[j] );
                            }
                        }
                        else {
                            lines.push( feature.geometry.coordinates );
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

            return this.postGeometry;
        }

        return this.postGeometry;
    }

    public error( err: HttpErrorResponse ): void {
        // TODO
        console.log( "ERROR", err );

        // Handle error
        //if (err !== null) {
        //    this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
        //    this.bsModalRef.content.message = (err.localizedMessage || err.message);
        //}
    }


}
