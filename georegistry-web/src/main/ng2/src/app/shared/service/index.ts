import { AuthService } from "./auth.service";
import { EventService } from "./event.service";
import { ExternalSystemService } from "./external-system.service";
import { AdminGuard, AuthGuard, ContributerGuard, MaintainerGuard } from "./guard.service";
import { LocalizationService } from "./localization.service";
import { ModalStepIndicatorService } from "./modal-step-indicator.service";
import { OrganizationService } from "./organization.service";
import { PendingChangesGuard } from "./pending-changes-guard";
import { ProfileService } from "./profile.service";
import { ProgressService } from "./progress.service";
import { SessionService } from "./session.service";
import { DateService } from "./date.service";

export const services: any[] = [
    AuthService,
    EventService,
    ExternalSystemService,
    AdminGuard,
    AuthGuard,
    ContributerGuard,
    MaintainerGuard,
    LocalizationService,
    ModalStepIndicatorService,
    OrganizationService,
    PendingChangesGuard,
    ProfileService,
    ProgressService,
    SessionService,
    DateService
];

export * from "./auth.service";
export * from "./event.service";
export * from "./external-system.service";
export * from "./guard.service";
export * from "./localization.service";
export * from "./modal-step-indicator.service";
export * from "./organization.service";
export * from "./pending-changes-guard";
export * from "./profile.service";
export * from "./progress.service";
export * from "./session.service";
export * from "./date.service";
