///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { LocalizationService } from '../../../shared/service/localization.service';

import { SystemLogo } from '../../model/system-logo';
import { SystemLogoService } from '../../service/system-logo.service';

declare let acp: string;

@Component({
  
  selector: 'system-logos',
  templateUrl: './system-logos.component.html',
  styleUrls: []
})
export class SystemLogosComponent implements OnInit {
  public icons: SystemLogo[];
  context: string;
  bsModalRef: BsModalRef;
  message: string = null;

  constructor(
    private router: Router,
    private service: SystemLogoService,
    private modalService: BsModalService,
    private localizeService: LocalizationService) {
	  
    this.context = acp as string;    
  }

  ngOnInit(): void {
    this.getIcons();    
  }
  
  onClickRemove(icon) : void {
    this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
        animated: true,
        backdrop: true,
        ignoreBackdropClick: true,
    } );
    this.bsModalRef.content.message = this.localizeService.decode( "system.image.removeContent" );
    this.bsModalRef.content.submitText = this.localizeService.decode( "modal.button.delete" );

    this.bsModalRef.content.onConfirm.subscribe( data => {
      this.remove(icon);
    } );
  }
    
  getIcons() : void {
    this.service.getIcons().then(icons => {
      this.icons = icons        
    })
    .catch(( err: HttpErrorResponse ) => {
      this.error( err );
    } );
  }
  
  edit(icon: SystemLogo) : void {
    this.router.navigate(['/admin/logo', icon.oid]);
  }  
  
  remove(icon: SystemLogo) : void {
    this.service.remove(icon.oid).then(response => {
      icon.custom = false;
    })
    .catch(( err: HttpErrorResponse ) => {
      this.error( err );
    } );
  }  
  
  error( err: HttpErrorResponse ): void {
    // Handle error
    if ( err !== null ) {
      this.message = ( err.error.localizedMessage || err.error.message || err.message );
    }
  }
}
