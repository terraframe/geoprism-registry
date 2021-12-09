import { Component, ViewEncapsulation, ViewChild, ElementRef, Input } from "@angular/core";
import { Router, ActivatedRoute } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";
import { Location } from "@angular/common";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";

import { FileUploader, FileUploaderOptions } from "ng2-file-upload";

import { AbstractAction, ChangeRequest, CreateGeoObjectAction, UpdateAttributeAction } from "@registry/model/crtable";
import { ActionTypes } from "@registry/model/constants";
import { GeoObjectOverTime } from "@registry/model/registry";

import { ChangeRequestService, GeometryService } from "@registry/service";
import { LocalizationService, AuthService, EventService } from "@shared/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";

declare var acp: string;

@Component({

    selector: "request-table",
    templateUrl: "./request-table.component.html",
    styleUrls: ["./request-table.css"],
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
export class RequestTableComponent {

    today: Date = new Date();
    todayString: string = this.dateService.getDateString(new Date());

    objectKeys = Object.keys;

    bsModalRef: BsModalRef;

    page: any = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    requests: ChangeRequest[] = [];

    actions: AbstractAction[];

    columns: any[] = [];

    @Input() toggleId: string;

    uploadRequest: ChangeRequest;

    filterCriteria: string = "ALL";

    sort: any[] = [{ attribute: "createDate", ascending: false }];

    hasBaseDropZoneOver: boolean = false;

    // Restrict page to the specified oid
    oid: string = null;

    /*
     * File uploader
     */
    uploader: FileUploader;

    @ViewChild("myFile")
    fileRef: ElementRef;

    isValid: boolean = true;

    isEditing: boolean = false;

    constructor(private service: ChangeRequestService, private geomService: GeometryService, private modalService: BsModalService, private authService: AuthService, private localizationService: LocalizationService,
        private eventService: EventService, private route: ActivatedRoute, private router: Router, private dateService: DateService, private location: Location) {
        this.columns = [
            { name: localizationService.decode("change.request.user"), prop: "createdBy", sortable: false },
            { name: localizationService.decode("change.request.createDate"), prop: "createDate", sortable: false, width: 195 },
            { name: localizationService.decode("change.request.status"), prop: "approvalStatus", sortable: false }
        ];
    }

    ngOnInit(): void {
        this.oid = this.route.snapshot.paramMap.get("oid");

        this.route.paramMap.subscribe(params => {
            this.oid = params.get("oid");
            this.refresh();
        });

        if (this.oid != null) {
            this.toggleId = this.oid;
        }

        let getUrl = acp + "/changerequest/upload-file-cr";

        let options: FileUploaderOptions = {
            queueLimit: 1,
            removeAfterUpload: true,
            url: getUrl
        };

        this.uploader = new FileUploader(options);

        this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
            form.append("crOid", this.uploadRequest.oid);
        };
        this.uploader.onBeforeUploadItem = (fileItem: any) => {
            this.eventService.start();
        };
        this.uploader.onCompleteItem = (item: any, response: any, status: any, headers: any) => {
            this.fileRef.nativeElement.value = "";
            this.eventService.complete();
        };
        this.uploader.onSuccessItem = (item: any, response: any, status: number, headers: any) => {
            const doc = JSON.parse(response);

            const index = this.requests.findIndex(request => request.oid === doc.requestId);

            if (index !== -1) {
                this.requests[index].documents.push(doc);
            }
        };
        this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
            const error = JSON.parse(response);

            this.error({ error: error });
        };

        this.refresh();
    }

    isSorting(attribute: string) {
        return this.sort.length > 0 && this.sort[0].attribute === attribute;
    }

    isAscending(attribute: string) {
        return this.sort.length > 0 && this.sort[0].ascending;
    }

    onSort(attribute: string) {
        let index = this.sort.findIndex(item => item.attribute === attribute);

        if (index !== -1) {
            let item = this.sort[index];

            item.ascending = !item.ascending;
        } else {
            this.sort = [{
                attribute: attribute,
                ascending: true
            }];
        }

        this.refresh();
    }

    getGOTLabel(action: any): string {
        if (action.geoObjectJson && action.geoObjectJson.attributes && action.geoObjectJson.attributes.displayLabel && action.geoObjectJson.attributes.displayLabel.values &&
            action.geoObjectJson.attributes.displayLabel.values[0] && action.geoObjectJson.attributes.displayLabel.values[0].value && action.geoObjectJson.attributes.displayLabel.values[0].value.localeValues &&
            action.geoObjectJson.attributes.displayLabel.values[0].value.localeValues[0] && action.geoObjectJson.attributes.displayLabel.values[0].value.localeValues[0].value) {
            return action.geoObjectJson.attributes.displayLabel.values[0].value.localeValues[0].value;
        } else if (action.geoObjectJson && action.geoObjectJson.attributes && action.geoObjectJson.attributes.code) {
            return action.geoObjectJson.attributes.code;
        } else {
            return this.localizationService.decode("geoObject.label");
        }
    }

    setValid(valid: boolean): void {
        this.isValid = valid;
    }

    onUpload(request: ChangeRequest): void {
        this.uploadRequest = request;

        if (this.uploader.queue != null && this.uploader.queue.length > 0) {
            this.uploader.uploadAll();
        } else {
            this.error({
                message: this.localizationService.decode("io.missing.file"),
                error: {}
            });
        }
    }

    onDownloadFile(request: ChangeRequest, fileOid: string): void {
        window.location.href = acp + "/changerequest/download-file-cr?crOid=" + request.oid + "&" + "vfOid=" + fileOid;
    }

    onDeleteFile(request: ChangeRequest, fileOid: string): void {
        this.service.deleteFile(request.oid, fileOid).then(() => {
            const index = request.documents.findIndex(doc => doc.oid === fileOid);

            if (index !== -1) {
                request.documents.splice(index, 1);
            }
        }).catch((response: HttpErrorResponse) => {
            this.error(response);
        });
    }

    public fileOverBase(e: any): void {
        this.hasBaseDropZoneOver = e;
    }

    pageChange(pageNumber: number = 1): void {
        this.oid = null;
        this.refresh(pageNumber);
    }

    refresh(pageNumber: number = 1): void {
        this.geomService.destroy();

        this.service.getAllRequests(this.page.pageSize, pageNumber, this.filterCriteria, this.sort, this.oid).then(requests => {
            this.page = requests;
            this.requests = requests.resultSet;

            // Copying the Geo-Object to add consistency for template processing
            this.requests.forEach((req) => {
                if (!req.current.geoObject) {
                    for (let i = 0; i < req.actions.length; i++) {
                        if (req.actions[0].actionType === ActionTypes.CREATEGEOOBJECTACTION) {
                            // This is the state of the Geo-Object as the Registry Contributor configured it.
                            req.current.geoObject = JSON.parse(JSON.stringify((req.actions[0] as CreateGeoObjectAction).geoObjectJson));
                        }
                    }
                }
            });
        }).catch((response: HttpErrorResponse) => {
            this.error(response);
        });
    }

    onSelect(selected: any): void {
        // this.request = selected.selected;

        this.geomService.destroy();

        this.service.getAllRequests(this.page.pageSize, 1, "ALL", this.sort, this.oid).then(requests => {
            this.requests = requests.resultSet;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onExecute(changeRequest: ChangeRequest): void {
        if (changeRequest != null) {
            this.service.implementDecisions(changeRequest).then(request => {
                changeRequest = request;

                // TODO: Determine if there is a way to update an individual record
                this.refresh();

                const bsModalRef = this.modalService.show(ConfirmModalComponent, {
                    animated: true,
                    backdrop: true,
                    ignoreBackdropClick: true
                });

                bsModalRef.content.submitText = this.localizationService.decode("change.requests.more.geoobject.updates.submit.btn");
                bsModalRef.content.cancelText = this.localizationService.decode("change.requests.more.geoobject.updates.cancel.btn");
                bsModalRef.content.message = this.localizationService.decode("change.requests.more.geoobject.updates.message");

                bsModalRef.content.onConfirm.subscribe(() => {
                    const object = this.getFirstGeoObjectInActions(request);

                    if (object != null) {
                        this.router.navigate(["/registry/location-manager"], {
                            queryParams: { text: object.attributes.code, date: this.todayString, type: object.geoObjectType.code, code: object.attributes.code, uid: object.attributes.uid }
                        });
                        // this.router.navigate(["/registry/location-manager", object.attributes.uid, object.geoObjectType.code, this.todayString, true]);
                    } else {
                        let object = request.current.geoObject;
                        let type = request.current.geoObjectType;

                        if (object != null && type != null) {
                            this.router.navigate(["/registry/location-manager"], {
                                queryParams: { text: object.attributes.code, date: this.todayString, type: type.code, code: object.attributes.code, uid: object.attributes.uid }
                            });

                            // this.router.navigate(["/registry/location-manager", object.attributes.uid, type.code, this.todayString, true]);
                        }
                    }
                });
            }).catch((response: HttpErrorResponse) => {
                this.error(response);
            });
        }
    }

    onReject(cr: ChangeRequest): void {
        this.service.rejectChangeRequest(cr).then(() => {
            // TODO: Determine if there is a way to update an individual record
            // TODO : cr.statusLabel needs to be updated...
            /*
            cr.approvalStatus = "REJECTED";
     
            let len = this.actions.length;
            for (let i = 0; i < len; ++i) {
                let action: AbstractAction = this.actions[i];
     
                action.approvalStatus = "REJECTED";
            }
            */

            this.refresh();
        }).catch((response: HttpErrorResponse) => {
            this.error(response);
        });
    }

    getFirstGeoObjectInActions(request: ChangeRequest): GeoObjectOverTime {
        for (let i = 0; i < request.actions.length; i++) {
            let action = request.actions[i];

            // eslint-disable-next-line no-prototype-builtins
            if (action.hasOwnProperty("geoObjectJson")) {
                return action["geoObjectJson"];
            }
        }

        return null;
    }

    onDelete(changeRequest: ChangeRequest): void {
        if (changeRequest != null) {
            const bsModalRef = this.modalService.show(ConfirmModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });

            bsModalRef.content.type = "danger";
            bsModalRef.content.submitText = this.localizationService.decode("change.request.delete.request.confirm.btn");
            bsModalRef.content.message = this.localizationService.decode("change.request.delete.request.message");

            bsModalRef.content.onConfirm.subscribe(data => {
                this.service.delete(changeRequest.oid).then(deletedRequestId => {
                    let pos = -1;
                    for (let i = 0; i < this.requests.length; i++) {
                        let req = this.requests[i];
                        if (req.oid === deletedRequestId) {
                            pos = i;
                            break;
                        }
                    }

                    if (pos > -1) {
                        this.requests.splice(pos, 1);
                    }

                    this.refresh();
                }).catch((response: HttpErrorResponse) => {
                    this.error(response);
                });
            });
        }
    }

    onUpdate(changeRequest: ChangeRequest): void {
        if (changeRequest != null) {
            this.service.update(changeRequest).then(request => {
                this.refresh();

                this.isEditing = false;
            }).catch((response: HttpErrorResponse) => {
                this.error(response);
            });
        }
    }

    applyActionStatusProperties(action: any): void {
        // var action = JSON.parse(JSON.stringify(this.action));
        // action.geoObjectJson = this.attributeEditor.getGeoObject();

        this.service.setActionStatus(action.oid, action.approvalStatus).then(response => {
            action.decisionMaker = (action.approvalStatus !== "PENDING") ? this.authService.getUsername() : "";

            // this.crtable.refresh()
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public error(err: any): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

    requestTrackBy(index: number, request: ChangeRequest) {
        return request.oid;
    }

    toggle(event: any, oid: string): void {
        this.location.replaceState("/registry/change-requests/" + oid);

        if (!event.target.parentElement.className.includes("btn") && !event.target.className.includes("btn")) {
            if (this.toggleId === oid) {
                this.toggleId = null;
            } else {
                this.toggleId = oid;
                //                this.onSelect({ selected: [{ oid: oid }] });
                this.requests.forEach(req => {
                    if (req.oid === oid) {
                        this.actions = req.actions;
                    }
                });
            }
        }
    }

    filter(criteria: string): void {
        this.filterCriteria = criteria;

        this.refresh(1);
    }

    setActionStatus(action: CreateGeoObjectAction | UpdateAttributeAction, status: string): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });

        bsModalRef.content.onConfirm.subscribe(data => {
            action.approvalStatus = status;

            this.applyActionStatusProperties(action);
        });
    }

    getActiveDetailComponent(action: CreateGeoObjectAction | UpdateAttributeAction): any {
        // TODO: I know this scales poorly to lots of different action types but I'm not sure how to do it better
        if (action.actionType.endsWith("CreateGeoObjectAction") || action.actionType.endsWith("UpdateGeoObjectAction")) {
            // return this.cuDetail;
        }
        //   if (this.arDetail != null && (this.action.actionType.endsWith('AddChildAction') || this.action.actionType.endsWith('RemoveChildAction')))
        //   {
        //     return this.arDetail;
        //   }

        return action;
    }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    getUsername(): string {
        return this.authService.getUsername();
    }

    isRequestTooOld(request: ChangeRequest): boolean {
        if (request.actions && request.actions.length > 0) {
            let firstAction = request.actions[0];

            if (firstAction.actionType === ActionTypes.UPDATEGEOOBJECTACTION) {
                return true;
            } else if (firstAction.actionType === ActionTypes.CREATEGEOOBJECTACTION && !(firstAction as CreateGeoObjectAction).geoObjectJson.attributes["exists"]) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    onEditAttributes(): void {
        this.isEditing = !this.isEditing;
    }

    canEdit(request: ChangeRequest): boolean {
        return (request.permissions.includes("WRITE_DETAILS") && this.isEditing);
    }

}
