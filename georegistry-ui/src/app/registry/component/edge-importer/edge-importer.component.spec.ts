import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EdgeImporterComponent } from './edge-importer.component';

describe('EdgeImporterComponent', () => {
  let component: EdgeImporterComponent;
  let fixture: ComponentFixture<EdgeImporterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EdgeImporterComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EdgeImporterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
