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
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { ImportHistory } from "@registry/model/registry";
import { LocalizedValue } from "@core/model/core";

@Component({
    selector: "import-history-modal",
    templateUrl: "./import-history-modal.component.html",
    styleUrls: ["./import-history-modal.css"],
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
export class ImportHistoryModalComponent implements OnInit {

    message: string = null;

    label: LocalizedValue;
    histories: ImportHistory[];

    constructor(public bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
    }

    init(label: LocalizedValue, histories: ImportHistory[]) {
        this.label = label;
        this.histories = histories;
    }

    close(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
