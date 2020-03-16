import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { MasterList } from '../../model/registry';
import { RegistryService } from '../../service/registry.service';
import { PublishModalComponent } from './publish-modal.component';

@Component({

	selector: 'master-list-view',
	templateUrl: './master-list-view.component.html',
	styleUrls: ['./master-list-view.component.css']
})
export class MasterListViewComponent implements OnInit {

	content: string = "";
	list: MasterList = null;

	/*
	 * Reference to the modal current showing
     */
	private bsModalRef: BsModalRef;


	constructor(private service: RegistryService, private modalService: BsModalService, private route: ActivatedRoute) {
	}

	ngOnInit(): void {
		const oid = this.route.snapshot.paramMap.get('oid');

		if (oid != null) {
			this.content = "PUB";
		}

		this.service.getMasterList(oid).then(list => {
			this.list = list;
		});
	}

	onViewMetadata(event: any): void {
		event.preventDefault();

		this.bsModalRef = this.modalService.show(PublishModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.readonly = true;
		this.bsModalRef.content.master = this.list;
	}




	renderContent(content: string): void {
		this.content = content;
	}
}
