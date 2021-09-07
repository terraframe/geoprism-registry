import { Component, Input, ViewChild } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";

export interface ActionDetailComponent {
    endEdit(): void;
}

@Component({
    selector: "action-detail-modal",
    templateUrl: "./action-detail-modal.component.html",
    styleUrls: []
})
export class ActionDetailModalComponent {

    action: any;

    readonly: boolean;

    @ViewChild("cuDetail") cuDetail: ActionDetailComponent;
    @ViewChild("arDetail") arDetail: ActionDetailComponent;
    @ViewChild("spDetail") spDetail: ActionDetailComponent;

    @Input()
    curAction(action: any, readonly: boolean) {
        this.action = action;
        this.readonly = readonly;
    }

    /*
     * Called on confirm
     */
    public onFormat: Subject<any>;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
    }

    cancel(): void {
        if (this.cuDetail != null) {
            this.cuDetail.endEdit();
        }

        if (this.arDetail != null) {
            this.arDetail.endEdit();
        }

        if (this.spDetail != null) {
            this.spDetail.endEdit();
        }

        this.bsModalRef.hide();
    }

    confirm(): void {
        this.bsModalRef.hide();
    }
}
