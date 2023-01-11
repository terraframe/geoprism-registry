import { Component, OnDestroy, OnInit } from "@angular/core";
import { ListTypeVersion } from "@registry/model/list-type";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observer, Subject, Subscription } from "rxjs";


@Component({
    selector: "select-type-modal",
    templateUrl: "./select-type-modal.component.html",
    styleUrls: []
})
export class SelectTypeModalComponent implements OnInit, OnDestroy {

    version: ListTypeVersion;

    type: string;

    /*
     * Called on confirm
     */
    onCreate: Subject<string>;

    subscription: Subscription;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onCreate = new Subject();
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.onCreate.unsubscribe();
    }

    init(version: ListTypeVersion, observer: Observer<string>): void {
        this.version = version;
        this.subscription = this.onCreate.subscribe(observer);
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onCreate.next(this.type);
    }
}
