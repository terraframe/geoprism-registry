import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';
import { CgrAppModule } from './app/cgr-app.module';

if (process.env.ENV === 'production') {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(CgrAppModule)
  .then(success => console.log('Bootstrap success'))
  .catch(error => console.log(error));

