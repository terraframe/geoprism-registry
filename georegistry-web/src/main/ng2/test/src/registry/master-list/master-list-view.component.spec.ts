import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { NO_ERRORS_SCHEMA } from "@angular/core";

import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { LOCALIZED_LABEL } from "@test/shared/mocks";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { RegistryService, IOService } from "@registry/service";
import { MASTER_LIST } from "../mocks";
import { MasterListViewComponent } from "@registry/component/master-list/master-list-view.component";

describe("MasterListViewComponent", () => {
	let component: MasterListViewComponent;
	let fixture: ComponentFixture<MasterListViewComponent>;
	let router: Router;
	let service: RegistryService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [MasterListViewComponent],
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
		router = TestBed.inject(Router);

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

		expect(component.content).toEqual("PUB");
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


