import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService } from "@registry/service";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { ProgrammaticTypeService } from "@registry/service/programmatic-type.service";
import { ProgrammaticType, ProgrammaticTypeByOrg } from "@registry/model/programmatic-type";
import { CreateProgrammaticTypeModalComponent } from "./modals/create-programmatic-type-modal.component";
import { ManageProgrammaticTypeModalComponent } from "./modals/manage-programmatic-type-modal.component";

@Component({
    selector: "programmatic-type-manager",
    templateUrl: "./programmatic-type-manager.component.html",
    styleUrls: ["./programmatic-type-manager.css"]
})
export class ProgrammaticTypeManagerComponent implements OnInit {

    message: string = null;
    orgs: ProgrammaticTypeByOrg[];

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(public service: ProgrammaticTypeService, private modalService: BsModalService, private router: Router, private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.service.getByOrganization().then(orgs => {
            this.orgs = orgs;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreate(org: ProgrammaticTypeByOrg): void {
        this.bsModalRef = this.modalService.show(CreateProgrammaticTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(org);
        this.bsModalRef.content.onProgrammaticTypeChange.subscribe((type: ProgrammaticType) => {
            org.types.push(type);
        });
    }

    onView(type: ProgrammaticType): void {
        this.service.edit(type.oid).then(t => {
            this.bsModalRef = this.modalService.show(ManageProgrammaticTypeModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(t, true);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onEdit(org: ProgrammaticTypeByOrg, type: ProgrammaticType): void {
        this.service.edit(type.oid).then(t => {
            this.bsModalRef = this.modalService.show(ManageProgrammaticTypeModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(t, false);

            this.bsModalRef.content.onProgrammaticTypeChange.subscribe(t => {
                const index = org.types.findIndex((tt) => type.code === tt.code);

                if(index !== -1) {
                    org.types[index] = t;
                }
            });
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onDelete(org: ProgrammaticTypeByOrg, type: ProgrammaticType): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + type.displayLabel.localizedValue + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(type).then(() => {
                org.types = org.types.filter((t) => {
                    return t.code !== type.code;
                });
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
