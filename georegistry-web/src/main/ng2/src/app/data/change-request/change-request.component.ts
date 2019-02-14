import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';

import { HierarchyService } from '../../service/hierarchy.service';

import { IOService } from '../../service/io.service';
import { Hierarchy, GeoObjectType } from '../hierarchy/hierarchy';

declare var acp: string;

@Component( {
    selector: 'change-request',
    templateUrl: './change-request.component.html',
    styleUrls: []
} )
export class ChangeRequestComponent implements OnInit {

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    geoObjectTypes: GeoObjectType[] = [];
    currentGeoObjectType: GeoObjectType;
    currentGeoObjectId: string;


    constructor( private service: IOService, private modalService: BsModalService, private hierarchyService: HierarchyService ) { }

    ngOnInit(): void {
        this.hierarchyService.getGeoObjectTypes([])
            .then(types => {
                this.geoObjectTypes = types;

                this.geoObjectTypes.sort((a, b) => {
                    if (a.localizedLabel.toLowerCase() < b.localizedLabel.toLowerCase()) return -1;
                    else if (a.localizedLabel.toLowerCase() > b.localizedLabel.toLowerCase()) return 1;
                    else return 0;
                });

                let pos = this.getGeoObjectTypePosition("ROOT");
                if (pos) {
                    this.geoObjectTypes.splice(pos, 1);
                }

                // this.currentGeoObjectType = this.geoObjectTypes[1];

            }).catch((err: Response) => {
                this.error(err.json());
            });
    }

    private getGeoObjectTypePosition(code: string): number {
        for (let i = 0; i < this.geoObjectTypes.length; i++) {
            let obj = this.geoObjectTypes[i];
            if (obj.code === code) {
                return i;
            }
        }

        return null;
    }

    onSelectGeoObjectType(event: any): void {
        console.log(event)
        let selectedGeoObjectTypeCode = event.target.value;

    }

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }
}
