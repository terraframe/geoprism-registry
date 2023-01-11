import { TestBed, ComponentFixture, waitForAsync, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { RouterTestingModule } from '@angular/router/testing';

import { LocationManagerComponent } from "@registry/component/location-manager/location-manager.component";
import { CgrHeaderComponent } from "@shared/component/header/header.component";

import { LocalizationService, EventService, ProfileService, AuthService, DateService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MapService, RegistryService } from "@registry/service";
import { ActivatedRoute } from "@angular/router";
import { GEO_OBJECT, LOCATION_INFORMATION } from "../mocks";

describe("LocationManagerComponent", () => {
	let component: LocationManagerComponent;
	let fixture: ComponentFixture<LocationManagerComponent>;
	let service: MapService;
	let cgrHeaderComponent: CgrHeaderComponent;

	beforeEach(waitForAsync(() => {
		TestBed.configureTestingModule({
			declarations: [
				LocationManagerComponent,
				CgrHeaderComponent
			],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				BrowserAnimationsModule,
				ModalModule.forRoot(),
				BsDropdownModule.forRoot(),
				SharedModule,
				RouterTestingModule,
			],
			providers: [
				{
			        provide: ActivatedRoute,
			        useValue: {
			          params: of({
				        geoobjectuid: 'test-oid',
						geoobjecttypecode: "test-type-code",
						datestr: "5000-12-31",
						hideSearchOptions: "false",
						backReference: "test-back-reference"
				      }),
			        },
			    },
//				{
//					provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 'test-oid' } } }
//				},
				BsModalService,
				BsModalRef,
				LocalizationService,
				EventService,
				MapService,
				RegistryService,
				ProfileService,
				AuthService,
				DateService,

			]
		}).compileComponents();
		
//		jasmine.DEFAULT_TIMEOUT_INTERVAL = 1000000;
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isAdmin = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);
		service = TestBed.inject(MapService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");

		// initialize component
		fixture = TestBed.createComponent(LocationManagerComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});
	
	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, waitForAsync(() => {
		
		expect(component.data).toBeTruthy();
		expect(component.map).toBeTruthy();
		expect(component.bsModalRef).toBeFalsy();
		expect(component.dateStr).toEqual('5000-12-31');
		expect(component.current).toBeFalsy();
	}));
	

	it('Test back(null)', fakeAsync(() => {
		service.roots = jasmine.createSpy().and.returnValue(
			Promise.resolve(LOCATION_INFORMATION)
		);

		component.map.getSource = jasmine.createSpy().and.returnValue({
			setData: () => { }
		});

		tick(500);

		expect(component.current).toBeFalsy();
	}));


	it('Test handleDateChange', fakeAsync(() => {
		service.roots = jasmine.createSpy().and.returnValue(
			Promise.resolve(LOCATION_INFORMATION)
		);

		component.map.getSource = jasmine.createSpy().and.returnValue({
			setData: () => { }
		});

		component.dateStr = "12/31/2020";
		component.handleDateChange();

		tick(500);

		expect(component.current).toBeFalsy();
	}));
	
//	it(`Test expand`, waitForAsync(() => {
//		expect(component.current).toBeFalsy();
//
//		component.select(GEO_OBJECT, null);
//
//		expect(component.current).toBeTruthy();
//	}));

//	it(`Test setData`, async(() => {
//		component.data = null;
//
//		component.setData(LOCATION_INFORMATION);
//
//		expect(component.data).toBeTruthy()
//	}));

});


