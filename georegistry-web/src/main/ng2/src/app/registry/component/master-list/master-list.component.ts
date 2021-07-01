import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { Observable } from 'rxjs';
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { MasterListVersion } from '@registry/model/registry';
import { RegistryService } from '@registry/service';
import { DateService } from '@shared/service/date.service';
import { ExportFormatModalComponent } from './export-format-modal.component';
import { GeoObjectEditorComponent } from '../geoobject-editor/geoobject-editor.component';

import { ErrorHandler } from '@shared/component';
import { LocalizationService, AuthService, ProgressService } from '@shared/service';

declare var acp: string;
declare var $: any;

@Component({
	selector: 'master-list',
	templateUrl: './master-list.component.html',
	styleUrls: ['./master-list.component.css']
})
export class MasterListComponent implements OnInit, OnDestroy {
	message: string = null;
	list: MasterListVersion = null;
	p: number = 1;
	current: string = '';
	filter: { attribute: string, value: string, label: string }[] = [];
	selected: string[] = [];
	page: any = {
		count: 0,
		pageNumber: 1,
		pageSize: 100,
		results: []
	};
	sort = { attribute: 'code', order: 'ASC' };
	isPublished: boolean = true;
	isRefreshing: boolean = false;
	isWritable: boolean = false;

    /*
     * Reference to the modal current showing
    */
	private bsModalRef: BsModalRef;

	public searchPlaceholder = "";

	notifier: WebSocketSubject<{ type: string, content: any }>;


	constructor(public service: RegistryService, private pService: ProgressService, private route: ActivatedRoute, private dateService: DateService,
		private modalService: BsModalService, private localizeService: LocalizationService, private authService: AuthService ) {

		this.searchPlaceholder = localizeService.decode("masterlist.search");
	}

	ngOnInit(): void {
		const oid = this.route.snapshot.paramMap.get('oid');
		this.isPublished = (this.route.snapshot.paramMap.get('published') == "true");

		this.service.getMasterListVersion(oid).then(version => {
			this.list = version;
			this.list.attributes.forEach(attribute => {
				attribute.isCollapsed = true;
			});
			const orgCode = this.list.orgCode;
			const typeCode = this.list.superTypeCode != null ? this.list.superTypeCode : this.list.typeCode;

			this.isWritable = this.authService.isGeoObjectTypeRC(orgCode, typeCode);

			this.onPageChange(1);
			
			if (version.refreshProgress != null)
			{
			  this.handleProgressChange(version.refreshProgress);
			}
		});

		let baseUrl = "wss://" + window.location.hostname + (window.location.port ? ':' + window.location.port : '') + acp;

		this.notifier = webSocket(baseUrl + '/websocket/progress/' + oid);
		this.notifier.subscribe(message => {
		  if (message.content != null)
		  {
			  this.handleProgressChange(message.content);
			}
			else
			{
			  this.handleProgressChange(message);
			}
		});
	}

	ngOnDestroy() {
		this.notifier.complete();
	}
	
	ngAfterViewInit() {

	}


	onPageChange(pageNumber: number): void {

		this.message = null;

		this.service.data(this.list.oid, pageNumber, this.page.pageSize, this.filter, this.sort).then(page => {
			this.page = page;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	//    onSearch(): void {
	//        this.filter = this.current;
	//
	//        this.onPageChange( 1 );
	//    }

	onSort(attribute: { name: string, label: string }): void {
		if (this.sort.attribute === attribute.name) {
			this.sort.order = (this.sort.order === 'ASC' ? 'DESC' : 'ASC');
		}
		else {
			this.sort = { attribute: attribute.name, order: 'ASC' };
		}

		this.onPageChange(1);
	}

	clearFilters(): void {
		this.list.attributes.forEach(attr => {
			attr.search = null;
		});

		this.filter = [];
		this.selected = [];

		this.onPageChange(1);
	}

	toggleFilter(attribute: any): void {
		attribute.isCollapsed = !attribute.isCollapsed;
	}

	getValues(attribute: any): void {
		return Observable.create((observer: any) => {
			this.message = null;

			// Get the valid values
			this.service.values(this.list.oid, attribute.search, attribute.name, attribute.base, this.filter).then(options => {
				options.unshift({ label: '[' + this.localizeService.decode("masterlist.nofilter") + ']', value: null });

				observer.next(options);
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}

	handleProgressChange(progress: any): void {

		this.isRefreshing = (progress.current < progress.total);

		this.pService.progress(progress);
	}

	handleDateChange(attribute: any): void {
		attribute.isCollapsed = true;

		// Remove the current attribute filter if it exists
		this.filter = this.filter.filter(f => f.attribute !== attribute.base);
		this.selected = this.selected.filter(s => s !== attribute.base);

		if (attribute.value != null && (attribute.value.start !== '' || attribute.value.end !== '')) {

			let label = '[' + attribute.label + '] : [';

			if (attribute.value.start != null) {
				label += attribute.value.start;
			}

			if (attribute.value.start != null && attribute.value.end != null) {
				label += ' - ';
			}

			if (attribute.value.end != null) {
				label += attribute.value.end;
			}

			label += ']';

			this.filter.push({ attribute: attribute.base, value: attribute.value, label: label });
			this.selected.push(attribute.base);
		}

		this.onPageChange(1);
	}

	handleInputChange(attribute: any): void {
		attribute.isCollapsed = true;

		// Remove the current attribute filter if it exists
		this.filter = this.filter.filter(f => f.attribute !== attribute.base);
		this.selected = this.selected.filter(s => s !== attribute.base);

		if (attribute.value != null && attribute.value !== '') {
			const label = '[' + attribute.label + '] : ' + '[' + attribute.value + ']';

			this.filter.push({ attribute: attribute.base, value: attribute.value, label: label });
			this.selected.push(attribute.base);
		}

		this.onPageChange(1);
	}

	handleListChange(e: TypeaheadMatch, attribute: any): void {
		attribute.value = e.item;
		attribute.isCollapsed = true;

		// Remove the current attribute filter if it exists
		this.filter = this.filter.filter(f => f.attribute !== attribute.base);
		this.selected = this.selected.filter(s => s !== attribute.base);

		this.list.attributes.forEach(attr => {
			if (attr.base === attribute.base) {
				attr.search = '';
			}
		});

		if (attribute.value.value != null && attribute.value.value !== '') {
			const label = '[' + attribute.label + '] : ' + '[' + attribute.value.label + ']';

			this.filter.push({ attribute: attribute.base, value: e.item.value, label: label });
			this.selected.push(attribute.base);
			attribute.search = e.item.label;
		}
		else {
			attribute.search = '';
		}

		this.onPageChange(1);
	}

	isFilterable(attribute: any): boolean {
		return attribute.type !== 'none' && (attribute.dependency.length === 0 || this.selected.indexOf(attribute.base) !== -1 || this.selected.filter(value => attribute.dependency.includes(value)).length > 0);
	}

	onEdit(data): void {
		let editModal = this.modalService.show(GeoObjectEditorComponent, { backdrop: true, ignoreBackdropClick: true });
		editModal.content.configureAsExisting(data.code, this.list.typeCode, this.list.forDate, this.list.isGeometryEditable);
		editModal.content.setMasterListId(this.list.oid);
		editModal.content.setOnSuccessCallback(() => {
			// Refresh the page
			this.onPageChange(this.page.pageNumber);
		});
	}

	onPublish(): void {
		this.message = null;

		this.service.publishMasterList(this.list.oid).toPromise()
			.then( (historyOid: string) => {
				this.isRefreshing = true;
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
			
			
			//this.list = list;
        //this.list.attributes.forEach(attribute => {
        //  attribute.isCollapsed = true;
        //});

        // Refresh the resultSet
        //this.onPageChange(1);
	}

	onNewGeoObject(): void {
		let editModal = this.modalService.show(GeoObjectEditorComponent, { backdrop: true, ignoreBackdropClick: true });
		//editModal.content.fetchGeoObject( data.code, this.list.typeCode );
		editModal.content.configureAsNew(this.list.typeCode, this.list.forDate, this.list.isGeometryEditable);
		editModal.content.setMasterListId(this.list.oid);
		editModal.content.setOnSuccessCallback(() => {
			// Refresh the page
			this.onPageChange(this.page.pageNumber);
		});
	}

	onExport(): void {
		this.bsModalRef = this.modalService.show(ExportFormatModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.onFormat.subscribe(format => {
			if (format == 'SHAPEFILE') {
				window.location.href = acp + '/master-list/export-shapefile?oid=' + this.list.oid + "&filter=" + encodeURIComponent(JSON.stringify(this.filter));
			}
			else if (format == 'EXCEL') {
				window.location.href = acp + '/master-list/export-spreadsheet?oid=' + this.list.oid + "&filter=" + encodeURIComponent(JSON.stringify(this.filter));
			}
		});
	}

	changeTypeaheadLoading(attribute: any, loading: boolean): void {
		attribute.loading = loading;
	}
	
	formatDate(date: string): string {
		return this.dateService.formatDateForDisplay(date);
	}
	
	onWheel(event: WheelEvent): void {
		let tableEl = (<Element>event.target).parentElement.closest('table').parentElement;
//	    if (event.deltaY > 0) tableEl!.scrollLeft += 40;
//	    else tableEl!.scrollLeft -= 40;

		tableEl.scrollLeft += event.deltaY;
   		event.preventDefault();
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}