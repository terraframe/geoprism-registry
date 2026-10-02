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

import { ApplicationConfig, importProvidersFrom, provideZoneChangeDetection } from '@angular/core';
import { BsDatepickerModule } from 'ngx-bootstrap/datepicker';
import { NgxPaginationModule } from 'ngx-pagination';
import { TabsModule } from 'ngx-bootstrap/tabs';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { ProgressbarModule } from 'ngx-bootstrap/progressbar';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { ButtonsModule } from 'ngx-bootstrap/buttons';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ConfigurationService } from '@core/service/configuration.service';
import { HttpErrorInterceptor } from '@core/service/http-error.interceptor';
import { HTTP_INTERCEPTORS, withInterceptorsFromDi, provideHttpClient } from '@angular/common/http';
import { APP_BASE_HREF, HashLocationStrategy, LocationStrategy, PlatformLocation } from '@angular/common';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideAppInitializer } from '@angular/core';
import { inject } from '@angular/core';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { ModalModule } from 'ngx-bootstrap/modal';
import { InjectionToken, type Provider } from '@angular/core';

/**
 * Injection token for the MapLibre GL JS worker URL, applied via `setWorkerUrl()`
 * before a map is created. Prefer {@link provideMaplibreWorker} over using this
 * token directly.
 *
 * @category Map Component
 */
export const MAPLIBRE_WORKER_URL = new InjectionToken<string>('ngx-maplibre-gl worker URL');

/**
 * Provides the URL of the MapLibre GL JS web worker script
 * (`maplibre-gl-worker.mjs`). This defers the `setWorkerUrl()` call until the
 * first `mgl-map` is set up, keeping `maplibre-gl` out of the initial bundle.
 *
 * Relative URLs are resolved against `document.baseURI` so sub-path deployments
 * (`--base-href`) work. The worker file and `maplibre-gl-shared.mjs` must be
 * served at that location (typically via `angular.json` `assets`).
 *
 * @example
 * ```typescript
 * // app.config.ts
 * export const appConfig: ApplicationConfig = {
 *   providers: [provideMaplibreWorker('maplibre-gl-worker.mjs')],
 * };
 * ```
 *
 * @see [setWorkerUrl is bundler-only](https://maplibre.org/maplibre-gl-js/docs/guides/v5-to-v6-migration-guide/#setworkerurl-is-bundler-only)
 *
 * @category Map Component
 */
export function provideMaplibreWorker(workerUrl: string): Provider {
  return { provide: MAPLIBRE_WORKER_URL, useValue: workerUrl };
}

export const appConfig: ApplicationConfig = {
  providers: [
    {
      provide: APP_BASE_HREF,
      useFactory: (s: PlatformLocation) => s.getBaseHrefFromDOM(),
      deps: [PlatformLocation],
    },
    provideZoneChangeDetection(),
    provideMaplibreWorker('maplibre-gl-worker.mjs'),
    provideHttpClient(withInterceptorsFromDi()),
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpErrorInterceptor,
      multi: true,
    },
    // ForgotPasswordService,
    // PasswordStrengthBarComponent,
    // HubService,
    ConfigurationService,
    provideRouter(routes),
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    provideAppInitializer(() => {
      const service = inject(ConfigurationService);
      return service.load();
    }),
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: false || 'none',
        },
      },
    }),
    importProvidersFrom(
      // FileUploadModule,
      // TreeModule,
      NgxPaginationModule,
      ModalModule,
      BsDropdownModule,
      ButtonsModule,
      TypeaheadModule,
      ProgressbarModule,
      CollapseModule,
      TabsModule,
      BsDatepickerModule
    ),
  ],
};
