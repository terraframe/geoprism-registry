import { ErrorHandler } from "./error-handler/error-handler";
import { CgrHeaderComponent } from "./header/header.component";
import { LoadingBarComponent } from "./loading-bar/loading-bar.component";
import { LocalizeComponent } from "./localize/localize.component";
import { ErrorMessageComponent } from "./message/error-message.component";
import { MessageComponent } from "./message/message.component";
import { ConfirmModalComponent } from "./modals/confirm-modal.component";
import { ErrorModalComponent } from "./modals/error-modal.component";
import { ModalStepIndicatorComponent } from "./modals/modal-step-indicator.component";
import { SuccessModalComponent } from "./modals/success-modal.component";
import { PasswordStrengthBarComponent } from "./password-strength-bar/password-strength-bar.component";
import { ProfileComponent } from "./profile/profile.component";
import { ProgressBarComponent } from "./progress-bar/progress-bar.component";
import { GenericModalComponent } from "./modals/generic-modal.component";
import { DateFieldComponent } from "./form-fields/date-field/date-field.component";
import { DateTextComponent } from "./date-text/date-text.component";

export const components: any[] = [
    ErrorHandler,
    CgrHeaderComponent,
    LoadingBarComponent,
    LocalizeComponent,
    ErrorMessageComponent,
    MessageComponent,
    ConfirmModalComponent,
    GenericModalComponent,
    ErrorModalComponent,
    ModalStepIndicatorComponent,
    SuccessModalComponent,
    PasswordStrengthBarComponent,
    ProfileComponent,
    ProgressBarComponent,
    DateFieldComponent,
    DateTextComponent
];

export * from "./error-handler/error-handler";
export * from "./header/header.component";
export * from "./loading-bar/loading-bar.component";
export * from "./localize/localize.component";
export * from "./message/error-message.component";
export * from "./message/message.component";
export * from "./modals/confirm-modal.component";
export * from "./modals/generic-modal.component";
export * from "./modals/error-modal.component";
export * from "./modals/modal-step-indicator.component";
export * from "./modals/success-modal.component";
export * from "./password-strength-bar/password-strength-bar.component";
export * from "./profile/profile.component";
export * from "./progress-bar/progress-bar.component";
export * from "./form-fields/date-field/date-field.component";
export * from "./date-text/date-text.component";
