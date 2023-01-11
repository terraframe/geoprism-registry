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