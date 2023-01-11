import { Component } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";
import { ConfigurationService } from "@core/service/configuration.service";
import { filter } from "rxjs";
import { environment } from 'src/environments/environment';

declare const window: any;

@Component({

    selector: "app-root",
    templateUrl: "./cgr-app.component.html",
    styleUrls: []
})
export class CgrAppComponent {

    favIcon: HTMLLinkElement = document.querySelector('#appIcon');
    customFont: string;

    constructor(private router: Router, private configuration: ConfigurationService) {
        this.favIcon.href = environment.apiUrl + '/api/asset/view?oid=logo';
        this.customFont = configuration.getCustomFont();

        const token = configuration.getAnalyticsToken();

        if (token != null && token.trim().length > 0) {
            this.addGAScript(token);

            this.router.events.pipe(
                filter(event => event instanceof NavigationEnd)
            ).subscribe((event: NavigationEnd) => {
                /** START : Code to Track Page View  */
                if (window.gtag != null) {
                    window.gtag('event', 'page_view', {
                        page_path: event.urlAfterRedirects
                    })
                }
                /** END */
            })
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
            window.gtag = function () { window.dataLayer.push(arguments); };

            window.gtag('js', new Date());
            /** Disable automatic page view hit to fix duplicate page view count  **/
            window.gtag('config', token, { send_page_view: false });
        }
    }
}
