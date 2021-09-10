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

import { LocalizationService } from "@shared/service";

import { ControlContainer, NgForm } from "@angular/forms";
import { StandardAttributeCRModel } from "./StandardAttributeCRModel";

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

    isValid: boolean = true;
    @Output() isValidChange = new EventEmitter<boolean>();

    @Input() readonly: boolean = false;

    @Input() attributeType: AttributeType;

    @Input() changeRequest: ChangeRequest;

    @Input() geoObjectType: GeoObjectType;

    @Input() geoObject: GeoObjectOverTime;

    @Input() isNewGeoObject: boolean = false;

    view: {
        summaryKey: SummaryKey;
        summaryKeyLocalized: string;
        oldValue?: any;
        value: any;
    };

    model: StandardAttributeCRModel;

    // eslint-disable-next-line no-useless-constructor
    constructor(public cdr: ChangeDetectorRef, public service: RegistryService, public lService: LocalizationService,
        public changeDetectorRef: ChangeDetectorRef, public dateService: DateService, private authService: AuthService,
        private requestService: ChangeRequestService, private modalService: BsModalService, private elementRef: ElementRef) { }

    ngOnInit(): void {
        this.model = new StandardAttributeCRModel(this.attributeType, this.geoObject, this.changeRequest);
        this.calculateView();
    }

    ngAfterViewInit() {
    }

    public stringify(obj: any): string {
        return JSON.stringify(obj);
    }

    calculateView(): void {
        let diff = this.model.diff;

        if (diff != null) {
            let newVal = diff.newValue == null ? null : JSON.parse(JSON.stringify(diff.newValue));
            this.view = {
                value: newVal,
                summaryKey: SummaryKey.VALUE_CHANGE,
                summaryKeyLocalized: this.lService.decode("changeovertime.manageVersions.summaryKey." + SummaryKey.VALUE_CHANGE)
            };

            if (diff.oldValue !== null && diff.oldValue !== undefined) {
                this.view.oldValue = JSON.parse(JSON.stringify(diff.oldValue));
            }
        } else {
            this.view = {
                value: this.model.value,
                summaryKey: SummaryKey.UNMODIFIED,
                summaryKeyLocalized: this.lService.decode("changeovertime.manageVersions.summaryKey." + SummaryKey.UNMODIFIED)
            };
        }
    }

    onValueChange(): void {
        this.calculateView();
    }

    onApprove(): void {
        let editAction = this.model.editAction;

        this.requestService.setActionStatus(editAction.oid, GovernanceStatus.ACCEPTED).then(results => {
            editAction.approvalStatus = GovernanceStatus.ACCEPTED;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onReject(): void {
        let editAction = this.model.editAction;

        this.requestService.setActionStatus(editAction.oid, GovernanceStatus.REJECTED).then(results => {
            editAction.approvalStatus = GovernanceStatus.REJECTED;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onPending(): void {
        let editAction = this.model.editAction;

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
