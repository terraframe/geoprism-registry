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
import { ChangeRequest, SummaryKey } from "@registry/model/crtable";
import { GovernanceStatus } from "@registry/model/constants";
import { AuthService } from "@shared/service/auth.service";

import { ErrorHandler } from "@shared/component";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { RegistryService } from "@registry/service";
import { ChangeRequestService } from "@registry/service/change-request.service";
import { DateService } from "@shared/service/date.service";

import { LocalizationService } from "@shared/service/localization.service";

import { ControlContainer, NgForm } from "@angular/forms";
import { StandardAttributeCRModel, StandardDiffView, ListDiffView } from "./StandardAttributeCRModel";
import { ChangeRequestEditor } from "./change-request-editor";
import { ExternalId } from "@core/model/core";
import { ExternalSystemService } from "@shared/service";
import { ExternalSystem } from "@shared/model/core";

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
                transition(":leave",
                    animate("500ms",
                        style({
                            opacity: 0
                        })
                    )
                )
            ])
        ]],
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]

})
export class StandardAttributeEditorComponent implements OnInit {

    bsModalRef: BsModalRef;

    @Input() isNew: boolean = false;

    message: string = null;
    
    @Input() systems: ExternalSystem[];

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
    constructor(public cdr: ChangeDetectorRef, public service: RegistryService, public lService: LocalizationService,
        public changeDetectorRef: ChangeDetectorRef, public dateService: DateService, private authService: AuthService,
        private requestService: ChangeRequestService, private modalService: BsModalService, private elementRef: ElementRef,
        private externalSystemService: ExternalSystemService) { }

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
    
    getExternalSystemLabel(externalSystemId: string): string {
        let matches = this.systems.filter(system => externalSystemId === system.oid);
        
        if (matches.length > 0) {
            return matches[0].label.localizedValue;
        } else {
            return externalSystemId;
        }
    }
    
    getAvailableSystems(externalSystemId: string): ExternalSystem[] {
        let usedSystems: string[] = this.view.value.map((id: ExternalId) => id.externalSystemId);
        return this.systems.filter(system => (externalSystemId && externalSystemId === system.oid) || usedSystems.indexOf(system.oid) === -1);
    }
    
    removeAltId(externalId: ExternalId): void {
      let i = this.view.value.findIndex((id: ExternalId) => id.id === externalId.id && id.externalSystemId === externalId.externalSystemId);
      
      if (i !== -1) {
        this.view.value.splice(i,1);
      }
    }
    
    onAddNewId(): void {
        let es = this.getAvailableSystems(null)[0];
        
        (this.view as ListDiffView).add({
            id: "",
            externalSystemId: es.oid,
            externalSystemLabel: es.label.localizedValue,
            type: "EXTERNAL_ID"
        });
    }
    
    getExternalId(alternateIds: ExternalId[], externalSystemId: string): ExternalId {
        let ids = alternateIds.filter(id => id.externalSystemId === externalSystemId);
        
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
