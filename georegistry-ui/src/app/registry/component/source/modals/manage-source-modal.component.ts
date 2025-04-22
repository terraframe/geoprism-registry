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
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { Source } from "@registry/model/source";
import { SourceService } from "@registry/service/source.service";
import { LocalizationService } from "@shared/service/localization.service";

@Component({
    selector: "manage-source-modal",
    templateUrl: "./manage-source-modal.component.html",
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
                transition(":leave",
                    animate("500ms",
                        style({
                            opacity: 0
                        })
                    )
                )
            ])
        ]]
})
export class ManageSourceModalComponent implements OnInit {

    message: string = null;
    source: Source;
    public onSourceChange: Subject<Source>;
    readOnly: boolean = false;

    constructor(public service: SourceService, private localizationService: LocalizationService, private modalService: BsModalService, public bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.onSourceChange = new Subject();
    }

    init(source: Source, readOnly: boolean) {
        this.source = source;
        this.readOnly = readOnly;
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
