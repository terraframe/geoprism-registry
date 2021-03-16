import { Component, OnInit, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import {
	trigger,
	style,
	animate,
	transition,
} from '@angular/animations';

import { GeoObjectType, Attribute, GeoObjectOverTime } from '@registry/model/registry';


@Component({
	selector: 'manage-versions-modal',
	templateUrl: './manage-versions-modal.component.html',
	styleUrls: ['./manage-versions-modal.css'],
	host: { '[@fadeInOut]': 'true' },
	animations: [
		[
			trigger('fadeInOut', [
				transition('void => *', [
					style({
						opacity: 0
					}),
					animate('1000ms')
				]),
				transition(':leave',
					animate('500ms', 
						style({
							opacity: 0
						})
					)
				)
			])
		]]
})
export class ManageVersionsModalComponent implements OnInit {
	
	@Input() readonly: boolean;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
	onAttributeVersionChange: Subject<GeoObjectOverTime>;

	attribute: Attribute;

	geoObjectType: GeoObjectType;

	geoObjectOverTime: GeoObjectOverTime;

	isNewGeoObject: boolean = false;

	constructor(public bsModalRef: BsModalRef) { }

	ngOnInit(): void {
		this.onAttributeVersionChange = new Subject();
	}

	onVersionChange(geoObjectOverTime: GeoObjectOverTime): void {
		if (geoObjectOverTime != null) {
			this.onAttributeVersionChange.next(geoObjectOverTime);
		}

		this.bsModalRef.hide();
	}
}
