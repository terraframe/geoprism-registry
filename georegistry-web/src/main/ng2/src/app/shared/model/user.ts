/* eslint-disable padded-blocks */
import { LocaleView } from "@core/model/core";
import { RegistryRole } from "./core";

export class User {
    userName: string;
    loggedIn: boolean;
    roles: RegistryRole[];
    roleDisplayLabels: string[];
    version: string;
    installedLocales: LocaleView[];
}