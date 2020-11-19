import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { HierarchyOverTime } from '@registry/model/registry';

@Component({
	selector: 'manage-parent-versions-modal',
	templateUrl: './manage-parent-versions-modal.component.html',
	styleUrls: []
})
export class ManageParentVersionsModalComponent implements OnInit {
    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
	onVersionChange: Subject<HierarchyOverTime>;

	hierarchy: HierarchyOverTime = null;

	constructor(public bsModalRef: BsModalRef) { }

	ngOnInit(): void {

		this.onVersionChange = new Subject();
	}

	init(hierarchy: HierarchyOverTime): void {
		this.hierarchy = hierarchy;
	}


	handleVersionChange(hierarchy: HierarchyOverTime): void {
		if (hierarchy != null) {
			this.onVersionChange.next(hierarchy);
		}

		this.bsModalRef.hide();
	}

}
