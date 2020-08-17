import { TestBed, ComponentFixture, async, tick, fakeAsync, discardPeriodicTasks } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { NgxPaginationModule } from "ngx-pagination";

import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { LOCALIZED_LABEL, MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { RegistryService, IOService } from "@registry/service";
import { PublishedMasterListHistoryComponent } from "@registry/component/master-list/published-master-list-history.component";
import { MasterListComponent } from "@registry/component/master-list/master-list.component";
import { MASTER_LIST, MASTER_LIST_VERSION, PAGINATION_PAGE } from "../mocks";

describe("PublishedMasterListHistoryComponent", () => {
	let component: PublishedMasterListHistoryComponent;
	let fixture: ComponentFixture<PublishedMasterListHistoryComponent>;
	let router: Router;
	let service: RegistryService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [PublishedMasterListHistoryComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				RouterTestingModule.withRoutes([
					{
						path: 'registry/master-list/:oid/:published',
						component: MasterListComponent,
					},
				]),
				BrowserAnimationsModule,
				ModalModule.forRoot(),
				BsDropdownModule.forRoot(),
				NgxPaginationModule,
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
		fixture = TestBed.createComponent(PublishedMasterListHistoryComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, fakeAsync(() => {
		
		tick(500);
		
		expect(component.list).toBeFalsy();
		expect(component.bsModalRef).toBeFalsy();
		expect(component.message).toBeFalsy();
	}));

	it(`Error`, fakeAsync(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		tick(500);
		
		expect(component.message).toBeTruthy();
	}));


	it('Test ngOnInit', fakeAsync(() => {
		service.getMasterListHistory = jasmine.createSpy().and.returnValue(
			Promise.resolve(MASTER_LIST)
		);

		component.ngOnInit();

		tick(500);

		discardPeriodicTasks();

		expect(component.list).toBeTruthy();
	}));

	it('Test onPublish', fakeAsync(() => {
		service.publishMasterListVersions = jasmine.createSpy().and.returnValue(
			Promise.resolve({ job: 'JOB-ID' })
		);

		service.getPublishMasterListJobs = jasmine.createSpy().and.returnValue(
			Promise.resolve(PAGINATION_PAGE(MASTER_LIST_VERSION, 2))
		);

		component.list = MASTER_LIST;

		component.onPublish();

		tick(500);

		discardPeriodicTasks();

		expect(component.page.pageNumber).toEqual(2);
	}));

	it(`test onViewMetadata`, fakeAsync(() => {
		component.list = MASTER_LIST;

		component.onViewMetadata();

		tick(500);

		expect(component.bsModalRef).toBeTruthy();
	}));

	it(`test onDelete`, fakeAsync(() => {
		component.list = MASTER_LIST;

		component.onDeleteMasterListVersion(MASTER_LIST_VERSION);

		tick(500);

		expect(component.bsModalRef).toBeTruthy();
	}));

	it('Test onView', fakeAsync(() => {
		component.onView(MASTER_LIST_VERSION);

		tick(500);

		discardPeriodicTasks();

		expect(router.url).toEqual('/registry/master-list/' + MASTER_LIST_VERSION.oid + '/true');
	}));


});


