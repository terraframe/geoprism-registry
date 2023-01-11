import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { AccountInviteComponent } from "@admin/component/account/account-invite.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { USER } from "@test/admin/mocks";
import { MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";
import { AccountService } from "@admin/service/account.service";
import { AdminModule } from "@admin/admin.module";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";

describe("AccountInviteComponent", () => {
	let component: AccountInviteComponent;
	let fixture: ComponentFixture<AccountInviteComponent>;
	let service: AccountService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [AccountInviteComponent],
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
		fixture = TestBed.createComponent(AccountInviteComponent);
		component = fixture.componentInstance;

		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Test Error`, async(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		expect(component.message).toBeTruthy();
	}));

	it(`Init`, async(() => {
		expect(component.bsModalRef).toBeTruthy();
		expect(component.message).toBeNull();
	}));

	it('Test ngOnInit', fakeAsync(() => {
		service.newInvite = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				changePassword: false,
				user: USER,
				roles: ['Test ABC']
			})
		);

		component.ngOnInit();

		tick(500);

		expect(component.invite.roles.length).toEqual(1);
	}));

	it(`Test onRoleIdsUpdate`, async(() => {
		expect(component.roleIds.length).toEqual(0);

		component.onRoleIdsUpdate(['id-1', 'id-2']);

		expect(component.roleIds.length).toEqual(2);
	}));

	it('Test inviteUser', fakeAsync(() => {

		component.roleIds = ["Test-Role"];
		service.inviteUser = jasmine.createSpy().and.returnValue(
			Promise.resolve()
		);

		component.onSubmit();

		tick(500);
	}));
});


