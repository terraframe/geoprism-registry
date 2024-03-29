///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { Location } from "@angular/common";
import { HttpErrorResponse } from "@angular/common/http";

import { ForgotPasswordService } from "@core/service/forgotpassword.service";

import { ErrorHandler } from "@shared/component";

@Component({
    selector: "forgotpassword",
    templateUrl: "./forgotpassword.component.html",
    styleUrls: ["./forgotpassword.component.css"]
})
export class ForgotPasswordComponent {

    username: string;
    emailIsSent: boolean = false;
    message: string = null;

    constructor(private service: ForgotPasswordService,
        private router: Router,
        private route: ActivatedRoute,
        private location: Location) {}

    cancel(): void {
        this.router.navigate(["/"]);
    }

    onSubmit(): void {
        this.service.submit(this.username)
            .then(response => {
                this.emailIsSent = true;
            })
            .catch((err: HttpErrorResponse) => {
                this.error(err);
            });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
