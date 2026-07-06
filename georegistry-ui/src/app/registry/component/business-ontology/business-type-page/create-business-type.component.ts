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

import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";

import { LocalizationService } from "@shared/service/localization.service";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { Organization, OrganizationGroup } from "@shared/model/core";
import { LocalizedTextComponent } from "../../form-fields/localized-text/localized-text.component";
import { ConvertKeyLabel } from "@shared/component/localize/convert-key-label.component";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { NgIf, NgFor } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { BusinessType } from "@registry/model/object-class";

@Component({
    selector: "create-business-type",
    templateUrl: "./create-business-type.component.html",
    styleUrls: [],
    standalone: true,
    imports: [FormsModule, LocalizeComponent, NgFor, ConvertKeyLabel, LocalizedTextComponent]
})
export class CreateBusinessTypeComponent implements OnInit {

    @Input() organization: Organization = null;

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<BusinessType> = new EventEmitter<BusinessType>()

    type: BusinessType = null;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: BusinessTypeService, private lService: LocalizationService, public bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.type = {
            code: "",
            organization: "",
            displayLabel: this.lService.create(),
            description: this.lService.create(),
            attributes: [],
            labelAttribute: "",
            organizationLabel: ""
        };

        this.type.organization = this.organization.code;
        this.type.organizationLabel = this.organization.label.localizedValue;
    }

    handleOnSubmit(): void {

        this.service.apply(this.type).then(data => {
            this.typeChange.next(data);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.onError.emit(err);
        });
    }

    handleCancel(): void {
        this.onCancel.emit();
    }
}
