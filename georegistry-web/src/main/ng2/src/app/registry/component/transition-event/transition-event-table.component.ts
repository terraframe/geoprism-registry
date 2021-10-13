import { Component, ViewEncapsulation } from "@angular/core";
import { Router, ActivatedRoute } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";

import { ErrorHandler } from "@shared/component";
import { PageResult } from "@shared/model/core";
import { TransitionEventService } from "@registry/service/transition-event.service";
import { TransitionEvent } from "@registry/model/transition-event";
import { TransitionEventModalComponent } from "./transition-event-modal.component";
import { DateService } from "@shared/service";

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

    bsModalRef: BsModalRef;


    constructor(private service: TransitionEventService, private modalService: BsModalService, private dateService: DateService) { }

    ngOnInit(): void {

        this.refresh();
    }

    refresh(pageNumber: number = 1): void {

        this.service.getPage(this.page.pageSize, pageNumber).then(page => {
            this.page = page;
        }).catch((response: HttpErrorResponse) => {
            this.error(response);
        });
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

    onView(event: TransitionEvent): void {
        this.service.getDetails(event.oid).then(response => {
            this.bsModalRef = this.modalService.show(TransitionEventModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(true, response);
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
