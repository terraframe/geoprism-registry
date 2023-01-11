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


