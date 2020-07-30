import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from '@shared/component';
import { HierarchyType } from '@registry/model/hierarchy';
import { RegistryService, HierarchyService } from '@registry/service';

import { LocalizationService, AuthService } from '@shared/service';


@Component( {
    selector: 'create-hierarchy-type-modal',
    templateUrl: './create-hierarchy-type-modal.component.html',
    styleUrls: []
} )
export class CreateHierarchyTypeModalComponent implements OnInit {

    hierarchyType: HierarchyType;
    organizations: any = [];
    message: string = null;
    
    edit: boolean = false; // if true, we are updating an existing. If false, we are creating new
    
    readOnly: boolean = false;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onHierarchytTypeCreate: Subject<HierarchyType>;

    constructor( private lService: LocalizationService, private auth: AuthService, private registryService: RegistryService, private hierarchyService: HierarchyService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onHierarchytTypeCreate = new Subject();

        this.hierarchyType = {
            "code": "",
            "label": this.lService.create(),
            "description": this.lService.create(),
            "rootGeoObjectTypes": [],
            "organizationCode": ""
        };
        
        this.registryService.getOrganizations().then(orgs => {
        
          // Filter out organizations they're not RA's of, unless we're readOnly.
          if (!this.readOnly)
          {
            this.organizations = [];
            
            for (var i = 0; i < orgs.length; ++i)
            {
              if (this.auth.isOrganizationRA(orgs[i].code))
              {
                this.organizations.push(orgs[i]);
              }
            }
          }
          else
          {
            this.organizations = orgs;
          }
          
          if (!this.edit && this.organizations.length === 1)
          {
            this.hierarchyType.organizationCode = this.organizations[0].code;
          }
          
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleOnSubmit(): void {
        this.message = null;
        
        if (this.readOnly)
        {
          this.bsModalRef.hide();
          return;
        }

        if ( this.edit ) {
            this.hierarchyService.updateHierarchyType( JSON.stringify( this.hierarchyType ) ).then( data => {
                this.onHierarchytTypeCreate.next( data );
                this.bsModalRef.hide();
            } ).catch(( err: HttpErrorResponse) => {
                this.error( err );
            } );
        }
        else {
            this.hierarchyService.createHierarchyType( JSON.stringify( this.hierarchyType ) ).then( data => {
                this.onHierarchytTypeCreate.next( data );
                this.bsModalRef.hide();
            } ).catch(( err: HttpErrorResponse) => {
                this.error( err );
            } );
        }
    }

    error( err: HttpErrorResponse ): void {
            this.message = ErrorHandler.getMessageFromError(err);
    }

}
