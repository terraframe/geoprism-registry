import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { ContextLayerModalComponent } from "@registry/component/location-manager/context-layer-modal.component";
import { LocationManagerComponent } from "@registry/component/location-manager/location-manager.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MapService, RegistryService } from "@registry/service";
import { ActivatedRoute } from "@angular/router";
import { GEO_OBJECT, LOCATION_INFORMATION } from "../mocks";

describe("LocationManagerComponent", () => {
	let component: ContextLayerModalComponent;
	let fixture: ComponentFixture<ContextLayerModalComponent>;
	let service: MapService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [ContextLayerModalComponent],
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
		fixture = TestBed.createComponent(ContextLayerModalComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.bsModalRef).toBeTruthy();
	}));

//	it(`Test groupHasContextLayers`, async(() => {
//		component.groupHasContextLayers();
//	}));

});


