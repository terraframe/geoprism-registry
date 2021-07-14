import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { OrganizationModalComponent } from "@admin/component/organization/organization-modal.component";
import { LocalizationService, OrganizationService, EventService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";

import { ORGANIZATION } from "@test/admin/mocks";

describe("OrganizationModalComponent", () => {
	let component: OrganizationModalComponent;
	let fixture: ComponentFixture<OrganizationModalComponent>;
	let orgService: OrganizationService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [OrganizationModalComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				FormsModule,
				SharedModule,
				ModalModule.forRoot()
			],
			providers: [
				BsModalService,
				BsModalRef,
				OrganizationService,
				LocalizationService,
				EventService,
				AuthService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		let lService = TestBed.inject(LocalizationService);
		orgService = TestBed.inject(OrganizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");
		lService.create = jasmine.createSpy().and.returnValue({
			localizedValue: '',
			localeValues: [{ locale: "defaultLocale", value: 'Test' }]
		});

		// initialize component
		fixture = TestBed.createComponent(OrganizationModalComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.isNewOrganization).toBeTrue();
		expect(component.onSuccess).toBeTruthy();
	}));

	it('Test cancel', fakeAsync(() => {
		component.cancel();

		tick(500);

		expect(component.bsModalRef).toBeTruthy();
	}));

	it('Test submit', fakeAsync(() => {
		let response = null;

		component.onSuccess.subscribe(data => {
			response = data;
		})

		orgService.newOrganization = jasmine.createSpy().and.returnValue(
			Promise.resolve(ORGANIZATION)
		);

		component.onSubmit();

		tick(500);

		expect(response).toBeTruthy();
		expect(response.code).toEqual(ORGANIZATION.code);
	}));
});


