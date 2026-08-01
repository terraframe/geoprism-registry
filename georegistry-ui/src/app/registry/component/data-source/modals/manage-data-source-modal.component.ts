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

import { Component, OnInit } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { DataSource, SourceAuthority } from "@registry/model/source";
import { DataSourceService } from "@registry/service/data-source.service";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { FormsModule } from "@angular/forms";
import { NgFor, NgIf } from "@angular/common";
import { SourceAuthorityService } from "@registry/service/source-authority.service";
import { LocalizedInputComponent } from "@registry/component/form-fields/localized-input/localized-input.component";

@Component({
    selector: "manage-data-source-modal",
    templateUrl: "./manage-data-source-modal.component.html",
    styleUrls: [],
    // host: { '[@fadeInOut]': 'true' },
    animations: [
        [
            trigger("fadeInOut", [
                transition("void => *", [
                    style({
                        opacity: 0
                    }),
                    animate("500ms")
                ]),
                transition(":leave", animate("500ms", style({
                    opacity: 0
                })))
            ])
        ]
    ],
    standalone: true,
    imports: [NgIf, NgFor, FormsModule, LocalizeComponent, LocalizedInputComponent]
})
export class ManageDataSourceModalComponent implements OnInit {

    message: string = null;

    authorities: SourceAuthority[] = [];
    source: DataSource;
    public onSourceChange: Subject<DataSource>;
    readOnly: boolean = false;

    constructor(
        private service: DataSourceService,
        private authorityService: SourceAuthorityService,
        private bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.onSourceChange = new Subject();
    }

    init(source: DataSource, readOnly: boolean) {
        this.source = source;
        this.readOnly = readOnly;

        this.authorityService.getAll().then(authorities => {
            this.authorities = authorities;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleSourceChange(): void {
        this.onSourceChange.next(this.source);
    }

    update(): void {
        this.service.apply(this.source).then(type => {
            this.onSourceChange.next(type);

            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    close(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
