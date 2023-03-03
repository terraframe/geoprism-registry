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

import { Component, Input } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { LocalizationService } from "@shared/service/localization.service";

@Component({
    selector: "error-modal",
    templateUrl: "./error-modal.component.html",
    styleUrls: ["./error-modal.css"]
})
export class ErrorModalComponent {

    /*
     * Message
     */
    @Input() message: string = this.localizeService.decode("error.modal.default.message");

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, private localizeService: LocalizationService) { }

}
