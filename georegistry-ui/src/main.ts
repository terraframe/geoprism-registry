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

import { enableProdMode } from '@angular/core';

import { environment } from './environments/environment';
import { CgrAppComponent } from './app/app.component';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { setTheme } from 'ngx-bootstrap/utils';
import { PlacementForBs5 } from 'ngx-bootstrap/positioning';

if (environment.production) {
    enableProdMode();
}

setTheme('bs5');

/*
 * Work around an ngx-bootstrap positioning bug. When a popup with a variation
 * (e.g. the date field's 'bottom start', internally 'bottom left') fits
 * neither above nor below its input, adaptivePosition flips it to a side but
 * keeps the variation, giving 'right left' etc. PlacementForBs5 has no entry
 * for those, so updateContainerClass throws "dataPlacement is undefined" on
 * every repositioning. Mapping them to the bare side makes the flip behave as
 * it does for a placement without a variation.
 */
Object.assign(PlacementForBs5 as unknown as Record<string, string>, {
    'left left': 'start',
    'left right': 'start',
    'right left': 'end',
    'right right': 'end',
});

bootstrapApplication(CgrAppComponent, appConfig)
    .catch(err => console.error(err));
