import { Component, OnInit, Input, EventEmitter, Output, ViewChild, OnChanges, SimpleChanges } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ParentTreeNode, GeoObject, HierarchyOverTime } from '../../model/registry';
import { RegistryService } from '../../service/registry.service';

import { ManageVersionsModalComponent } from '../geoobject-shared-attribute-editor/manage-versions-modal.component';

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
        this.calculate();

        const day = this.forDate.getUTCDate();

        this.dateStr = this.forDate.getUTCFullYear() + "-" + ( this.forDate.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
    }

    ngOnChanges( changes: SimpleChanges ) {
        this.calculate();
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

                if ( time >= startDate && time <= endDate ) {
                    object['parents'] = pot.parents;
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
            displayLabel: new LocalizedValue(),
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
        return this.getParents() != null;
    }

    public getHierarchies(): any {
        return this.hierarchies;
    }

    public getParents(): any {
        return CascadingGeoSelector.staticGetParents( this.hierarchies );
    }

    public static staticGetParents( hierarchies: any ): ParentTreeNode {
        //        let ptn = new ParentTreeNode();
        //        ptn.parents = [];
        //
        //        for ( var i = 0; i < hierarchies.length; ++i ) {
        //            let hierarchy: any = hierarchies[i];
        //
        //            if ( hierarchy.parents.length > 0 ) {
        //                let parent: any = hierarchy.parents[hierarchy.parents.length - 1];
        //
        //                if ( parent.ptn != null && parent.geoObject != null && parent.geoObject.properties.code.length > 0 ) {
        //                    ptn.parents.push( parent.ptn );
        //                }
        //            }
        //        }
        //
        //        if ( ptn.parents.length > 0 ) {
        //            return ptn;
        //        }
        //        else {
        return null;
        //        }
    }

    onManageAttributeVersions( attribute: any ): void {
        this.bsModalRef = this.modalService.show( ManageVersionsModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );

        // TODO: sending the properties like this is wrong
        // this.bsModalRef.content.geoObject = this.preGeoObject;
        // this.bsModalRef.content.geoObjectType = this.geoObjectType;
        // this.bsModalRef.content.attributeCode = attribute.code;
        // this.bsModalRef.content.attribute = attribute;
        // this.bsModalRef.content.onAttributeVersionChange.subscribe( versionObj => {
        //     console.log(versionObj)

        // TODO: set the version on the GeoObject attribute
        // } );
    }

    public error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            let bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            bsModalRef.content.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
