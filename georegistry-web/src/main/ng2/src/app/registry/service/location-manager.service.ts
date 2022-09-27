
import { EventEmitter, Injectable } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { LocationManagerState } from "@registry/component/location-manager/location-manager.component";
import { Subscription } from "rxjs";

@Injectable()
export class LocationManagerService {

    public stateChange$: EventEmitter<LocationManagerState>;
    private _state: LocationManagerState = { attrPanelOpen: true };
    private subscription: Subscription;
    private _updatingUrl: boolean = false;

    constructor(private route: ActivatedRoute, private router: Router) {
        this.stateChange$ = new EventEmitter();
        this.subscription = this.route.queryParams.subscribe(urlParams => { this.handleUrlChange(urlParams); });
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    private handleUrlChange(urlParams) {
        if (!this._updatingUrl) {
            let newState = JSON.parse(JSON.stringify(urlParams));

            newState.graphPanelOpen = (newState.graphPanelOpen === "true");
            newState.attrPanelOpen = (newState.attrPanelOpen === "true" || newState.attrPanelOpen === undefined);

            this._state = newState;
            this.stateChange$.emit(this._state);
        } else {
            this._updatingUrl = false;
        }
    }

    public getState(): LocationManagerState {
        return this._state;
    }

    public setState(state: LocationManagerState): void {
        Object.assign(this._state, state);

        this._updatingUrl = true;

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: this._state,
            queryParamsHandling: "merge",
            replaceUrl: true
        });

        this.stateChange$.emit(this._state);
    }

}
