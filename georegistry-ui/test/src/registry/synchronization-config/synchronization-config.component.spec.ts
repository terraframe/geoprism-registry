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

import { TestBed, ComponentFixture, async, tick, fakeAsync, flush, discardPeriodicTasks } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { SynchronizationConfigComponent } from "@registry/component/synchronization-config/synchronization-config.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { MOCK_HTTP_ERROR_RESPONSE, PAGE } from "@test/shared/mocks";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { SynchronizationConfigService } from "@registry/service";
import { ActivatedRoute } from "@angular/router";
import { SYNCH_CONFIG, EXPORT_JOB, MOCK_SOCKET } from "../mocks";

describe("SynchronizationConfigComponent", () => {
	let component: SynchronizationConfigComponent;
	let fixture: ComponentFixture<SynchronizationConfigComponent>;
	let service: SynchronizationConfigService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [SynchronizationConfigComponent],
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
				SynchronizationConfigService,
				ProfileService,
				AuthService,
				{
					provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 'test-oid' } } }
				},

			]
		}).compileComponents();
	}));

	beforeEach(() => {
		spyOn(window, 'WebSocket').and.returnValue(MOCK_SOCKET);
		
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isAdmin = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);
		service = TestBed.inject(SynchronizationConfigService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");

		// initialize component
		fixture = TestBed.createComponent(SynchronizationConfigComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.message).toBeFalsy();
		expect(component.config).toBeFalsy();
		expect(component.page).toBeTruthy();
	}));

	it(`Error`, async(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		expect(component.message).toBeTruthy();
	}));

	it(`getJobStatus`, async(() => {
		expect(component.getJobStatus(EXPORT_JOB)).toEqual("WORKING");
	}));

	it('Test ngOnInit', fakeAsync(() => {
		service.get = jasmine.createSpy().and.returnValue(
			Promise.resolve(SYNCH_CONFIG)
		);

		component.ngOnInit();

		tick(500);

		discardPeriodicTasks();

		expect(component.config).toBeTruthy();
	}));

	it('Test onRun', fakeAsync(() => {
		component.config = SYNCH_CONFIG;

		service.getJobs = jasmine.createSpy().and.returnValue(
			Promise.resolve(PAGE(EXPORT_JOB, 2))
		);

		service.run = jasmine.createSpy().and.returnValue(
			Promise.resolve()
		);

		component.onRun();

		tick(500);

		discardPeriodicTasks();

		expect(component.page.pageNumber).toEqual(2);
	}));

	it('Test onPageChange', fakeAsync(() => {
		component.config = SYNCH_CONFIG;

		service.getJobs = jasmine.createSpy().and.returnValue(
			Promise.resolve(PAGE(EXPORT_JOB, 2))
		);

		component.onPageChange(2);

		tick(500);

		discardPeriodicTasks();

		expect(component.page.pageNumber).toEqual(2);
		expect(component.page.resultSet[0].stepConfig).toBeTruthy();
	}));
});


