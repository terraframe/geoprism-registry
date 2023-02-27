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

import { CanDeactivate } from "@angular/router";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

// Thanks to : https://stackoverflow.com/a/41187919/219843

export interface ComponentCanDeactivate {
    canDeactivate: () => boolean | Observable<boolean>;
    afterDeactivate: (arg: boolean) => void;
}

@Injectable()
export class PendingChangesGuard implements CanDeactivate<ComponentCanDeactivate> {

    canDeactivate(component: ComponentCanDeactivate): boolean | Observable<boolean> {
        if (!component.canDeactivate()) {
            // NOTE: this warning message will only be shown when navigating elsewhere within your angular app;
            // when navigating away from your angular app, the browser will show a generic warning message
            // see http://stackoverflow.com/a/42207299/7307355
            let confirmRet = confirm("WARNING: You have unsaved changes. Press Cancel to go back and save these changes, or OK to lose these changes.");

            component.afterDeactivate(confirmRet);

            return confirmRet;
        }

        return true;
    }
}