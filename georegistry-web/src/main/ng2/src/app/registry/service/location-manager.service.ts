
import { EventEmitter, Injectable } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { LocationManagerState } from "@registry/component/location-manager/location-manager.component";
import { Subscription } from "rxjs";

@Injectable()
export class LocationManagerService {

    public stateChange$: EventEmitter<LocationManagerState>;
    private _state: LocationManagerState = { attrPanelOpen: true };
    private subscription: Subscription;

    constructor(private route: ActivatedRoute, private router: Router) {
        this.stateChange$ = new EventEmitter();
        this.subscription = this.route.queryParams.subscribe(urlParams => { this.handleUrlChange(urlParams); });
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    private handleUrlChange(urlParams) {
        let newState = JSON.parse(JSON.stringify(urlParams));

        newState.graphPanelOpen = (newState.graphPanelOpen === "true");
        newState.attrPanelOpen = (newState.attrPanelOpen === "true" || newState.attrPanelOpen === undefined);

        this._state = newState;
        this.stateChange$.emit(JSON.parse(JSON.stringify(this._state)));
    }

    public getState(): LocationManagerState {
        return this._state;
    }

    public setState(state: LocationManagerState, pushBackHistory: boolean): void {
        Object.assign(this._state, state);

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: JSON.parse(JSON.stringify(this._state)),
            queryParamsHandling: "merge",
            replaceUrl: !pushBackHistory
        });
    }

}
