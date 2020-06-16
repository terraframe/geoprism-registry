import { Component, OnInit, ViewChild, SimpleChanges, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService } from '../../service/registry.service';

import { Map, LngLatBounds, NavigationControl } from 'mapbox-gl';
import * as MapboxDraw from '@mapbox/mapbox-gl-draw';
import * as StaticMode from '@mapbox/mapbox-gl-draw-static-mode';
import * as mapboxgl from 'mapbox-gl';

declare var acp: string;


@Component( {
    selector: 'geoobject-editor-map[geometryType]',
    templateUrl: './geoobject-editor-map.component.html',
    styleUrls: ['./geoobject-editor-map.component.css']
} )

/**
 * This component is used in the master list when editing a row. In the future it will also be used by the navigator and has
 * potential to also be used in the submit change request and manage change requests.
 */
export class GeoObjectEditorMapComponent implements OnInit, OnDestroy {

    /*
     * Required. The GeometryType of the GeoJSON. Expected to be in uppercase (because that's how it is in the GeoObjectType for some reason)
     */
    @Input() geometryType: string;

    /*
     * Optional. We will invoke this event with GeoJSON when the user makes an edit to the geometry.
     */
    @Output() geometryChange = new EventEmitter<any>();

    /*
     * Optional. If specified, we will diff based on this GeoJSON geometry.
     */
    @Input() preGeometry: any;

    /*
     * Optional. If we are read-only, this will be displayed as a layer. If we are not, it will be editable.
     */
    @Input() postGeometry: any;

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

    /*
     * Optional. If specified, we will display an edit button on the map, and when it is clicked we will emit this event.
     */
    @Output() onClickEdit = new EventEmitter<void>();

    @ViewChild( "simpleEditControl" ) simpleEditControl;

    @ViewChild( "mapDiv" ) mapDiv;

    map: Map;

    editingControl: any;

    constructor( private registryService: RegistryService ) {

    }

    ngOnInit(): void {
    }

    ngAfterViewInit() {
        setTimeout(() => {
            //this.registryService.getGeoObjectOverTime( "22", "Province" )
            //.then( geoObject => {

            ( mapboxgl as any ).accessToken = 'pk.eyJ1IjoidGVycmFmcmFtZSIsImEiOiJjanZxNTFnaTYyZ2RuNDlxcmNnejNtNjN6In0.-kmlS8Tgb2fNc1NPb5rJEQ';

            this.mapDiv.nativeElement.id = Math.floor( Math.random() * ( 899999 ) ) + 100000;

            this.map = new Map( {
                container: this.mapDiv.nativeElement.id,
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

    ngOnChanges( changes: SimpleChanges ) {
        if ( changes['preGeometry'] || changes['postGeometry'] ) {
            this.reload();
        }
    }

    public reload(): void {
        if ( this.map != null ) {
            this.removeLayers();
            this.addLayers();
        }
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
        this.geometryChange.emit( this.saveDraw() );
    }

    initMap(): void {

        this.map.on( 'style.load', () => {
            this.addLayers();
            this.onValidChange();
        } );


        this.addLayers();

        if ( this.preGeometry != null && this.preGeometry !== "" ) {
            this.zoomToBbox();
        }

        // Add zoom and rotation controls to the map.
        this.map.addControl( new NavigationControl() );

        if ( !this.readOnly ) {
            this.enableEditing();
        }
        else {
            this.addEditButton();
        }

        this.onValidChange();
    }

    addEditButton(): void {
        this.simpleEditControl.editEmitter.subscribe( versionObj => {
            this.onClickEdit.emit();
        } );

        this.map.addControl( this.simpleEditControl );
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

        if ( this.postGeometry != null ) {
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
        else if ( this.geometryType === "LINE" || this.geometryType === "MultiLine" ) {
            this.map.removeLayer( sourceName + "-line" );
        }

        this.map.removeSource( sourceName );
    }

    removeLayers(): void {
        if ( this.map.getSource( "pre-geoobject" ) ) {
            this.removeSource( "pre" );
        }
        if ( this.map.getSource( "post-geoobject" ) ) {
            this.removeSource( "post" );
        }
    }

    addLayers(): void {
        if ( this.preGeometry != null && this.preGeometry !== "" ) {
            this.renderGeometryAsLayer( this.preGeometry, "pre", "#EFA22E" )
        }
        if ( this.readOnly && this.postGeometry != null && this.postGeometry !== "") {
            this.renderGeometryAsLayer( this.postGeometry, "post", "#3368EF" );
        }
    }

    renderGeometryAsLayer( geometry: any, prefix: string, color: string ) {
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
        if ( this.bboxCode != null && this.bboxType != null ) {
            if ( this.bboxDate == null ) {
                this.registryService.getGeoObjectBounds( this.bboxCode, this.bboxType ).then( boundArr => {
                    let bounds = new LngLatBounds( [boundArr[0], boundArr[1]], [boundArr[2], boundArr[3]] );

                    this.map.fitBounds( bounds, { padding: 50 } );
                } ).catch(( err: HttpErrorResponse ) => {
                    this.error( err );
                } );
            }
            else {
                this.registryService.getGeoObjectBoundsAtDate( this.bboxCode, this.bboxType, this.bboxDate ).then( boundArr => {
                    let bounds = new LngLatBounds( [boundArr[0], boundArr[1]], [boundArr[2], boundArr[3]] );

                    this.map.fitBounds( bounds, { padding: 50 } );
                } ).catch(( err: HttpErrorResponse ) => {
                    this.error( err );
                } );
            }
        }
    }

    saveDraw(): any {
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
