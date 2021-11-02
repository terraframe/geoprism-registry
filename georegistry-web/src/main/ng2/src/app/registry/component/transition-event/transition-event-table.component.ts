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

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: TransitionEventService, private modalService: BsModalService, private iService: IOService, private dateService: DateService, private authService: AuthService, private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.refresh();

        this.attrConditions.push(this.dateCondition);
        this.attrConditions.push(this.beforeTypeCondition);

        this.iService.listGeoObjectTypes(false).then(types => {
            let myOrgTypes = [];
            for (let i = 0; i < types.length; ++i) {
                const orgCode = types[i].orgCode;
                const typeCode = types[i].superTypeCode != null ? types[i].superTypeCode : types[i].code;

                if (this.authService.isGeoObjectTypeRM(orgCode, typeCode)) {
                    myOrgTypes.push(types[i]);
                }
            }
            this.types = myOrgTypes;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
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
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + transitionEvent.description.localizedValue + "]";
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

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
