import { Input, Component, OnInit } from "@angular/core";
import { LocaleView, LocalizedValue } from "@core/model/core";
import { ConfigurationService } from "@core/service/configuration.service";

import { LocalizationService } from "@shared/service";

@Component({

    selector: "convert-key-label",
    templateUrl: "./convert-key-label.component.html",
    styleUrls: []
})
export class ConvertKeyLabel implements OnInit {

    @Input() key: any;
    text: string;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: LocalizationService, private configuration: ConfigurationService) { }

    ngOnInit(): void {
        if (this.key != null && this.key.label != null && this.key.label.localizedValue != null) {
            this.text = this.key.label.localizedValue;
            return;
        }

        let locales: LocaleView[] = this.configuration.getLocales();

        let len = locales.length;
        for (let i = 0; i < len; ++i) {
            let locale: LocaleView = locales[i];

            if (locale.toString === this.key) {
                this.text = this.getValue(locale.label, this.service.getLocale());

                return;
            }
        }

        if (this.key === "defaultLocale") {
            this.text = this.service.decode("localization.defaultLocal");
        } else {
            this.text = this.key;
        }
    }

    public getValue(lv: LocalizedValue, localeToString: string): string {
        let len = lv.localeValues.length;

        for (let i = 0; i < len; ++i) {
            let value = lv.localeValues[i];

            if (value.locale === localeToString) {
                return value.value;
            }
        }

        return lv.localizedValue;
    }

}
