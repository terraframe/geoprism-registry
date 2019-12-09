import { Component, OnInit, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';

import { HierarchyOverTime } from '../../model/registry';

import { RegistryService } from '../../service/registry.service';

import { IOService } from '../../service/io.service';
import { LocalizationService } from '../../../shared/service/localization.service';


@Component( {
    selector: 'manage-parent-versions-modal',
    templateUrl: './manage-parent-versions-modal.component.html',
    styleUrls: []
} )
export class ManageParentVersionsModalComponent implements OnInit {
    message: string = null;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
    onVersionChange: Subject<HierarchyOverTime>;

    hierarchy: HierarchyOverTime = null;

    hasDuplicateDate: boolean = false;


    constructor( private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {

        this.onVersionChange = new Subject();

    }

    init( hierarchy: HierarchyOverTime ): void {
        this.hierarchy = hierarchy;
    }

    onAddNewVersion(): void {

        let parents = {};

        for ( let i = 0; i < this.hierarchy.types.length; i++ ) {
            let current = this.hierarchy.types[i];

            parents[current.code] = {};
        }

        const entry = {
            startDate: null,
            endDate: null,
            parents: parents
        }

        this.hierarchy.entries.push( entry );
    }

    remove( entry: any ): void {

        for ( let i = 0; i < this.hierarchy.entries.length; i++ ) {
            let vals = this.hierarchy.entries[i];

            if ( vals.startDate === entry.startDate ) {
                this.hierarchy.entries.splice( i, 1 );
            }
        }

        this.snapDates();
    }

    getTypeAheadObservable( date: string, type: any, entry: any, index: number ): Observable<any> {

        let geoObjectTypeCode = type.code;

        let parentCode = null;
        let hierarchyCode = null;

        if ( index > 0 ) {
            let pType = this.hierarchy.types[index - 1];
            const parent = entry.parents[pType.code];

            if ( parent.geoObject != null && parent.geoObject.properties.code != null ) {
                hierarchyCode = this.hierarchy.code;
                parentCode = parent.geoObject.properties.code;
            }
        }

        return Observable.create(( observer: any ) => {
            this.service.getGeoObjectSuggestions( entry.parents[type.code].text, geoObjectTypeCode, parentCode, hierarchyCode, date ).then( results => {
                observer.next( results );
            } );
        } );
    }

    typeaheadOnSelect( e: TypeaheadMatch, type: any, entry: any, date: string ): void {
        //        let ptn: ParentTreeNode = parent.ptn;

        let parentTypes = [];

        for ( let i = 0; i < this.hierarchy.types.length; i++ ) {
            let current = this.hierarchy.types[i];

            parentTypes.push( current.code );

            if ( current.code === type.code ) {
                break;
            }
        }

        this.service.getParentGeoObjects( e.item.uid, type.code, parentTypes, true, date ).then( ancestors => {

            entry.parents[type.code].geoObject = ancestors.geoObject;
            entry.parents[type.code].text = ancestors.geoObject.properties.displayLabel.localizedValue;

            for ( let i = 0; i < this.hierarchy.types.length; i++ ) {
                let current = this.hierarchy.types[i];
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
                    entry.parents[current.code].geoObject = ancestor.geoObject;
                    entry.parents[current.code].text = ancestor.geoObject.properties.displayLabel.localizedValue;
                }
            }

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    onSubmit(): void {

        this.onVersionChange.next( this.hierarchy );

        this.bsModalRef.hide();
    }

    onDateChange( event: any ): any {
        this.snapDates();
    }

    snapDates() {
        var dateOffset = ( 24 * 60 * 60 * 1000 ) * 1; //1 days

        this.hasDuplicateDate = false;

        // Sort the data
        this.hierarchy.entries.sort( function( a, b ) {

            if ( a.startDate == null || a.startDate === '' ) {
                return 1;
            }
            else if ( b.startDate == null || b.startDate === '' ) {
                return -1;
            }

            let first: any = new Date( a.startDate );
            let next: any = new Date( b.startDate );
            return first - next;
        } );


        for ( let i = 1; i < this.hierarchy.entries.length; i++ ) {
            let prev = this.hierarchy.entries[i - 1];
            let current = this.hierarchy.entries[i];

            prev.endDate = this.formatDateString( new Date( new Date( current.startDate ).getTime() - dateOffset ) );

            if ( prev.startDate === current.startDate ) {
                this.hasDuplicateDate = true;
            }
        }

        if ( this.hierarchy.entries.length > 0 ) {
            this.hierarchy.entries[this.hierarchy.entries.length - 1].endDate = '5000-12-31';
        }
    }

    formatDateString( dateObj: Date ): string {
        const day = dateObj.getUTCDate();

        return dateObj.getUTCFullYear() + "-" + ( dateObj.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
    }


    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
