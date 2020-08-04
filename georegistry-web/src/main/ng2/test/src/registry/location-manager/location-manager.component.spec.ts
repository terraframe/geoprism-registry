import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { LocationManagerComponent } from "@registry/component/location-manager/location-manager.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MapService, RegistryService } from "@registry/service";
import { ActivatedRoute } from "@angular/router";
import { GEO_OBJECT, LOCATION_INFORMATION } from "../mocks";

describe("LocationManagerComponent", () => {
	let component: LocationManagerComponent;
	let fixture: ComponentFixture<LocationManagerComponent>;
	let service: MapService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [LocationManagerComponent],
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
				MapService,
				RegistryService,
				ProfileService,
				AuthService,
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

	it(`Init`, async(() => {
		expect(component.data).toBeTruthy();
		expect(component.map).toBeTruthy();
		expect(component.bsModalRef).toBeFalsy();
		expect(component.childType).toBeFalsy();
		expect(component.hierarchy).toBeFalsy();
		expect(component.dateStr).toBeFalsy();
		expect(component.breadcrumbs.length).toEqual(0);
		expect(component.current).toBeFalsy();
	}));

	it('Test back(null)', fakeAsync(() => {
		service.roots = jasmine.createSpy().and.returnValue(
			Promise.resolve(LOCATION_INFORMATION)
		);

		component.breadcrumbs = [GEO_OBJECT];

		component.map.getSource = jasmine.createSpy().and.returnValue({
			setData: () => { }
		});

		component.back(null);

		tick(500);

		expect(component.current).toBeFalsy();
	}));

	it('Test back', fakeAsync(() => {
		service.select = jasmine.createSpy().and.returnValue(
			Promise.resolve(LOCATION_INFORMATION)
		);

		component.breadcrumbs = [GEO_OBJECT];

		component.map.getSource = jasmine.createSpy().and.returnValue({
			setData: () => { }
		});

		component.back(GEO_OBJECT);

		tick(500);

		expect(component.current).toBeTruthy()
		expect(component.current.properties.code).toEqual(GEO_OBJECT.properties.code);
	}));

	it('Test refresh(null)', fakeAsync(() => {
		service.roots = jasmine.createSpy().and.returnValue(
			Promise.resolve(LOCATION_INFORMATION)
		);

		component.map.getSource = jasmine.createSpy().and.returnValue({
			setData: () => { }
		});

		component.refresh();

		tick(500);

		expect(component.data).toBeTruthy()
		expect(component.hierarchy).toEqual(LOCATION_INFORMATION.hierarchy);
		expect(component.childType).toEqual(LOCATION_INFORMATION.childType);
	}));

	it('Test refresh', fakeAsync(() => {
		service.select = jasmine.createSpy().and.returnValue(
			Promise.resolve(LOCATION_INFORMATION)
		);

		component.map.getSource = jasmine.createSpy().and.returnValue({
			setData: () => { }
		});

		component.current = GEO_OBJECT;
		component.refresh();

		tick(500);

		expect(component.data).toBeTruthy()
		expect(component.hierarchy).toEqual(LOCATION_INFORMATION.hierarchy);
		expect(component.childType).toEqual(LOCATION_INFORMATION.childType);
	}));

	it('Test handleDateChange', fakeAsync(() => {
		service.roots = jasmine.createSpy().and.returnValue(
			Promise.resolve(LOCATION_INFORMATION)
		);

		component.breadcrumbs = [GEO_OBJECT];

		component.map.getSource = jasmine.createSpy().and.returnValue({
			setData: () => { }
		});

		component.dateStr = "12/31/2020";
		component.handleDateChange();

		tick(500);

		expect(component.current).toBeFalsy();
	}));

	it(`Test addContextLayerModal`, async(() => {
		expect(component.bsModalRef).toBeFalsy();

		component.addContextLayerModal();

		expect(component.bsModalRef).toBeTruthy();
	}));

	it(`Test addBreadcrumb`, async(() => {
		expect(component.breadcrumbs.length).toEqual(0);

		component.addBreadcrumb(GEO_OBJECT);

		expect(component.breadcrumbs.length).toEqual(1);
	}));

	it(`Test Duplicate addBreadcrumb`, async(() => {
		expect(component.breadcrumbs.length).toEqual(0);

		component.addBreadcrumb(GEO_OBJECT);
		component.addBreadcrumb(GEO_OBJECT);

		expect(component.breadcrumbs.length).toEqual(1);
	}));

	it(`Test expand`, async(() => {
		expect(component.current).toBeFalsy();

		component.expand(GEO_OBJECT);

		expect(component.current).toBeTruthy();
	}));

	it(`Test setData`, async(() => {
		component.data = null;

		component.setData(LOCATION_INFORMATION);

		expect(component.data).toBeTruthy()
		expect(component.hierarchy).toEqual(LOCATION_INFORMATION.hierarchy);
		expect(component.childType).toEqual(LOCATION_INFORMATION.childType);
	}));
});


