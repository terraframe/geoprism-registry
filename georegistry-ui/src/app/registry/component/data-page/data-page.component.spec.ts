///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///


import { Component, Input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgClass } from '@angular/common';
import { RouterLink, provideRouter } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';

import { LocalizationService } from '@shared/service/localization.service';
import { AuthService } from '@shared/service/auth.service';
import { LocalizeComponent } from '@shared/component/localize/localize.component';

import { DataPageComponent } from './data-page.component';

/** Stand-in for the importer, so the page can be rendered without its services */
@Component({ selector: 'dataimporter', template: '', standalone: true })
class DataImporterStubComponent {
  @Input() format: string;
}

/** Stand-in for the page chrome (header, side nav), which needs the whole application */
@Component({ selector: 'page-container', template: '<ng-content></ng-content>', standalone: true })
class PageContainerStubComponent {}

function authStub(maintainer: boolean): Partial<AuthService> {
  return {
    isAdmin: () => false,
    isMaintainer: () => maintainer,
    isContributer: () => true,
  };
}

describe('DataPageComponent', () => {
  let fixture: ComponentFixture<DataPageComponent>;
  let component: DataPageComponent;
  let element: HTMLElement;
  let decode: jasmine.Spy;

  async function create(maintainer: boolean): Promise<void> {
    decode = jasmine.createSpy('decode').and.callFake((key: string) => 'decoded:' + key);

    await TestBed.configureTestingModule({
      imports: [DataPageComponent],
      providers: [
        provideRouter([]),
        { provide: LocalizationService, useValue: { decode: decode } },
        { provide: AuthService, useValue: authStub(maintainer) },
        { provide: BsModalService, useValue: {} },
      ],
    })
      .overrideComponent(DataPageComponent, {
        set: {
          imports: [PageContainerStubComponent, RouterLink, NgClass, LocalizeComponent, DataImporterStubComponent],
        },
      })
      .compileComponents();

    fixture = TestBed.createComponent(DataPageComponent);
    component = fixture.componentInstance;
    element = fixture.nativeElement;
    fixture.detectChanges();
  }

  describe('as a maintainer', () => {
    beforeEach(async () => create(true));

    it('renderContent("GEOPACKAGE") sets the content and localized page title', () => {
      component.renderContent('GEOPACKAGE');

      expect(component.content).toBe('GEOPACKAGE');
      expect(decode).toHaveBeenCalledWith('geopackage.title');
      expect(component.pageTitle).toBe('decoded:geopackage.title');
    });

    it('shows the geopackage menu entry, which renders a GEOPACKAGE importer', () => {
      const link = Array.from(element.querySelectorAll('a')).find(
        (a) => a.querySelector('localize')?.getAttribute('key') === 'import-geopackage-localization-manager.menu'
      );

      expect(link).toBeTruthy();

      link.click();
      fixture.detectChanges();

      expect(component.content).toBe('GEOPACKAGE');
      expect(link.classList).toContain('active');
      expect(element.querySelector('h2').textContent.trim()).toBe('decoded:geopackage.title');

      const importers = fixture.debugElement
        .queryAll((de) => de.componentInstance instanceof DataImporterStubComponent)
        .map((de) => de.componentInstance as DataImporterStubComponent);

      expect(importers.length).toBe(1);
      expect(importers[0].format).toBe('GEOPACKAGE');
    });

    it('still renders the shapefile importer for SHAPEFILE', () => {
      component.renderContent('SHAPEFILE');
      fixture.detectChanges();

      expect(component.pageTitle).toBe('decoded:shapefile.title');

      const importer = fixture.debugElement.query((de) => de.componentInstance instanceof DataImporterStubComponent);
      expect((importer.componentInstance as DataImporterStubComponent).format).toBe('SHAPEFILE');
    });
  });

  describe('as a non-maintainer', () => {
    beforeEach(async () => create(false));

    it('does not show the geopackage menu entry', () => {
      const keys = Array.from(element.querySelectorAll('localize')).map((e) => e.getAttribute('key'));

      expect(component.content).toBe('EXPORT');
      expect(keys).not.toContain('import-geopackage-localization-manager.menu');
    });
  });
});
