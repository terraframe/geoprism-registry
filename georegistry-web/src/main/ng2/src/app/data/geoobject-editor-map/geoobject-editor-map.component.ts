import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input, Output, EventEmitter } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';


import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term, ParentTreeNode } from '../../model/registry';

import { GeoObjectAttributeExcludesPipe } from '../../data/geoobject-shared-attribute-editor/geoobject-attribute-excludes.pipe';
import { ToEpochDateTimePipe } from '../../data/submit-change-request/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

import { AuthService } from '../../core/auth/auth.service';

import { Map, LngLatBounds, NavigationControl, ImageSource } from 'mapbox-gl';
import * as MapboxDraw from '@mapbox/mapbox-gl-draw';
import * as StaticMode from '@mapbox/mapbox-gl-draw-static-mode';
import * as mapboxgl from 'mapbox-gl';

declare var acp: string;


@Component( {
    selector: 'geoobject-editor-map',
    templateUrl: './geoobject-editor-map.component.html',
    styleUrls: ['./geoobject-editor-map.component.css']
} )

/**
 * This component is used in the master list when editing a row. In the future it will also be used by the navigator and has
 * potential to also be used in the submit change request and manage change requests.
 */
export class GeoObjectEditorMapComponent implements OnInit {

    /* 
     * mapbox-gl map
     */
    map: Map;

    /* 
     * Draw control
     */
    draw: MapboxDraw;

    editingControl: any;

    geoprismEditingControl: any;

    isEditing: boolean;

    postGeoObjectProperties: any;

    /*
   * The state of the GeoObject after our edit has been applied 
   */
    @Input() postGeoObject: GeoObject;

    @Input() geoObjectType: GeoObjectType;

    @Input() isNew: boolean;

    @Output() valid = new EventEmitter<boolean>();

    constructor( private service: IOService, private modalService: BsModalService, public bsModalRef: BsModalRef, private registryService: RegistryService ) {

    }

    ngOnInit(): void {

    }

    ngAfterViewInit() {
        setTimeout(() => {
            this.postGeoObjectProperties = JSON.parse( JSON.stringify( this.postGeoObject.properties ) );

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
        }, 10 );
    }

    getIsValid(): boolean {
        let isValid: boolean = false;
        let featureCollection: any = this.editingControl.getAll();

        if ( featureCollection.features.length > 0 ) {
            isValid = true;
        }

        return isValid;
    }

    private onValidChange(): void {
        this.valid.emit();
    }

    initMap(): void {

        this.map.on( 'style.load', () => {
            this.refresh( false );
        } );

        this.refresh( true );

        // Add zoom and rotation controls to the map.
        this.map.addControl( new NavigationControl() );

        var gt = this.geoObjectType.geometryType.toUpperCase();
        if ( gt === "MULTIPOLYGON" || gt === "POLYGON" ) {
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
        else if ( gt === "POINT" || gt === "MULTIPOINT" ) {
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
        else if ( gt === "LINE" || gt === "MULTILINE" ) {
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

        if ( this.postGeoObject.type != null && this.postGeoObject.geometry != null ) {
            this.editingControl.add( this.postGeoObject );
        }
    }

    refresh( zoom: boolean ): void {
        if ( zoom && this.postGeoObject.geometry != null && !this.isNew ) {

            this.registryService.getGeoObjectBounds( this.postGeoObject.properties.code, this.postGeoObject.properties.type )
                .then( boundArr => {
                    let bounds = new LngLatBounds( [boundArr[0], boundArr[1]], [boundArr[2], boundArr[3]] );

                    this.map.fitBounds( bounds, { padding: 50 } );
                } ).catch(( err: Response ) => {
                    this.error( err.json() );
                } );
        }
    }

    saveDraw(): GeoObject {
        let featureCollection: any = this.editingControl.getAll();

        if ( featureCollection.features.length > 0 ) {

            // The first Feature is our GeoObject.

            // Any additional features were created using the draw editor. Combine them into the GeoObject if its a multi-polygon.
            if ( this.geoObjectType.geometryType.toUpperCase() === "MULTIPOLYGON" ) {
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

                this.postGeoObject.geometry = {
                    coordinates: polygons,
                    type: 'MultiPolygon'
                };
            }
            else if ( this.geoObjectType.geometryType.toUpperCase() === "MULTIPOINT" ) {
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

                this.postGeoObject.geometry = {
                    coordinates: points,
                    type: 'MultiPoint'
                };
            }
            else if ( this.geoObjectType.geometryType.toUpperCase() === "MULTILINE" ) {
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

                this.postGeoObject.geometry = {
                    coordinates: lines,
                    type: 'MultiLineString'
                };
            }
            else {
                this.postGeoObject = featureCollection.features[0];
            }

            // If they deleted the Primary feature and then re-created it, then the properties won't exist.
            this.postGeoObject.properties = JSON.parse( JSON.stringify( this.postGeoObjectProperties ) );
        }

        return this.postGeoObject;
    }

    public error( err: any ): void {
        // TODO

        // Handle error
        //if (err !== null) {
        //    this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
        //    this.bsModalRef.content.message = (err.localizedMessage || err.message);
        //}
    }


}
