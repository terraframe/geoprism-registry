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

import { Component, DOCUMENT, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { ConfigurationService } from '@core/service/configuration.service';
import { filter } from 'rxjs';
import { environment } from 'src/environments/environment';
import { PhetsarathFontComponent } from './core/component/phetsarath-font/phetsarath-font.component';
import { MAPLIBRE_WORKER_URL } from './app.config';
import { setWorkerUrl } from 'maplibre-gl';
declare const window: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: [],
  standalone: true,
  imports: [PhetsarathFontComponent, RouterOutlet],
})
export class CgrAppComponent {
  favIcon: HTMLLinkElement | null = document.querySelector('#appIcon');
  customFont: string;
  readonly workerUrl = inject(MAPLIBRE_WORKER_URL, { optional: true });
  readonly document = inject(DOCUMENT);

  constructor(
    private router: Router,
    private configuration: ConfigurationService
  ) {
    this.favIcon!.href = environment.apiUrl + '/api/asset/view?oid=logo';
    this.customFont = configuration.getCustomFont();
    const token = configuration.getAnalyticsToken();

    if (this.workerUrl) {
      setWorkerUrl(new URL(this.workerUrl, this.document.baseURI).href);
    } else {
      console.warn(
        'maplibre-gl: MAPLIBRE_WORKER_URL is not provided. ' +
          'MapLibre GL JS v6 requires a worker URL to function correctly. ' +
          "Provide it via provideMaplibreWorker('maplibre-gl-worker.mjs') " +
          'in your application config or component providers.'
      );
    }

    if (token != null && token.trim().length > 0) {
      this.addGAScript(token);

      this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe((event: NavigationEnd) => {
        /** START : Code to Track Page View  */
        if (window.gtag != null) {
          window.gtag('event', 'page_view', {
            page_path: event.urlAfterRedirects,
          });
        }
        /** END */
      });
    }
  }
  /** Add Google Analytics Script Dynamically */
  addGAScript(token: string) {
    let gtagScript: HTMLScriptElement = document.createElement('script');
    gtagScript.async = true;
    gtagScript.src = 'https://www.googletagmanager.com/gtag/js?id=' + token;
    document.head.prepend(gtagScript);

    gtagScript.onload = () => {
      window.dataLayer = window.dataLayer || [];
      window.gtag = function () {
        window.dataLayer.push(arguments);
      };

      window.gtag('js', new Date());
      /** Disable automatic page view hit to fix duplicate page view count  **/
      window.gtag('config', token, { send_page_view: false });
    };
  }
}
