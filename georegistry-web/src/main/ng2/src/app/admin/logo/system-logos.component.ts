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

import { SystemLogo } from './system-logo';
import { SystemLogoService } from './system-logo.service';

declare let acp: string;

@Component({
  
  selector: 'system-logos',
  templateUrl: './system-logos.component.html',
  styleUrls: []
})
export class SystemLogosComponent implements OnInit {
  public icons: SystemLogo[];
  context: string;

  constructor(
    private router: Router,
    private service: SystemLogoService) {
	  
    this.context = acp as string;    
  }

  ngOnInit(): void {
    this.getIcons();    
  }
    
  getIcons() : void {
    this.service.getIcons().then(icons => {
      this.icons = icons        
    })
  }
  
  edit(icon: SystemLogo) : void {
    this.router.navigate(['/admin/logo', icon.oid]);
  }  
  
  remove(icon: SystemLogo) : void {
    this.service.remove(icon.oid).then(response => {
      icon.custom = false;
    });
  }  
}
