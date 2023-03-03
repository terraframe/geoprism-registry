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

import { Component, Input, OnDestroy, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { LocalizationService } from "@shared/service/localization.service";
import { ModalTypes } from "@shared/model/modal";

@Component({
    selector: "confirm-modal",
    templateUrl: "./confirm-modal.component.html",
    styleUrls: ["./modal.css"]
})
export class ConfirmModalComponent implements OnInit, OnDestroy {

    /*
     * Message
     */
    @Input() message: string = this.localizeService.decode("confirm.modal.default.message");

    @Input() data: any;

    @Input() submitText: string = this.localizeService.decode("modal.button.submit");

    @Input() cancelText: string = this.localizeService.decode("modal.button.cancel");

    @Input() type: ModalTypes = ModalTypes.warning;

    /*
     * Called on confirm
     */
    public onConfirm: Subject<any>;

    /*
     * Called on cancel
     */
    public onCancel: Subject<any>;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.onConfirm = new Subject();
        this.onCancel = new Subject();
    }

    ngOnDestroy(): void {
        this.onConfirm.unsubscribe();
        this.onCancel.unsubscribe();
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onConfirm.next(this.data);
    }

    cancel(): void {
        this.bsModalRef.hide();
        this.onCancel.next(this.data);
    }

}
