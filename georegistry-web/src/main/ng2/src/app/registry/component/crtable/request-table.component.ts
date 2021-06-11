import { Component, ViewEncapsulation, ViewChild, ElementRef, Input } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import {
	trigger,
	style,
	animate,
	transition,
} from '@angular/animations';

import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';

import { ChangeRequest, AbstractAction, AddChildAction, SetParentAction, CreateGeoObjectAction, RemoveChildAction, UpdateGeoObjectAction } from '@registry/model/crtable';
import { GeoObjectOverTime } from '@registry/model/registry';

import { ChangeRequestService } from '@registry/service';
import { LocalizationService, AuthService, EventService } from '@shared/service';
import { DateService } from '@shared/service/date.service';
import { ActionDetailModalComponent } from './action-detail/action-detail-modal.component'

import { ErrorHandler, ErrorModalComponent, ConfirmModalComponent } from '@shared/component';

declare var acp: string;
declare var $: any;

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
		]
	]
})
export class RequestTableComponent {
	
	today: string = this.dateService.getDateString(new Date());

	objectKeys = Object.keys;

	bsModalRef: BsModalRef;

	requests: ChangeRequest[] = [];

	actions: AbstractAction[] | SetParentAction[] | AddChildAction[] | CreateGeoObjectAction[] | RemoveChildAction[] | UpdateGeoObjectAction[];

	columns: any[] = [];

	@Input() toggleId: string;
	
	targetActionId: string

	filterCriteria: string = 'ALL';

	hasBaseDropZoneOver:boolean = false;
	
	waitingOnScroll: boolean = false;
	
	/*
     * File uploader
     */
	uploader: FileUploader;
	
	@ViewChild('myFile')
	fileRef: ElementRef;

	constructor(private service: ChangeRequestService, private modalService: BsModalService, private authService: AuthService, private localizationService: LocalizationService,
				private eventService: EventService, private router: Router, private dateService: DateService) {

		this.columns = [
			{ name: localizationService.decode('change.request.user'), prop: 'createdBy', sortable: false },
			{ name: localizationService.decode('change.request.createDate'), prop: 'createDate', sortable: false, width: 195 },
			{ name: localizationService.decode('change.request.status'), prop: 'approvalStatus', sortable: false }
		];

		this.refresh();
	}
	
	ngOnInit(): void{
		var getUrl = acp + '/changerequest/upload-file-action';

		let options: FileUploaderOptions = {
			queueLimit: 1,
			removeAfterUpload: true,
			url: getUrl
		};

		this.uploader = new FileUploader(options);

		this.uploader.onBuildItemForm = (fileItem: any, form: any) => {
			form.append('actionOid', this.targetActionId);
		};
		this.uploader.onBeforeUploadItem = (fileItem: any) => {
			this.eventService.start();
		};
		this.uploader.onCompleteItem = (item: any, response: any, status: any, headers: any) => {
			this.fileRef.nativeElement.value = "";
			this.eventService.complete();
		};
		this.uploader.onSuccessItem = (item: any, response: any, status: number, headers: any) => {
			
			for(let i=0; i<this.actions.length; i++){
				let action = this.actions[i];
				
				if(action.oid === this.targetActionId){
					
					action.documents.push(JSON.parse(response));
					
					break;
				}
			}
			
		};
		this.uploader.onErrorItem = (item: any, response: string, status: number, headers: any) => {
			const error = JSON.parse(response)

			this.error({ error: error });
		}
		
		if (this.toggleId != null)
		{
		  this.onSelect({ selected: [{ oid: this.toggleId }] });
		  this.waitingOnScroll = true;
		}
	}
	
	scrollToBottom(): void {
    //try {
      // This is a hack but I expect it will need to be redone when we have pagination anyway.
      $(".new-admin-design-main")[0].scrollTop = $(".new-admin-design-main")[0].scrollHeight;
    //} catch(err) {
    //  console.log(err);
    //}
  }
	
	getGOTLabel(action: any): string {
	  if (action.geoObjectJson && action.geoObjectJson.attributes && action.geoObjectJson.attributes.displayLabel && action.geoObjectJson.attributes.displayLabel.values
	      && action.geoObjectJson.attributes.displayLabel.values[0] && action.geoObjectJson.attributes.displayLabel.values[0].value && action.geoObjectJson.attributes.displayLabel.values[0].value.localeValues
	      && action.geoObjectJson.attributes.displayLabel.values[0].value.localeValues[0] && action.geoObjectJson.attributes.displayLabel.values[0].value.localeValues[0].value)
	  {
	    return action.geoObjectJson.attributes.displayLabel.values[0].value.localeValues[0].value;
	  }
	  else if (action.geoObjectJson && action.geoObjectJson.attributes && action.geoObjectJson.attributes.code) 
	  {
	    return action.geoObjectJson.attributes.code;
	  }
	  else
	  {
	    return this.localizationService.decode("geoObject.label");
	  }
	}
	
	onUpload(action: AbstractAction): void {
		this.targetActionId = action.oid;
		
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
	
	onDownloadFile(actionOid: string, fileOid: string): void {
		window.location.href = acp + '/changerequest/download-file-action?actionOid=' + actionOid + '&' + 'vfOid=' + fileOid;
	}
	
	onDeleteFile(actionOid: string, fileOid: string): void {
		this.service.deleteFile(actionOid, fileOid).then(response => {
			
			let docPos = -1;
			for(let i=0; i<this.actions.length; i++){
				let action = this.actions[i];
				if(action.oid === actionOid){
					
					for(let index=0; index<action.documents.length; index++){
						let doc = action.documents[index];
						if(doc.oid === fileOid){
							docPos = index;
							break;
						}
					}
					
					if(docPos > -1){
						action.documents.splice(docPos, 1)
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
			
			if (this.waitingOnScroll)
			{
			  let that = this;
			  setTimeout(function(){ that.scrollToBottom(); }, 100);
			  this.waitingOnScroll = false;
			}

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
				
				const bsModalRef = this.modalService.show(ConfirmModalComponent, {
					animated: true,
					backdrop: true,
					ignoreBackdropClick: true,
				});
				
				bsModalRef.content.submitText = this.localizationService.decode('change.requests.more.geoobject.updates.submit.btn');
				bsModalRef.content.cancelText = this.localizationService.decode('change.requests.more.geoobject.updates.cancel.btn');
				bsModalRef.content.message = this.localizationService.decode('change.requests.more.geoobject.updates.message');
				
				bsModalRef.content.onConfirm.subscribe(data => {
					
					let firstGeoObject = this.getFirstGeoObjectInActions();
					
					if(firstGeoObject){
						this.router.navigate(['/registry/location-manager', firstGeoObject.attributes.uid, firstGeoObject.geoObjectType.code, this.today, true]);
					}
					else{
						this.router.navigate(['/registry/location-manager', firstGeoObject.attributes.uid, firstGeoObject.geoObjectType.code, this.today, true]);
					}
				});
			
			}).catch((response: HttpErrorResponse) => {
					this.error(response);
				});
		}
	}
	
	getFirstGeoObjectInActions(): GeoObjectOverTime {
		for(let i=0; i<this.actions.length; i++){
			let action = this.actions[i];
			
			if(action.hasOwnProperty("geoObjectJson")){
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
				ignoreBackdropClick: true,
			});
			
			bsModalRef.content.type = "danger";
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

	showActionDetail(action: any, cr: any) {

		this.bsModalRef = this.modalService.show(ActionDetailModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.curAction(action, !cr.permissions.includes("WRITE_DETAILS"));
	}
	formatDate(date: string): string {
		return this.dateService.formatDateForDisplay(date);
	}
	
	getUsername(): string {
		return this.authService.getUsername();
	}

}
