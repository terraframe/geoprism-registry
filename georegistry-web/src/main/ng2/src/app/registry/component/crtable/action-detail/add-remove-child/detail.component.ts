import { Input, Component, HostListener } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { Observable } from "rxjs";
import { Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { CreateGeoObjectAction, UpdateAttributeAction } from "@registry/model/crtable";
import { ChangeRequestService } from "@registry/service";
import { ComponentCanDeactivate, AuthService } from "@shared/service";

import { ErrorHandler } from "@shared/component";
import { ActionDetailComponent } from "../action-detail-modal.component";

declare var acp: any;
declare var $: any;

@Component({

    selector: "crtable-detail-add-remove-child",
    templateUrl: "./detail.component.html",
    styleUrls: []
})
export class AddRemoveChildDetailComponent implements ComponentCanDeactivate, ActionDetailComponent {

    @Input() action: CreateGeoObjectAction | UpdateAttributeAction;

    original: CreateGeoObjectAction | UpdateAttributeAction;

    @Input() readOnly: boolean;

    isEditing: boolean = false;

    private bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(private router: Router, private changeRequestService: ChangeRequestService, private modalService: BsModalService, private authService: AuthService) {}

    ngOnInit(): void {

        this.original = Object.assign({}, this.action);

    }

    applyAction() {

        this.changeRequestService.applyAction(this.action).then(response => {

            this.original = Object.assign({}, this.action);

            this.unlockAction();

        }).catch((err: HttpErrorResponse) => {

            this.error(err);

        });

    }

    // Big thanks to https://stackoverflow.com/questions/35922071/warn-user-of-unsaved-changes-before-leaving-page
    @HostListener("window:beforeunload")
    canDeactivate(): Observable<boolean> | boolean {

        if (this.isEditing) {

            // event.preventDefault();
            // event.returnValue = 'Are you sure?';
            // return 'Are you sure?';

            return false;

        }

        return true;

    }

    afterDeactivate(isDeactivating: boolean) {

        if (isDeactivating && this.isEditing) {

            this.unlockActionSync();

        }

    }

    startEdit(): void {

        this.lockAction();

    }

    public endEdit(): void {

        this.unlockAction();

    }

    lockAction() {

        this.changeRequestService.lockAction(this.action.oid).then(response => {

            this.isEditing = true;

        }).catch((err: HttpErrorResponse) => {

            this.error(err);

        });

    }

    unlockAction() {

        this.changeRequestService.unlockAction(this.action.oid).then(response => {

            this.isEditing = false;

            this.action = this.original;

        }).catch((err: HttpErrorResponse) => {

            this.error(err);

        });

    }

    // https://stackoverflow.com/questions/4945932/window-onbeforeunload-ajax-request-in-chrome
    unlockActionSync() {

        $.ajax({
            url: acp + "/changerequest/unlockAction",
            method: "POST",
            data: { actionId: this.action.oid },
            success: function(a) {

            },
            async: false
        });

    }

    onSelect(action: CreateGeoObjectAction | UpdateAttributeAction) {

        this.action = action;

    }

    getUsername(): string {

        return this.authService.getUsername();

    }

    public error(err: HttpErrorResponse): void {

        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);

    }

}
