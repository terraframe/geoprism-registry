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

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from '../../../shared/component/error-handler/error-handler';

import { ForgotPasswordService } from '../../service/forgotpassword.service';


@Component({
  templateUrl: './forgotpassword-complete.component.html',
  styleUrls: ['./forgotpassword-complete.component.css']
})
export class ForgotPasswordCompleteComponent implements OnInit {
  newPassword: string;
  token: string;
  passwordIsReset: boolean = false;
  private sub: any;
  message: string = null;

  constructor(
    private service: ForgotPasswordService,
    private router: Router,      
    private route: ActivatedRoute) {
  }
  
  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
       this.token = params['token'];
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }
  
  
  cancel(): void {    
    this.router.navigate(['/']);
  } 
  
  onSubmit(): void {
    this.service.complete(this.newPassword, this.token)
      .then(() => {
        this.passwordIsReset = true;
      })
      .catch(( err: HttpErrorResponse ) => {
        this.error( err );
      } );
  }
  
  error( err: HttpErrorResponse ): void {
      this.message = ErrorHandler.getMessageFromError(err);
  }
}
