///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { NO_ERRORS_SCHEMA } from "@angular/core";

import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { LocalizationService, EventService, ProfileService, AuthService, DateService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { LOCALIZED_LABEL } from "@test/shared/mocks";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { RegistryService, IOService } from "@registry/service";
import { MASTER_LIST } from "../mocks";
import { MasterListViewComponent } from "@registry/component/master-list/master-list-view.component";

describe("MasterListViewComponent", () => {
	let component: MasterListViewComponent;
	let fixture: ComponentFixture<MasterListViewComponent>;
	let service: RegistryService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [MasterListViewComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				BrowserAnimationsModule,
				ModalModule.forRoot(),
				BsDropdownModule.forRoot(),
				SharedModule
			],
			providers: [
				BsModalService,
				BsModalRef,
				LocalizationService,
				EventService,
				RegistryService,
				ProfileService,
				AuthService,
				IOService,
				DateService,
				{
					provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 'test-oid' } } }
				},

			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isAdmin = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);

		service = TestBed.inject(RegistryService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");
		lService.create = jasmine.createSpy().and.returnValue(LOCALIZED_LABEL);

		// initialize component
		fixture = TestBed.createComponent(MasterListViewComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.bsModalRef).toBeFalsy();
		expect(component.list).toBeFalsy();
		expect(component.content).toEqual("PUB");
	}));

	it('Test ngOnInit no oid', fakeAsync(() => {
		service.getMasterList = jasmine.createSpy().and.returnValue(
			Promise.resolve(MASTER_LIST)
		);

		component.ngOnInit();

		tick(500);

		expect(component.content).toEqual("EXP");
		expect(component.list).toBeTruthy();
	}));

	it(`Test renderContent`, async(() => {
		component.renderContent('TEST');

		expect(component.content).toEqual("TEST");
	}));

	it(`test onViewMetadata`, async(() => {
		component.list = MASTER_LIST;

		component.onViewMetadata({ preventDefault: () => { } });

		expect(component.bsModalRef).toBeTruthy();
	}));
});


