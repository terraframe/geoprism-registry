import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";

import { LocalizationService } from "@shared/service";
import { Organization } from "@shared/model/core";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { BusinessType } from "@registry/model/business-type";

@Component({
    selector: "create-business-type-modal",
    templateUrl: "./create-business-type-modal.component.html",
    styleUrls: []
})
export class CreateBusinessTypeModalComponent implements OnInit {

    type: BusinessType;
    organization: Organization = null;
    message: string = null;
    organizationLabel: string;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful
     */
    public onBusinessTypeChange: Subject<BusinessType>;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: BusinessTypeService, private lService: LocalizationService, public bsModalRef: BsModalRef) {
        this.onBusinessTypeChange = new Subject<BusinessType>();
    }

    ngOnInit(): void {
        this.type = {
            code: "",
            organization: "",
            displayLabel: this.lService.create(),
            description: this.lService.create(),
            attributes: [],
            labelAttribute: ""
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
            this.onBusinessTypeChange.next(data);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
