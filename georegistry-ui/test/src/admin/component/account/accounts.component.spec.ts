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

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { AccountsComponent } from "@admin/component/account/accounts.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { USER } from "@test/admin/mocks";
import { MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";
import { AccountService } from "@admin/service/account.service";
import { AdminModule } from "@admin/admin.module";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";

describe("AccountsComponent", () => {
	let component: AccountsComponent;
	let fixture: ComponentFixture<AccountsComponent>;
	let service: AccountService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [AccountsComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				BrowserAnimationsModule,
				ModalModule.forRoot(),
				BsDropdownModule.forRoot(),
				SharedModule,
				AdminModule,
			],
			providers: [
				BsModalService,
				BsModalRef,
				LocalizationService,
				EventService,
				AccountService,
				ProfileService,
				AuthService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isAdmin = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);
		service = TestBed.inject(AccountService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");

		// initialize component
		fixture = TestBed.createComponent(AccountsComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.res).toBeTruthy();
		expect(component.bsModalRef).toBeUndefined();
		expect(component.message).toBeNull();
	}));

	it(`Test Error`, async(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		expect(component.bsModalRef).toBeTruthy();
	}));

	it('Test ngOnInit', fakeAsync(() => {
		service.page = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				resultSet: [USER],
				count: 1,
				pageNumber: 1,
				pageSize: 10
			})
		);

		component.ngOnInit();

		tick(500);

		expect(component.res.count).toEqual(1);
	}));


	it(`Test Edit`, async(() => {
		expect(component.bsModalRef).toBeFalsy();

		component.edit(USER);

		expect(component.bsModalRef).toBeTruthy();
	}));

	it(`Test New instance`, async(() => {
		expect(component.bsModalRef).toBeFalsy();

		component.newInstance();

		expect(component.bsModalRef).toBeTruthy();
	}));


	it('Test OnPageChange', fakeAsync(() => {
		service.page = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				resultSet: [USER],
				count: 1,
				pageNumber: 2,
				pageSize: 10
			})
		);

		component.onPageChange(2);

		tick(500);

		expect(component.res.count).toEqual(1);
		expect(component.res.pageNumber).toEqual(2);
	}));

});


