import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";

import { LocalizationService } from "@shared/service";
import { Organization } from "@shared/model/core";
import { ProgrammaticTypeService } from "@registry/service/programmatic-type.service";
import { ProgrammaticType } from "@registry/model/programmatic-type";

@Component({
    selector: "create-programmatic-type-modal",
    templateUrl: "./create-programmatic-type-modal.component.html",
    styleUrls: []
})
export class CreateProgrammaticTypeModalComponent implements OnInit {

    type: ProgrammaticType;
    organization: Organization = null;
    message: string = null;
    organizationLabel: string;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful
     */
    public onProgrammaticTypeChange: Subject<ProgrammaticType>;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: ProgrammaticTypeService, private lService: LocalizationService, public bsModalRef: BsModalRef) {
        this.onProgrammaticTypeChange = new Subject<ProgrammaticType>();
    }

    ngOnInit(): void {
        this.type = {
            code: "",
            organization: "",
            displayLabel: this.lService.create(),
            description: this.lService.create(),
            attributes: []
        };
    }

    init(organization: any) {
        // Filter out organizations they're not RA's of
        this.organization = organization;

        this.type.organization = this.organization.code;
        this.type.organizationLabel = this.organization.label;
    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.apply(this.type).then(data => {
            this.onProgrammaticTypeChange.next(data);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }
}
