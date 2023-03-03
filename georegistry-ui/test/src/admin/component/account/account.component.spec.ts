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

import { AccountComponent } from "@admin/component/account/account.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { ACCOUNT } from "@test/admin/mocks";
import { MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";
import { AccountService } from "@admin/service/account.service";
import { AdminModule } from "@admin/admin.module";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";

describe("AccountComponent", () => {
	let component: AccountComponent;
	let fixture: ComponentFixture<AccountComponent>;
	let service: AccountService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [AccountComponent],
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
		TestBed.inject(AuthService).isSRA = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);
		service = TestBed.inject(AccountService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");

		// initialize component
		fixture = TestBed.createComponent(AccountComponent);
		component = fixture.componentInstance;
		component.account = ACCOUNT;

		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.account).toBeTruthy();
		expect(component.bsModalRef).toBeTruthy();
		expect(component.message).toBeNull();
		expect(component.isSRA).toBeTrue();
	}));

	it(`Test Error`, async(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		expect(component.message).toBeTruthy();
	}));

	it(`Test Change Password`, async(() => {
		component.onChangePassword();

		expect(component.account.changePassword).toBeTrue();
	}));

	it(`Test Submit no roles`, async(() => {
		expect(component.message).toBeFalsy();

		component.onSubmit();

		expect(component.message).toBeTruthy();
	}));

	it('Test submit', fakeAsync(() => {
		let response = null;
		

		component.onEdit.subscribe(data => {
			response = data;
		})

		component.roleIds = ["Test-Role"];
		service.apply = jasmine.createSpy().and.returnValue(
			Promise.resolve(ACCOUNT)
		);

		component.onSubmit();

		tick(500);

		expect(response).toBeTruthy();
		expect(ACCOUNT.user.oid).toEqual(ACCOUNT.user.oid);
	}));

});


