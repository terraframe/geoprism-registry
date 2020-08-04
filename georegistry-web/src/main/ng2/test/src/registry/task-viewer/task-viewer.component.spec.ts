import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';

import { TaskViewerComponent } from "@registry/component/task-viewer/task-viewer.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { TaskService } from "@registry/service";
import { TASK } from "../mocks";

describe("TaskViewerComponent", () => {
	let component: TaskViewerComponent;
	let fixture: ComponentFixture<TaskViewerComponent>;
	let service: TaskService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [TaskViewerComponent],
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
				TaskService,
				ProfileService,
				AuthService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isAdmin = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);
		service = TestBed.inject(TaskService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");

		// initialize component
		fixture = TestBed.createComponent(TaskViewerComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.inProgressTasks).toBeTruthy();
		expect(component.completedTasks).toBeTruthy();
	}));

	it('Test ngOnInit', fakeAsync(() => {
		service.getMyTasks = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				results: [TASK],
				count: 1,
				pageNumber: 1,
				pageSize: 10
			})
		);

		component.ngOnInit();

		tick(500);

		expect(component.inProgressTasks.count).toEqual(1);
	}));

	it(`upper`, async(() => {

		expect(component.upper("sS")).toEqual("SS");
		expect(component.upper(null)).toEqual("");
	}));

	it('Test onInProgressTasksPageChange', fakeAsync(() => {
		service.getMyTasks = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				results: [TASK],
				count: 1,
				pageNumber: 2,
				pageSize: 10
			})
		);

		component.onInProgressTasksPageChange(2)

		tick(500);

		expect(component.inProgressTasks.pageNumber).toEqual(2);
	}));

	it('Test onCompletedTasksPageChange', fakeAsync(() => {
		service.getMyTasks = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				results: [TASK],
				count: 1,
				pageNumber: 2,
				pageSize: 10
			})
		);

		component.onCompletedTasksPageChange(2)

		tick(500);

		expect(component.completedTasks.pageNumber).toEqual(2);
	}));

	it('Test onViewAllCompletedTasks', fakeAsync(() => {
		service.getMyTasks = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				results: [TASK],
				count: 1,
				pageNumber: 1,
				pageSize: 10
			})
		);

		component.onViewAllCompletedTasks();

		tick(500);

		expect(component.completedTasks.pageNumber).toEqual(1);
		expect(component.isViewAllOpen).toBeTrue();
	}));

	it('Test onCompleteTask', fakeAsync(() => {
		service.getMyTasks = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				results: [TASK],
				count: 1,
				pageNumber: 1,
				pageSize: 10
			})
		);
		
		service.completeTask = jasmine.createSpy().and.returnValue(
			Promise.resolve(TASK)
		);

		component.ngOnInit();

		tick(500);

		expect(component.inProgressTasks.results.length).toEqual(1);
		expect(component.completedTasks.results.length).toEqual(0);
		
		component.onCompleteTask(TASK);

		tick(500);

		expect(component.completedTasks.results.length).toEqual(1);
		expect(component.inProgressTasks.results.length).toEqual(0);
	}));

	it('Test onMoveTaskToInProgress', fakeAsync(() => {
		service.getMyTasks = jasmine.createSpy().and.returnValue(
			Promise.resolve({
				results: [TASK],
				count: 1,
				pageNumber: 1,
				pageSize: 10
			})
		);
		
		service.setTaskStatus = jasmine.createSpy().and.returnValue(
			Promise.resolve(TASK)
		);

		component.onCompletedTasksPageChange(1);

		tick(500);

		expect(component.inProgressTasks.results.length).toEqual(0);
		expect(component.completedTasks.results.length).toEqual(1);
		
		component.onMoveTaskToInProgress(TASK);

		tick(500);

		expect(component.completedTasks.results.length).toEqual(0);
		expect(component.inProgressTasks.results.length).toEqual(1);
	}));



	//
	//
	//	it(`Test Edit`, async(() => {
	//		expect(component.bsModalRef).toBeFalsy();
	//
	//		component.edit(USER);
	//
	//		expect(component.bsModalRef).toBeTruthy();
	//	}));
	//
	//	it(`Test New instance`, async(() => {
	//		expect(component.bsModalRef).toBeFalsy();
	//
	//		component.newInstance();
	//
	//		expect(component.bsModalRef).toBeTruthy();
	//	}));
	//
	//
	//	it('Test OnPageChange', fakeAsync(() => {
	//		service.page = jasmine.createSpy().and.returnValue(
	//			Promise.resolve({
	//				results: [USER],
	//				count: 1,
	//				pageNumber: 2,
	//				pageSize: 10
	//			})
	//		);
	//
	//		component.onPageChange(2);
	//
	//		tick(500);
	//
	//		expect(component.res.count).toEqual(1);
	//		expect(component.res.pageNumber).toEqual(2);
	//	}));
	//
});


