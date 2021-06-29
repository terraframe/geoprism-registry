import { RegistryRole, LocaleView} from './core';

export class User {
  userName:string;
  loggedIn:boolean;
  roles:RegistryRole[];
  roleDisplayLabels:string[];
  version:string;
  installedLocales:LocaleView[];
}