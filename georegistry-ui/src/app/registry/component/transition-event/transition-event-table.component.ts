import { Component, ViewEncapsulation } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { trigger, style, animate, transition } from "@angular/animations";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";

import { ConfirmModalComponent, ErrorHandler } from "@shared/component";
import { PageResult } from "@shared/model/core";
import { TransitionEventService } from "@registry/service/transition-event.service";
import { TransitionEvent } from "@registry/model/transition-event";
import { TransitionEventModalComponent } from "./transition-event-modal.component";
import { AuthService, DateService, LocalizationService } from "@shared/service";
import { IOService } from "@registry/service";

@Component({

    selector: "transition-event-table",
    templateUrl: "./transition-event-table.component.html",
    styleUrls: ["./transition-event-table.css"],
    encapsulation: ViewEncapsulation.None,
    animations: [
        [
            trigger("fadeInOut", [
                transition(":enter", [
                    style({
                        opacity: 0
                    }),
                    animate("300ms")
                ]),
                transition(":leave",
                    animate("100ms",
                        style({
                            opacity: 0
                        })
                    )
                )
            ]),
            trigger("fadeIn", [
                transition(":enter", [
                    style({
                        opacity: 0
                    }),
                    animate("500ms")
                ])
            ])
        ]
    ]
})
export class TransitionEventTableComponent {

    page: PageResult<TransitionEvent> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    attrConditions: any = [];

    dateCondition = {
        attribute: "eventDate",
        startDate: "",
        endDate: ""
    };

    beforeTypeCondition = {
        attribute: "beforeTypeCode",
        value: ""
    };

    /*
     * List of geo object types from the system
     */
    types: { label: string, code: string }[] = [];

    bsModalRef: BsModalRef;

    readOnly: boolean = true;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: TransitionEventService, private modalService: BsModalService, private iService: IOService, public dateService: DateService, private authService: AuthService, private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.readOnly = !this.authService.isSRA() && !this.authService.isRA() && !this.authService.isRM();
        this.refresh();

        this.attrConditions.push(this.dateCondition);
        this.attrConditions.push(this.beforeTypeCondition);

        this.iService.listGeoObjectTypes(false).then(types => {
            this.types = this.filterTypesBasedOnMyOrg(types);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    filterTypesBasedOnMyOrg(types) {
        let isSRA = this.authService.isSRA();
        let myOrgTypes = [];
        let myOrgs: string[] = this.authService.getMyOrganizations();

        for (let i = 0; i < types.length; ++i) {
            const type = types[i];
            const orgCode = type.orgCode;
            let myOrgIndex = myOrgs.indexOf(orgCode);

            if (myOrgIndex !== -1 || isSRA) {
                myOrgTypes.push(type);
            }
        }

        return myOrgTypes;
    }

    refresh(pageNumber: number = 1): void {
        this.service.getPage(this.page.pageSize, pageNumber, this.attrConditions).then(page => {
            this.page = page;
        }).catch((response: HttpErrorResponse) => {
            this.error(response);
        });
    }

    filterChange(): void {
        this.refresh(this.page.pageNumber);
    }

    onCreate(): void {
        this.bsModalRef = this.modalService.show(TransitionEventModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(false);
        this.bsModalRef.content.onEventChange.subscribe((event: TransitionEvent) => {
            this.refresh(this.page.pageNumber);
        });
    }

    deleteEvent(jsEvent, transitionEvent: TransitionEvent): void {
        jsEvent.stopPropagation();

        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + transitionEvent.eventId + "]";
        this.bsModalRef.content.data = transitionEvent;
        this.bsModalRef.content.type = "DANGER";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");

        (<ConfirmModalComponent> this.bsModalRef.content).onConfirm.subscribe(data => {
            this.service.delete(transitionEvent).then(response => {
                this.refresh(this.page.pageNumber);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onView(event: TransitionEvent): void {
        this.service.getDetails(event.oid).then(response => {
            this.bsModalRef = this.modalService.show(TransitionEventModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(false, response);
            this.bsModalRef.content.onEventChange.subscribe((event: TransitionEvent) => {
                this.refresh(this.page.pageNumber);
            });
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
