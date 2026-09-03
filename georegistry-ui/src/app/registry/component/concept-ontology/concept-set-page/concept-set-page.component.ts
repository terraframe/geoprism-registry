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

import { Component, OnInit, Output, EventEmitter, Input } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService } from "ngx-bootstrap/modal";

import { ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { ModalTypes } from "@shared/model/modal";
import * as lodash from 'lodash';

import { ManageConceptSetComponent } from "./manage-concept-set.component";
import { BsDropdownModule } from "ngx-bootstrap/dropdown";
import { NgFor, NgIf, NgClass } from "@angular/common";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { AccordionModule } from "ngx-bootstrap/accordion";
import { FormsModule } from "@angular/forms";
import { ConceptClass, ConceptEdgeType, ConceptSet } from "@registry/model/object-class";
import { ConceptSetService } from "@registry/service/concept-set.service";

enum Action {
    VIEW = 0, CREATE = 1, EDIT = 2
}

interface Selection {
    action: Action

    set?: ConceptSet;
    readOnly?: boolean
}

@Component({
    selector: "concept-set-page",
    templateUrl: "./concept-set-page.component.html",
    styleUrls: ["./concept-set-page.css"],
    standalone: true,
    imports: [FormsModule, AccordionModule, LocalizeComponent, NgFor, NgIf, NgClass, BsDropdownModule, ManageConceptSetComponent]
})
export class ConceptSetPageComponent implements OnInit {
    Action = Action;

    @Input() conceptClasses: ConceptClass[] = [];
    @Input() conceptEdgeTypes: ConceptEdgeType[] = [];
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()

    sets: ConceptSet[] = [];

    selection: Selection;

    constructor(
        public localizeService: LocalizationService,
        private modalService: BsModalService,
        private service: ConceptSetService) {
    }

    ngOnInit(): void {
        this.service.getAll().then(sets => {
            this.sets = sets;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    createConceptSet(): void {
        this.selection = {
            action: Action.CREATE,
            set: {
                code: "",
                displayLabel: this.localizeService.create(),
                description: this.localizeService.create(),
                discreteType: "",
                conceptEdgeTypes: [""],
                conceptClasses: [""],
                rootTerm: ""
            }
        };
    }

    handleTypeView(set: ConceptSet): void {
        this.service.get(set.code).then(t => {
            this.selection = {
                action: Action.VIEW,
                set: t,
                readOnly: true
            };
        }).catch(e => this.onError.emit(e))
    }

    handleEditConceptSet(set: ConceptSet): void {
        this.service.get(set.code).then(t => {
            this.selection = {
                action: Action.EDIT,
                set: t,
                readOnly: false
            };
        }).catch(e => this.onError.emit(e))
    }

    handleDeleteConceptSet(set: ConceptSet): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: false, backdrop: true, ignoreBackdropClick: true
        });
        bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + set.displayLabel.localizedValue + "]";
        bsModalRef.content.data = set.code;
        bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        bsModalRef.content.type = ModalTypes.danger;

        bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(set).then(() => {
                const sets = [...this.sets];
                const index = sets.findIndex(t => t.code === set.code);

                if (index !== -1) {
                    sets.splice(index, 1);

                    this.sets = sets;
                }

            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }


    handleTypeChange(set: ConceptSet): void {
        this.selection = null;

        const sets = [...this.sets];
        const index = sets.findIndex(t => t.code === set.code);

        if (index !== -1) {
            sets[index] = set;

            this.selection = {
                action: Action.VIEW,
                set: lodash.cloneDeep(set),
                readOnly: true
            };
        }
        else {
            sets.push(set);

            this.selection = {
                action: Action.EDIT,
                set: lodash.cloneDeep(set),
                readOnly: false
            };

        }

        this.sets = sets;
    }

    public error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
