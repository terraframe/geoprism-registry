import { Component, ViewEncapsulation, ViewChild, ElementRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import {
	trigger,
	style,
	animate,
	transition,
	state,
	group,
	query,
	stagger
} from '@angular/animations';

import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';

import { ChangeRequest, AbstractAction, AddChildAction, SetParentAction, CreateGeoObjectAction, RemoveChildAction, UpdateGeoObjectAction } from '@registry/model/crtable';

import { ChangeRequestService } from '@registry/service';
import { LocalizationService, AuthService, EventService, ExternalSystemService  } from '@shared/service';
import { ActionDetailModalComponent } from './action-detail/action-detail-modal.component'

import { ErrorHandler, ErrorModalComponent, ConfirmModalComponent } from '@shared/component';

declare var acp: string;

@Component({

	selector: 'request-table',
	templateUrl: './request-table.component.html',
	styleUrls: ['./request-table.css'],
	encapsulation: ViewEncapsulation.None,
	animations: [
		[
			trigger('fadeInOut', [
				transition(':enter', [
					style({
						opacity: 0
					}),
					animate('300ms')
				]),
				transition(':leave',
					animate('100ms', 
						style({
							opacity: 0
						})
					)
				)
			]),
			trigger('fadeIn', [
				transition(':enter', [
					style({
						opacity: 0
					}),
					animate('500ms')
				])
			]),
			trigger('slideInOut', [
			    state('in', style({height: '*'})),
			    transition('* => void', [
			      style({height: '*'}),
			      animate(250, style({height: 0}))
			    ])
			])
		]
	]
})
export class RequestTableComponent {

	objectKeys = Object.keys;

	bsModalRef: BsModalRef;

	requests: ChangeRequest[] = [];

	actions: AbstractAction[] | SetParentAction[] | AddChildAction[] | CreateGeoObjectAction[] | RemoveChildAction[] | UpdateGeoObjectAction[];

	columns: any[] = [];

	toggleId: string;

	filterCriteria: string = 'ALL';

	isMaintainer: boolean = false;
	
	hasBaseDropZoneOver:boolean = false;
	
	/*
     * File uploader
     */
	uploader: FileUploader;
	
	@ViewChild('myFile')
	fileRef: ElementRef;

	constructor(private service: ChangeRequestService, private modalService: BsModalService, private authService: AuthService, private localizationService: LocalizationService,
				private eventService: EventService) {

		this.isMaintainer = authService.isAdmin() || authService.isMaintainer();

		this.columns = [
			{ name: localizationService.decode('change.request.user'), prop: 'createdBy', sortable: false },
			{ name: localizationService.decode('change.request.createDate'), prop: 'createDate', sortable: false, width: 195 },
			{ name: localizationService.decode('change.request.status'), prop: 'approvalStatus', sortable: false }
		];

		this.refresh();
	}
	
	ngOnInit(): void{
		var getUrl = acp + '/changerequest/upload-file';

		let options: FileUploaderOptions = {
			queueLimit: 1,
			removeAfterUpload: true,
			url: getUrl
		};

		this.uploader = new FileUploader(options);

		this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
			form.append('crOid', this.toggleId);
		};
		this.uploader.onBeforeUploadItem = (fileItem: any) => {
			this.eventService.start();
		};
		this.uploader.onCompleteItem = (item: any, response: any, status: any, headers: any) => {
			this.fileRef.nativeElement.value = "";
			this.eventService.complete();
		};
		this.uploader.onSuccessItem = (item: any, response: any, status: number, headers: any) => {
			
			for(let i=0; i<this.requests.length; i++){
				let req = this.requests[i];
				if(req.oid === this.toggleId){
					
					req.documents.push(JSON.parse(response));
					
					break;
				}
			}
			
		};
		this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
			const error = JSON.parse(response)

			this.error({ error: error });
		}
	}
	
	onUpload(): void {

		if (this.uploader.queue != null && this.uploader.queue.length > 0) {
			this.uploader.uploadAll();
		}
		else {
			this.error({
				message: this.localizationService.decode('io.missing.file'),
				error: {},
			});
		}
	}
	
	onDownloadFile(requestOid: string, fileOid: string): void {
		window.location.href = acp + '/changerequest/download-file?crOid=' + requestOid + '&' + 'vfOid=' + fileOid;
	}
	
	onDeleteFile(requestOid: string, fileOid: string): void {
		this.service.deleteFile(requestOid, fileOid).then(response => {
			
			let docPos = -1;
			for(let i=0; i<this.requests.length; i++){
				let req = this.requests[i];
				if(req.oid === this.toggleId){
					
					for(let index=0; index<req.documents.length; index++){
						let doc = req.documents[index];
						if(doc.oid === fileOid){
							docPos = index;
							break;
						}
					}
					
					if(docPos > -1){
						req.documents.splice(docPos, 1)
					}
					
					break;
				}
			}

		}).catch((response: HttpErrorResponse) => {
			this.error(response);
		})
	}
	
	public fileOverBase(e:any):void {
	    this.hasBaseDropZoneOver = e;
	}

	refresh(): void {

		this.service.getAllRequests("ALL").then(requests => {
			
			this.requests = requests;

		}).catch((response: HttpErrorResponse) => {
			this.error(response);
		})

	}


	onSelect(selected: any): void {

		// this.request = selected.selected;

		this.service.getAllActions(selected.selected[0].oid).then(actions => {

			this.actions = actions;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onExecute(changeRequest: ChangeRequest): void {

		if (changeRequest != null) {
			this.service.execute(changeRequest.oid).then(request => {
				changeRequest = request;

				// TODO: Determine if there is a way to update an individual record
				this.refresh();
			}).catch((response: HttpErrorResponse) => {
				this.error(response);
			});
		}
	}
	
	onDelete(changeRequest: ChangeRequest): void {

		if (changeRequest != null) {
			const bsModalRef = this.modalService.show(ConfirmModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
			});
			
			bsModalRef.content.type = "DANGER";
			bsModalRef.content.submitText = this.localizationService.decode('change.request.delete.request.confirm.btn');
			bsModalRef.content.message = this.localizationService.decode('change.request.delete.request.message');

			bsModalRef.content.onConfirm.subscribe(data => {
				this.service.delete(changeRequest.oid).then(deletedRequestId => {

					let pos = -1;
					for(let i=0; i<this.requests.length; i++){
						let req = this.requests[i];
						if(req.oid === deletedRequestId){
							pos = i;
							break;
						}
					}
					
					if(pos > -1){
						this.requests.splice(pos, 1);
					}
	
					this.refresh();
				}).catch((response: HttpErrorResponse) => {
					this.error(response);
				});
			});
		}
	}

	applyActionStatusProperties(action: any): void {
		// var action = JSON.parse(JSON.stringify(this.action));
		// action.geoObjectJson = this.attributeEditor.getGeoObject();

		this.service.applyActionStatusProperties(action).then(response => {
			action.decisionMaker = (action.approvalStatus !== 'PENDING') ? this.authService.getUsername() : '';
			
			console.log(action);
			// this.crtable.refresh()
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onApproveAll(changeRequest: ChangeRequest): void {

		if (changeRequest != null) {
			const bsModalRef = this.modalService.show(ConfirmModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
			});

			bsModalRef.content.onConfirm.subscribe(data => {
				this.service.approveAllActions(changeRequest.oid, this.actions).then(actions => {
					this.actions = actions;
				}).catch((response: HttpErrorResponse) => {
					this.error(response);
				});
			});

		}
	}

	onRejectAll(changeRequest: ChangeRequest): void {
		if (changeRequest != null) {
			const bsModalRef = this.modalService.show(ConfirmModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
			});

			bsModalRef.content.onConfirm.subscribe(data => {
				this.service.rejectAllActions(changeRequest.oid, this.actions).then(actions => {
					this.actions = actions;

					// TODO: Determine if there is a way to update an individual record
					// this.refresh();
				}).catch((response: HttpErrorResponse) => {
					this.error(response);
				});
			});
		}
	}

	public error(err: any): void {
		this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
	}

	requestTrackBy(index: number, request: ChangeRequest) {
		return request.oid;
	}

	toggle(event: any, oid: string): void {

		if (!event.target.parentElement.className.includes("btn") && !event.target.className.includes("btn")) {
			if (this.toggleId === oid) {
				this.toggleId = null;
			}
			else {
				this.toggleId = oid;
				this.onSelect({ selected: [{ oid: oid }] });
			}
		}
	}

	filter(criteria: string): void {

		this.service.getAllRequests(criteria).then(requests => {
			this.requests = requests;
		}).catch((response: HttpErrorResponse) => {
			this.error(response);
		})

		this.filterCriteria = criteria;
	}

	setActionStatus(action: AbstractAction, status: string): void {
		const bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});

		bsModalRef.content.onConfirm.subscribe(data => {
			action.approvalStatus = status;

			this.applyActionStatusProperties(action);
		});
	}

	getActiveDetailComponent(action: AbstractAction): any {
		// TODO: I know this scales poorly to lots of different action types but I'm not sure how to do it better
		if (action.actionType.endsWith('CreateGeoObjectAction') || action.actionType.endsWith('UpdateGeoObjectAction')) {
			// return this.cuDetail;
		}
		//   if (this.arDetail != null && (this.action.actionType.endsWith('AddChildAction') || this.action.actionType.endsWith('RemoveChildAction')))
		//   {
		//     return this.arDetail;
		//   }

		return action;
	}

	showActionDetail(action: any) {

		this.bsModalRef = this.modalService.show(ActionDetailModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.curAction = action;

		//   var detail = this.getActiveDetailComponent();
		//   if (detail != null)
		//   {
		// action.onSelect(action);
		//   }
	}
	
	editGeoObject(action: any): void {
		
	}
	
	formatDate(date: string): string {
		return this.localizationService.formatDateForDisplay(date);
	}
	
	getUsername(): string {
		return this.authService.getUsername();
	}

}
