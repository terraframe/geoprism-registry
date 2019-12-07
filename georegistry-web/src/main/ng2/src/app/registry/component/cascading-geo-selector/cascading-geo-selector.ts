import { Component, OnInit, Input, EventEmitter, Output, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ParentTreeNode, GeoObject, HierarchyOverTime } from '../../model/registry';
import { RegistryService } from '../../service/registry.service';

import { ManageParentVersionsModalComponent } from './manage-parent-versions-modal.component';

import { LocalizedValue } from '../../../shared/model/core';
import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';

@Component( {

    selector: 'cascading-geo-selector',
    templateUrl: './cascading-geo-selector.html',
} )
export class CascadingGeoSelector {

    @Input() hierarchies: HierarchyOverTime[];

    @Output() valid = new EventEmitter<boolean>();

    @Input() isValid: boolean = true;

    @ViewChild( "mainForm" ) mainForm;

    @Input() forDate: Date = new Date();

    dateStr: string;

    cHierarchies: any[] = [];

    parentMap: any = {};

    bsModalRef: BsModalRef;

    constructor( private modalService: BsModalService, private registryService: RegistryService ) {

    }

    ngOnInit(): void {
        const day = this.forDate.getUTCDate();

        this.dateStr = this.forDate.getUTCFullYear() + "-" + ( this.forDate.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
        
        // Truncate any hours/minutes/etc which may be part of the date
        this.forDate = new Date( Date.parse( this.dateStr ) );

        this.calculate();
    }

    ngOnChanges( changes: SimpleChanges ) {

        if ( changes['forDate'] ) {
            this.calculate();
        }
    }

    calculate(): any {
        const time = this.forDate.getTime();

        this.cHierarchies = [];
        this.hierarchies.forEach( hierarchy => {
            const object = {};
            object['label'] = hierarchy.label;
            object['code'] = hierarchy.code;

            hierarchy.entries.forEach( pot => {
                const startDate = Date.parse( pot.startDate );
                const endDate = Date.parse( pot.endDate );

                console.log( "Date", this.dateStr );
                console.log( "Start Date", pot.startDate );
                console.log( "End Date", pot.endDate );

                console.log( "Date", time );
                console.log( "Start Date", startDate );
                console.log( "End Date", endDate );

                if ( time >= startDate && time <= endDate ) {
                    let parents = [];

                    hierarchy.types.forEach( type => {
                        let parent: any = {
                            code: type.code,
                            label: type.label
                        }

                        if ( pot.parents[type.code] != null ) {
                            parent.text = pot.parents[type.code].text;
                            parent.geoObject = pot.parents[type.code].geoObject;
                        }

                        parents.push( parent );
                    } );

                    object['parents'] = parents;
                }
            } );

            this.cHierarchies.push( object );
        } );
    }



    valueChange( newValue: string, index: number, hierarchy: any ): void {
        let invalid: boolean = false;

        for ( let i = index; i < hierarchy.parents.length; ++i ) {
            let parent = hierarchy.parents[i];

            parent.geoObject = this.newGeoObject();

            if ( i === index ) {
                parent.geoObject.properties.displayLabel.localizedValue = newValue;
            }

            invalid = true;
        }

        this.valid.emit();
    }

    private newGeoObject(): GeoObject {
        let go = new GeoObject();
        go.properties = {
            uid: "",
            code: "",
            displayLabel: new LocalizedValue( null, null ),
            type: "",
            status: [""],
            sequence: "",
            createDate: "",
            lastUpdateDate: ""
        };

        return go;
    }

    getTypeAheadObservable( text: string, parent: any, hierarchy: any, index: number ): Observable<any> {

        let geoObjectTypeCode = parent.code;

        let parentCode = null;
        let hierarchyCode = null;

        if ( index > 0 ) {
            let parentParentType = hierarchy.parents[index - 1];

            if ( parentParentType.geoObject.properties.code != null ) {

                hierarchyCode = hierarchy.code;
                parentCode = parentParentType.geoObject.properties.code;
            }
        }

        return Observable.create(( observer: any ) => {
            this.registryService.getGeoObjectSuggestions( text, geoObjectTypeCode, parentCode, hierarchyCode, this.dateStr ).then( results => {
                observer.next( results );
            } );
        } );
    }

    typeaheadOnSelect( e: TypeaheadMatch, parent: any, hierarchy: any ): void {
        //        let ptn: ParentTreeNode = parent.ptn;

        let parentTypes = [];

        for ( let i = 0; i < hierarchy.parents.length; i++ ) {
            let current = hierarchy.parents[i];

            parentTypes.push( current.code );

            if ( current.code === parent.code ) {
                break;
            }
        }

        this.registryService.getParentGeoObjects( e.item.uid, parent.code, parentTypes, true, this.dateStr ).then( ancestors => {

            parent.geoObject = ancestors.geoObject;
            parent.text = ancestors.geoObject.properties.displayLabel.localizedValue;

            for ( let i = 0; i < hierarchy.parents.length; i++ ) {
                let current = hierarchy.parents[i];
                let ancestor = ancestors;

                while ( ancestor != null && ancestor.geoObject.properties.type != current.code ) {
                    if ( ancestor.parents.length > 0 ) {
                        ancestor = ancestor.parents[0];
                    }
                    else {
                        ancestor = null;
                    }
                }

                if ( ancestor != null ) {
                    current.geoObject = ancestor.geoObject;
                    current.text = ancestor.geoObject.properties.displayLabel.localizedValue;
                }
            }

            this.valid.emit();
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    public getIsValid(): boolean {
        return true;
    }

    public getHierarchies(): any {
        return this.hierarchies;
    }

    onManageVersions( code: string ): void {

        const hierarchy = this.hierarchies.find( h => h.code === code );

        this.bsModalRef = this.modalService.show( ManageParentVersionsModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.init( hierarchy );
        this.bsModalRef.content.onVersionChange.subscribe( hierarchy => {
            this.calculate();
        } );
    }

    public error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            let bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            bsModalRef.content.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
