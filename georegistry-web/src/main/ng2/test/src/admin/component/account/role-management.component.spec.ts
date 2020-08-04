import { TestBed, ComponentFixture, async } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from "ngx-bootstrap/modal";

import { RoleManagementComponent } from "@admin/component/account/role-management.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { AdminModule } from "@admin/admin.module";
import { ROLE } from "@test/admin/mocks";

describe("RoleManagementComponent", () => {
	let component: RoleManagementComponent;
	let fixture: ComponentFixture<RoleManagementComponent>;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [RoleManagementComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				SharedModule,
				AdminModule,
				ModalModule.forRoot()
			],
			providers: [
				BsModalService,
				BsModalRef,
				LocalizationService,
				EventService,
				AuthService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isSRA = jasmine.createSpy().and.returnValue(true);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");

		// initialize component
		fixture = TestBed.createComponent(RoleManagementComponent);
		component = fixture.componentInstance;

		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.isSRA).toBeTrue();
		expect(component.isAdmin).toBeTrue();
		expect(component.isMaintainer).toBeTrue();
		expect(component.isContributor).toBeTrue();

		expect(component.message).toBeNull();
	}));

	it(`Test formatRoles`, async(() => {
		const result = component.formatRoles([ROLE]);

		expect(result.ORGANIZATIONS.length).toEqual(1);
	}));

});


