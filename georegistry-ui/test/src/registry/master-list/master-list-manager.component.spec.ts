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
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { NO_ERRORS_SCHEMA } from "@angular/core";

import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { MasterListManagerComponent } from "@registry/component/master-list/master-list-manager.component";
import { LocalizationService, EventService, ProfileService, AuthService, DateService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { MOCK_HTTP_ERROR_RESPONSE, LOCALIZED_LABEL } from "@test/shared/mocks";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { RegistryService, IOService } from "@registry/service";
import { MASTER_LIST_BY_ORG, MASTER_LIST } from "../mocks";
import { MasterListViewComponent } from "@registry/component/master-list/master-list-view.component";

describe("MasterListManagerComponent", () => {
	let component: MasterListManagerComponent;
	let fixture: ComponentFixture<MasterListManagerComponent>;
	let router: Router;
	let service: RegistryService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [MasterListManagerComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				RouterTestingModule.withRoutes([
					{
						path: 'registry/master-list-view/:oid',
						component: MasterListViewComponent,
					},
				]),
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
				DateService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isAdmin = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);
		router = TestBed.inject(Router);

		service = TestBed.inject(RegistryService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");
		lService.create = jasmine.createSpy().and.returnValue(LOCALIZED_LABEL);

		// initialize component
		fixture = TestBed.createComponent(MasterListManagerComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.bsModalRef).toBeFalsy();
		expect(component.message).toBeFalsy();
		expect(component.orgs).toBeFalsy();
	}));

	it('Test ngOnInit', fakeAsync(() => {
		service.getMasterListsByOrg = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				locales: ['en_us'],
				orgs: [MASTER_LIST_BY_ORG]
			})
		);

		component.ngOnInit();

		tick(500);

		expect(component.orgs.length).toEqual(1);
	}));

	it(`Error`, async(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		expect(component.message).toBeTruthy();
	}));


	it(`test onCreate`, async(() => {
		component.onCreate(MASTER_LIST_BY_ORG);

		expect(component.bsModalRef).toBeTruthy();
	}));


	it('Test onView', fakeAsync(() => {
		component.onView(MASTER_LIST_BY_ORG.oid);

		tick(500);

		expect(router.url).toEqual('/registry/master-list-view/' + MASTER_LIST_BY_ORG.oid);
	}));


	it('Test onEdit', fakeAsync(() => {
		service.getMasterList = jasmine.createSpy().and.returnValue(
			Promise.resolve(MASTER_LIST)
		);

		component.onEdit({ label: 'Test', oid: MASTER_LIST.oid, visibility: "PUBLIC" });

		tick(500);

		expect(component.bsModalRef).toBeTruthy();
	}));


	it(`test onDelete`, async(() => {
		component.onDelete(MASTER_LIST_BY_ORG, { label: 'Test list', oid: MASTER_LIST.oid });

		expect(component.bsModalRef).toBeTruthy();
	}));
});


