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

import {
    Component,
    OnInit,
    Input,
    Output,
    ChangeDetectorRef,
    EventEmitter,
    ElementRef
} from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { HttpErrorResponse } from "@angular/common/http";
import { GeoObjectType, AttributeType, GeoObjectOverTime } from "@registry/model/registry";
import { ChangeRequest } from "@registry/model/crtable";
import { GovernanceStatus } from "@registry/model/constants";

import { ErrorHandler } from "@shared/component";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ChangeRequestService } from "@registry/service/change-request.service";

import { LocalizationService } from "@shared/service/localization.service";

import { ControlContainer, NgForm, FormsModule } from "@angular/forms";
import { StandardAttributeCRModel, StandardDiffView, ListDiffView } from "./StandardAttributeCRModel";
import { ChangeRequestEditor } from "./change-request-editor";
import { ExternalId } from "@core/model/core";
import { LocalizePipe } from "@shared/pipe/localize.pipe";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { NgIf, NgFor, NgClass } from "@angular/common";
import { SourceAuthority } from "@registry/model/source";
import { UniqueAuthorityValidatorDirective } from "./unique-authority-validator.directive";

@Component({
    selector: "standard-attribute-editor",
    templateUrl: "./standard-attribute-editor.component.html",
    styleUrls: ["./standard-attribute-editor.component.css"],
    host: { "[@fadeInOut]": "true" },
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
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }],
    standalone: true,
    imports: [NgIf, LocalizeComponent, NgFor, NgClass, FormsModule, LocalizePipe, UniqueAuthorityValidatorDirective]
})
export class StandardAttributeEditorComponent implements OnInit {

    bsModalRef: BsModalRef;

    @Input() isNew: boolean = false;

    message: string = null;

    @Input() authorities: SourceAuthority[] = [];

    isValid: boolean = true;
    @Output() isValidChange = new EventEmitter<boolean>();

    @Input() readonly: boolean = false;

    @Input() attributeType: AttributeType;

    @Input() changeRequest: ChangeRequest;

    @Input() geoObjectType: GeoObjectType;

    @Input() geoObject: GeoObjectOverTime;

    @Input() isNewGeoObject: boolean = false;

    view: StandardDiffView;

    @Input() changeRequestEditor: ChangeRequestEditor;

    changeRequestAttributeEditor: StandardAttributeCRModel;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private lService: LocalizationService,
        private requestService: ChangeRequestService,
        private modalService: BsModalService) { }

    ngOnInit(): void {
        this.changeRequestAttributeEditor = this.changeRequestEditor.getEditorForAttribute(this.attributeType, null) as StandardAttributeCRModel;
        this.calculateView();
    }

    ngAfterViewInit() {
    }

    calculateView(): void {
        if (this.attributeType.type === 'list' && this.attributeType.code === 'altIds') {
            this.view = new ListDiffView(this.lService, this.changeRequestAttributeEditor);
        } else {
            this.view = new StandardDiffView(this.changeRequestAttributeEditor, this.lService);
        }
    }

    getExternalSystemLabel(code: string): string {
        let matches = this.authorities.filter(authority => code === authority.code);

        if (matches.length > 0) {
            return matches[0].label.localizedValue;
        } else {
            return code;
        }
    }

    removeAltId(externalId: ExternalId): void {
        let i = this.view.value.findIndex((id: ExternalId) => id.id === externalId.id && id.authority === externalId.authority);

        if (i !== -1) {
            this.view.value.splice(i, 1);
        }
    }

    onAddNewId(): void {
        (this.view as ListDiffView).add({
            id: "",
            authority: "",
            authorityLabel: "",
            type: "EXTERNAL_ID"
        });
    }

    getExternalId(alternateIds: ExternalId[], authority: string): ExternalId {
        let ids = alternateIds.filter(id => id.authority === authority);

        if (ids.length >= 0) {
            return ids[0];
        } else {
            return null;
        }
    }

    hasAlternateIdChanged(viewModel: StandardDiffView, externalSystemId: string): boolean {
        return viewModel.oldValue != null && this.getExternalId(viewModel.oldValue, externalSystemId).id !== this.getExternalId(viewModel.value, externalSystemId).id;
    }

    onValueChange(): void {
        this.calculateView();
    }

    onApprove(): void {
        let editAction = this.changeRequestAttributeEditor.editAction;

        this.requestService.setActionStatus(editAction.oid, GovernanceStatus.ACCEPTED).then(results => {
            editAction.approvalStatus = GovernanceStatus.ACCEPTED;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onReject(): void {
        let editAction = this.changeRequestAttributeEditor.editAction;

        this.requestService.setActionStatus(editAction.oid, GovernanceStatus.REJECTED).then(results => {
            editAction.approvalStatus = GovernanceStatus.REJECTED;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onPending(): void {
        let editAction = this.changeRequestAttributeEditor.editAction;

        this.requestService.setActionStatus(editAction.oid, GovernanceStatus.PENDING).then(results => {
            editAction.approvalStatus = GovernanceStatus.PENDING;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
